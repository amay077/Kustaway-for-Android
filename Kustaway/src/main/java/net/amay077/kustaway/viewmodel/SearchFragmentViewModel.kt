package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.amay077.kustaway.model.PagedResponseList
import net.amay077.kustaway.repository.TwitterRepository
import twitter4j.Query
import twitter4j.Status

class SearchFragmentViewModel(
        private val twitterRepo: TwitterRepository,
        private val keyword: String
) : ListBasedFragmentViewModel<String, Status, Query>(keyword) {

    class Factory(
            private val twitterRepo: TwitterRepository,
            private val keyword: String
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SearchFragmentViewModel(twitterRepo, keyword) as T
    }

    suspend override fun loadListItemsAsync(keyword: String, nextQuery: Query?): PagedResponseList<Status, Query> {

        val res = twitterRepo.let { repo ->
            if (nextQuery == null) {
                repo.search(keyword)
            } else {
                repo.search(nextQuery)
            }
        }

        return PagedResponseList(res.tweets, res.hasNext(), res.nextQuery())
    }

}