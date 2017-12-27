package net.amay077.kustaway.fragment.profile

import android.os.AsyncTask
import android.view.View
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserListAdapter
import net.amay077.kustaway.model.TwitterManager
import twitter4j.PagableResponseList
import twitter4j.UserList

/**
 * ユーザーの持つリスト一覧
 */
class UserListMembershipsFragment : ProfileBaseFragment<UserList>() {

    override fun createAdapter(): ProfileItemAdapter<UserList> =
        RecyclerUserListAdapter(context, ArrayList())

    override fun executeTask(isAdditional: Boolean) {
        FriendsListTask(isAdditional).execute(user.id)
    }

    private inner class FriendsListTask(private val isAdditional: Boolean) : AsyncTask<Long, Void, PagableResponseList<UserList>>() {
        override fun doInBackground(vararg params: Long?): PagableResponseList<UserList>? {
            try {
                val userLists = TwitterManager.getTwitter().getUserListMemberships(params[0] ?: -1, cursor)
                cursor = userLists.nextCursor
                return userLists
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }

        override fun onPostExecute(userLists: PagableResponseList<UserList>?) {
            binding.guruguru.visibility = View.GONE
            if (userLists == null) {
                return
            }

            if (!isAdditional) {
                adapter.clear()
            }

            for (userlist in userLists) {
                adapter.add(userlist)
            }

            if (userLists.hasNext()) {
                autoLoader = true
            }

            adapter.notifyDataSetChanged()
            binding.recyclerView.visibility = View.VISIBLE
            binding.ptrLayout.setRefreshing(false)
        }
    }
}
