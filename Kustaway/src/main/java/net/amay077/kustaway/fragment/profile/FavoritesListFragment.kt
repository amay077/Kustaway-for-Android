package net.amay077.kustaway.fragment.profile

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.greenrobot.event.EventBus
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerTweetAdapter
import net.amay077.kustaway.event.action.StatusActionEvent
import net.amay077.kustaway.event.model.StreamingDestroyStatusEvent
import net.amay077.kustaway.fragment.dialog.StatusMenuFragment
import net.amay077.kustaway.listener.StatusLongClickListener
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.viewmodel.FavoritesListFragmentViewModel
import twitter4j.Status
import twitter4j.User

class FavoritesListFragment : ProfileBaseFragment<Row, Status, FavoritesListFragmentViewModel>() {
    override fun createViewModel(user: User): FavoritesListFragmentViewModel =
            ViewModelProviders
                    .of(this, FavoritesListFragmentViewModel.Factory(
                            TwitterRepository(TwitterManager.getTwitter()),
                            user
                    ))
                    .get(FavoritesListFragmentViewModel::class.java)

    override fun createAdapter(): ProfileItemAdapter<Row> =
            RecyclerTweetAdapter(context, ArrayList())

    override fun convertDataToViewItem(dataItem: Status): Row = Row.newStatus(dataItem)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)

        adapter.onItemClickListener = { row ->
            StatusMenuFragment.newInstance(row)
                    .show(activity.getSupportFragmentManager(), "dialog")
        }

        adapter.onItemLongClickListener = { row ->
            StatusLongClickListener.handleRow(activity, row)
        }

        return v
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    fun onEventMainThread(event: StatusActionEvent) {
        adapter!!.notifyDataSetChanged()
    }

    fun onEventMainThread(event: StreamingDestroyStatusEvent) {
        adapter.remove(event.statusId!!)
    }
}
