package com.example.shwenyarmya.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class Profile extends AppCompatActivity {

    public Toolbar toolbar;
    private TextView textViewName, textViewEmail, textViewPhone;
    private String name, email, phone, user_image;
    private Method method;
    private ProgressBar progressBar;
    private CircleImageView circleImageView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        method = new Method(Profile.this);
        method.forceRTLIfSupported(getWindow());

        toolbar = findViewById(R.id.toolbar_profile);
        toolbar.setTitle(getResources().getString(R.string.profile));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout_profile);
        progressBar = findViewById(R.id.progressbar_profile);
        TextView textView = findViewById(R.id.textView_profile);
        circleImageView = findViewById(R.id.imageView_profile);
        textViewName = findViewById(R.id.textView_name_profile);
        textViewEmail = findViewById(R.id.textView_email_profile);
        textViewPhone = findViewById(R.id.textView_phone_profile);

        LinearLayout linearLayout = findViewById(R.id.linearLayout_profile);

        relativeLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);

        if (method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
        } else {
            method.showNonPersonalizedAds(linearLayout);
        }

        if (method.pref.getBoolean(method.pref_login, false)) {
            relativeLayout.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            if (method.isNetworkAvailable()) {
                profile(method.pref.getString(method.profileId, null));
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        } else {
            relativeLayout.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);

        MenuItem favItem = menu.findItem(R.id.action_edit);
        if (method.pref.getBoolean(method.pref_login, false)) {
            favItem.setVisible(true);
        } else {
            favItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_edit:
                if (method.isNetworkAvailable()) {
                    startActivity(new Intent(Profile.this, EditProfile.class)
                            .putExtra("name", name)
                            .putExtra("email", email)
                            .putExtra("phone", phone)
                            .putExtra("user_image", user_image)
                            .putExtra("profileId", method.pref.getString(method.profileId, null)));
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
                break;

            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }

        return true;
    }

    public void profile(String id) {

        progressBar.setVisibility(View.VISIBLE);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Profile.this));
        jsObj.addProperty("method_name", "user_profile");
        jsObj.addProperty("user_id", id);
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
                        String user_id = object.getString("user_id");
                        name = object.getString("name");
                        email = object.getString("email");
                        phone = object.getString("phone");
                        user_image = object.getString("user_image");
                        String success = object.getString("success");

                    }
                    progressBar.setVisibility(View.GONE);

                    method.editor.putString(method.userImage, user_image);
                    method.editor.commit();

                    if (!user_image.equals("")) {
                        try {
                            Picasso.get().load(user_image)
                                    .placeholder(R.drawable.profile).into(circleImageView);
                        } catch (Exception e) {
                            Log.d("error", e.toString());
                        }
                    }
                    textViewName.setText(name);
                    textViewEmail.setText(email);
                    textViewPhone.setText(phone);

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.contact_msg));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}
