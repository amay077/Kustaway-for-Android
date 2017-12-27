package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.settings.BasicSettings
import twitter4j.Status
import twitter4j.User

/**
 * フォロワー一覧画面の ViewModel
 */
class FavoritesListFragmentViewModel (
        private val twitterRepo: TwitterRepository,
        user: User
) : ProfileBaseFragmentViewModel<Status>(user) {

    class Factory(
            private val twitterRepo: TwitterRepository,
            private val user: User
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FavoritesListFragmentViewModel(twitterRepo, user) as T
    }

    suspend override fun readDataAsync(cursor: Long): TwitterRes<Status> {
        val res = twitterRepo.loadFavorites(user.id, cursor, BasicSettings.getPageCount());

        val nextCursor = res.firstOrNull { status ->
            cursor == 0L || cursor > status.id
        }?.id ?: -1

        return TwitterRes(res, true, nextCursor)
    }
}