package net.amay077.kustaway.extensions

import android.app.Activity
import net.amay077.kustaway.KustawayApplication
import net.amay077.kustaway.repository.TwitterRepository

fun Activity.getTwitterRepo() : TwitterRepository {
    return (this.application as KustawayApplication).twitterRepo
}