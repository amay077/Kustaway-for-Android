package net.amay077.kustaway.fragment.profile

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.amay077.kustaway.adapter.DividerItemDecoration
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.databinding.PullToRefreshList2Binding
import net.amay077.kustaway.extensions.addOnPagingListener
import twitter4j.User

/**
 * プロフィール画面の「ユーザータイムライン」「フォロー一覧」「フォロワー一覧」「リストユーザー一覧」「お気に入り一覧」のベースとなる Fragment
 */
abstract class ProfileBaseFragment<T> : Fragment() {
    protected lateinit var user: User
    protected var cursor: Long = -1
    protected var autoLoader = false

    protected lateinit var binding: PullToRefreshList2Binding
    protected lateinit var adapter: ProfileItemAdapter<T>

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bin = inflater?.let { inf -> PullToRefreshList2Binding.inflate(inf, container, false) }
        if (bin == null) {
            return null
        }
        binding = bin

        user = arguments.getSerializable("user") as User

        // RecyclerView の設定
        binding.recyclerView.visibility = View.GONE
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context)) // 罫線付ける

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(binding.recyclerView)

        // Status(ツイート)をViewに描写するアダプター
        adapter = createAdapter()
        binding.recyclerView.adapter = adapter

        executeTask(false)

        binding.recyclerView.addOnPagingListener {
            // ページング処理(追加読み込み)
            readData(true)
        }

        // Pull to Refresh の開始
        binding.ptrLayout.setOnRefreshListener {
            // 洗い替え
            readData(false)
        }

        return binding.root
    }

    abstract fun createAdapter() : ProfileItemAdapter<T>

    abstract fun executeTask(isAdditional:Boolean)

    private fun readData(isAdditional:Boolean) {
        if (!autoLoader) {
            return
        }

        if (!isAdditional) {
            cursor = -1
        } else {
            binding.guruguru.visibility = View.VISIBLE
        }

        autoLoader = false
        executeTask(isAdditional)
    }
}

