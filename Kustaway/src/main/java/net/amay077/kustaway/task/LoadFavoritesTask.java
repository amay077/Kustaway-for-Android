package net.amay077.kustaway.task;

import android.os.AsyncTask;

import net.amay077.kustaway.KustawayApplication;
import net.amay077.kustaway.model.AccessTokenManager;
import net.amay077.kustaway.model.FavRetweetManager;
import net.amay077.kustaway.model.TwitterManager;
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