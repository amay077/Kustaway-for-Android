package net.amay077.kustaway.fragment.profile

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.greenrobot.event.EventBus
import net.amay077.kustaway.adapter.DataItemAdapter
import net.amay077.kustaway.adapter.RecyclerTweetAdapter
import net.amay077.kustaway.event.action.StatusActionEvent
import net.amay077.kustaway.event.model.StreamingDestroyStatusEvent
import net.amay077.kustaway.extensions.getTwitterRepo
import net.amay077.kustaway.fragment.common.ListBasedFragment
import net.amay077.kustaway.fragment.dialog.StatusMenuFragment
import net.amay077.kustaway.listener.StatusLongClickListener
import net.amay077.kustaway.model.Row
import net.amay077.kustaway.viewmodel.FavoritesListFragmentViewModel
import twitter4j.Status
import twitter4j.User

class FavoritesListFragment : ListBasedFragment<Row, Long, Status, Long, FavoritesListFragmentViewModel>() {
    override val id: Long
        get() = (arguments.getSerializable("user") as User).id

    override fun createViewModel(userId: Long): FavoritesListFragmentViewModel =
            ViewModelProviders
                    .of(this, FavoritesListFragmentViewModel.Factory(
                            this.getTwitterRepo(),
                            userId
                    ))
                    .get(FavoritesListFragmentViewModel::class.java)

    override fun createAdapter(): DataItemAdapter<Row> =
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
