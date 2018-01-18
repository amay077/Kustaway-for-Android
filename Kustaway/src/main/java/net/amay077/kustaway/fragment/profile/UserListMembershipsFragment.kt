package net.amay077.kustaway.fragment.profile

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.adapter.DataItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserListAdapter
import net.amay077.kustaway.extensions.getTwitterRepo
import net.amay077.kustaway.fragment.common.ListBasedFragment
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
                            this.getTwitterRepo(),
                            userId
                    ))
                    .get(UserListMembershipsFragmentViewModel::class.java)

    override fun createAdapter(): DataItemAdapter<UserList> =
        RecyclerUserListAdapter(context, ArrayList())

    override fun convertDataToViewItem(dataItem: UserList): UserList = dataItem
}
