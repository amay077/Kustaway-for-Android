package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.amay077.kustaway.model.PagedResponseList
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.settings.BasicSettings
import twitter4j.Status

class UserListFragmentViewModel(
        private val twitterRepo: TwitterRepository,
        private val userListId: Long
) : ListBasedFragmentViewModel<Long, Status, Long>(userListId) {

    class Factory(
            private val twitterRepo: TwitterRepository,
            private val userListId: Long
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                UserListFragmentViewModel(twitterRepo, userListId) as T
    }

    suspend override fun loadListItemsAsync(userListId: Long, cursor: Long?): PagedResponseList<Status, Long> {
        val actualCursor = cursor ?: -1L
        val res = twitterRepo.loadUserListStatuses(userListId, actualCursor, BasicSettings.getPageCount());

        val nextCursor = res.lastOrNull { status ->
            actualCursor < 0L || actualCursor > status.id
        }?.id ?: -1L

        return PagedResponseList(res, true, nextCursor)
    }

}