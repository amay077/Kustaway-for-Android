package net.amay077.kustaway.fragment.profile

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import net.amay077.kustaway.adapter.DividerItemDecoration
import net.amay077.kustaway.adapter.RecyclerUserAdapter
import net.amay077.kustaway.databinding.ListGuruguruBinding
import net.amay077.kustaway.model.TwitterManager

import java.util.ArrayList

import twitter4j.PagableResponseList
import twitter4j.User

class FollowingListFragment : Fragment() {
    private var mAdapter: RecyclerUserAdapter? = null
    private var mUserId: Long = 0
    private var mCursor: Long = -1
    private var mAutoLoader = false

    private var binding: ListGuruguruBinding? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = inflater?.let { inf -> ListGuruguruBinding.inflate(inf, container, false) }
        if (binding == null) {
            return null
        }

        val user = arguments.getSerializable("user") as User ?: return null

        mUserId = user.id

        // リストビューの設定
        binding!!.recyclerView.visibility = View.GONE
        binding!!.recyclerView.addItemDecoration(DividerItemDecoration(context)) // 罫線付ける

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(binding!!.recyclerView)

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = RecyclerUserAdapter(activity, ArrayList())
        binding!!.recyclerView.adapter = mAdapter

        FriendsListTask().execute(mUserId)

        binding!!.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(view: RecyclerView?, scrollState: Int) {}

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // see - http://recyclerview.hatenablog.com/entry/2016/11/05/182404
                val totalCount = recyclerView!!.adapter.itemCount //合計のアイテム数
                val childCount = recyclerView.childCount // RecyclerViewに表示されてるアイテム数
                val layoutManager = recyclerView.layoutManager

                if (layoutManager is GridLayoutManager) { // GridLayoutManager
                    val firstPosition = layoutManager.findFirstVisibleItemPosition() // RecyclerViewに表示されている一番上のアイテムポジション
                    if (totalCount == childCount + firstPosition) {
                        // ページング処理
                        // GridLayoutManagerを指定している時のページング処理
                    }
                } else if (layoutManager is LinearLayoutManager) { // LinearLayoutManager
                    val firstPosition = layoutManager.findFirstVisibleItemPosition() // RecyclerViewの一番上に表示されているアイテムのポジション
                    if (totalCount == childCount + firstPosition) {
                        // ページング処理
                        additionalReading()
                    }
                }
            }
        })

        return binding!!.root
    }

    private fun additionalReading() {
        if (!mAutoLoader) {
            return
        }
        binding!!.guruguru.visibility = View.VISIBLE
        mAutoLoader = false
        FriendsListTask().execute(mUserId)
    }

    private inner class FriendsListTask : AsyncTask<Long, Void, PagableResponseList<User>>() {
        protected override fun doInBackground(vararg params: Long?): PagableResponseList<User>? {
            try {
                val friendsList = TwitterManager.getTwitter().getFriendsList(params[0] ?: -1, mCursor) // TODO 雑すぎ
                mCursor = friendsList.nextCursor
                return friendsList
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }

        override fun onPostExecute(friendsList: PagableResponseList<User>?) {
            binding!!.guruguru.visibility = View.GONE
            if (friendsList == null) {
                return
            }
            for (friendUser in friendsList) {
                mAdapter!!.add(friendUser)
            }
            if (friendsList.hasNext()) {
                mAutoLoader = true
            }
            mAdapter!!.notifyDataSetChanged()
            binding!!.recyclerView.visibility = View.VISIBLE
        }
    }
}
