package net.amay077.kustaway.fragment.list

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserAdapter
import net.amay077.kustaway.fragment.common.ListBasedFragment
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.viewmodel.UserMemberFragmentViewModel
import twitter4j.User

class UserMemberFragment : ListBasedFragment<User, Long, User, Long, UserMemberFragmentViewModel>() {
    override val id: Long
        get() = arguments.getLong("listId")

    override fun createViewModel(listId: Long): UserMemberFragmentViewModel =
            ViewModelProviders
                    .of(this, UserMemberFragmentViewModel.Factory(
                            TwitterRepository(TwitterManager.getTwitter()),
                            listId
                    ))
                    .get(UserMemberFragmentViewModel::class.java)

    override fun createAdapter(): ProfileItemAdapter<User> =
            RecyclerUserAdapter(context, ArrayList())

    override fun convertDataToViewItem(dataItem: User): User = dataItem
}
