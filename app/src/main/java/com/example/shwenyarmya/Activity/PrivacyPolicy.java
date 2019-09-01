package com.example.shwenyarmya.Activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Method;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class PrivacyPolicy extends AppCompatActivity {

    private Method method;
    public Toolbar toolbar;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        method = new Method(PrivacyPolicy.this);
        method.forceRTLIfSupported(getWindow());

        toolbar = findViewById(R.id.toolbar_privacy_policy);
        toolbar.setTitle(getResources().getString(R.string.privacy_policy));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        WebView webView = findViewById(R.id.webView_privacy_policy);

        LinearLayout linearLayout = findViewById(R.id.linearLayout_privacy_policy);

        if (Method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
        } else {
            method.showNonPersonalizedAds(linearLayout);
        }

        if (Constant_Api.aboutUsList != null) {
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setFocusableInTouchMode(false);
            webView.setFocusable(false);
            webView.getSettings().setDefaultTextEncodingName("UTF-8");
            String mimeType = "text/html";
            String encoding = "utf-8";
            String htmlText = Constant_Api.aboutUsList.getApp_privacy_policy();

            String text = "<html><head>"
                    + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/Poppins-Medium_0.ttf\")}body{font-family: MyFont;color: #8b8b8b;line-height:1.6}"
                    + "</style></head>"
                    + "<body>"
                    + htmlText
                    + "</body></html>";

            webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
