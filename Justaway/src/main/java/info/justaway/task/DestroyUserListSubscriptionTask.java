package info.justaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.event.model.DestroyUserListEvent;
import twitter4j.UserList;

public class DestroyUserListSubscriptionTask extends AsyncTask<Void, Void, Boolean> {

    UserList mUserList;

    public DestroyUserListSubscriptionTask(UserList userList) {
        mUserList = userList;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            JustawayApplication.getApplication().getTwitter().destroyUserListSubscription(mUserList.getId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            JustawayApplication.showToast(R.string.toast_destroy_user_list_subscription_success);
            EventBus.getDefault().post(new DestroyUserListEvent(mUserList.getId()));
            JustawayApplication.getApplication().getUserLists().remove(mUserList);
        } else {
            JustawayApplication.showToast(R.string.toast_destroy_user_list_subscription_failure);
        }
    }
}
