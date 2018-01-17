package net.amay077.kustaway.fragment.main.tab

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerTweetAdapter
import net.amay077.kustaway.fragment.dialog.StatusMenuFragment
import net.amay077.kustaway.fragment.profile.ListBasedFragment
import net.amay077.kustaway.listener.StatusLongClickListener
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.viewmodel.HomeTimelineFragmentViewModel
import twitter4j.Status

class HomeTimelineFragment : ListBasedFragment<Row, Unit, Status, HomeTimelineFragmentViewModel>() {
    override val id: Unit
        get() = Unit

    override fun createViewModel(dummy: Unit): HomeTimelineFragmentViewModel  =
            ViewModelProviders
                    .of(this, HomeTimelineFragmentViewModel.Factory(
                            TwitterRepository(TwitterManager.getTwitter())
                    ))
                    .get(HomeTimelineFragmentViewModel::class.java)

    override fun convertDataToViewItem(dataItem: Status): Row = Row.newStatus(dataItem)

    override fun createAdapter(): ProfileItemAdapter<Row> =
            RecyclerTweetAdapter(activity, ArrayList())

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)

        adapter.onItemClickListener = { row ->
            StatusMenuFragment.newInstance(row)
                    .show(activity.getSupportFragmentManager(), "dialog")
        }

        adapter.onItemLongClickListener = { row ->
            StatusLongClickListener.handleRow(activity, row)
        }

        return v
    }
}