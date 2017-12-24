package net.amay077.kustaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import net.amay077.kustaway.R;
import net.amay077.kustaway.event.model.StreamingDestroyMessageEvent;
import net.amay077.kustaway.model.TwitterManager;
import net.amay077.kustaway.util.MessageUtil;
import twitter4j.DirectMessage;

public class DestroyDirectMessageTask extends AsyncTask<Long, Void, DirectMessage> {

    @Override
    protected DirectMessage doInBackground(Long... params) {
        try {
            return TwitterManager.getTwitter().destroyDirectMessage(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(DirectMessage directMessage) {
        if (directMessage != null) {
            MessageUtil.showToast(R.string.toast_destroy_direct_message_success);
            EventBus.getDefault().post(new StreamingDestroyMessageEvent(directMessage.getId()));
        } else {
            MessageUtil.showToast(R.string.toast_destroy_direct_message_failure);
        }
    }
}
