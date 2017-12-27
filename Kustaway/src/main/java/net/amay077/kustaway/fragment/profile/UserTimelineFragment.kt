package net.amay077.kustaway.fragment.profile

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.greenrobot.event.EventBus
import net.amay077.kustaway.adapter.DividerItemDecoration
import net.amay077.kustaway.adapter.RecyclerTweetAdapter
import net.amay077.kustaway.databinding.PullToRefreshList2Binding
import net.amay077.kustaway.event.action.StatusActionEvent
import net.amay077.kustaway.event.model.StreamingDestroyStatusEvent
import net.amay077.kustaway.extensions.addOnPagingListener
import net.amay077.kustaway.fragment.dialog.StatusMenuFragment
import net.amay077.kustaway.listener.StatusLongClickListener
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.settings.BasicSettings
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.User

/**
 * ユーザーのタイムライン
 */
class UserTimelineFragment : Fragment() {

    private lateinit var mAdapter: RecyclerTweetAdapter
    private lateinit var mUser: User
    private var mAutoLoader = false
    private var mReload = false
    private var mMaxId = 0L

    private lateinit var binding: PullToRefreshList2Binding

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bin = inflater?.let { inf -> PullToRefreshList2Binding.inflate(inf, container, false) }
        if (bin == null) {
            return null
        }
        binding = bin

        mUser = arguments.getSerializable("user") as User

        // リストビューの設定
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

        binding.recyclerView.addOnPagingListener {
            // ページング処理
            additionalReading()
        }

        binding.ptrLayout.setOnRefreshListener {
            mReload = true
            mMaxId = 0
            UserTimelineTask().execute(mUser.screenName)
        }

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
                binding.ptrLayout.setRefreshing(false)
                return
            }

            for (status in statuses) {
                if (mMaxId == 0L || mMaxId > status.id) {
                    mMaxId = status.id
                }
                mAdapter.add(Row.newStatus(status))
            }
            mAutoLoader = true
            binding.ptrLayout.setRefreshing(false)
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
}
