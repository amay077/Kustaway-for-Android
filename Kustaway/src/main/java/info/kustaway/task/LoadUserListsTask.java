package info.kustaway.task;

import android.os.AsyncTask;

import info.kustaway.model.AccessTokenManager;
import info.kustaway.model.TwitterManager;
import info.kustaway.model.UserListCache;
import twitter4j.ResponseList;
import twitter4j.UserList;

/**
 * アプリケーションのメンバ変数にユーザーリストを読み込む
 */
public class LoadUserListsTask extends AsyncTask<Void, Void, ResponseList<UserList>> {
    @Override
    protected ResponseList<UserList> doInBackground(Void... params) {
        try {
            return TwitterManager.getTwitter().getUserLists(AccessTokenManager.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ResponseList<UserList> userLists) {
        if (userLists != null) {
            UserListCache.setUserLists(userLists);
        }
    }
}
