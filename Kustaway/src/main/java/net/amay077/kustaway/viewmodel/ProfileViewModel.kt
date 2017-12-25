package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.amay077.kustaway.R
import java.util.concurrent.ExecutorService
import kotlin.coroutines.experimental.suspendCoroutine

class ProfileViewModel(
        private val twitter: twitter4j.Twitter,
        private val twitterExecutor: ExecutorService
) : ViewModel() {

    class Factory(
            private val twitter: twitter4j.Twitter,
            private val twitterExecutor: ExecutorService
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(twitter, twitterExecutor) as T
    }

    // Toastの表示メッセージ
    private val _toastRequest = MutableLiveData<Int>()
    val toastRequest : LiveData<Int> = _toastRequest

    // 画面再起動メッセージ
    private val _restartRequest = MutableLiveData<Unit>()
    val restartRequest : LiveData<Unit> = _restartRequest

    // プログレスの表示メッセージ（空文字の場合、プログレスを非表示にする ← TODO わかりづらい）
    private val _progressRequest = MutableLiveData<String>()
    val progressRequest : LiveData<String> = _progressRequest

    /** 公式ミュートOFF TODO xxxWrapper メソッドは、View側が kotlin 化できたらそっちに移動する */
    fun destroyOfficialMuteWrapper(userId: Long) {
        launch(UI) {
            val success = destroyOfficialMute(userId)

            _progressRequest.postValue("")
            if (success) {
                _toastRequest.postValue(R.string.toast_destroy_official_mute_success)
                _restartRequest.postValue(Unit)
            } else {
                _toastRequest.postValue(R.string.toast_destroy_official_mute_failure)
            }
        }
    }

    suspend fun destroyOfficialMute(userId: Long) : Boolean {
        return suspendCoroutine { cont ->
            twitterExecutor.submit {
                try {
                    twitter.destroyMute(userId)
                    net.amay077.kustaway.model.Relationship.removeOfficialMute(userId)
                    cont.resume(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    cont.resume(false)
                }
            }
        }
    }

    /** 公式ミュートON */
    fun createOfficialMuteWrapper(userId: Long) {
        launch(UI) {
            val success = createOfficialMute(userId)

            _progressRequest.postValue("")
            if (success) {
                _toastRequest.postValue(R.string.toast_create_official_mute_success)
                _restartRequest.postValue(Unit)
            } else {
                _toastRequest.postValue(R.string.toast_create_official_mute_failure)
            }
        }
    }

    suspend fun createOfficialMute(userId: Long) : Boolean {
        return suspendCoroutine { cont ->
            twitterExecutor.submit {
                try {
                    twitter.createMute(userId)
                    net.amay077.kustaway.model.Relationship.setOfficialMute(userId)
                    cont.resume(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    cont.resume(false)
                }
            }
        }
    }
}