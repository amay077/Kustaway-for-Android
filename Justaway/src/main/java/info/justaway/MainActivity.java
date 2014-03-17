package info.justaway;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import info.justaway.adapter.MainPagerAdapter;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.fragment.main.BaseFragment;
import info.justaway.fragment.main.DirectMessagesFragment;
import info.justaway.fragment.main.InteractionsFragment;
import info.justaway.fragment.main.TimelineFragment;
import info.justaway.fragment.main.UserListFragment;
import info.justaway.model.Row;
import info.justaway.task.DestroyDirectMessageTask;
import info.justaway.task.ReFetchFavoriteStatus;
import info.justaway.task.UpdateStatusTask;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserStreamAdapter;

/**
 * @author aska
 */
public class MainActivity extends FragmentActivity {

    private JustawayApplication mApplication;
    private MainPagerAdapter mMainPagerAdapter;
    private ViewPager mViewPager;
    private ProgressDialog mProgressDialog;
    ActionBar mActionBar;
    private TextView mTitle;
    private TextView mSignalButton;
    private static final int REQUEST_CHOOSE_USER_LIST = 100;
    private static final int REQUEST_ACCOUNT_SETTING = 200;
    private static final int REQUEST_SETTINGS = 300;
    private static final int ERROR_CODE_DUPLICATE_STATUS = 187;
    private static final long TAB_ID_TIMELINE = -1L;
    private static final long TAB_ID_INTERACTIONS = -2L;
    private static final long TAB_ID_DIRECT_MESSAGE = -3L;

    /*
     * Activity よりも寿命が長いインスタンスたち(画面回転後もストリームを切らないようにするため)
     */
    private TwitterStream mTwitterStream;
    private MyUserStreamAdapter mUserStreamAdapter;
    private MyConnectionLifeCycleListener mConnectionLifeCycleListener;
    // ここまで

    private static final class StreamHolder {
        public final TwitterStream twitterStream;
        public final MyUserStreamAdapter userStreamAdapter;
        public final MyConnectionLifeCycleListener connectionLifeCycleListener;

        private StreamHolder(TwitterStream twitterStream, MyUserStreamAdapter userStreamAdapter, MyConnectionLifeCycleListener connectionLifeCycleListener) {
            this.twitterStream = twitterStream;
            this.userStreamAdapter = userStreamAdapter;
            this.connectionLifeCycleListener = connectionLifeCycleListener;
        }
    }

    private Status mInReplyToStatus;

    public void setInReplyToStatus(Status inReplyToStatus) {
        this.mInReplyToStatus = inReplyToStatus;
    }

