package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class ScaleImageActivityViewModel : ViewModel() {

    class Factory(
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ScaleImageActivityViewModel() as T
    }

}