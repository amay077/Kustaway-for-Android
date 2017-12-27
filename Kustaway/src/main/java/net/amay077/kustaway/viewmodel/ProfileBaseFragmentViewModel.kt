package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import twitter4j.TwitterResponse
import twitter4j.User

data class ProfileData<T>(
        val data:List<T>,
        val isAdditional: Boolean
)

data class TwitterRes<T>(
        val data:List<T>,
        val hasNext: Boolean,
        val nextCursor: Long
)

/**
 * Created by nepula_h_okuyama on 2017/12/27.
 */
abstract class ProfileBaseFragmentViewModel<T : TwitterResponse?>(
        protected val user: User
) : ViewModel() {

    private val _isVisibleBottomProgress = MutableLiveData<Boolean>()
    val isVisibleBottomProgress : LiveData<Boolean> = _isVisibleBottomProgress

    private val _isVisibleListView = MutableLiveData<Boolean>()
    val isVisibleListView : LiveData<Boolean> = _isVisibleListView

    private val _isVisiblePullProgress = MutableLiveData<Boolean>()
    val isVisiblePullProgress : LiveData<Boolean> = _isVisiblePullProgress

    private val _data = MutableLiveData<ProfileData<T>>()
    val data : LiveData<ProfileData<T>> = _data

    private var cursor: Long = -1
    private var autoLoader = true

    fun readData(isAdditional:Boolean) {
        launch (UI) {
            if (!autoLoader) {
                return@launch
            }

            if (!isAdditional) {
                cursor = -1
            } else {
                _isVisibleBottomProgress.postValue(true)
            }

            autoLoader = false
            val res = readDataAsync(cursor);
            cursor = res.nextCursor
            autoLoader = res.hasNext

            _data.postValue(ProfileData(res.data, isAdditional))

            _isVisibleListView.postValue(true)
            _isVisiblePullProgress.postValue(false)
            _isVisibleBottomProgress.postValue(false)
        }
    }

    suspend abstract fun readDataAsync(cursor: Long) : TwitterRes<T>
}