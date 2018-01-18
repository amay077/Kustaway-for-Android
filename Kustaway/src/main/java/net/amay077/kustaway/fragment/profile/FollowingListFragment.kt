package net.amay077.kustaway.fragment.profile

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.adapter.DataItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserAdapter
import net.amay077.kustaway.extensions.getTwitterRepo
import net.amay077.kustaway.fragment.common.ListBasedFragment
import net.amay077.kustaway.viewmodel.FollowingListFragmentViewModel
import twitter4j.User

/**
 * フォロー一覧
 */
class FollowingListFragment : ListBasedFragment<User, Long, User, Long, FollowingListFragmentViewModel>() {
    override val id: Long
        get() = (arguments.getSerializable("user") as User).id

    override fun createViewModel(userId: Long): FollowingListFragmentViewModel =
            ViewModelProviders
                    .of(this, FollowingListFragmentViewModel.Factory(
                            this.getTwitterRepo(),
                            userId
                    ))
                    .get(FollowingListFragmentViewModel::class.java)

    override fun createAdapter(): DataItemAdapter<User> =
        RecyclerUserAdapter(activity, ArrayList())

    override fun convertDataToViewItem(dataItem: User): User = dataItem
}