    /**
     * ActionBarでCustomView使ってるので自分で再実装
     */
    @Override
    public void setTitle(CharSequence title) {
        if (mTitle != null) {
            mTitle.setText(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    @SuppressWarnings("MagicConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JustawayApplication.getApplication().setTheme(this);

        // クイックモード時に起動と同時にキーボードが出現するのを抑止
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mActionBar = getActionBar();
        if (mActionBar != null) {
            int options = mActionBar.getDisplayOptions();
            if ((options & ActionBar.DISPLAY_SHOW_CUSTOM) == ActionBar.DISPLAY_SHOW_CUSTOM) {
                mActionBar.setDisplayOptions(options ^ ActionBar.DISPLAY_SHOW_CUSTOM);
            } else {
                mActionBar.setDisplayOptions(options | ActionBar.DISPLAY_SHOW_CUSTOM);
                if (mActionBar.getCustomView() == null) {
                    mActionBar.setCustomView(R.layout.action_bar_main);
                    ViewGroup group = (ViewGroup) mActionBar.getCustomView();
                    mTitle = (TextView) group.findViewById(R.id.title);
                    mSignalButton = (TextView) group.findViewById(R.id.signal);
                    mSignalButton.setTypeface(JustawayApplication.getFontello());
                    mSignalButton.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final boolean turnOn = !mApplication.getStreamingMode();
                                    DialogFragment dialog = StreamingSwitchDialogFragment.newInstance(turnOn);
                                    dialog.show(getSupportFragmentManager(), "dialog");
                                }
                            }
                    );
                }
            }
        }

        setContentView(R.layout.activity_main);

        setTitle(R.string.title_main);

        // クイックモード時に起動と同時に入力エリアにフォーカスするのを抑止
        findViewById(R.id.main).requestFocus();

        mApplication = JustawayApplication.getApplication();

        // アクセストークンがない場合に認証用のアクティビティを起動する
        if (!mApplication.hasAccessToken()) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setup();

        /**
         * 違うタブだったら移動、同じタブだったら最上部にスクロールという美しい実装
         * ActionBarのタブに頼っていない為、自力でsetCurrentItemでタブを動かしている
         * タブの切替がスワイプだけで良い場合はこの処理すら不要
         */
        Typeface fontello = JustawayApplication.getFontello();
        Button home = (Button) findViewById(R.id.action_timeline);
        Button interactions = (Button) findViewById(R.id.action_interactions);
        Button directMessage = (Button) findViewById(R.id.action_direct_message);
        Button tweet = (Button) findViewById(R.id.action_tweet);
        Button send = (Button) findViewById(R.id.send);
        findViewById(R.id.action_timeline).setSelected(true);
        home.setTypeface(fontello);
        interactions.setTypeface(fontello);
        directMessage.setTypeface(fontello);
        tweet.setTypeface(fontello);
        send.setTypeface(fontello);
        bindTabListener(home, 0);
        bindTabListener(interactions, 1);
        bindTabListener(directMessage, 2);
        tweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PostActivity.class);
                if (findViewById(R.id.quick_tweet_layout).getVisibility() == View.VISIBLE) {
                    EditText status = (EditText) findViewById(R.id.quick_tweet_edit);
                    if (status == null) {
                        return;
                    }
                    String msg = status.getText() != null ? status.getText().toString() : null;
                    if (msg != null && msg.length() > 0) {
                        intent.putExtra("status", msg);
                        intent.putExtra("selection", msg.length());
                        if (mInReplyToStatus != null) {
                            intent.putExtra("inReplyToStatus", mInReplyToStatus);
                        }
                        status.setText("");
                        status.clearFocus();
                    }
                }
                startActivity(intent);
            }
        });
        tweet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (findViewById(R.id.quick_tweet_layout).getVisibility() == View.VISIBLE) {
                    hideQuickPanel();
                } else {
                    showQuickPanel();
                }
                return true;
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText status = (EditText) findViewById(R.id.quick_tweet_edit);
                String msg = status.getText() != null ? status.getText().toString() : null;
                if (msg != null && msg.length() > 0) {
                    showProgressDialog(getString(R.string.progress_sending));
                    StatusUpdate statusUpdate = new StatusUpdate(msg);
                    if (mInReplyToStatus != null) {
                        statusUpdate.setInReplyToStatusId(mInReplyToStatus.getId());
                        setInReplyToStatus(null);
                    }

                    UpdateStatusTask task = new UpdateStatusTask(null) {
                        @Override
                        protected void onPostExecute(TwitterException e) {
                            dismissProgressDialog();
                            if (e == null) {
                                EditText status = (EditText) findViewById(R.id.quick_tweet_edit);
                                status.setText("");
                            } else if (e.getErrorCode() == ERROR_CODE_DUPLICATE_STATUS) {
                                JustawayApplication.showToast(getString(R.string.toast_update_status_already));
                            } else {
                                JustawayApplication.showToast(R.string.toast_update_status_failure);
                            }
                        }
                    };
                    task.execute(statusUpdate);
                }
            }
        });

        final StreamHolder holder = (StreamHolder) getLastCustomNonConfigurationInstance();
        if (holder != null) {
            mTwitterStream = holder.twitterStream;
            mUserStreamAdapter = holder.userStreamAdapter;
            mConnectionLifeCycleListener = holder.connectionLifeCycleListener;

            mUserStreamAdapter.updateActivity(this);
            mConnectionLifeCycleListener.updateActivity(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("signalButtonColor", mSignalButton.getCurrentTextColor());

        LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);
        int count = tab_menus.getChildCount();
        final int tabColors[] = new int[count];
        for (int i = 0; i < count; i++) {
            Button button = (Button) tab_menus.getChildAt(i);
            if (button == null) {
                continue;
            }
            tabColors[i] = button.getCurrentTextColor();
        }

        outState.putIntArray("tabColors", tabColors);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mSignalButton.setTextColor(savedInstanceState.getInt("signalButtonColor"));

        final int[] tabColors = savedInstanceState.getIntArray("tabColors");
        assert tabColors != null;
        LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);
        int count = Math.min(tab_menus.getChildCount(), tabColors.length);
        for (int i = 0; i < count; i++) {
            Button button = (Button) tab_menus.getChildAt(i);
            if (button == null) {
                continue;
            }
            button.setTextColor(tabColors[i]);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (mTwitterStream == null) {
            return null;
        }
        return new StreamHolder(mTwitterStream, mUserStreamAdapter, mConnectionLifeCycleListener);
    }

    public void showQuickPanel() {
        findViewById(R.id.quick_tweet_layout).setVisibility(View.VISIBLE);
        EditText editStatus = (EditText) findViewById(R.id.quick_tweet_edit);
        editStatus.setFocusable(true);
        editStatus.setFocusableInTouchMode(true);
        editStatus.setEnabled(true);
        mApplication.setQuickMod(true);
    }

    public void hideQuickPanel() {
        EditText editStatus = (EditText) findViewById(R.id.quick_tweet_edit);
        editStatus.setFocusable(false);
        editStatus.setFocusableInTouchMode(false);
        editStatus.setEnabled(false);
        editStatus.clearFocus();
        findViewById(R.id.quick_tweet_layout).setVisibility(View.GONE);
        setInReplyToStatus(null);
        mApplication.setQuickMod(false);
    }

    public void setupTab() {
        LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);

        int count = tab_menus.getChildCount();
        // 4つめ以降のタブを消す
        if (count > 3) {
            for (int position = count - 1; position > 2; position--) {
                View view = tab_menus.getChildAt(position);
                if (view != null) {
                    tab_menus.removeView(view);
                }
                mMainPagerAdapter.removeTab(position);
            }
        }

        Typeface fontello = JustawayApplication.getFontello();
        ArrayList<JustawayApplication.Tab> tabs = mApplication.loadTabs();
        if (tabs.size() > 3) {
            int position = 2;
            TypedValue outValueBackground = new TypedValue();
            TypedValue outValueTextColor = new TypedValue();
            Resources.Theme theme = getTheme();
            if (theme != null) {
                theme.resolveAttribute(R.attr.button_stateful, outValueBackground, true);
                theme.resolveAttribute(R.attr.menu_text_color, outValueTextColor, true);
            }
            for (JustawayApplication.Tab tab : tabs) {
                // 標準のタブを動的に生成する時に実装する
                if (tab.id > 0) {
                    Button button = new Button(this);
                    button.setWidth(60);
                    button.setTypeface(fontello);
                    button.setTextSize(22);
                    button.setText(R.string.fontello_list);
                    button.setTextColor(outValueTextColor.data);
                    button.setBackgroundResource(outValueBackground.resourceId);
                    bindTabListener(button, ++position);
                    tab_menus.addView(button);
                    Bundle args = new Bundle();
                    args.putLong("userListId", tab.id);
                    mMainPagerAdapter.addTab(UserListFragment.class, args, tab.name, tab.id);
                }
            }
        }

        mMainPagerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 前回バグで強制終了した場合はダイアログ表示、Yesでレポート送信
        MyUncaughtExceptionHandler.showBugReportDialogIfExist(this);

        // スリープさせない指定
        if (JustawayApplication.getApplication().getKeepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        mApplication.resetDisplaySettings();

        // フォントサイズの変更や他のアクティビティでのfav/RTを反映
        mMainPagerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTwitterStream != null && isFinishing()) {
            mTwitterStream.cleanUp();
            mTwitterStream.shutdown();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHOOSE_USER_LIST:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle == null) {
                        return;
                    }
                    @SuppressWarnings("unchecked")
                    ArrayList<Long> lists = (ArrayList<Long>) bundle.getSerializable("lists");
                    ArrayList<Long> tabs = new ArrayList<Long>();
                    // 後々タブ設定画面に標準のタブを含める
                    tabs.add(-1L);
                    tabs.add(-2L);
                    tabs.add(-3L);
                    tabs.addAll(lists);
                    mApplication.saveTabs(tabs);
                    setupTab();
                    if (lists != null && lists.size() > 0) {
                        JustawayApplication.showToast(R.string.toast_register_list_for_tab);
                    }
                } else {

                    /**
                     * リストの削除を検出してタブを再構成
                     */
                    ArrayList<JustawayApplication.Tab> tabs = mApplication.loadTabs();
                    ArrayList<Long> new_tabs = new ArrayList<Long>();
                    for (JustawayApplication.Tab tab : tabs) {
                        if (tab.id > 0 && mApplication.getUserList(tab.id) == null) {
                            continue;
                        }
                        new_tabs.add(tab.id);
                    }
                    if (tabs.size() != new_tabs.size()) {
                        mApplication.saveTabs(new_tabs);
                        setupTab();
                    }
                }
                break;
            case REQUEST_ACCOUNT_SETTING:

                if (mTwitterStream != null) {
                    mTwitterStream.cleanUp();
                    mTwitterStream.shutdown();
                }

                setupTab();
                int count = mMainPagerAdapter.getCount();
                for (int id = 0; id < count; id++) {
                    BaseFragment fragment = mMainPagerAdapter
                            .findFragmentByPosition(id);
                    if (fragment != null) {
                        fragment.getListAdapter().clear();
                        fragment.reload();
                    }
                }
            case REQUEST_SETTINGS:
                if (resultCode == RESULT_OK) {
                    mApplication.resetDisplaySettings();
                    finish();
                }
            default:
                break;
        }
    }

    private void bindTabListener(TextView textView, final int position) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseFragment f = mMainPagerAdapter.findFragmentByPosition(position);
                if (f == null) {
                    return;
                }
                int id = mViewPager.getCurrentItem();
                if (id != position) {
                    mViewPager.setCurrentItem(position);
                    if (f.isTop()) {
                        showTopView();
                    }
                } else {
                    f.goToTop();
                }
            }
        });
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                BaseFragment f = mMainPagerAdapter.findFragmentByPosition(position);
                if (f == null) {
                    return false;
                }
                f.reload();
                return true;
            }
        });
    }

    private void setup() {

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mMainPagerAdapter = new MainPagerAdapter(this, mViewPager);

        mMainPagerAdapter.addTab(TimelineFragment.class, null, getString(R.string.title_main), TAB_ID_TIMELINE);
        mMainPagerAdapter.addTab(InteractionsFragment.class, null, getString(R.string.title_interactions), TAB_ID_INTERACTIONS);
        mMainPagerAdapter.addTab(DirectMessagesFragment.class, null, getString(R.string.title_direct_messages), TAB_ID_DIRECT_MESSAGE);
        setupTab();

        findViewById(R.id.footer).setVisibility(View.VISIBLE);

        /**
         * タブは前後タブまでは状態が保持されるがそれ以上離れるとViewが破棄されてしまう、
         * あまりに使いづらいの上限を増やしている、指定値＋前後のタブまでが保持されるようになる
         * デフォルト値は1（表示しているタブの前後までしか保持されない）
         */
        mViewPager.setOffscreenPageLimit(10);

        /**
         * スワイプ移動でも移動先が未読アプしている場合、アピ解除判定を行う
         */
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                BaseFragment f = mMainPagerAdapter.findFragmentByPosition(position);
                if (f.isTop()) {
                    showTopView();
                }
                LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);
                int count = tab_menus.getChildCount();
                for (int i = 0; i < count; i++) {
                    Button button = (Button) tab_menus.getChildAt(i);
                    if (button == null) {
                        continue;
                    }
                    if (i == position) {
                        button.setSelected(true);
                    } else {
                        button.setSelected(false);
                    }
                }
                setTitle(mMainPagerAdapter.getPageTitle(position));
            }
        });

        if (mApplication.getQuickMode()) {
            showQuickPanel();
        }
    }

    public void setupStream() {
        if (!mApplication.getStreamingMode()) {
            return;
        }
        if (mTwitterStream != null) {
            mTwitterStream.cleanUp();
            mTwitterStream.shutdown();
            mTwitterStream.setOAuthAccessToken(mApplication.getAccessToken());
        } else {
            mTwitterStream = mApplication.getTwitterStream();
            mUserStreamAdapter = getUserStreamAdapter();
            mTwitterStream.addListener(mUserStreamAdapter);
            mConnectionLifeCycleListener = new MyConnectionLifeCycleListener(this);
            mTwitterStream.addConnectionLifeCycleListener(mConnectionLifeCycleListener);
        }
        mTwitterStream.user();
    }

    /**
     * 新しいツイートが来たアピ
     */
    public void onNewTimeline(Boolean autoScroll) {
        // 表示中のタブかつ自動スクロール時はハイライトしない
        if (mViewPager.getCurrentItem() == 0 && autoScroll) {
            return;
        }
        Button button = (Button) findViewById(R.id.action_timeline);
        mApplication.setThemeTextColor(this, button, R.attr.holo_blue);
    }

    /**
     * 新しいリプが来たアピ
     */
    public void onNewInteractions(Boolean autoScroll) {
        // 表示中のタブかつ自動スクロール時はハイライトしない
        if (mViewPager.getCurrentItem() == 1 && autoScroll) {
            return;
        }
        Button button = (Button) findViewById(R.id.action_interactions);
        mApplication.setThemeTextColor(this, button, R.attr.holo_blue);
    }

    /**
     * 新しいDMが来たアピ
     */
    public void onNewDirectMessage(Boolean autoScroll) {
        // 表示中のタブかつ自動スクロール時はハイライトしない
        if (mViewPager.getCurrentItem() == 2 && autoScroll) {
            return;
        }
        Button button = (Button) findViewById(R.id.action_direct_message);
        mApplication.setThemeTextColor(this, button, R.attr.holo_blue);
    }

    /**
     * 新しいツイートが来たアピ
     */
    public void onNewListStatus(long listId, Boolean autoScroll) {
        // 表示中のタブかつ自動スクロール時はハイライトしない
        int position = mMainPagerAdapter.findPositionById(listId);
        if (mViewPager.getCurrentItem() == position && autoScroll) {
            return;
        }
        if (position >= 0) {
            LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);
            Button button = (Button) tab_menus.getChildAt(position);
            if (button != null) {
                mApplication.setThemeTextColor(this, button, R.attr.holo_blue);
            }
        }
    }

    /**
     * 新しいレコードを見たアピ
     */
    public void showTopView() {
        LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);
        Button button = (Button) tab_menus.getChildAt(mViewPager.getCurrentItem());
        if (button != null) {
            mApplication.setThemeTextColor(this, button, R.attr.menu_text_color);
        }
    }

    /**
     * 弄らないとアプリをバックボタンで閉じる度にタイムラインが初期化されてしまう（アクティビティがfinishされる）
     * moveTaskToBackはホームボタンを押した時と同じ動き
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            EditText editText = (EditText) findViewById(R.id.quick_tweet_edit);
            if (editText != null && editText.getText() != null && editText.getText().length() > 0) {
                editText.setText("");
                setInReplyToStatus(null);
                return false;
            }
            finish();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.profile) {
            /**
             * screenNameは変更可能なのでuserIdを使う
             */
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userId", mApplication.getUserId());
            startActivity(intent);
        } else if (itemId == R.id.user_list) {
            Intent intent = new Intent(this, ChooseUserListsActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSE_USER_LIST);
        } else if (itemId == R.id.search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_SETTINGS);
        } else if (itemId == R.id.official_website) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_website)));
            startActivity(intent);
        } else if (itemId == R.id.feedback) {
            View singleLineTweet = findViewById(R.id.quick_tweet_layout);
            if (singleLineTweet != null && singleLineTweet.getVisibility() == View.VISIBLE) {
                EditText editStatus = (EditText) findViewById(R.id.quick_tweet_edit);
                editStatus.setText(" #justaway");
                editStatus.requestFocus();
                mApplication.showKeyboard(editStatus);
                return true;
            }
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("status", " #justaway");
            startActivity(intent);
        } else if (itemId == R.id.account) {
            Intent intent = new Intent(this, AccountSettingActivity.class);
            startActivityForResult(intent, REQUEST_ACCOUNT_SETTING);
        }
        return true;
    }

    /**
     * ストリーミング受信時の処理
     */
    private MyUserStreamAdapter getUserStreamAdapter() {
        return new MyUserStreamAdapter(this);
    }

    private void showProgressDialog(String message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    public void notifyDataSetChanged() {

        /**
         * 重く同期で処理すると一瞬画面が固まる
         */
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int count = mMainPagerAdapter.getCount();
                for (int id = 0; id < count; id++) {
                    BaseFragment fragment = mMainPagerAdapter.findFragmentByPosition(id);
                    if (fragment != null) {
                        TwitterAdapter twitterAdapter = fragment.getListAdapter();
                        if (twitterAdapter != null) {
                            twitterAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

    public void doDestroyDirectMessage(Long id) {
        new DestroyDirectMessageTask().execute(id);
        // 自分宛のDMを消してもStreaming APIで拾えないで自力で消す
        DirectMessagesFragment fragment = (DirectMessagesFragment) mMainPagerAdapter
                .findFragmentById(TAB_ID_DIRECT_MESSAGE);
        if (fragment != null) {
            fragment.remove(id);
        }
    }

    private static final class MyUserStreamAdapter extends UserStreamAdapter {

        private WeakReference<MainActivity> mActivityRef;

        public MyUserStreamAdapter(MainActivity act) {
            updateActivity(act);
        }

        public void updateActivity(MainActivity act) {
            mActivityRef = new WeakReference<MainActivity>(act);
        }

        @Override
        public void onStatus(Status status) {
            // TODO activity を経由すると、画面回転時に一部取りこぼしが発生するかもしれない(確認が必要)。
            final MainActivity act = mActivityRef.get();
            if (act == null) {
                return;
            }

            Row row = Row.newStatus(status);
            if (JustawayApplication.isMute(row)) {
                return;
            }

            /**
             * ツイートを表示するかどうかはFragmentに任せる
             */
            int count = act.mMainPagerAdapter.getCount();
            for (int id = 0; id < count; id++) {
                BaseFragment fragment = act.mMainPagerAdapter
                        .findFragmentByPosition(id);
                if (fragment != null) {
                    fragment.add(row);
                }
            }
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            super.onDeletionNotice(statusDeletionNotice);

            final MainActivity act = mActivityRef.get();
            if (act == null) {
                return;
            }

            int count = act.mMainPagerAdapter.getCount();
            for (int id = 0; id < count; id++) {
                BaseFragment fragment = act.mMainPagerAdapter
                        .findFragmentByPosition(id);
                if (fragment != null) {
                    fragment.removeStatus(statusDeletionNotice.getStatusId());
                }
            }
        }

        @Override
        public void onFavorite(User source, User target, Status status) {
            final MainActivity act = mActivityRef.get();
            if (act == null) {
                return;
            }

            // 自分の fav を反映
            if (source.getId() == act.mApplication.getUserId()) {
                act.mApplication.setFav(status.getId());
                return;
            }
            Row row = Row.newFavorite(source, target, status);
            BaseFragment fragment = act.mMainPagerAdapter
                    .findFragmentById(TAB_ID_INTERACTIONS);
            new ReFetchFavoriteStatus(fragment).execute(row);
        }

        @Override
        public void onUnfavorite(User arg0, User arg1, Status arg2) {
            final MainActivity act = mActivityRef.get();
            if (act == null) {
                return;
            }

            final User source = arg0;
            final Status status = arg2;

            // 自分の unfav を反映
            if (source.getId() == act.mApplication.getUserId()) {
                act.mApplication.removeFav(status.getId());
                return;
            }

            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JustawayApplication.showToast(source.getScreenName() + " unfav "
                            + status.getText());
                }
            });
        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) {
            super.onDirectMessage(directMessage);

            final MainActivity act = mActivityRef.get();
            if (act == null) {
                return;
            }

            BaseFragment fragment = act.mMainPagerAdapter
                    .findFragmentById(TAB_ID_DIRECT_MESSAGE);
            if (fragment != null) {
                fragment.add(Row.newDirectMessage(directMessage));
            }
        }

        @Override
        public void onDeletionNotice(long directMessageId, long userId) {
            super.onDeletionNotice(directMessageId, userId);

            final MainActivity act = mActivityRef.get();
            if (act == null) {
                return;
            }

            DirectMessagesFragment fragment = (DirectMessagesFragment) act.mMainPagerAdapter
                    .findFragmentById(TAB_ID_DIRECT_MESSAGE);
            if (fragment != null) {
                fragment.remove(directMessageId);
            }
        }
    }

    private static final class MyConnectionLifeCycleListener implements ConnectionLifeCycleListener {
        private WeakReference<MainActivity> mActivityRef;

        public MyConnectionLifeCycleListener(MainActivity act) {
            updateActivity(act);
        }

        public void updateActivity(MainActivity act) {
            mActivityRef = new WeakReference<MainActivity>(act);
        }

        @Override
        public void onConnect() {
            final MainActivity act = mActivityRef.get();
            if (act == null) {
                return;
            }

            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final TextView signalButton = act.mSignalButton;
                    if (signalButton != null) {
                        act.mApplication.setThemeTextColor(act, act.mSignalButton, R.attr.holo_green);
                    }
                }
            });
        }

        @Override
        public void onDisconnect() {
            final MainActivity act = mActivityRef.get();
            if (act == null) {
                return;
            }

            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final TextView signalButton = act.mSignalButton;
                    if (signalButton != null) {
                        if (act.mApplication.getStreamingMode()) {
                            act.mApplication.setThemeTextColor(act, signalButton, R.attr.holo_red);
                        } else {
                            signalButton.setTextColor(Color.WHITE);
                        }
                    }
                }
            });
        }

        @Override
        public void onCleanUp() {
            final MainActivity act = mActivityRef.get();
            if (act == null) {
                return;
            }

            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final TextView signalButton = act.mSignalButton;
                    if (signalButton != null) {
                        if (act.mApplication.getStreamingMode()) {
                            act.mApplication.setThemeTextColor(act, signalButton, R.attr.holo_orange);
                        } else {
                            signalButton.setTextColor(Color.WHITE);
                        }
                    }
                }
            });
        }
    }

    public static final class StreamingSwitchDialogFragment extends DialogFragment {
        private MainActivity mActivity;

        private static StreamingSwitchDialogFragment newInstance(boolean turnOn) {
            final Bundle args = new Bundle(1);
            args.putBoolean("turnOn", turnOn);

            final StreamingSwitchDialogFragment f = new StreamingSwitchDialogFragment();
            f.setArguments(args);
            return f;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            mActivity = (MainActivity) activity;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final boolean turnOn = getArguments().getBoolean("turnOn");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(turnOn ? R.string.confirm_create_streaming : R.string.confirm_destroy_streaming);
            builder.setPositiveButton(getString(R.string.button_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.mApplication.setStreamingMode(turnOn);
                            if (turnOn) {
                                mActivity.setupStream();
                                JustawayApplication.showToast(R.string.toast_create_streaming);
                            } else {
                                if (mActivity.mTwitterStream != null) {
                                    mActivity.mTwitterStream.cleanUp();
                                    mActivity.mTwitterStream.shutdown();
                                }
                                JustawayApplication.showToast(R.string.toast_destroy_streaming);
                            }
                            dismiss();
                        }
                    }
            );
            builder.setNegativeButton(getString(R.string.button_cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }
            );
            return builder.create();
        }
    }
}
