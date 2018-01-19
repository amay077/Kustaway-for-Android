package net.amay077.kustaway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.amay077.kustaway.model.AccessTokenManager
import net.amay077.kustaway.model.PagedResponseList
import net.amay077.kustaway.model.Row
import twitter4j.TwitterResponse
import java.util.concurrent.TimeUnit

/**
 * List(RecyclerView) を持つ画面(Fragment)のベースViewModel
 */
abstract class ListBasedFragmentViewModel<TId, TDataItem : TwitterResponse?, TCursor>(
        private val id: TId
) : ViewModel() {

    /** List用のデータを非同期で読む */
    protected suspend abstract fun loadListItemsAsync(id:TId, cursor: TCursor?) : PagedResponseList<TDataItem, TCursor>

    /** List最下段のプログレスの表示ON/OFF */
    private val _isVisibleBottomProgress = MutableLiveData<Boolean>()
    val isVisibleBottomProgress : LiveData<Boolean> = _isVisibleBottomProgress

    /** ListView自体のプログレスの表示ON/OFF */
    private val _isVisibleListView = MutableLiveData<Boolean>()
    val isVisibleListView : LiveData<Boolean> = _isVisibleListView

    /** Pull to Refresh のプログレスの表示ON/OFF */
    private val _isVisiblePullProgress = MutableLiveData<Boolean>()
    val isVisiblePullProgress : LiveData<Boolean> = _isVisiblePullProgress

    /** ListView にバインドするデータ */
    private val _listItems = MutableLiveData<ProfileItemList<TDataItem>>()
    val listItems: LiveData<ProfileItemList<TDataItem>> = _listItems

    // 追加読み込みを有効とするか？
    private var isEnabledAdditionalLoading = false
    private var cursor: TCursor? = null

    private val disposables = CompositeDisposable()

    protected open fun getDataItemStream() : Flowable<TDataItem> =
        Observable.empty<TDataItem>().toFlowable(BackpressureStrategy.LATEST)

    fun startReceiveStreaming() {
        getDataItemStream()
                .buffer(250, TimeUnit.MILLISECONDS)
                .subscribe { items ->
                    items.forEach {
                        Log.d("Stream", it.toString())
                    }

                    if (items.size > 0) {
                        _listItems.postValue(ProfileItemList(items, AddtionalType.AddToTop))
                    }
                }.addTo(disposables)

//        twitterRepo.onCreateStatus
//                .buffer(250, TimeUnit.MILLISECONDS)
//                .subscribe({
//                    //            _listItems.postValue(ProfileItemList(it
////                            .filter({ev-> !isSkip(ev.row)})
////                            .map { it.row.status }, true))
//
//                    var items = it
//                            .filter { ev -> !isSkip(ev.row) }
//                            .map { it.row.status }
//
//                    items.forEach {
//                        Log.d("Stream", it.text)
//                    }
//
//                    if (items.size > 0) {
//                        _listItems.postValue(ProfileItemList(items, AddtionalType.AddToTop))
//                    }
//                }).addTo(disposables)
    }

    /** ListView にバインドするデータをロードして listItems に通知する TODO いずれ RxCommand にする */
    fun loadListItems(isAdditional:Boolean) {
        launch (UI) {
            if (isAdditional) {
                if (!isEnabledAdditionalLoading) {
                    return@launch
                }

                // 追加読み込みの場合は下部プログレスを表示ON
                _isVisibleBottomProgress.postValue(true)
            } else {
                _isVisiblePullProgress.postValue(true)
                cursor = null
            }

            isEnabledAdditionalLoading = false

            // データを非同期で読む
            val res = loadListItemsAsync(id, cursor);

            cursor = res.nextCursor
            isEnabledAdditionalLoading = res.hasNext

            // 読んだデータを通知してリストを表示ON
            _listItems.postValue(ProfileItemList(res.items, AddtionalType.AddToBottom))
            _isVisibleListView.postValue(true)

            // プログレス類は表示OFF
            _isVisiblePullProgress.postValue(false)
            _isVisibleBottomProgress.postValue(false)
        }
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}