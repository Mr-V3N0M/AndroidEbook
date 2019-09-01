package com.example.shwenyarmya.Util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;

import com.onesignal.OneSignal;
import com.example.shwenyarmya.R;

import androidx.multidex.MultiDex;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public class YouApplication extends Application {

    private static YouApplication mInstance;
    public SharedPreferences preferences;
    public String prefName = "Book";

    public YouApplication() {
        mInstance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .init();

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Poppins-Regular_0.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }

    }

    public static synchronized YouApplication getInstance() {
        return mInstance;
    }

    public void saveIsNotification(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("IsNotification", flag);
        editor.apply();
    }

    public boolean getNotification() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsNotification", true);
    }

}
