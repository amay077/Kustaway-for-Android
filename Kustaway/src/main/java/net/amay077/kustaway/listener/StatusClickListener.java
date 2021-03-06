package net.amay077.kustaway.listener;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;

import net.amay077.kustaway.adapter.TwitterAdapter;
import net.amay077.kustaway.fragment.dialog.StatusMenuFragment;

public class StatusClickListener implements AdapterView.OnItemClickListener {

    private FragmentActivity mFragmentActivity;

    public StatusClickListener(FragmentActivity fragmentActivity) {
        mFragmentActivity = fragmentActivity;
    }

    public TwitterAdapter getAdapter(AdapterView<?> adapterView) {
        return (TwitterAdapter) adapterView.getAdapter();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TwitterAdapter twitterAdapter = getAdapter(adapterView);
        StatusMenuFragment.newInstance(twitterAdapter.getItem(i))
                .show(mFragmentActivity.getSupportFragmentManager(), "dialog");
    }
}
