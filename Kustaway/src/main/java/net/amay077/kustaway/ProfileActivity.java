package net.amay077.kustaway;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.amay077.kustaway.adapter.SimplePagerAdapter;
import net.amay077.kustaway.databinding.ActivityProfileBinding;
import net.amay077.kustaway.event.AlertDialogEvent;
import net.amay077.kustaway.fragment.profile.DescriptionFragment;
import net.amay077.kustaway.fragment.profile.FavoritesListFragment;
import net.amay077.kustaway.fragment.profile.FollowersListFragment;
import net.amay077.kustaway.fragment.profile.FollowingListFragment;
import net.amay077.kustaway.fragment.profile.SummaryFragment;
import net.amay077.kustaway.fragment.profile.UserListMembershipsFragment;
import net.amay077.kustaway.fragment.profile.UserTimelineFragment;
import net.amay077.kustaway.model.Profile;
import net.amay077.kustaway.model.TwitterManager;
import net.amay077.kustaway.task.ShowUserLoader;
import net.amay077.kustaway.util.ImageUtil;
import net.amay077.kustaway.util.MessageUtil;
import net.amay077.kustaway.util.ThemeUtil;

import de.greenrobot.event.EventBus;
import twitter4j.Relationship;
import twitter4j.User;

public class ProfileActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<Profile> {

    private ActivityProfileBinding binding = null;

    private User mUser;
    private Relationship mRelationship;
    private Menu menu;
    private static final int OPTION_MENU_GROUP_RELATION = 1;
    private static final int OPTION_MENU_CREATE_BLOCK = 1;
    private static final int OPTION_MENU_CREATE_OFFICIAL_MUTE = 2;
    private static final int OPTION_MENU_CREATE_NO_RETWEET = 3;
    private static final int OPTION_MENU_DESTROY_BLOCK = 4;
    private static final int OPTION_MENU_DESTROY_OFFICIAL_MUTE = 5;
    private static final int OPTION_MENU_DESTROY_NO_RETWEET = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTheme(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // インテント経由での起動をサポート
        Intent intent = getIntent();
        Bundle args = new Bundle(1);
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null
                && intent.getData().getLastPathSegment() != null
                && !intent.getData().getLastPathSegment().isEmpty()) {
            args.putString("screenName", intent.getData().getLastPathSegment());
        } else {
            String screenName = intent.getStringExtra("screenName");
            if (screenName != null) {
                args.putString("screenName", screenName);
            } else {
                args.putLong("userId", intent.getLongExtra("userId", 0));
            }
        }
        MessageUtil.showProgressDialog(this, getString(R.string.progress_loading));
        getSupportLoaderManager().initLoader(0, args, this);


