package net.amay077.kustaway.fragment.profile

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.amay077.kustaway.adapter.DividerItemDecoration
import net.amay077.kustaway.adapter.ProfileItemAdapter
import net.amay077.kustaway.databinding.PullToRefreshList2Binding
import net.amay077.kustaway.extensions.addOnPagingListener
import net.amay077.kustaway.viewmodel.ProfileBaseFragmentViewModel
import twitter4j.TwitterResponse
import twitter4j.User

/**
 * プロフィール画面の「ユーザータイムライン」「フォロー一覧」「フォロワー一覧」「リストユーザー一覧」「お気に入り一覧」のベースとなる Fragment
 */
abstract class ProfileBaseFragment<TViewItem, TDataItem : TwitterResponse?, TViewModel : ProfileBaseFragmentViewModel<TDataItem>> : Fragment() {
    protected lateinit var binding: PullToRefreshList2Binding
    protected lateinit var adapter: ProfileItemAdapter<TViewItem>

    abstract fun createViewModel(user: User): TViewModel

    abstract fun convertDataToViewItem(dataItem:TDataItem): TViewItem

    private lateinit var viewModel: TViewModel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bin = inflater?.let { inf -> PullToRefreshList2Binding.inflate(inf, container, false) }
        if (bin == null) {
            return null
        }
        binding = bin

        val user = arguments.getSerializable("user") as User
        viewModel = createViewModel(user)

        // RecyclerView の設定
        binding.recyclerView.visibility = View.GONE
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context)) // 罫線付ける

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(binding.recyclerView)

        // Status(ツイート)をViewに描写するアダプター
        adapter = createAdapter()
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnPagingListener {
            // ページング処理(追加読み込み)
            viewModel.readData(true)
        }

        // Pull to Refresh の開始
        binding.ptrLayout.setOnRefreshListener {
            // 洗い替え
            viewModel.readData(false)
        }

        // ViewModel の監視

        // 追加読み込みの Progress
        viewModel.isVisibleBottomProgress.observe(this, Observer { isVisible ->
            binding.guruguru.visibility = if (isVisible ?: false) View.VISIBLE else View.GONE
        })

        // Pull to Refresh の Progress
        viewModel.isVisiblePullProgress.observe(this, Observer { isVisible ->
            binding.ptrLayout.isRefreshing = isVisible ?: false
        })

        // リストビューの Visible
        viewModel.isVisibleListView.observe(this, Observer { isVisible ->
            binding.recyclerView.visibility = if (isVisible ?: false) View.VISIBLE else View.GONE
        })

        // 読み込んだデータ
        viewModel.data.observe(this, Observer { data ->
            if (data == null) {
                return@Observer
            }

            if (!data.isAdditional) {
                adapter.clear()
            }

            for (dataItem in data.data) {
                adapter.add(convertDataToViewItem(dataItem))
            }
            adapter.notifyDataSetChanged()
        })

        viewModel.readData(false)

        return binding.root
    }

    abstract fun createAdapter() : ProfileItemAdapter<TViewItem>
}

