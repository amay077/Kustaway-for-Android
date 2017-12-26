package net.amay077.kustaway.adapter

import android.support.v7.widget.RecyclerView

/**
 * プロフィール画面の RecyclerView の共通既定Adapter
 */
abstract class ProfileItemAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    abstract fun add(item: T)
}