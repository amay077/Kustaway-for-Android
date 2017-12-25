package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.os.AsyncTask
import android.provider.Contacts
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.amay077.kustaway.R
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.util.MessageUtil
import kotlin.coroutines.experimental.suspendCoroutine

class ProfileViewModel : ViewModel() {

    class Factory() : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel() as T
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
            try {
                TwitterManager.getTwitter().destroyMute(userId)
                net.amay077.kustaway.model.Relationship.removeOfficialMute(userId)
                cont.resume(true)
            } catch (e: Exception) {
                e.printStackTrace()
                cont.resume(false)
            }
        }
    }
}