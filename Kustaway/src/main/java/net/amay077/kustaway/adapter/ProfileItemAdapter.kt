package net.amay077.kustaway.adapter

import android.support.v7.widget.RecyclerView
import net.amay077.kustaway.listener.StatusClickListener

/**
 * プロフィール画面の RecyclerView の共通既定Adapter
 */
abstract class ProfileItemAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    abstract fun add(item: T)
    abstract var onItemClickListener: ((T)->Unit)
    abstract var onItemLongClickListener: ((T)->Boolean)
    abstract fun clear()
    abstract fun remove(id: Long)
}