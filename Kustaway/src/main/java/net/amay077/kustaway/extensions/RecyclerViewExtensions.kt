/**
 * RecyclerView 関連の拡張メソッド群
 */
package net.amay077.kustaway.extensions

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

fun RecyclerView.addOnPagingListener(listener:()->Unit) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(view: RecyclerView?, scrollState: Int) {}

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            // see - http://recyclerview.hatenablog.com/entry/2016/11/05/182404
            val totalCount = recyclerView!!.adapter.itemCount //合計のアイテム数
            val childCount = recyclerView.childCount // RecyclerViewに表示されてるアイテム数
            val layoutManager = recyclerView.layoutManager

            if (layoutManager is GridLayoutManager) { // GridLayoutManager
                val firstPosition = layoutManager.findFirstVisibleItemPosition() // RecyclerViewに表示されている一番上のアイテムポジション
                if (totalCount == childCount + firstPosition) {
                    // ページング処理
                    listener()
                }
            } else if (layoutManager is LinearLayoutManager) { // LinearLayoutManager
                val firstPosition = layoutManager.findFirstVisibleItemPosition() // RecyclerViewの一番上に表示されているアイテムのポジション
                if (totalCount == childCount + firstPosition) {
                    // ページング処理
                    listener()
                }
            }
        }
    })
}