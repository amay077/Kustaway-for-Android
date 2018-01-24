package net.amay077.kustaway.extensions

import android.app.Activity
import net.amay077.kustaway.KustawayApplication
import net.amay077.kustaway.repository.TwitterRepository

fun Activity.getTwitterRepo() : TwitterRepository {
    val app = (this.application as KustawayApplication)!!
    return app.twitterRepo!!
}