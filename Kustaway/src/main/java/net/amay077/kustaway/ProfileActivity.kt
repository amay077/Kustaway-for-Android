package net.amay077.kustaway

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import de.greenrobot.event.EventBus
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.amay077.kustaway.adapter.SimplePagerAdapter
import net.amay077.kustaway.databinding.ActivityProfileBinding
import net.amay077.kustaway.event.AlertDialogEvent
import net.amay077.kustaway.extensions.waitForFinish
import net.amay077.kustaway.fragment.profile.*
import net.amay077.kustaway.model.Profile
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.util.ImageUtil
import net.amay077.kustaway.util.MessageUtil
import net.amay077.kustaway.util.ThemeUtil
import net.amay077.kustaway.viewmodel.ProfileActivityViewModel
import twitter4j.User
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileActivityViewModel

    private lateinit var mUser: User
    // Option Menu ID と遷移先URLのマップ
    private val navigateMenuMap = HashMap<Int, String>()

    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTheme(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)

        viewModel = ViewModelProviders
                .of(this, ProfileActivityViewModel.Factory(
                        TwitterRepository(TwitterManager.getTwitter())
                ))
                .get(ProfileActivityViewModel::class.java)

        binding.viewModel = viewModel

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.transitionIcon.transitionName = "image"
            launch (UI) {
                val transition = window.sharedElementEnterTransition
                if (transition != null) {
                    transition.waitForFinish()
                }
                binding.transitionFrame.visibility = View.GONE
            }
        } else {
            binding.transitionFrame.visibility = View.GONE
        }

        // インテント経由での起動をサポート
        val intent = intent
        var userId: Long? = null
        var screenName: String? = null

        if (Intent.ACTION_VIEW == intent.action && intent.data != null
                && intent.data!!.lastPathSegment != null
                && !intent.data!!.lastPathSegment.isEmpty()) {
            screenName = intent.data!!.lastPathSegment
        } else {
            screenName = intent.getStringExtra("screenName")
            if (screenName == null) {
                userId = intent.getLongExtra("userId", 0)
            }
        }

        val profileImageURL = intent.getStringExtra("profileImageURL")
        if (!TextUtils.isEmpty(profileImageURL)) {
            ImageUtil.displayRoundedImage(profileImageURL, binding.transitionIcon)
        }

        // イベントハンドラ登録&ViewModelからの通知受信
        registerEvents()

        // ユーザーのプロフィールを読む
        viewModel.loadProfile(userId, screenName)
    }

    private fun registerEvents() {
        // Toastの表示要求に応答
        viewModel.toastRequest.observe(this, Observer { messageResId -> MessageUtil.showToast(messageResId!!) })

        // プログレスの表示要求に応答
        viewModel.progressRequest.observe(this, Observer { message ->
            if (!TextUtils.isEmpty(message)) {
                MessageUtil.showProgressDialog(this@ProfileActivity, message)
            } else {
                MessageUtil.dismissProgressDialog()
            }
        })

        // 画面の再起動要求に応答
        viewModel.restartRequest.observe(this, Observer { userId -> restart(userId!!) })

        // 読み込んだプロフィール
        viewModel.profile.observe(this, Observer { profile -> onProfileReceived(profile) })
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    fun onEventMainThread(event: AlertDialogEvent) {
        event.dialogFragment.show(supportFragmentManager, "dialog")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile, menu)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.groupId == OPTION_MENU_GROUP_RELATION) {
            when (item.itemId) {
                OPTION_MENU_CREATE_BLOCK -> AlertDialog.Builder(this@ProfileActivity)
                        .setMessage(R.string.confirm_create_block)
                        .setPositiveButton(
                                R.string.button_create_block
                        ) { dialog, which ->
                            MessageUtil.showProgressDialog(this@ProfileActivity, getString(R.string.progress_process))
                            viewModel.updateBlockEnabled(true)
                        }
                        .setNegativeButton(
                                R.string.button_cancel
                        ) { dialog, which -> }
                        .show()
                OPTION_MENU_CREATE_OFFICIAL_MUTE -> AlertDialog.Builder(this@ProfileActivity)
                        .setMessage(R.string.confirm_create_official_mute)
                        .setPositiveButton(
                                R.string.button_create_official_mute
                        ) { dialog, which ->
                            MessageUtil.showProgressDialog(this@ProfileActivity, getString(R.string.progress_process))
                            viewModel.updateOfficialMute(true)
                        }
                        .setNegativeButton(
                                R.string.button_cancel
                        ) { dialog, which -> }
                        .show()
                OPTION_MENU_CREATE_NO_RETWEET -> AlertDialog.Builder(this@ProfileActivity)
                        .setMessage(R.string.confirm_create_no_retweet)
                        .setPositiveButton(
                                R.string.button_create_no_retweet
                        ) { dialog, which ->
                            MessageUtil.showProgressDialog(this@ProfileActivity, getString(R.string.progress_process))
                            viewModel.updateFriendshipRetweetEnabled(false)
                        }
                        .setNegativeButton(
                                R.string.button_cancel
                        ) { dialog, which -> }
                        .show()
                OPTION_MENU_DESTROY_BLOCK -> AlertDialog.Builder(this@ProfileActivity)
                        .setMessage(R.string.confirm_create_block)
                        .setPositiveButton(
                                R.string.button_destroy_block
                        ) { dialog, which ->
                            MessageUtil.showProgressDialog(this@ProfileActivity, getString(R.string.progress_process))
                            viewModel.updateBlockEnabled(false)

                        }
                        .setNegativeButton(
                                R.string.button_cancel
                        ) { dialog, which -> }
                        .show()
                OPTION_MENU_DESTROY_OFFICIAL_MUTE -> AlertDialog.Builder(this@ProfileActivity)
                        .setMessage(R.string.confirm_destroy_official_mute)
                        .setPositiveButton(
                                R.string.button_destroy_official_mute
                        ) { dialog, which ->
                            MessageUtil.showProgressDialog(this@ProfileActivity, getString(R.string.progress_process))
                            viewModel.updateOfficialMute(false)
                        }
                        .setNegativeButton(
                                R.string.button_cancel
                        ) { dialog, which -> }
                        .show()
                OPTION_MENU_DESTROY_NO_RETWEET -> AlertDialog.Builder(this@ProfileActivity)
                        .setMessage(R.string.confirm_destroy_no_retweet)
                        .setPositiveButton(
                                R.string.button_destroy_no_retweet
                        ) { dialog, which ->
                            MessageUtil.showProgressDialog(this@ProfileActivity, getString(R.string.progress_process))
                            viewModel.updateFriendshipRetweetEnabled(true)
                        }
                        .setNegativeButton(
                                R.string.button_cancel
                        ) { dialog, which -> }
                        .show()
                else -> {
                }
            }
            return true
        }
        val intent: Intent
        val text: String
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.send_reply -> {
                intent = Intent(this, PostActivity::class.java)
                text = "@" + mUser!!.screenName + " "
                intent.putExtra("status", text)
                intent.putExtra("selection", text.length)
                startActivity(intent)
            }
            R.id.send_direct_messages -> {
                intent = Intent(this, PostActivity::class.java)
                text = "D " + mUser!!.screenName + " "
                intent.putExtra("status", text)
                intent.putExtra("selection", text.length)
                startActivity(intent)
            }
            R.id.add_to_list -> {
                intent = Intent(this, RegisterUserListActivity::class.java)
                intent.putExtra("userId", mUser!!.id)
                startActivity(intent)
            }
            R.id.open_twitter, R.id.open_favstar, R.id.open_aclog, R.id.open_twilog -> {
                val url = navigateMenuMap[item.itemId]
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
            R.id.report_spam -> AlertDialog.Builder(this@ProfileActivity)
                    .setMessage(R.string.confirm_report_spam)
                    .setPositiveButton(
                            R.string.button_report_spam
                    ) { dialog, which ->
                        MessageUtil.showProgressDialog(this@ProfileActivity, getString(R.string.progress_process))
                        viewModel.reportSpam()
                    }
                    .setNegativeButton(
                            R.string.button_cancel
                    ) { dialog, which -> }
                    .show()
        }
        return true
    }

    private fun onProfileReceived(profile: Profile?) {
        MessageUtil.dismissProgressDialog()
        if (profile == null) {
            MessageUtil.showToast(R.string.toast_load_data_failure, "(null)")
            return
        }
        if (profile.error != null && !profile.error!!.isEmpty()) {
            MessageUtil.showToast(R.string.toast_load_data_failure, profile.error)
            return
        }

        val user = profile.user
        if (user == null) {
            MessageUtil.showToast(R.string.toast_load_data_failure, "(missing user)")
            return
        }
        mUser = user

        // Option Menu 用のマッピング
        navigateMenuMap.put(R.id.open_twitter, "https://twitter.com/" + user.screenName)
        navigateMenuMap.put(R.id.open_favstar, "http://ja.favstar.fm/users/" + user.screenName + "/recent")
        navigateMenuMap.put(R.id.open_aclog, "http://aclog.koba789.com/" + user.screenName + "/timeline")
        navigateMenuMap.put(R.id.open_twilog, "http://twilog.org/" + user.screenName)

        binding.favouritesCount.text = getString(R.string.label_favourites, String.format("%1$,3d", user.favouritesCount))
        binding.statusesCount.text = getString(R.string.label_tweets, String.format("%1$,3d", user.statusesCount))
        binding.friendsCount.text = getString(R.string.label_following, String.format("%1$,3d", user.friendsCount))
        binding.followersCount.text = getString(R.string.label_followers, String.format("%1$,3d", user.followersCount))
        binding.listedCount.text = getString(R.string.label_listed, String.format("%1$,3d", user.listedCount))

        val bannerUrl = user.profileBannerMobileRetinaURL
        if (bannerUrl != null) {
            ImageUtil.displayImage(bannerUrl, binding.banner)
        }

        val relationship = profile.relationship

        if (menu != null) {
            if (relationship!!.isSourceBlockingTarget) {
                menu!!.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_DESTROY_BLOCK, 100, R.string.menu_destroy_block)
            } else {
                menu!!.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_CREATE_BLOCK, 100, R.string.menu_create_block)
            }
            if (relationship.isSourceFollowingTarget) {
                if (relationship.isSourceMutingTarget) {
                    menu!!.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_DESTROY_OFFICIAL_MUTE, 100, R.string.menu_destory_official_mute)
                } else {
                    menu!!.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_CREATE_OFFICIAL_MUTE, 100, R.string.menu_create_official_mute)
                }
                if (relationship.isSourceWantRetweets) {
                    menu!!.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_CREATE_NO_RETWEET, 100, R.string.menu_create_no_retweet)
                } else {
                    menu!!.add(OPTION_MENU_GROUP_RELATION, OPTION_MENU_DESTROY_NO_RETWEET, 100, R.string.menu_destory_no_retweet)
                }
            }
        }

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        val simplePagerAdapter = SimplePagerAdapter(this, binding.pager)

        val args = Bundle()
        args.putSerializable("user", user)
        args.putSerializable("relationship", relationship)
        simplePagerAdapter.addTab(SummaryFragment::class.java, args)
        simplePagerAdapter.addTab(DescriptionFragment::class.java, args)
        simplePagerAdapter.notifyDataSetChanged()
        binding.symbol.setViewPager(binding.pager)

        /**
         * スワイプの度合いに応じて背景色を暗くする
         * これは透明度＆背景色黒で実現している、背景色黒だけだと背景画像が見えないが、
         * 透明度を指定することで背景画像の表示と白色のテキストの視認性を両立している
         */
        binding.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                /**
                 * 背景色の透過度の範囲は00〜99とする（FFは真っ黒で背景画像が見えない）
                 * 99は10進数で153
                 * positionは0が1ページ目（スワイプ中含む）で1だと完全に2ページ目に遷移した状態
                 * positionOffsetには0.0〜1.0のスクロール率がかえってくる、真ん中だと0.5
                 * hexにはpositionOffsetに応じて00〜99（153）の値が入るように演算を行う
                 * 例えばpositionOffsetが0.5の場合はhexは4dになる
                 * positionが1の場合は最大値（99）を無条件で設定している
                 */

                val maxHex = 153 // 0x99
                val hex = if (position == 1) "99" else String.format("%02X", (maxHex * positionOffset).toInt())
                binding.pager.setBackgroundColor(Color.parseColor("#" + hex + "000000"))

                // OnPageChangeListenerは1つしかセットできないのでCirclePageIndicatorの奴も呼んであげる
                binding.symbol.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                // OnPageChangeListenerは1つしかセットできないのでCirclePageIndicatorの奴も呼んであげる
                binding.symbol.onPageSelected(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                // OnPageChangeListenerは1つしかセットできないのでCirclePageIndicatorの奴も呼んであげる
                binding.symbol.onPageScrollStateChanged(state)
            }
        })

        // ユーザリスト用のタブ
        val listPagerAdapter = SimplePagerAdapter(this, binding.listPager)

        val listArgs = Bundle()
        listArgs.putSerializable("user", user)
        listPagerAdapter.addTab(UserTimelineFragment::class.java, listArgs)
        listPagerAdapter.addTab(FollowingListFragment::class.java, listArgs)
        listPagerAdapter.addTab(FollowersListFragment::class.java, listArgs)
        listPagerAdapter.addTab(UserListMembershipsFragment::class.java, listArgs)
        listPagerAdapter.addTab(FavoritesListFragment::class.java, listArgs)
        listPagerAdapter.notifyDataSetChanged()
        binding.listPager.offscreenPageLimit = 5

        /**
         * タブのラベル情報を配列に入れておく
         */
        val tabs = arrayOf(binding.statusesCount, binding.friendsCount, binding.followersCount, binding.listedCount, binding.favouritesCount)


        val colorBlue = ThemeUtil.getThemeTextColor(R.attr.holo_blue)
        val colorWhite = ThemeUtil.getThemeTextColor(R.attr.text_color)

        tabs[0].setTextColor(colorBlue)

        binding.listPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                /**
                 * タブのindexと選択されたpositionを比較して色を設定
                 */
                for (i in tabs.indices) {
                    tabs[i].setTextColor(if (i == position) colorBlue else colorWhite)
                }
            }
        })

        for (i in tabs.indices) {
            tabs[i].setOnClickListener { binding.listPager.currentItem = i }
        }

    }

    fun restart(userId: Long) {
        val intent = Intent()
        intent.setClass(this, ProfileActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
        finish()
    }

    companion object {
        private val OPTION_MENU_GROUP_RELATION = 1
        private val OPTION_MENU_CREATE_BLOCK = 1
        private val OPTION_MENU_CREATE_OFFICIAL_MUTE = 2
        private val OPTION_MENU_CREATE_NO_RETWEET = 3
        private val OPTION_MENU_DESTROY_BLOCK = 4
        private val OPTION_MENU_DESTROY_OFFICIAL_MUTE = 5
        private val OPTION_MENU_DESTROY_NO_RETWEET = 6
    }
}