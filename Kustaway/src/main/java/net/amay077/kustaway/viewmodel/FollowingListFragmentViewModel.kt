package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.amay077.kustaway.repository.TwitterRepository
import twitter4j.PagableResponseList
import twitter4j.User

/**
 * フォロー一覧画面の ViewModel
 */
class FollowingListFragmentViewModel (
        private val twitterRepo: TwitterRepository,
        user: User
) : ProfileBaseFragmentViewModel<User>(user) {

    class Factory(
            private val twitterRepo: TwitterRepository,
            private val user: User
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FollowingListFragmentViewModel(twitterRepo, user) as T
    }

    suspend override fun readDataAsync(cursor: Long): TwitterRes<User> {
        val res = twitterRepo.loadFriendList(user.id, cursor);
        return TwitterRes(res, res.hasNext(), res.nextCursor)
    }

}