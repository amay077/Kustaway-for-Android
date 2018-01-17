package net.amay077.kustaway.fragment.common

interface SupportListInterface {
    val isTop: Boolean get

    fun goToTop(): Boolean

    fun firstLoad()

    fun reload()
}