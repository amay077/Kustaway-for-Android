package net.amay077.kustaway.fragment.profile

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ListView
import android.widget.ProgressBar

import de.greenrobot.event.EventBus
import net.amay077.kustaway.R
import net.amay077.kustaway.adapter.DividerItemDecoration
import net.amay077.kustaway.adapter.RecyclerTweetAdapter
import net.amay077.kustaway.adapter.TwitterAdapter
import net.amay077.kustaway.databinding.ListGuruguruBinding
import net.amay077.kustaway.databinding.PullToRefreshListBinding
import net.amay077.kustaway.event.model.StreamingDestroyStatusEvent
import net.amay077.kustaway.event.action.StatusActionEvent
import net.amay077.kustaway.fragment.dialog.StatusMenuFragment
import net.amay077.kustaway.listener.StatusClickListener
import net.amay077.kustaway.listener.StatusLongClickListener
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.settings.BasicSettings
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.User
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener


/**
 * ユーザーのタイムライン
 */
class UserTimelineFragment : Fragment(), OnRefreshListener {

    private lateinit var mAdapter: RecyclerTweetAdapter
    private lateinit var mUser: User
    private var mAutoLoader = false
    private var mReload = false
    private var mMaxId = 0L

    private lateinit var binding: PullToRefreshListBinding

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bin = inflater?.let { inf -> PullToRefreshListBinding.inflate(inf, container, false) }
        if (bin == null) {
            return null
        }
        binding = bin

        mUser = arguments.getSerializable("user") as User

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(activity)
                .theseChildrenArePullable(R.id.list_view)
                .listener(this)
                .setup(binding.ptrLayout)

        // リストビューの設定
        binding.listView.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context)) // 罫線付ける

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = RecyclerTweetAdapter(activity, ArrayList())
        binding.recyclerView.adapter = mAdapter

        mAdapter.onItemClickListener = { row ->
            StatusMenuFragment.newInstance(row)
                    .show(activity.getSupportFragmentManager(), "dialog")
        }

        mAdapter.onItemLongClickListener = { row ->
            StatusLongClickListener.handleRow(activity, row)
        }

        UserTimelineTask().execute(mUser.screenName)

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(view: RecyclerView?, scrollState: Int) {}

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // see - http://recyclerview.hatenablog.com/entry/2016/11/05/182404
                val totalCount = recyclerView!!.adapter.itemCount //合計のアイテム数
                val childCount = recyclerView.childCount // RecyclerViewに表示されてるアイテム数
                val layoutManager = recyclerView.layoutManager

                if (layoutManager is GridLayoutManager) { // GridLayoutManager
                    val firstPosition = layoutManager.findFirstVisibleItemPosition() // RecyclerViewに表示されている一番上のアイテムポジション
                    if (totalCount == childCount + firstPosition) {
                        // ページング処理
                        // GridLayoutManagerを指定している時のページング処理
                    }
                } else if (layoutManager is LinearLayoutManager) { // LinearLayoutManager
                    val firstPosition = layoutManager.findFirstVisibleItemPosition() // RecyclerViewの一番上に表示されているアイテムのポジション
                    if (totalCount == childCount + firstPosition) {
                        // ページング処理
                        additionalReading()
                    }
                }
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    fun onEventMainThread(event: StatusActionEvent) {
        mAdapter.notifyDataSetChanged()
    }

    fun onEventMainThread(event: StreamingDestroyStatusEvent) {
        mAdapter.remove(event.statusId)
    }

    override fun onRefreshStarted(view: View) {
        mReload = true
        mMaxId = 0
        UserTimelineTask().execute(mUser.screenName)
    }

    private fun additionalReading() {
        if (!mAutoLoader || mReload) {
            return
        }
        binding.guruguru.visibility = View.VISIBLE
        mAutoLoader = false
        UserTimelineTask().execute(mUser.screenName)
    }

    private inner class UserTimelineTask : AsyncTask<String, Void, ResponseList<Status>>() {
        override fun doInBackground(vararg params: String): ResponseList<twitter4j.Status>? {
            try {
                val paging = Paging()
                if (mMaxId > 0) {
                    paging.maxId = mMaxId - 1
                    paging.count = BasicSettings.getPageCount()
                }
                return TwitterManager.getTwitter().getUserTimeline(params[0], paging)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }

        override fun onPostExecute(statuses: ResponseList<twitter4j.Status>?) {
            binding.guruguru.visibility = View.GONE
            if (statuses == null || statuses.size == 0) {
                return
            }

            if (mReload) {
                mAdapter.clear()
                for (status in statuses) {
                    if (mMaxId == 0L || mMaxId > status.id) {
                        mMaxId = status.id
                    }
                    mAdapter.add(Row.newStatus(status))
                }
                mReload = false
                binding.ptrLayout.setRefreshComplete()
                return
            }

            for (status in statuses) {
                if (mMaxId == 0L || mMaxId > status.id) {
                    mMaxId = status.id
                }
                mAdapter.add(Row.newStatus(status))
            }
            mAutoLoader = true
            binding.ptrLayout.setRefreshComplete()
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
}
