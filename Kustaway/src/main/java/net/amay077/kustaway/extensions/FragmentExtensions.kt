package net.amay077.kustaway.extensions

import android.support.v4.app.Fragment
import net.amay077.kustaway.repository.TwitterRepository

fun Fragment.getTwitterRepo() : TwitterRepository {
    return this.activity.getTwitterRepo()
}