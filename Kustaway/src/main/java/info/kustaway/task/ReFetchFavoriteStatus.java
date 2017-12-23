package info.kustaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import info.kustaway.event.model.StreamingCreateFavoriteEvent;
import info.kustaway.model.Row;
import info.kustaway.model.TwitterManager;
import info.kustaway.util.MessageUtil;

public class ReFetchFavoriteStatus extends AsyncTask<Row, Void, twitter4j.Status> {

    private Row mRow;
    // TODO: use http://cdn.api.twitter.com/1/urls/count.json

    public ReFetchFavoriteStatus() {
        super();
    }

    @Override
    protected twitter4j.Status doInBackground(Row... params) {
        mRow = params[0];
        try {
            return TwitterManager.getTwitter().showStatus(mRow.getStatus().getId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(twitter4j.Status status) {
        if (status != null) {
            mRow.setStatus(status);
            EventBus.getDefault().post(new StreamingCreateFavoriteEvent(mRow));
            MessageUtil.showToast(mRow.getSource().getScreenName() + " fav "
                    + mRow.getStatus().getText());
        }
    }
}