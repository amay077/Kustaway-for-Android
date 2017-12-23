package info.kustaway.task;

import android.content.Context;

import info.kustaway.KustawayApplication;
import info.kustaway.model.AccessTokenManager;
import info.kustaway.model.TwitterManager;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;

public class UserListsLoader extends AbstractAsyncTaskLoader<ResponseList<UserList>> {

    public UserListsLoader(Context context) {
        super(context);
    }

    @Override
    public ResponseList<UserList> loadInBackground() {
        try {
            return TwitterManager.getTwitter().getUserLists(AccessTokenManager.getUserId());
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
