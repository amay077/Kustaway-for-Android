package net.amay077.kustaway;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import net.amay077.kustaway.task.FavoriteTask;

public class FavoriteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.getBooleanExtra("notification", false)) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
        }
        long statusId = intent.getLongExtra("statusId", -1L);
        if (statusId > 0) {
            new FavoriteTask(statusId).execute();
        }
        finish();
    }
}
