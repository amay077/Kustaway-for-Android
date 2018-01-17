package net.amay077.kustaway.extensions

import android.support.v4.app.FragmentActivity
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.fragment.dialog.StatusMenuFragment
import net.amay077.kustaway.listener.StatusLongClickListener
import net.amay077.kustaway.model.Row

fun ProfileItemAdapter<Row>.applyTapEvents(activity:FragmentActivity) : ProfileItemAdapter<Row> {
    this.onItemClickListener = { row ->
        StatusMenuFragment.newInstance(row)
                .show(activity.getSupportFragmentManager(), "dialog")
    }

    this.onItemLongClickListener = { row ->
        StatusLongClickListener.handleRow(activity, row)
    }

    return this
}