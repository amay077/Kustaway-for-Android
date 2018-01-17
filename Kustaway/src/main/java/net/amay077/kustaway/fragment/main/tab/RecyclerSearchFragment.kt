package net.amay077.kustaway.fragment.main.tab

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerTweetAdapter
import net.amay077.kustaway.extensions.applyTapEvents
import net.amay077.kustaway.fragment.common.ListBasedFragment
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.viewmodel.SearchFragmentViewModel
import twitter4j.Query
import twitter4j.Status

class RecyclerSearchFragment : ListBasedFragment<Row, String, Status, Query, SearchFragmentViewModel>() {

    override val id: String
        get() = arguments.getString("searchWord")

    override fun createAdapter(): ProfileItemAdapter<Row> =
            RecyclerTweetAdapter(activity, ArrayList()).applyTapEvents(activity)

    override fun convertDataToViewItem(dataItem: Status): Row = Row.newStatus(dataItem)

    override fun createViewModel(keyword: String): SearchFragmentViewModel =
            ViewModelProviders
                    .of(this, SearchFragmentViewModel.Factory(
                            TwitterRepository(TwitterManager.getTwitter()),
                            keyword
                    ))
                    .get(SearchFragmentViewModel::class.java)
}