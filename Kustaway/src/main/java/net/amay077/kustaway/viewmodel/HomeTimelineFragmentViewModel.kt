package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.amay077.kustaway.model.PagedResponseList
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.settings.BasicSettings
import twitter4j.Status

class HomeTimelineFragmentViewModel (
        private val twitterRepo: TwitterRepository
) : ListBasedFragmentViewModel<Unit, Status>(Unit) {

    class Factory(
            private val twitterRepo: TwitterRepository
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeTimelineFragmentViewModel(twitterRepo) as T
    }

    suspend override fun loadListItemsAsync(id: Unit, cursor: Long): PagedResponseList<Status> {
        val res = twitterRepo.loadHomeTimeline(cursor, BasicSettings.getPageCount());

        val nextCursor = res.lastOrNull { status ->
            cursor < 0L || cursor > status.id
        }?.id ?: -1L

        return PagedResponseList(res, true, nextCursor)
    }
}