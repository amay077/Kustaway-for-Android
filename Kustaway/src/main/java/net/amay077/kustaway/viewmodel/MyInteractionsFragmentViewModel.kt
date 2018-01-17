package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.amay077.kustaway.model.PagedResponseList
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.settings.BasicSettings
import twitter4j.Status

class MyInteractionsFragmentViewModel(
    private val twitterRepo: TwitterRepository
    ) : ListBasedFragmentViewModel<Unit, Status, Long>(Unit) {

        class Factory(
                private val twitterRepo: TwitterRepository
        ) : ViewModelProvider.NewInstanceFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MyInteractionsFragmentViewModel(twitterRepo) as T
        }

        suspend override fun loadListItemsAsync(id: Unit, cursor: Long?): PagedResponseList<Status, Long> {
            val actualCursor = cursor ?: -1L
            val res = twitterRepo.loadMyMentionsTimeline(actualCursor, BasicSettings.getPageCount());

            val nextCursor = res.lastOrNull { status ->
                actualCursor < 0L || actualCursor > status.id
            }?.id ?: -1L

            return PagedResponseList(res, true, nextCursor)
        }

}