        binding.collapseLabel.setOnClickListener(v -> {
            View frame = findViewById(R.id.frame);
            if (frame.getVisibility() == View.VISIBLE) {
                binding.frame.setVisibility(View.GONE);
                binding.collapseLabel.setText(R.string.fontello_down);
            } else {
                binding.frame.setVisibility(View.VISIBLE);
                binding.collapseLabel.setText(R.string.fontello_up);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(AlertDialogEvent event) {
        event.getDialogFragment().show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getGroupId() == OPTION_MENU_GROUP_RELATION) {
            switch (item.getItemId()) {
                case OPTION_MENU_CREATE_BLOCK:
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setMessage(R.string.confirm_create_block)
                            .setPositiveButton(
                                    R.string.button_create_block,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MessageUtil.showProgressDialog(ProfileActivity.this, getString(R.string.progress_process));
                                            new CreateBlockTask().execute(mUser.getId());
                                        }
                                    }
                            )
                            .setNegativeButton(
                                    R.string.button_cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                    break;
                case OPTION_MENU_CREATE_OFFICIAL_MUTE:
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setMessage(R.string.confirm_create_official_mute)
                            .setPositiveButton(
                                    R.string.button_create_official_mute,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MessageUtil.showProgressDialog(ProfileActivity.this, getString(R.string.progress_process));
                                            new CreateOfficialMuteTask().execute(mUser.getId());
                                        }
                                    }
                            )
                            .setNegativeButton(
                                    R.string.button_cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                    break;
                case OPTION_MENU_CREATE_NO_RETWEET:
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setMessage(R.string.confirm_create_no_retweet)
                            .setPositiveButton(
                                    R.string.button_create_no_retweet,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MessageUtil.showProgressDialog(ProfileActivity.this, getString(R.string.progress_process));
                                            new CreateNoRetweetTask(mRelationship.isSourceNotificationsEnabled()).execute(mUser.getId());
                                        }
                                    }
                            )
                            .setNegativeButton(
                                    R.string.button_cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                    break;
                case OPTION_MENU_DESTROY_BLOCK:
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setMessage(R.string.confirm_create_block)
                            .setPositiveButton(
                                    R.string.button_destroy_block,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MessageUtil.showProgressDialog(ProfileActivity.this, getString(R.string.progress_process));
                                            new DestroyBlockTask().execute(mUser.getId());
                                        }
                                    }
                            )
                            .setNegativeButton(
                                    R.string.button_cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                    break;
                case OPTION_MENU_DESTROY_OFFICIAL_MUTE:
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setMessage(R.string.confirm_destroy_official_mute)
                            .setPositiveButton(
                                    R.string.button_destroy_official_mute,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MessageUtil.showProgressDialog(ProfileActivity.this, getString(R.string.progress_process));
                                            new DestroyOfficialMuteTask().execute(mUser.getId());
                                        }
                                    }
                            )
                            .setNegativeButton(
                                    R.string.button_cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                    break;
                case OPTION_MENU_DESTROY_NO_RETWEET:
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setMessage(R.string.confirm_destroy_no_retweet)
                            .setPositiveButton(
                                    R.string.button_destroy_no_retweet,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MessageUtil.showProgressDialog(ProfileActivity.this, getString(R.string.progress_process));
                                            new DestroyNoRetweetTask(mRelationship.isSourceNotificationsEnabled()).execute(mUser.getId());
                                        }
                                    }
                            )
                            .setNegativeButton(
                                    R.string.button_cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                    break;
                default:
                    break;
            }
            return true;
        }
        Intent intent;
        String text;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.send_reply:
                intent = new Intent(this, PostActivity.class);
                text = "@" + mUser.getScreenName() + " ";
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                startActivity(intent);
                break;
            case R.id.send_direct_messages:
                intent = new Intent(this, PostActivity.class);
                text = "D " + mUser.getScreenName() + " ";
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                startActivity(intent);
                break;
            case R.id.add_to_list:
                intent = new Intent(this, RegisterUserListActivity.class);
                intent.putExtra("userId", mUser.getId());
                startActivity(intent);
                break;
            case R.id.open_twitter:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"
                        + mUser.getScreenName()));
                startActivity(intent);
                break;
            case R.id.open_favstar:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://ja.favstar.fm/users/"
                        + mUser.getScreenName() + "/recent"));
                startActivity(intent);
                break;
            case R.id.open_aclog:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://aclog.koba789.com/"
                        + mUser.getScreenName() + "/timeline"));
                startActivity(intent);
                break;
            case R.id.open_twilog:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://twilog.org/"
                        + mUser.getScreenName()));
                startActivity(intent);
                break;
            case R.id.report_spam:
                new AlertDialog.Builder(ProfileActivity.this)
                        .setMessage(R.string.confirm_report_spam)
                        .setPositiveButton(
                                R.string.button_report_spam,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MessageUtil.showProgressDialog(ProfileActivity.this, getString(R.string.progress_process));
                                        new ReportSpamTask().execute(mUser.getId());
                                    }
                                }
                        )
                        .setNegativeButton(
                                R.string.button_cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }
                        )
                        .show();

                break;
        }
        return true;
    }

    @Override
    public Loader<Profile> onCreateLoader(int arg0, Bundle args) {
        String screenName = args.getString("screenName");
        if (screenName != null) {
            return new ShowUserLoader(this, screenName);
        } else {
            return new ShowUserLoader(this, args.getLong("userId"));
        }
    }

    @Override
    public void onLoadFinished(Loader<Profile> arg0, Profile profile) {
        MessageUtil.dismissProgressDialog();
        if (profile == null) {
            MessageUtil.showToast(R.string.toast_load_data_failure, "(null)");
            return;
        }
        if (profile.getError() != null && !profile.getError().isEmpty()) {
            MessageUtil.showToast(R.string.toast_load_data_failure, profile.getError());
            return;
        }
        mUser = profile.getUser();
        if (mUser == null) {
            MessageUtil.showToast(R.string.toast_load_data_failure, "(missing user)");
            return;
        }
        binding.favouritesCount.setText(getString(R.string.label_favourites, String.format("%1$,3d", mUser.getFavouritesCount())));
        binding.statusesCount.setText(getString(R.string.label_tweets, String.format("%1$,3d", mUser.getStatusesCount())));
        binding.friendsCount.setText(getString(R.string.label_following, String.format("%1$,3d", mUser.getFriendsCount())));
        binding.followersCount.setText(getString(R.string.label_followers, String.format("%1$,3d", mUser.getFollowersCount())));
        binding.listedCount.setText(getString(R.string.label_listed, String.format("%1$,3d", mUser.getListedCount())));

        String bannerUrl = mUser.getProfileBannerMobileRetinaURL();
        if (bannerUrl != null) {
            ImageUtil.displayImage(bannerUrl, binding.banner);
        }

        Relationship relationship = profile.getRelationship();
        mRelationship = relationship;

        if (menu != null) {
            if (relationship.isSourceBlockingTarget()) {
                menu.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_DESTROY_BLOCK, 100, R.string.menu_destroy_block);
            } else {
                menu.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_CREATE_BLOCK, 100, R.string.menu_create_block);
            }
            if (relationship.isSourceFollowingTarget()) {
                if (relationship.isSourceMutingTarget()) {
                    menu.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_DESTROY_OFFICIAL_MUTE, 100, R.string.menu_destory_official_mute);
                } else {
                    menu.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_CREATE_OFFICIAL_MUTE, 100, R.string.menu_create_official_mute);
                }
                if (relationship.isSourceWantRetweets()) {
                    menu.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_CREATE_NO_RETWEET, 100, R.string.menu_create_no_retweet);
                } else {
                    menu.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_DESTROY_NO_RETWEET, 100, R.string.menu_destory_no_retweet);
                }
            }
        }

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        SimplePagerAdapter simplePagerAdapter = new SimplePagerAdapter(this, binding.pager);

        Bundle args = new Bundle();
        args.putSerializable("user", mUser);
        args.putSerializable("relationship", relationship);
        simplePagerAdapter.addTab(SummaryFragment.class, args);
        simplePagerAdapter.addTab(DescriptionFragment.class, args);
        simplePagerAdapter.notifyDataSetChanged();
        binding.symbol.setViewPager(binding.pager);

        /**
         * スワイプの度合いに応じて背景色を暗くする
         * これは透明度＆背景色黒で実現している、背景色黒だけだと背景画像が見えないが、
         * 透明度を指定することで背景画像の表示と白色のテキストの視認性を両立している
         */
        binding.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                /**
                 * 背景色の透過度の範囲は00〜99とする（FFは真っ黒で背景画像が見えない）
                 * 99は10進数で153
                 * positionは0が1ページ目（スワイプ中含む）で1だと完全に2ページ目に遷移した状態
                 * positionOffsetには0.0〜1.0のスクロール率がかえってくる、真ん中だと0.5
                 * hexにはpositionOffsetに応じて00〜99（153）の値が入るように演算を行う
                 * 例えばpositionOffsetが0.5の場合はhexは4dになる
                 * positionが1の場合は最大値（99）を無条件で設定している
                 */

                final int maxHex = 153; // 0x99
                String hex = position == 1 ? "99" : String.format("%02X", (int) (maxHex * positionOffset));
                binding.pager.setBackgroundColor(Color.parseColor("#" + hex + "000000"));

                // OnPageChangeListenerは1つしかセットできないのでCirclePageIndicatorの奴も呼んであげる
                binding.symbol.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                // OnPageChangeListenerは1つしかセットできないのでCirclePageIndicatorの奴も呼んであげる
                binding.symbol.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // OnPageChangeListenerは1つしかセットできないのでCirclePageIndicatorの奴も呼んであげる
                binding.symbol.onPageScrollStateChanged(state);
            }
        });

        // ユーザリスト用のタブ
        SimplePagerAdapter listPagerAdapter = new SimplePagerAdapter(this, binding.listPager);

        Bundle listArgs = new Bundle();
        listArgs.putSerializable("user", mUser);
        listPagerAdapter.addTab(UserTimelineFragment.class, listArgs);
        listPagerAdapter.addTab(FollowingListFragment.class, listArgs);
        listPagerAdapter.addTab(FollowersListFragment.class, listArgs);
        listPagerAdapter.addTab(UserListMembershipsFragment.class, listArgs);
        listPagerAdapter.addTab(FavoritesListFragment.class, listArgs);
        listPagerAdapter.notifyDataSetChanged();
        binding.listPager.setOffscreenPageLimit(5);

        /**
         * タブのラベル情報を配列に入れておく
         */
        final TextView[] tabs = {
                binding.statusesCount,
                binding.friendsCount,
                binding.followersCount,
                binding.listedCount,
                binding.favouritesCount,
        };


        final int colorBlue = ThemeUtil.getThemeTextColor(R.attr.holo_blue);
        final int colorWhite = ThemeUtil.getThemeTextColor(R.attr.text_color);

        tabs[0].setTextColor(colorBlue);

        binding.listPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                /**
                 * タブのindexと選択されたpositionを比較して色を設定
                 */
                for (int i = 0; i < tabs.length; i++) {
                    tabs[i].setTextColor(i == position ? colorBlue : colorWhite);
                }
            }
        });

        for (int i = 0; i < tabs.length; i++) {
            final int finalI = i;
            tabs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.listPager.setCurrentItem(finalI);
                }
            });
        }

    }

    @Override
    public void onLoaderReset(Loader<Profile> arg0) {
    }

    public void restart() {
        Intent intent = new Intent();
        intent.setClass(this, ProfileActivity.class);
        intent.putExtra("userId", mUser.getId());
        startActivity(intent);
        finish();
    }

    private class ReportSpamTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... params) {
            Long userId = params[0];
            try {
                TwitterManager.getTwitter().reportSpam(userId);
                net.amay077.kustaway.model.Relationship.setBlock(userId);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            MessageUtil.dismissProgressDialog();
            if (success) {
                MessageUtil.showToast(R.string.toast_report_spam_success);
                restart();
            } else {
                MessageUtil.showToast(R.string.toast_report_spam_failure);
            }

        }
    }

    private class CreateBlockTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... params) {
            Long userId = params[0];
            try {
                TwitterManager.getTwitter().createBlock(userId);
                net.amay077.kustaway.model.Relationship.setBlock(userId);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            MessageUtil.dismissProgressDialog();
            if (success) {
                MessageUtil.showToast(R.string.toast_create_block_success);
                restart();
            } else {
                MessageUtil.showToast(R.string.toast_create_block_failure);
            }

        }
    }

    private class DestroyBlockTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... params) {
            Long userId = params[0];
            try {
                TwitterManager.getTwitter().destroyBlock(userId);
                net.amay077.kustaway.model.Relationship.removeBlock(userId);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            MessageUtil.dismissProgressDialog();
            if (success) {
                MessageUtil.showToast(R.string.toast_destroy_block_success);
                restart();
            } else {
                MessageUtil.showToast(R.string.toast_destroy_block_failure);
            }

        }
    }

    private class CreateOfficialMuteTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... params) {
            Long userId = params[0];
            try {
                TwitterManager.getTwitter().createMute(userId);
                net.amay077.kustaway.model.Relationship.setOfficialMute(userId);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            MessageUtil.dismissProgressDialog();
            if (success) {
                MessageUtil.showToast(R.string.toast_create_official_mute_success);
                restart();
            } else {
                MessageUtil.showToast(R.string.toast_create_official_mute_failure);
            }

        }
    }

    private class DestroyOfficialMuteTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... params) {
            Long userId = params[0];
            try {
                TwitterManager.getTwitter().destroyMute(userId);
                net.amay077.kustaway.model.Relationship.removeOfficialMute(userId);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            MessageUtil.dismissProgressDialog();
            if (success) {
                MessageUtil.showToast(R.string.toast_destroy_official_mute_success);
                restart();
            } else {
                MessageUtil.showToast(R.string.toast_destroy_official_mute_failure);
            }

        }
    }

    private class CreateNoRetweetTask extends AsyncTask<Long, Void, Boolean> {
        private boolean notification;

        public CreateNoRetweetTask(boolean notification) {
            this.notification = notification;
        }

        @Override
        protected Boolean doInBackground(Long... params) {
            Long userId = params[0];
            try {
                TwitterManager.getTwitter().updateFriendship(userId, notification, false);
                net.amay077.kustaway.model.Relationship.setNoRetweet(userId);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            MessageUtil.dismissProgressDialog();
            if (success) {
                MessageUtil.showToast(R.string.toast_create_no_retweet_success);
                restart();
            } else {
                MessageUtil.showToast(R.string.toast_create_no_retweet_failure);
            }

        }
    }

    private class DestroyNoRetweetTask extends AsyncTask<Long, Void, Boolean> {
        private boolean notification;

        public DestroyNoRetweetTask(boolean notification) {
            this.notification = notification;
        }

        @Override
        protected Boolean doInBackground(Long... params) {
            Long userId = params[0];
            try {
                TwitterManager.getTwitter().updateFriendship(userId, notification, true);
                net.amay077.kustaway.model.Relationship.removeNoRetweet(userId);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            MessageUtil.dismissProgressDialog();
            if (success) {
                MessageUtil.showToast(R.string.toast_destroy_no_retweet_success);
                restart();
            } else {
                MessageUtil.showToast(R.string.toast_destroy_no_retweet_failure);
            }

        }
    }
}