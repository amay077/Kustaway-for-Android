package net.amay077.kustaway.fragment.profile

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserAdapter
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.viewmodel.FollowingListFragmentViewModel
import twitter4j.User

/**
 * フォロー一覧
 */
class FollowingListFragment : ListBasedFragment<User, Long, User, FollowingListFragmentViewModel>() {
    override val id: Long
        get() = (arguments.getSerializable("user") as User).id

    override fun createViewModel(userId: Long): FollowingListFragmentViewModel =
            ViewModelProviders
                    .of(this, FollowingListFragmentViewModel.Factory(
                            TwitterRepository(TwitterManager.getTwitter()),
                            userId
                    ))
                    .get(FollowingListFragmentViewModel::class.java)

    override fun createAdapter(): ProfileItemAdapter<User> =
        RecyclerUserAdapter(activity, ArrayList())

    override fun convertDataToViewItem(dataItem: User): User = dataItem
}
