package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.amay077.kustaway.R
import net.amay077.kustaway.util.MessageUtil
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

    private val _restartRequest = MutableLiveData<Unit>()
    val restartRequest : LiveData<Unit> = _restartRequest

    fun destroyOfficialMute(userId: Long) {
        launch(UI) {
            val success = destroyOfficialMuteAsync(userId)

            MessageUtil.dismissProgressDialog()
            if (success) {
                MessageUtil.showToast(R.string.toast_destroy_official_mute_success)
                _restartRequest.postValue(Unit)
            } else {
                MessageUtil.showToast(R.string.toast_destroy_official_mute_failure)
            }
        }
    }

    suspend fun destroyOfficialMuteAsync(userId: Long) : Boolean {
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

    fun createOfficialMute(userId: Long) {
        launch(UI) {
            val success = createOfficialMuteAsync(userId)

            MessageUtil.dismissProgressDialog()
            if (success) {
                MessageUtil.showToast(R.string.toast_create_official_mute_success)
                _restartRequest.postValue(Unit)
            } else {
                MessageUtil.showToast(R.string.toast_create_official_mute_failure)
            }
        }
    }

    suspend fun createOfficialMuteAsync(userId: Long) : Boolean {
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