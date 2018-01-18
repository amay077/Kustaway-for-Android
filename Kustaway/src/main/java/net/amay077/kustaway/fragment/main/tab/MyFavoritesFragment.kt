package net.amay077.kustaway.fragment.main.tab

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.extensions.getTwitterRepo
import net.amay077.kustaway.fragment.common.TweetListBasedFragment
import net.amay077.kustaway.viewmodel.MyFavoritesFragmentViewModel

class MyFavoritesFragment : TweetListBasedFragment<MyFavoritesFragmentViewModel>() {

    override fun createViewModel(dummy: Unit): MyFavoritesFragmentViewModel =
            ViewModelProviders
                    .of(this, MyFavoritesFragmentViewModel.Factory(
                            this.getTwitterRepo()
                    ))
                    .get(MyFavoritesFragmentViewModel::class.java)
}