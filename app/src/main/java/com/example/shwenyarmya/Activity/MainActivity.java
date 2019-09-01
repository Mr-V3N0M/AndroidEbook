package com.example.shwenyarmya.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.shwenyarmya.Fragment.AuthorFragment;
import com.example.shwenyarmya.Fragment.CategoryFragment;
import com.example.shwenyarmya.Fragment.DownloadFragment;
import com.example.shwenyarmya.Fragment.FavouriteFragment;
import com.example.shwenyarmya.Fragment.HomeFragment;
import com.example.shwenyarmya.Fragment.LatestFragment;
import com.example.shwenyarmya.Fragment.SettingFragment;
import com.example.shwenyarmya.Item.AboutUsList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Events;
import com.example.shwenyarmya.Util.GlobalBus;
import com.example.shwenyarmya.Util.Method;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.htetznaing.app_updater.AppUpdater;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static Toolbar toolbar;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;
    boolean doubleBackToExitPressedOnce = false;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private LinearLayout linearLayout;
    private Method method;
    private ConsentForm form;
    public boolean download_menu = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String check_update_json = "http://shwenyarmya.xyz/check_update_json";
        AppUpdater appUpdater = new AppUpdater(this, check_update_json);
        appUpdater.check();

        GlobalBus.getBus().register(this);

        method = new Method(MainActivity.this);
        method.forceRTLIfSupported(getWindow());

        toolbar = findViewById(R.id.toolbar_main);
        toolbar.setTitle(getResources().getString(R.string.app_name));

        linearLayout = findViewById(R.id.linearLayout_main);

        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_side_nav);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (method.pref.getBoolean(method.pref_login, false)) {
            navigationView.getMenu().getItem(8).setIcon(R.drawable.ic_logout);
            navigationView.getMenu().getItem(8).setTitle(getResources().getString(R.string.logout));
        }

        checkPer();

        if (method.isNetworkAvailable()) {
            aboutUs();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
            } else {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, getResources().getString(R.string.Please_click_BACK_again_to_exit), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (item.isChecked())
            item.setChecked(false);
        else
            item.setChecked(true);

        //Closing drawer on item click
        drawer.closeDrawers();

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {

            case R.id.home:
                getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main, new HomeFragment(), "home").commit();
                return true;

            case R.id.latest:
                LatestFragment latestFragment = new LatestFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "latest");
                latestFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main,
                        latestFragment, "latest").commit();
                return true;

            case R.id.category:
                getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main, new CategoryFragment(), "category").commit();
                return true;

            case R.id.author:
                getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main, new AuthorFragment(), "author").commit();
                return true;

            case R.id.download:
                if (Method.allowPermissionExternalStorage) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main, new DownloadFragment(), "download").commit();
                } else {
                    download_menu = true;
                    checkPer();
                }
                return true;

            case R.id.favorite:
                getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main, new FavouriteFragment(), "favourite").commit();
                return true;

            case R.id.setting:
                getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main, new SettingFragment(), "settings").commit();
                return true;

            case R.id.profile:
                startActivity(new Intent(MainActivity.this, Profile.class));
                return true;

            case R.id.login:
                if (method.pref.getBoolean(method.pref_login, false)) {
                    method.editor.putBoolean(method.pref_login, false);
                    method.editor.commit();
                }
                startActivity(new Intent(MainActivity.this, Login.class));
                finishAffinity();
                return true;

            default:
                return true;
        }
    }

    public void checkPer() {
        if ((ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE" + "android.permission.WRITE_INTERNAL_STORAGE" + "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_INTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                Method.allowPermissionExternalStorage = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        boolean canUseExternalStorage = false;

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canUseExternalStorage = true;
                    Method.allowPermissionExternalStorage = true;
                    if (download_menu) {
                        download_menu = false;
                        getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main, new DownloadFragment(), "download").commit();
                    }
                }
                if (!canUseExternalStorage) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.cannot_use_save_permission), Toast.LENGTH_SHORT).show();
                    Method.allowPermissionExternalStorage = false;
                }
            }
        }
    }

    public void aboutUs() {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(MainActivity.this));
        jsObj.addProperty("method_name", "get_app_details");
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
                        String app_name = object.getString("app_name");
                        String app_logo = object.getString("app_logo");
                        String app_version = object.getString("app_version");
                        String app_author = object.getString("app_author");
                        String app_contact = object.getString("app_contact");
                        String app_email = object.getString("app_email");
                        String app_website = object.getString("app_website");
                        String app_description = object.getString("app_description");
                        String app_privacy_policy = object.getString("app_privacy_policy");
                        String publisher_id = object.getString("publisher_id");
                        boolean interstital_ad = Boolean.parseBoolean(object.getString("interstital_ad"));
                        String interstital_ad_id = object.getString("interstital_ad_id");
                        String interstital_ad_click = object.getString("interstital_ad_click");
                        boolean banner_ad = Boolean.parseBoolean(object.getString("banner_ad"));
                        String banner_ad_id = object.getString("banner_ad_id");

                        Constant_Api.aboutUsList = new AboutUsList(app_name, app_logo, app_version, app_author, app_contact, app_email, app_website, app_description, app_privacy_policy, publisher_id, interstital_ad_id, interstital_ad_click, banner_ad_id, interstital_ad, banner_ad);

                    }

                    try {
                        Constant_Api.AD_COUNT_SHOW = Integer.parseInt(Constant_Api.aboutUsList.getInterstital_ad_click());
                    } catch (Exception e) {
                        Log.d("error", e.toString());
                    }

                    try {
                        getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main, new HomeFragment(), "home").commit();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.wrong), Toast.LENGTH_SHORT).show();
                    }

                    checkForConsent();

                } catch (JSONException e) {
                    e.printStackTrace();
                    method.alertBox(getResources().getString(R.string.contact_msg));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void checkForConsent() {

        ConsentInformation consentInformation = ConsentInformation.getInstance(MainActivity.this);
        String[] publisherIds = {Constant_Api.aboutUsList.getPublisher_id()};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                Log.d("consentStatus", consentStatus.toString());
                // User's consent status successfully updated.
                switch (consentStatus) {
                    case PERSONALIZED:
                        Method.personalization_ad = true;
                        method.showPersonalizedAds(linearLayout);
                        break;
                    case NON_PERSONALIZED:
                        Method.personalization_ad = false;
                        method.showNonPersonalizedAds(linearLayout);
                        break;
                    case UNKNOWN:
                        if (ConsentInformation.getInstance(getBaseContext())
                                .isRequestLocationInEeaOrUnknown()) {
                            requestConsent();
                        } else {
                            Method.personalization_ad = true;
                            method.showPersonalizedAds(linearLayout);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
            }
        });

    }

    public void requestConsent() {
        URL privacyUrl = null;
        try {
            // TODO: Replace with your app's privacy policy URL.
            privacyUrl = new URL(getResources().getString(R.string.admob_privacy_link));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }
        form = new ConsentForm.Builder(MainActivity.this, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        showForm();
                        // Consent form loaded successfully.
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                    }

                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        Log.d("consentStatus_form", consentStatus.toString());
                        switch (consentStatus) {
                            case PERSONALIZED:
                                Method.personalization_ad = true;
                                method.showPersonalizedAds(linearLayout);
                                break;
                            case NON_PERSONALIZED:
                                Method.personalization_ad = false;
                                method.showNonPersonalizedAds(linearLayout);
                                break;
                            case UNKNOWN:
                                Method.personalization_ad = false;
                                method.showNonPersonalizedAds(linearLayout);
                        }
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.d("errorDescription", errorDescription);
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();
        form.load();
    }

    private void showForm() {
        if (form != null) {
            form.show();
        }
    }

    @Subscribe
    public void getLogin(Events.Login login) {
        if (method != null) {
            if (method.pref.getBoolean(method.pref_login, false)) {
                if (navigationView != null) {
                    navigationView.getMenu().getItem(8).setIcon(R.drawable.ic_logout);
                    navigationView.getMenu().getItem(8).setTitle(getResources().getString(R.string.logout));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }

}

