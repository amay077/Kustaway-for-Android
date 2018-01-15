package net.amay077.kustaway.fragment.main.tab

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.amay077.kustaway.model.AccessTokenManager
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.model.TabManager
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.settings.BasicSettings
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status

/**
 * タイムライン、すべての始まり
 */
class TimelineFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        firstLoad()
        return view
    }

    /**
     * このタブを表す固有のID、ユーザーリストで正数を使うため負数を使う
     */
    override val tabId: Long
        get() = TabManager.TIMELINE_TAB_ID

    /**
     * このタブに表示するツイートの定義
     * @param row ストリーミングAPIから受け取った情報（ツイート）
     * @return trueは表示しない、falseは表示する
     */
    override fun isSkip(row: Row): Boolean {
        if (row.isStatus) {
            val retweet = row.status.retweetedStatus
            return retweet != null && retweet.user.id == AccessTokenManager.getUserId()
        } else {
            return true
        }
    }

    override fun taskExecute() {
        HomeTimelineTask().execute()
    }

    private inner class HomeTimelineTask : AsyncTask<Void, Void, ResponseList<Status>>() {
        override fun doInBackground(vararg params: Void): ResponseList<twitter4j.Status>? {
            try {
                val paging = Paging()
                if (mMaxId > 0 && !mReloading) {
                    paging.maxId = mMaxId - 1
                    paging.count = BasicSettings.getPageCount()
                }
                return TwitterManager.getTwitter().getHomeTimeline(paging)
            } catch (e: OutOfMemoryError) {
                return null
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }

        override fun onPostExecute(statuses: ResponseList<twitter4j.Status>?) {
            mFooter.visibility = View.GONE
            if (statuses == null || statuses.size == 0) {
                mReloading = false
                mPullToRefreshLayout.setRefreshComplete()
                mListView.visibility = View.VISIBLE
                return
            }
            if (mReloading) {
                clear()
                for (status in statuses) {
                    if (mMaxId <= 0L || mMaxId > status.id) {
                        mMaxId = status.id
                    }
                    mAdapter!!.add(Row.newStatus(status))
                }
                mReloading = false
            } else {
                for (status in statuses) {
                    if (mMaxId <= 0L || mMaxId > status.id) {
                        mMaxId = status.id
                    }
                    mAdapter!!.extensionAdd(Row.newStatus(status))
                }
                mAutoLoader = true
                mListView.visibility = View.VISIBLE
            }
            mPullToRefreshLayout.setRefreshComplete()
        }
    }
}
