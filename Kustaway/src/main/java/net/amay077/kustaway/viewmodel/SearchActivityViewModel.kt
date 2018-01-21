package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.amay077.kustaway.repository.TwitterRepository

/**
 * Created by h_okuyama on 2018/01/21.
 */
class SearchActivityViewModel(
    private val twitterRepo: TwitterRepository
) : ViewModel() {
    private val TAG = "SearchActivityViewModel"

    class Factory(
            private val twitterRepo: TwitterRepository
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchActivityViewModel(twitterRepo) as T
    }
}