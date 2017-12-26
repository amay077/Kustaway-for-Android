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

    override fun executeTask(userId: Long) {
        FriendsListTask().execute(mUserId)
    }

    private inner class FriendsListTask : AsyncTask<Long, Void, PagableResponseList<User>>() {
        protected override fun doInBackground(vararg params: Long?): PagableResponseList<User>? {
            try {
                val users = TwitterManager.getTwitter().getFriendsList(params[0] ?: -1, mCursor) // TODO 雑すぎ
                mCursor = users.nextCursor
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
            for (user in users) {
                mAdapter.add(user)
            }
            if (users.hasNext()) {
                mAutoLoader = true
            }
            mAdapter.notifyDataSetChanged()
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
}
