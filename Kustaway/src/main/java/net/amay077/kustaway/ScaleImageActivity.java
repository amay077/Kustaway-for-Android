package net.amay077.kustaway;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.viewpagerindicator.CirclePageIndicator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.BindView;
import twitter4j.Status;

import net.amay077.kustaway.adapter.SimplePagerAdapter;
import net.amay077.kustaway.event.connection.StreamingConnectionEvent;
import net.amay077.kustaway.fragment.ScaleImageFragment;
import net.amay077.kustaway.model.TwitterManager;
import net.amay077.kustaway.util.ImageUtil;
import net.amay077.kustaway.util.MessageUtil;
import net.amay077.kustaway.util.StatusUtil;
import net.amay077.kustaway.widget.ScaleImageViewPager;

/**
 * 画像の拡大表示用のActivity、かぶせて使う
 *
 * @author aska
 */
public class ScaleImageActivity extends AppCompatActivity {

    @BindView(R.id.pager) ScaleImageViewPager pager;
    @BindView(R.id.transitionImage) ImageView transitionImage;
    @BindView(R.id.symbol) CirclePageIndicator symbol;

    private ArrayList<String> imageUrls = new ArrayList<>();
    private SimplePagerAdapter simplePagerAdapter;
    static final int REQUEST_PERMISSIONS_STORAGE = 1;

    /** 画像表示用の StartActivity */
    public static void startActivityWithImage(
            Activity activity,
            Status status,
            int openIndex,
            View sharedView,
            String transitionName) {
        Intent intent = new Intent(activity, ScaleImageActivity.class);
        intent.putExtra("status", status);
        intent.putExtra("index", openIndex);

        if (sharedView != null && transitionName != null &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                    sharedView, transitionName);
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_scale_image);
        ButterKnife.bind(this);

        simplePagerAdapter = new SimplePagerAdapter(this, pager);
        symbol.setViewPager(pager);
        pager.setOffscreenPageLimit(4);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                pager.setVisibility(View.VISIBLE);
                transitionImage.setVisibility(View.GONE);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        String firstUrl;

        // インテント経由での起動をサポート
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data == null) {
                return;
            }
            firstUrl = data.toString();
        } else {
            Bundle args = intent.getExtras();
            if (args == null) {
                return;
            }

            Status status = (Status) args.getSerializable("status");
            if (status != null) {
                Integer index = args.getInt("index", 0);
                showStatus(status, index);
            }

            firstUrl = args.getString("url");
        }

        if (firstUrl == null) {
            return;
        }

        Pattern pattern = Pattern.compile("https?://twitter\\.com/\\w+/status/(\\d+)/photo/(\\d+)/?.*");
        Matcher matcher = pattern.matcher(firstUrl);
        if (matcher.find()) {
            final Long statusId = Long.valueOf(matcher.group(1));
            new AsyncTask<Void, Void, Status>() {
                @Override
                protected twitter4j.Status doInBackground(Void... params) {
                    try {
                        return TwitterManager.getTwitter().showStatus(statusId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(twitter4j.Status status) {
                    if (status != null) {
                        showStatus(status, 0);
                    }
                }
            }.execute();
            return;
        }

        symbol.setVisibility(View.GONE);
        imageUrls.add(firstUrl);
        Bundle args = new Bundle();
        args.putString("url", firstUrl);
        simplePagerAdapter.addTab(ScaleImageFragment.class, args);
        simplePagerAdapter.notifyDataSetChanged();
    }

    public void showStatus(twitter4j.Status status, Integer index) {
        ArrayList<String> urls = StatusUtil.getImageUrls(status);
        if (urls.size() == 1) {
            symbol.setVisibility(View.GONE);
        }
        for (final String imageURL : urls) {
            imageUrls.add(imageURL);
            Bundle args = new Bundle();
            args.putString("url", imageURL);
            simplePagerAdapter.addTab(ScaleImageFragment.class, args);
        }

        // Activity Transition 用の TransitionName を設定
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ImageUtil.displayImage(imageUrls.get(index), transitionImage);
            transitionImage.setTransitionName(getString(R.string.transition_tweet_image));
        }

        simplePagerAdapter.notifyDataSetChanged();
        pager.setCurrentItem(index);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scale_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.save) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                saveImage();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_STORAGE);
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImage();
                } else {
                    MessageUtil.showToast(R.string.toast_save_image_failure);
                }
            }
        }
    }

    public void saveImage() {
        final URL url;
        try {
            url = new URL(imageUrls.get(pager.getCurrentItem()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            MessageUtil.showToast(R.string.toast_save_image_failure);
            return;
        }
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                int count;
                Boolean isSuccess;
                try {
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    InputStream input = new BufferedInputStream(url.openStream(), 10 * 1024);
                    File root = new File(Environment.getExternalStorageDirectory(), "/Download/");
                    File file = new File(root, new Date().getTime() + ".jpg");
                    OutputStream output = new FileOutputStream(file);
                    byte data[] = new byte[1024];
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                    String[] paths = {file.getPath()};
                    String[] types = {"image/jpeg"};
                    MediaScannerConnection.scanFile(getApplicationContext(), paths, types, null);
                    isSuccess = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    isSuccess = false;
                }
                return isSuccess;
            }

            @Override
            protected void onPostExecute(Boolean isSuccess) {
                if (isSuccess) {
                    MessageUtil.showToast(R.string.toast_save_image_success);
                } else {
                    MessageUtil.showToast(R.string.toast_save_image_failure);
                }
            }
        };
        task.execute();
    }
}
