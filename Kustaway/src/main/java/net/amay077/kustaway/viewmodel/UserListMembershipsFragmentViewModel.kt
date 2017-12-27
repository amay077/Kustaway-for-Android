package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.amay077.kustaway.model.PagedResponseList
import net.amay077.kustaway.repository.TwitterRepository
import twitter4j.User
import twitter4j.UserList

/**
 * フォロー一覧画面の ViewModel
 */
class UserListMembershipsFragmentViewModel (
        private val twitterRepo: TwitterRepository,
        user: User
) : ProfileBaseFragmentViewModel<UserList>(user) {

    class Factory(
            private val twitterRepo: TwitterRepository,
            private val user: User
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                UserListMembershipsFragmentViewModel(twitterRepo, user) as T
    }

    suspend override fun loadListItemsAsync(userId:Long, cursor: Long): PagedResponseList<UserList> {
        val res = twitterRepo.loadUserListMemberships(userId, cursor);
        return PagedResponseList(res, res.hasNext(), res.nextCursor)
    }
}