package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import net.amay077.kustaway.model.AccessTokenManager
import net.amay077.kustaway.model.PagedResponseList
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.settings.BasicSettings
import twitter4j.Status

class MyTimelineFragmentViewModel(
        private val twitterRepo: TwitterRepository
) : ListBasedFragmentViewModel<Unit, Status, Long>(Unit) {

    class Factory(
            private val twitterRepo: TwitterRepository
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MyTimelineFragmentViewModel(twitterRepo) as T
    }

    private val isSkip : (Row)->Boolean =  { row ->
        if (row.isStatus) {
            val retweet = row.status.retweetedStatus
            retweet != null && retweet.user.id == AccessTokenManager.getUserId()
        } else {
            true
        }
    }

    override fun getDataItemStream() : Flowable<Status> {
        if (twitterRepo == null) {
            return Observable.empty<Status>().toFlowable(BackpressureStrategy.LATEST)
        } else {
            return twitterRepo.onCreateStatus.filter { !isSkip(it.row) }.map { it.row.status }
        }
    }

    suspend override fun loadListItemsAsync(id: Unit, cursor: Long?): PagedResponseList<Status, Long> {
        val actualCursor = cursor ?: -1L
        val res = twitterRepo.loadMyTimeline(actualCursor, BasicSettings.getPageCount());

        val nextCursor = res.lastOrNull { status ->
            actualCursor < 0L || actualCursor > status.id
        }?.id ?: -1L

        return PagedResponseList(res, true, nextCursor)
    }
}