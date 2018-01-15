package net.amay077.kustaway.fragment.main.tab

import android.os.AsyncTask
import android.view.View

import java.util.ArrayList

import net.amay077.kustaway.event.model.StreamingCreateFavoriteEvent
import net.amay077.kustaway.event.model.StreamingUnFavoriteEvent
import net.amay077.kustaway.model.AccessTokenManager
import net.amay077.kustaway.model.FavRetweetManager
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.model.TabManager
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.settings.BasicSettings
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status

/**
 * お気に入りタブ
 */
class FavoritesFragment : BaseFragment() {

    /**
     * このタブを表す固有のID、ユーザーリストで正数を使うため負数を使う
     */
    override fun getTabId(): Long {
        return TabManager.FAVORITES_TAB_ID
    }

    /**
     * このタブに表示するツイートの定義
     * @param row ストリーミングAPIから受け取った情報（ツイート＋ふぁぼ）
     * CreateFavoriteEventをキャッチしている為、ふぁぼイベントを受け取ることが出来る
     * @return trueは表示しない、falseは表示する
     */
    override fun isSkip(row: Row): Boolean {
        return !row.isFavorite || row.source.id != AccessTokenManager.getUserId()
    }

    override fun taskExecute() {
        FavoritesTask().execute()
    }

    private inner class FavoritesTask : AsyncTask<Void, Void, ResponseList<Status>>() {
        override fun doInBackground(vararg params: Void): ResponseList<twitter4j.Status>? {
            try {
                val paging = Paging()
                if (mMaxId > 0 && !mReloading) {
                    paging.maxId = mMaxId - 1
                    paging.count = BasicSettings.getPageCount()
                }
                return TwitterManager.getTwitter().getFavorites(paging)
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
                    FavRetweetManager.setFav(status.id)
                    if (mMaxId <= 0L || mMaxId > status.id) {
                        mMaxId = status.id
                    }
                    mAdapter.add(Row.newStatus(status))
                }
                mReloading = false
            } else {
                for (status in statuses) {
                    FavRetweetManager.setFav(status.id)
                    if (mMaxId <= 0L || mMaxId > status.id) {
                        mMaxId = status.id
                    }
                    mAdapter.extensionAdd(Row.newStatus(status))
                }
                mAutoLoader = true
                mListView.visibility = View.VISIBLE
            }
            mPullToRefreshLayout.setRefreshComplete()
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
        val removePositions = mAdapter.removeStatus(event.status.id)
        for (removePosition in removePositions) {
            if (removePosition >= 0) {
                val visiblePosition = mListView.firstVisiblePosition
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
