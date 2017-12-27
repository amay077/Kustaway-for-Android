package net.amay077.kustaway.fragment.profile

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserAdapter
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.viewmodel.FollowersListFragmentViewModel
import twitter4j.User

/**
 * フォロワー一覧
 */
class FollowersListFragment : ProfileBaseFragment<User, User, FollowersListFragmentViewModel>() {
    override fun createViewModel(user: User): FollowersListFragmentViewModel =
        ViewModelProviders
                .of(this, FollowersListFragmentViewModel.Factory(
                        TwitterRepository(TwitterManager.getTwitter()),
                        user
                ))
                .get(FollowersListFragmentViewModel::class.java)

    override fun createAdapter(): ProfileItemAdapter<User> =
        RecyclerUserAdapter(activity, ArrayList())

    override fun convertDataToViewItem(dataItem: User): User = dataItem
}

