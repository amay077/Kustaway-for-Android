package net.amay077.kustaway.fragment.main.tab

/**
 * Created by h_okuyama on 2018/01/16.
 */
interface SupportListInterface {
    val isTop: Boolean get

    fun goToTop(): Boolean

    fun firstLoad()

    fun reload()
}