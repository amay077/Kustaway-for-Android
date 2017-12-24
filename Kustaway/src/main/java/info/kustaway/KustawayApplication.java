package info.kustaway;

import android.app.Application;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.StrictMode;

import info.kustaway.model.Relationship;
import info.kustaway.model.UserIconManager;
import info.kustaway.settings.BasicSettings;
import info.kustaway.settings.MuteSettings;
import info.kustaway.util.ImageUtil;

public class KustawayApplication extends Application {

    private static KustawayApplication sApplication;
    private static Typeface sFontello;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        // Twitter4J の user stream の shutdown() で NetworkOnMainThreadException が発生してしまうことに対する暫定対応
        if (!BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }

        /**
         * 画像のキャッシュや角丸の設定を行う
         */
        ImageUtil.init();

        /**
         * 設定ファイル読み込み
         */
        MuteSettings.init();

        BasicSettings.init();

        UserIconManager.warmUpUserIconMap();

        Relationship.init();

        sFontello = Typeface.createFromAsset(getAssets(), "fontello.ttf");
    }

    /**
     * 終了時
     * 
     * @see android.app.Application#onTerminate()
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * 空きメモリ逼迫時
     * 
     * @see android.app.Application#onLowMemory()
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /**
     * 実行時において変更できるデバイスの設定時 ( 画面のオリエンテーション、キーボードの使用状態、および言語など )
     * https://sites.google.com/a/techdoctranslator.com/jp/android/guide/resources/runtime-changes
     *
     * @see android.app.Application#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static KustawayApplication getApplication() {
        return sApplication;
    }

    public static Typeface getFontello() {
        return sFontello;
    }
}
