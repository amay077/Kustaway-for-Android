package net.amay077.kustaway.fragment.list

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.adapter.DataItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserAdapter
import net.amay077.kustaway.extensions.getTwitterRepo
import net.amay077.kustaway.fragment.common.ListBasedFragment
import net.amay077.kustaway.viewmodel.UserMemberFragmentViewModel
import twitter4j.User

class UserMemberFragment : ListBasedFragment<User, Long, User, Long, UserMemberFragmentViewModel>() {
    override val id: Long
        get() = arguments.getLong("listId")

    override fun createViewModel(listId: Long): UserMemberFragmentViewModel =
            ViewModelProviders
                    .of(this, UserMemberFragmentViewModel.Factory(
                            this.getTwitterRepo(),
                            listId
                    ))
                    .get(UserMemberFragmentViewModel::class.java)

    override fun createAdapter(): DataItemAdapter<User> =
            RecyclerUserAdapter(context, ArrayList())

    override fun convertDataToViewItem(dataItem: User): User = dataItem
}
