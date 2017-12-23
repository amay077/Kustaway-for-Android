package info.kustaway.task;

import android.os.AsyncTask;

import info.kustaway.KustawayApplication;
import info.kustaway.model.AccessTokenManager;
import info.kustaway.model.FavRetweetManager;
import info.kustaway.model.TwitterManager;
import twitter4j.ResponseList;

public class LoadFavoritesTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            ResponseList<twitter4j.Status> favorites = TwitterManager.getTwitter().getFavorites(AccessTokenManager.getUserId());
            for (twitter4j.Status status : favorites) {
                FavRetweetManager.setFav(status.getId());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}