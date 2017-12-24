package net.amay077.kustaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import net.amay077.kustaway.R;
import net.amay077.kustaway.event.model.StreamingDestroyStatusEvent;
import net.amay077.kustaway.model.TwitterManager;
import net.amay077.kustaway.util.MessageUtil;

public class DestroyStatusTask extends AsyncTask<Long, Void, Boolean> {

    private long mStatusId;

    public DestroyStatusTask(long statusId) {
        this.mStatusId = statusId;
    }

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            TwitterManager.getTwitter().destroyStatus(mStatusId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            MessageUtil.showToast(R.string.toast_destroy_status_success);
            EventBus.getDefault().post(new StreamingDestroyStatusEvent(mStatusId));
        } else {
            MessageUtil.showToast(R.string.toast_destroy_status_failure);
        }
    }
}