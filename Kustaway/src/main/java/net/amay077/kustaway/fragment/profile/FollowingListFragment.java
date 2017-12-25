package net.amay077.kustaway.fragment.profile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.amay077.kustaway.adapter.DividerItemDecoration;
import net.amay077.kustaway.adapter.RecyclerUserAdapter;
import net.amay077.kustaway.databinding.ListGuruguruBinding;
import net.amay077.kustaway.model.TwitterManager;

import java.util.ArrayList;

import twitter4j.PagableResponseList;
import twitter4j.User;

public class FollowingListFragment extends Fragment {
    private RecyclerUserAdapter mAdapter;
    private long mUserId;
    private long mCursor = -1;
    private boolean mAutoLoader = false;

    private ListGuruguruBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ListGuruguruBinding.inflate(inflater, container, false);
        if (binding == null) {
            return null;
        }

        User user = (User) getArguments().getSerializable("user");
        if (user == null) {
            return null;
        }

        mUserId = user.getId();

        // リストビューの設定
        binding.recyclerView.setVisibility(View.GONE);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(binding.recyclerView);

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = new RecyclerUserAdapter(getActivity(), new ArrayList<>());
        binding.recyclerView.setAdapter(mAdapter);

        new FriendsListTask().execute(mUserId);

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // see - http://recyclerview.hatenablog.com/entry/2016/11/05/182404
                int totalCount = recyclerView.getAdapter().getItemCount(); //合計のアイテム数
                int childCount = recyclerView.getChildCount(); // RecyclerViewに表示されてるアイテム数
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

                if (layoutManager instanceof GridLayoutManager) { // GridLayoutManager
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                    int firstPosition = gridLayoutManager.findFirstVisibleItemPosition(); // RecyclerViewに表示されている一番上のアイテムポジション
                    if (totalCount == childCount + firstPosition) {
                        // ページング処理
                        // GridLayoutManagerを指定している時のページング処理
                    }
                } else if (layoutManager instanceof LinearLayoutManager) { // LinearLayoutManager
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                    int firstPosition = linearLayoutManager.findFirstVisibleItemPosition(); // RecyclerViewの一番上に表示されているアイテムのポジション
                    if (totalCount == childCount + firstPosition) {
                        // ページング処理
                        additionalReading();
                    }
                }
            }
        });

        return binding.getRoot();
    }

    private void additionalReading() {
        if (!mAutoLoader) {
            return;
        }
        binding.guruguru.setVisibility(View.VISIBLE);
        mAutoLoader = false;
        new FriendsListTask().execute(mUserId);
    }

    private class FriendsListTask extends AsyncTask<Long, Void, PagableResponseList<User>> {
        @Override
        protected PagableResponseList<User> doInBackground(Long... params) {
            try {
                PagableResponseList<User> friendsList = TwitterManager.getTwitter().getFriendsList(params[0], mCursor);
                mCursor = friendsList.getNextCursor();
                return friendsList;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(PagableResponseList<User> friendsList) {
            binding.guruguru.setVisibility(View.GONE);
            if (friendsList == null) {
                return;
            }
            for (User friendUser : friendsList) {
                mAdapter.add(friendUser);
            }
            if (friendsList.hasNext()) {
                mAutoLoader = true;
            }
            mAdapter.notifyDataSetChanged();
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
