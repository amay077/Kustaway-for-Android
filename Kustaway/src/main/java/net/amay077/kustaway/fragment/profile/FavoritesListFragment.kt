package net.amay077.kustaway.fragment.profile

import android.os.AsyncTask
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
import net.amay077.kustaway.settings.BasicSettings
import twitter4j.Paging
import twitter4j.ResponseList
import twitter4j.Status

class FavoritesListFragment : ProfileBaseFragment<Row>() {
    override fun createAdapter(): ProfileItemAdapter<Row> =
            RecyclerTweetAdapter(context, ArrayList())

    override fun executeTask(isAdditional: Boolean) {
        FavoritesListTask(isAdditional).execute(user.screenName)
    }

    private var mMaxId = 0L

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

    private inner class FavoritesListTask(private val isAdditional: Boolean) : AsyncTask<String, Void, ResponseList<Status>>() {
        override fun doInBackground(vararg params: String): ResponseList<twitter4j.Status>? {
            try {
                val paging = Paging()
                if (mMaxId > 0) {
                    paging.maxId = mMaxId - 1
                    paging.count = BasicSettings.getPageCount()
                }
                return TwitterManager.getTwitter().getFavorites(params[0], paging)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }

        override fun onPostExecute(statuses: ResponseList<twitter4j.Status>?) {
            binding.guruguru.visibility = View.GONE

            if (statuses == null || statuses.size == 0) {
                return
            }

            if (!isAdditional) {
                adapter.clear()
            }

            for (status in statuses) {
                if (mMaxId == 0L || mMaxId > status.id) {
                    mMaxId = status.id
                }
                adapter.add(Row.newStatus(status))
            }
            autoLoader = true
            adapter.notifyDataSetChanged()
            binding.recyclerView.visibility = View.VISIBLE
            binding.ptrLayout.setRefreshing(false)
        }
    }
}
