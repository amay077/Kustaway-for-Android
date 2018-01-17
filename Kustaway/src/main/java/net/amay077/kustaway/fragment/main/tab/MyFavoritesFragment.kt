package net.amay077.kustaway.fragment.main.tab

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.fragment.common.TweetListBasedFragment
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.repository.TwitterRepository
import net.amay077.kustaway.viewmodel.MyFavoritesFragmentViewModel

class MyFavoritesFragment : TweetListBasedFragment<MyFavoritesFragmentViewModel>() {

    override fun createViewModel(dummy: Unit): MyFavoritesFragmentViewModel =
            ViewModelProviders
                    .of(this, MyFavoritesFragmentViewModel.Factory(
                            TwitterRepository(TwitterManager.getTwitter())
                    ))
                    .get(MyFavoritesFragmentViewModel::class.java)
}