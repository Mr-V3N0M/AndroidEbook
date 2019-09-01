package com.example.shwenyarmya.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.squareup.picasso.Picasso;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class EditProfile extends AppCompatActivity {

    private Method method;
    public Toolbar toolbar;
    private EditText editText_name, editText_email, editText_password,
            editText_confirm_password, editText_phoneNo;
    private CircleImageView circleImageView;
    private String profileId;
    private ProgressBar progressBar;
    private int REQUEST_GALLERY_PICKER = 100;
    private ArrayList<Image> galleryImages;
    private String image_profile;
    private boolean is_profile;
    private InputMethodManager imm;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;
    private LinearLayout linearLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        method = new Method(EditProfile.this);
        method.forceRTLIfSupported(getWindow());

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Intent intent = getIntent();
        String set_name = intent.getStringExtra("name");
        String set_email = intent.getStringExtra("email");
        String set_phone = intent.getStringExtra("phone");
        profileId = intent.getStringExtra("profileId");
        String user_image = intent.getStringExtra("user_image");

        toolbar = findViewById(R.id.toolbar_edit_profile);
        toolbar.setTitle(getResources().getString(R.string.edit_profile));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressBar = findViewById(R.id.progressbar_edit_profile);
        circleImageView = findViewById(R.id.imageView_user_editPro);
        editText_name = findViewById(R.id.editText_name_edit_profile);
        editText_email = findViewById(R.id.editText_email_edit_profile);
        editText_password = findViewById(R.id.editText_password_edit_profile);
        editText_confirm_password = findViewById(R.id.editText_confirm_password_edit_profile);
        editText_phoneNo = findViewById(R.id.editText_phone_edit_profile);

        linearLayout = findViewById(R.id.linearLayout_edit_profile);

        assert user_image != null;
        if (!user_image.equals("")) {
            Picasso.get().load(user_image).placeholder(R.drawable.profile).into(circleImageView);
        }

        if (Method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
        } else {
            method.showNonPersonalizedAds(linearLayout);
        }

        progressBar.setVisibility(View.GONE);

        editText_name.setText(set_name);
        editText_email.setText(set_email);
        editText_phoneNo.setText(set_phone);

        if (!user_image.equals("")) {
            Picasso.get().load(user_image).placeholder(R.drawable.profile).into(circleImageView);
        }

        image_profile = user_image;

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Method.allowPermissionExternalStorage) {
                    chooseGalleryImage();
                } else {
                    alertBox(getResources().getString(R.string.cannot_use_upload_image));
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY_PICKER) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                is_profile = true;
                galleryImages = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
                image_profile = galleryImages.get(0).getPath();
                Uri uri = Uri.fromFile(new File(galleryImages.get(0).getPath()));
                Picasso.get().load(uri).into(circleImageView);
            }
        }
    }

    private void chooseGalleryImage() {
        try {
            ImagePicker.with(this)
                    .setFolderMode(true)
                    .setFolderTitle("Album")
                    .setImageTitle(getResources().getString(R.string.app_name))
                    .setStatusBarColor("#5387ED")
                    .setToolbarColor("#5387ED")
                    .setProgressBarColor("#5387ED")
                    .setMultipleMode(true)
                    .setMaxSize(1)
                    .setShowCamera(false)
                    .start();
        } catch (Exception e) {
            Log.e("error", e.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile_menu, menu);
        return true;
    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_save:

                String name = editText_name.getText().toString();
                String email = editText_email.getText().toString();
                String password = editText_password.getText().toString();
                String confirm_password = editText_confirm_password.getText().toString();
                String phoneNo = editText_phoneNo.getText().toString();

                editText_name.clearFocus();
                editText_email.clearFocus();
                editText_password.clearFocus();
                editText_confirm_password.clearFocus();
                editText_phoneNo.clearFocus();
                imm.hideSoftInputFromWindow(editText_name.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editText_email.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editText_password.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editText_confirm_password.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editText_phoneNo.getWindowToken(), 0);

                editText_name.setError(null);
                editText_email.setError(null);
                editText_password.setError(null);
                editText_confirm_password.setError(null);
                editText_phoneNo.setError(null);

                if (name.equals("") || name.isEmpty()) {
                    editText_name.requestFocus();
                    editText_name.setError(getResources().getString(R.string.please_enter_name));
                } else if (!isValidMail(email) || email.isEmpty()) {
                    editText_email.requestFocus();
                    editText_email.setError(getResources().getString(R.string.please_enter_email));
                } else if (phoneNo.equals("") || phoneNo.isEmpty()) {
                    editText_phoneNo.requestFocus();
                    editText_phoneNo.setError(getResources().getString(R.string.please_enter_phone));
                } else if (!password.equals(confirm_password)) {
                    method.alertBox(getString(R.string.password_not_match));
                } else {
                    if (method.isNetworkAvailable()) {
                        profileUpdate(profileId, name, email, password, phoneNo, image_profile);
                    } else {
                        method.alertBox(getResources().getString(R.string.internet_connection));
                    }
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

    public void profileUpdate(String id, String sendName, String sendEmail, String sendPassword, String sendPhone, String profile_image) {

        progressBar.setVisibility(View.VISIBLE);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(EditProfile.this));
        jsObj.addProperty("method_name", "user_profile_update");
        jsObj.addProperty("user_id", id);
        jsObj.addProperty("name", sendName);
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("password", sendPassword);
        jsObj.addProperty("phone", sendPhone);
        try {
            if (is_profile) {
                params.put("user_profile", new File(profile_image));
            } else {
                params.put("user_profile", profile_image);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
                        String msg = object.getString("msg");
                        String success = object.getString("success");

                        if (success.equals("1")) {
                            Toast.makeText(EditProfile.this, msg, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EditProfile.this, Profile.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        } else {
                            Toast.makeText(EditProfile.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    progressBar.setVisibility(View.GONE);

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

    //alert message box
    public void alertBox(String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditProfile.this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        checkPer();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void checkPer() {
        if ((ContextCompat.checkSelfPermission(EditProfile.this, "android.permission.WRITE_EXTERNAL_STORAGE"
                + "android.permission.WRITE_INTERNAL_STORAGE" + "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_INTERNAL_STORAGE",
                                "android.permission.READ_EXTERNAL_STORAGE"},
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canUseExternalStorage = true;
                    Method.allowPermissionExternalStorage = true;
                }
                if (!canUseExternalStorage) {
                    Toast.makeText(EditProfile.this, getResources().getString(R.string.cannot_use_save_permission), Toast.LENGTH_SHORT).show();
                    Method.allowPermissionExternalStorage = false;
                }
            }
        }
    }

}
