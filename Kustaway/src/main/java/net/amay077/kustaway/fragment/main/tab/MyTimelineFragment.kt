package net.amay077.kustaway.fragment.main.tab

import android.arch.lifecycle.ViewModelProviders
import net.amay077.kustaway.extensions.getTwitterRepo
import net.amay077.kustaway.fragment.common.TweetListBasedFragment
import net.amay077.kustaway.viewmodel.MyTimelineFragmentViewModel

class MyTimelineFragment : TweetListBasedFragment<MyTimelineFragmentViewModel>() {

    override fun createViewModel(dummy: Unit): MyTimelineFragmentViewModel =
            ViewModelProviders
                    .of(this, MyTimelineFragmentViewModel.Factory(
                            this.getTwitterRepo()
                    ))
                    .get(MyTimelineFragmentViewModel::class.java)

}