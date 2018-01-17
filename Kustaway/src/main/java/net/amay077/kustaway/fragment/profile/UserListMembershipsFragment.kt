package net.amay077.kustaway.fragment.profile

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserListAdapter
import net.amay077.kustaway.fragment.common.ListBasedFragment
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.viewmodel.UserListMembershipsFragmentViewModel
import twitter4j.User
import twitter4j.UserList

/**
 * ユーザーの持つリスト一覧
 */
class UserListMembershipsFragment : ListBasedFragment<UserList, Long, UserList, Long, UserListMembershipsFragmentViewModel>() {
    override val id: Long
        get() = (arguments.getSerializable("user") as User).id

    override fun createViewModel(userId: Long): UserListMembershipsFragmentViewModel =
            ViewModelProviders
                    .of(this, UserListMembershipsFragmentViewModel.Factory(
                            TwitterRepository(TwitterManager.getTwitter()),
                            userId
                    ))
                    .get(UserListMembershipsFragmentViewModel::class.java)

    override fun createAdapter(): ProfileItemAdapter<UserList> =
        RecyclerUserListAdapter(context, ArrayList())

    override fun convertDataToViewItem(dataItem: UserList): UserList = dataItem
}
