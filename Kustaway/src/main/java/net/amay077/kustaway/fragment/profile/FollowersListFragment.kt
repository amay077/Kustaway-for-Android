package net.amay077.kustaway.fragment.profile

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.adapter.DataItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserAdapter
import net.amay077.kustaway.extensions.getTwitterRepo
import net.amay077.kustaway.fragment.common.ListBasedFragment
import net.amay077.kustaway.viewmodel.FollowersListFragmentViewModel
import twitter4j.User

/**
 * フォロワー一覧
 */
class FollowersListFragment : ListBasedFragment<User, Long, User, Long, FollowersListFragmentViewModel>() {
    override val id: Long
        get() = (arguments.getSerializable("user") as User).id

    override fun createViewModel(userId: Long): FollowersListFragmentViewModel =
        ViewModelProviders
                .of(this, FollowersListFragmentViewModel.Factory(
                        this.getTwitterRepo(),
                        userId
                ))
                .get(FollowersListFragmentViewModel::class.java)

    override fun createAdapter(): DataItemAdapter<User> =
        RecyclerUserAdapter(activity, ArrayList())

    override fun convertDataToViewItem(dataItem: User): User = dataItem
}

