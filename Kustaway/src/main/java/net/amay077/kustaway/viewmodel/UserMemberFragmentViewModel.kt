package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.amay077.kustaway.model.PagedResponseList
import net.amay077.kustaway.repository.TwitterRepository
import twitter4j.User

/**
 * リストのユーザー一覧画面の ViewModel
 */
class UserMemberFragmentViewModel (
        private val twitterRepo: TwitterRepository,
        listId: Long
) : ListBasedFragmentViewModel<Long, User, Long>(listId) {

    class Factory(
            private val twitterRepo: TwitterRepository,
            private val userId: Long
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                UserMemberFragmentViewModel(twitterRepo, userId) as T
    }

    suspend override fun loadListItemsAsync(listId:Long, cursor: Long?): PagedResponseList<User, Long> {
        val res = twitterRepo.loadUserListMembers(listId, cursor ?: -1L);
        return PagedResponseList(res, res.hasNext(), res.nextCursor)
    }
}