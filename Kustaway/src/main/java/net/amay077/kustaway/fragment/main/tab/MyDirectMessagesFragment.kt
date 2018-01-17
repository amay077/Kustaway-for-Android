package net.amay077.kustaway.fragment.main.tab

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.adapter.DataItemAdapter
import net.amay077.kustaway.adapter.RecyclerTweetAdapter
import net.amay077.kustaway.extensions.applyTapEvents
import net.amay077.kustaway.fragment.common.ListBasedFragment
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.viewmodel.MyDirectMessageFragmentViewModel
import twitter4j.DirectMessage

class MyDirectMessagesFragment : ListBasedFragment<Row, Unit, DirectMessage, Long, MyDirectMessageFragmentViewModel>() {

    override val id: Unit
        get() = Unit

    override fun createAdapter(): DataItemAdapter<Row> =
            RecyclerTweetAdapter(activity, ArrayList()).applyTapEvents(activity)

    override fun convertDataToViewItem(dataItem: DirectMessage): Row = Row.newDirectMessage(dataItem)

    override fun createViewModel(dummy: Unit): MyDirectMessageFragmentViewModel =
            ViewModelProviders
                    .of(this, MyDirectMessageFragmentViewModel.Factory(
                            TwitterRepository(TwitterManager.getTwitter())
                    ))
                    .get(MyDirectMessageFragmentViewModel::class.java)
}