package net.amay077.kustaway.fragment.list


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
import net.amay077.kustaway.adapter.UserAdapter
import net.amay077.kustaway.model.TwitterManager
import twitter4j.PagableResponseList
import twitter4j.User

class UserMemberFragment : Fragment() {
    private var mAdapter: UserAdapter? = null
    private var mListId: Long = 0
    private var mCursor: Long = -1
    private var mListView: ListView? = null
    private var mFooter: ProgressBar? = null
    private var mAutoLoader = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.list_guruguru, container, false) ?: return null

        mListId = arguments.getLong("listId")

        // リストビューの設定
        mListView = v.findViewById<View>(R.id.list_view) as ListView
        mListView!!.visibility = View.GONE

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(mListView!!)

        mFooter = v.findViewById<View>(R.id.guruguru) as ProgressBar

        mAdapter = UserAdapter(activity, R.layout.row_user)
        mListView!!.adapter = mAdapter

        UserListMembersTask().execute(mListId)

        mListView!!.setOnScrollListener(object : AbsListView.OnScrollListener {

            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                // 最後までスクロールされたかどうかの判定
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    additionalReading()
                }
            }
        })
        return v
    }

    private fun additionalReading() {
        if (!mAutoLoader) {
            return
        }
        mFooter!!.visibility = View.VISIBLE
        mAutoLoader = false
        UserListMembersTask().execute(mListId)
    }


    private inner class UserListMembersTask : AsyncTask<Long, Void, PagableResponseList<User>>() {
        protected override fun doInBackground(vararg params: Long?): PagableResponseList<User>? {
            try {
                val userListsMembers = TwitterManager.getTwitter().getUserListMembers(params[0] ?: -1, mCursor)
                mCursor = userListsMembers.nextCursor
                return userListsMembers
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }

        override fun onPostExecute(userListsMembers: PagableResponseList<User>?) {
            mFooter!!.visibility = View.GONE
            if (userListsMembers == null) {
                return
            }
            for (user in userListsMembers) {
                mAdapter!!.add(user)
            }
            if (userListsMembers.hasNext()) {
                mAutoLoader = true
            }
            mListView!!.visibility = View.VISIBLE
        }
    }
}
