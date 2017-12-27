package net.amay077.kustaway.fragment.profile

import android.os.AsyncTask
import android.view.View
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserAdapter
import net.amay077.kustaway.model.TwitterManager
import twitter4j.PagableResponseList
import twitter4j.User

/**
 * フォロー一覧
 */
class FollowingListFragment : ProfileBaseFragment<User>() {

    override fun createAdapter(): ProfileItemAdapter<User> =
        RecyclerUserAdapter(activity, ArrayList())

    override fun executeTask(isAdditional: Boolean) {
        FriendsListTask(isAdditional).execute(user.id)
    }

    private inner class FriendsListTask(private val isAdditional: Boolean) : AsyncTask<Long, Void, PagableResponseList<User>>() {
        override fun doInBackground(vararg params: Long?): PagableResponseList<User>? {
            try {
                val users = TwitterManager.getTwitter().getFriendsList(params[0] ?: -1, cursor) // TODO 雑すぎ
                cursor = users.nextCursor
                return users
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }

        override fun onPostExecute(users: PagableResponseList<User>?) {
            binding.guruguru.visibility = View.GONE
            if (users == null) {
                return
            }

            if (!isAdditional) {
                adapter.clear()
            }

            for (user in users) {
                adapter.add(user)
            }
            if (users.hasNext()) {
                autoLoader = true
            }
            adapter.notifyDataSetChanged()
            binding.recyclerView.visibility = View.VISIBLE
            binding.ptrLayout.setRefreshing(false)
        }
    }
}
