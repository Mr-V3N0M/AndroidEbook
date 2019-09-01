package com.example.shwenyarmya.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Method;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SplashScreen extends AppCompatActivity {

    private Method method;
    // splash screen timer
    private static int SPLASH_TIME_OUT = 1000;
    private Boolean isCancelled = false;
    String bookid = "0";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splace_screen);

        method = new Method(SplashScreen.this);
        method.login();
        method.forceRTLIfSupported(getWindow());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }


        if (getIntent().hasExtra("bookid")) {
            bookid = getIntent().getStringExtra("bookid");
            Log.d("bookid", bookid);
        }

        if (method.isNetworkAvailable()) {
            new Handler().postDelayed(new Runnable() {

                /*
                 * Showing splash screen with a timer. This will be useful when you
                 * want to show case your app logo / company
                 */

                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    // Start your app main activity
                    if (!isCancelled) {
                        if (bookid.equals("0")) {
                            if (method.pref.getBoolean(method.pref_login, false)) {
                                Log.d("value", String.valueOf(method.pref.getBoolean(method.pref_login, false)));
                                login(method.pref.getString(method.userEmail, null), method.pref.getString(method.userPassword, null));
                            } else {
                                if (method.pref.getBoolean(method.show_login, true)) {
                                    method.editor.putBoolean(method.show_login, false);
                                    method.editor.commit();
                                    Intent i = new Intent(SplashScreen.this, Login.class);
                                    startActivity(i);
                                    finishAffinity();
                                } else {
                                    Intent i = new Intent(SplashScreen.this, MainActivity.class);
                                    startActivity(i);
                                    finishAffinity();
                                }
                            }
                        } else {
                            startActivity(new Intent(SplashScreen.this, BookDetail.class)
                                    .putExtra("bookId", bookid)
                                    .putExtra("position", 0)
                                    .putExtra("type", "notification"));
                            finishAffinity();
                        }
                    }

                }
            }, SPLASH_TIME_OUT);
        } else {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finishAffinity();
        }
    }

    public void login(final String sendEmail, final String sendPassword) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(SplashScreen.this));
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("password", sendPassword);
        jsObj.addProperty("method_name", "user_login");
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant_Api.url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                Log.d("Response", new String(responseBody));
                String res = new String(responseBody);

                try {
                    JSONObject jsonObject = new JSONObject(res);

                    JSONArray jsonArray = jsonObject.getJSONArray(Constant_Api.tag);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject object = jsonArray.getJSONObject(i);
                        String success = object.getString("success");
                        if (success.equals("1")) {
                            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            method.editor.putBoolean(method.pref_login, false);
                            method.editor.commit();
                            startActivity(new Intent(SplashScreen.this, Login.class));
                            finishAffinity();
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    method.alertBox(getResources().getString(R.string.contact_msg));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    @Override
    protected void onDestroy() {
        isCancelled = true;
        super.onDestroy();
    }

}
