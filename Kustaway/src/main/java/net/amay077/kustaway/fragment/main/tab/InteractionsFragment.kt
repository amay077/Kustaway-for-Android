package net.amay077.kustaway.fragment.main.tab

import android.os.AsyncTask
import android.view.View

import java.util.ArrayList

import net.amay077.kustaway.event.model.StreamingCreateFavoriteEvent
import net.amay077.kustaway.event.model.StreamingUnFavoriteEvent
import net.amay077.kustaway.extensions.firstVisiblePosition
import net.amay077.kustaway.extensions.setSelectionFromTop
import net.amay077.kustaway.model.AccessTokenManager
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.model.TabManager
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.settings.BasicSettings
import net.amay077.kustaway.util.StatusUtil
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status

/**
 * 将来「つながり」タブ予定のタブ、現在はリプしか表示されない
 */
class InteractionsFragment : BaseFragment() {

    /**
     * このタブを表す固有のID、ユーザーリストで正数を使うため負数を使う
     */
    override val tabId: Long
        get() = TabManager.INTERACTIONS_TAB_ID

    /**
     * このタブに表示するツイートの定義
     * @param row ストリーミングAPIから受け取った情報（ツイート＋ふぁぼ）
     * CreateFavoriteEventをキャッチしている為、ふぁぼイベントを受け取ることが出来る
     * @return trueは表示しない、falseは表示する
     */
    override fun isSkip(row: Row): Boolean {
        if (row.isFavorite) {
            return row.source.id == AccessTokenManager.getUserId()
        }
        if (row.isStatus) {

            val status = row.status
            val retweet = status.retweetedStatus

            /**
             * 自分のツイートがRTされた時
             */
            if (retweet != null && retweet.user.id == AccessTokenManager.getUserId()) {
                return false
            }

            /**
             * 自分宛のメンション（但し「自分をメンションに含むツイートがRTされた時」はうざいので除く）
             */
            if (retweet == null && StatusUtil.isMentionForMe(status)) {
                return false
            }
        }
        return true
    }

    override fun taskExecute() {
        MentionsTimelineTask().execute()
    }

    private inner class MentionsTimelineTask : AsyncTask<Void, Void, ResponseList<Status>>() {
        override fun doInBackground(vararg params: Void): ResponseList<twitter4j.Status>? {
            try {
                val paging = Paging()
                if (mMaxId > 0 && !mReloading) {
                    paging.maxId = mMaxId - 1
                    paging.count = BasicSettings.getPageCount()
                }
                return TwitterManager.getTwitter().getMentionsTimeline(paging)
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
                mPullToRefreshLayout.isRefreshing = false
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
            mPullToRefreshLayout.isRefreshing = false
        }
    }

    /**
     * ストリーミングAPIからふぁぼを受け取った時のイベント
     * @param event ふぁぼイベント
     */
    fun onEventMainThread(event: StreamingCreateFavoriteEvent) {
        addStack(event.row)
    }

    /**
     * ストリーミングAPIからあんふぁぼイベントを受信
     * @param event ツイート
     */
    fun onEventMainThread(event: StreamingUnFavoriteEvent) {
        val removePositions = mAdapter!!.removeStatus(event.status.id)
        for (removePosition in removePositions) {
            if (removePosition >= 0) {
                val visiblePosition = mListView.firstVisiblePosition()
                if (visiblePosition > removePosition) {
                    val view = mListView.getChildAt(0)
                    val y = view?.top ?: 0
                    mListView.setSelectionFromTop(visiblePosition - 1, y)
                    break
                }
            }
        }
    }
}
