package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.amay077.kustaway.model.PagedResponseList
import net.amay077.kustaway.repository.TwitterRepository
import twitter4j.User

/**
 * フォロー一覧画面の ViewModel
 */
class RetweetersFragmentViewModel (
        private val twitterRepo: TwitterRepository,
        statusId: Long
) : ListBasedFragmentViewModel<Long, User>(statusId) {

    class Factory(
            private val twitterRepo: TwitterRepository,
            private val statusId: Long
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RetweetersFragmentViewModel(twitterRepo, statusId) as T
    }

    suspend override fun loadListItemsAsync(listId:Long, cursor: Long): PagedResponseList<User> {
        val res = twitterRepo.loadRetweets(listId);
        // Retweet した user を一意にして返す
        return PagedResponseList(res.map { s -> s.user }.distinctBy { u -> u.id },
                false, -1L)
    }
}