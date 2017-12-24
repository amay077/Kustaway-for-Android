package net.amay077.kustaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import net.amay077.kustaway.R;
import net.amay077.kustaway.event.model.DestroyUserListEvent;
import net.amay077.kustaway.model.TwitterManager;
import net.amay077.kustaway.model.UserListCache;
import net.amay077.kustaway.util.MessageUtil;
import twitter4j.UserList;

public  class DestroyUserListTask extends AsyncTask<Void, Void, Boolean> {

    UserList mUserList;

    public DestroyUserListTask(UserList userList) {
        mUserList = userList;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            TwitterManager.getTwitter().destroyUserList(mUserList.getId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            MessageUtil.showToast(R.string.toast_destroy_user_list_success);
            EventBus.getDefault().post(new DestroyUserListEvent(mUserList.getId()));
            UserListCache.getUserLists().remove(mUserList);
        } else {
            MessageUtil.showToast(R.string.toast_destroy_user_list_failure);
        }
    }
}
