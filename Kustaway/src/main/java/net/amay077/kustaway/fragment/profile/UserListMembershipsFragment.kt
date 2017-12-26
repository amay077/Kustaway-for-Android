package net.amay077.kustaway.fragment.profile

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ListView
import android.widget.ProgressBar

import net.amay077.kustaway.R
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserListAdapter
import net.amay077.kustaway.adapter.UserListAdapter
import net.amay077.kustaway.model.TwitterManager
import twitter4j.PagableResponseList
import twitter4j.User
import twitter4j.UserList

/**
 * ユーザーの持つリスト一覧
 */
class UserListMembershipsFragment : ProfileBaseFragment<UserList>() {

    override fun createAdapter(): ProfileItemAdapter<UserList> =
        RecyclerUserListAdapter(context, ArrayList())

    override fun executeTask(userId: Long) {
        FriendsListTask().execute(mUserId)
    }

    private inner class FriendsListTask : AsyncTask<Long, Void, PagableResponseList<UserList>>() {
        protected override fun doInBackground(vararg params: Long?): PagableResponseList<UserList>? {
            try {
                val userLists = TwitterManager.getTwitter().getUserListMemberships(params[0] ?: -1, mCursor)
                mCursor = userLists.nextCursor
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
            for (userlist in userLists) {
                mAdapter!!.add(userlist)
            }
            if (userLists.hasNext()) {
                mAutoLoader = true
            }
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
}
