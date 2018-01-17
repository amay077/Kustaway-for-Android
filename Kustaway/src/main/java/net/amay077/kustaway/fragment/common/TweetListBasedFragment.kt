package net.amay077.kustaway.fragment.common

import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerTweetAdapter
import net.amay077.kustaway.extensions.applyTapEvents
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.viewmodel.ListBasedFragmentViewModel
import twitter4j.Status

abstract class TweetListBasedFragment<TViewModel:ListBasedFragmentViewModel<Unit, Status, Long>> : ListBasedFragment<Row, Unit, Status, Long, TViewModel>() {
    override val id: Unit
        get() = Unit

    override fun convertDataToViewItem(dataItem: Status): Row = Row.newStatus(dataItem)

    override fun createAdapter(): ProfileItemAdapter<Row> =
            RecyclerTweetAdapter(activity, ArrayList()).applyTapEvents(activity)
}