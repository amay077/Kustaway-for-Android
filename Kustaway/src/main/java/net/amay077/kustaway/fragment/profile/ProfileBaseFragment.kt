package net.amay077.kustaway.fragment.profile

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.amay077.kustaway.adapter.DividerItemDecoration
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.adapter.RecyclerUserAdapter
import net.amay077.kustaway.databinding.ListGuruguruBinding
import twitter4j.User
import java.util.*

/**
 * プロフィール画面の「フォロー一覧」「フォロワー一覧」「リストユーザー一覧」「お気に入り一覧」のベース Fragment
 */
abstract class ProfileBaseFragment<T> : Fragment() {
    protected var mUserId: Long = 0
    protected var mCursor: Long = -1
    protected var mAutoLoader = false

    protected lateinit var binding: ListGuruguruBinding
    protected  lateinit var mAdapter: ProfileItemAdapter<T>

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bin = inflater?.let { inf -> ListGuruguruBinding.inflate(inf, container, false) }
        if (bin == null) {
            return null
        }
        binding = bin

        val user = arguments.getSerializable("user") as User

        mUserId = user.id

        // RecyclerView の設定
        binding.listView.visibility = View.GONE // ListView は消しておく TODO 完全に RecyclerView 化できたら .xml からも消す
        binding.recyclerView.visibility = View.GONE
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context)) // 罫線付ける

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(binding.recyclerView)

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = createAdapter()
        binding.recyclerView.adapter = mAdapter

        executeTask(mUserId)

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

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

        return binding.root
    }

    abstract fun createAdapter() : ProfileItemAdapter<T>

    abstract fun executeTask(userId : Long)

    private fun additionalReading() {
        if (!mAutoLoader) {
            return
        }
        binding.guruguru.visibility = View.VISIBLE
        mAutoLoader = false
        executeTask(mUserId)
    }
}

