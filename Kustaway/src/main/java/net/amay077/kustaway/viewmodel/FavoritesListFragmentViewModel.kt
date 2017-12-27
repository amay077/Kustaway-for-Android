package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.amay077.kustaway.model.PagedResponseList
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.settings.BasicSettings
import twitter4j.Status

/**
 * フォロワー一覧画面の ViewModel
 */
class FavoritesListFragmentViewModel (
        private val twitterRepo: TwitterRepository,
        userId: Long
) : ListBasedFragmentViewModel<Long, Status>(userId) {

    class Factory(
            private val twitterRepo: TwitterRepository,
            private val userId: Long
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FavoritesListFragmentViewModel(twitterRepo, userId) as T
    }

    suspend override fun loadListItemsAsync(userId: Long, cursor: Long): PagedResponseList<Status> {
        val res = twitterRepo.loadFavorites(userId, cursor, BasicSettings.getPageCount());

        val nextCursor = res.firstOrNull { status ->
            cursor == 0L || cursor > status.id
        }?.id ?: -1

        return PagedResponseList(res, true, nextCursor)
    }
}