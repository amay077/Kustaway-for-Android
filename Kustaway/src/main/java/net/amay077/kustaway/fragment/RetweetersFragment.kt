package net.amay077.kustaway.fragment

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import net.amay077.kustaway.R
import net.amay077.kustaway.adapter.RecyclerUserAdapter
import net.amay077.kustaway.databinding.FragmentRecyclerRetweetersBinding
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.viewmodel.RetweetersFragmentViewModel


/**
 * リツイートを表示
 *
 * @author aska, amay077
 */
class RetweetersFragment : DialogFragment() {

    private lateinit var adapter: RecyclerUserAdapter

    private lateinit var binding: FragmentRecyclerRetweetersBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        binding = DataBindingUtil.inflate<FragmentRecyclerRetweetersBinding>(LayoutInflater.from(context),
                R.layout.fragment_recycler_retweeters, null, false)
        dialog.setContentView(binding.getRoot())

        adapter = RecyclerUserAdapter(activity, ArrayList())
        binding.recyclerView.adapter = adapter

        val statusId = arguments.getLong("statusId")

        val viewModel = ViewModelProviders
                .of(this, RetweetersFragmentViewModel.Factory(
                        TwitterRepository(TwitterManager.getTwitter()),
                        statusId
                ))
                .get(RetweetersFragmentViewModel::class.java)

        // ViewModel の監視

        // 追加読み込みの Progress
        viewModel.isVisibleBottomProgress.observe(this, Observer { isVisible ->
            binding.guruguru.visibility = if (isVisible ?: false) View.VISIBLE else View.GONE
        })

        // リストビューの Visible
        viewModel.isVisibleListView.observe(this, Observer { isVisible ->
            binding.recyclerView.visibility = if (isVisible ?: false) View.VISIBLE else View.GONE
        })

        // 読み込んだデータを RecyclerView のアダプタに適用
        viewModel.listItems.observe(this, Observer { data ->
            if (data == null) {
                return@Observer
            }

            // 追加でなかったら全消し
            if (!data.isAdditional) {
                adapter.clear()
            }

            for (dataItem in data.items) {
                adapter.add(dataItem)
            }
            adapter.notifyDataSetChanged()
        })

        viewModel.loadListItems(false)

        return dialog
    }
}
