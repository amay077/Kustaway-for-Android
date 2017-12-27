package net.amay077.kustaway.viewmodel

/**
 * プロフィール画面のリスト(RecycleView)に与えるデータ
 */
data class ProfileItemList<T>(
        val items:List<T>,
        val isAdditional: Boolean
)
