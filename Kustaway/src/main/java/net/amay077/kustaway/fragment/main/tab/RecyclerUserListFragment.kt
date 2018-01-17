package net.amay077.kustaway.fragment.main.tab

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.adapter.DataItemAdapter
import net.amay077.kustaway.adapter.RecyclerTweetAdapter
import net.amay077.kustaway.extensions.applyTapEvents
import net.amay077.kustaway.fragment.common.ListBasedFragment
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.viewmodel.UserListFragmentViewModel
import twitter4j.Status

class RecyclerUserListFragment : ListBasedFragment<Row, Long, Status, Long, UserListFragmentViewModel>() {

    override val id: Long
        get() = arguments.getLong("userListId")

    override fun createAdapter(): DataItemAdapter<Row> =
            RecyclerTweetAdapter(activity, ArrayList()).applyTapEvents(activity)

    override fun convertDataToViewItem(dataItem: Status): Row = Row.newStatus(dataItem)

    override fun createViewModel(userListId: Long): UserListFragmentViewModel =
            ViewModelProviders
                    .of(this, UserListFragmentViewModel.Factory(
                            TwitterRepository(TwitterManager.getTwitter()),
                            userListId
                    ))
                    .get(UserListFragmentViewModel::class.java)
}