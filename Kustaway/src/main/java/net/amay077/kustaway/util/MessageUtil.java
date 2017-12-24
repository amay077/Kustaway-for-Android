package net.amay077.kustaway.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import net.amay077.kustaway.KustawayApplication;

public class MessageUtil {
    private static ProgressDialog sProgressDialog;

    public static void showToast(String text) {
        KustawayApplication application = KustawayApplication.getApplication();
        Toast.makeText(application, text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int id) {
        KustawayApplication application = KustawayApplication.getApplication();
        String text = application.getString(id);
        Toast.makeText(application, text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int id, String description) {
        KustawayApplication application = KustawayApplication.getApplication();
        String text = application.getString(id) + "\n" + description;
        Toast.makeText(application, text, Toast.LENGTH_SHORT).show();
    }

    public static void showProgressDialog(Context context, String message) {
        sProgressDialog = new ProgressDialog(context);
        sProgressDialog.setMessage(message);
        sProgressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (sProgressDialog != null)
            try {
                sProgressDialog.dismiss();
            } finally {
                sProgressDialog = null;
            }
    }
}
