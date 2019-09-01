package com.example.shwenyarmya.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Events;
import com.example.shwenyarmya.Util.GlobalBus;
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
import cn.refactor.library.SmoothCheckBox;
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Login extends AppCompatActivity {

    private EditText editText_email, editText_password;
    private String email, password;

    private Method method;

    public static final String mypreference = "mypref";
    public static final String pref_email = "pref_email";
    public static final String pref_password = "pref_password";
    public static final String pref_check = "pref_check";
    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;
    TextView text_reme_title, title_acc, text_up_title;
    private InputMethodManager imm;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        method = new Method(Login.this);
        method.forceRTLIfSupported(getWindow());

        pref = getSharedPreferences(mypreference, 0); // 0 - for private mode
        editor = pref.edit();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        progressDialog = new ProgressDialog(Login.this);

        editText_email = findViewById(R.id.editText_email_login_activity);
        editText_password = findViewById(R.id.editText_password_login_activity);
        text_reme_title = findViewById(R.id.text_reme_title);
        title_acc = findViewById(R.id.title_acc);
        text_up_title = findViewById(R.id.text_up_title);
        Button button_login = findViewById(R.id.button_login_activity);
        TextView textView_signup_login = findViewById(R.id.textView_signup_login);
        Button button_skip = findViewById(R.id.button_skip_login_activity);
        TextView textView_forget_password_login = findViewById(R.id.textView_forget_password_login);
        final SmoothCheckBox checkBox = findViewById(R.id.checkbox_login_activity);
        checkBox.setChecked(false);

        text_reme_title.setTypeface(Method.scriptable);
        button_skip.setTypeface(Method.scriptable);
        button_login.setTypeface(Method.scriptable);
        textView_signup_login.setTypeface(Method.scriptable);
        textView_forget_password_login.setTypeface(Method.scriptable);
        title_acc.setTypeface(Method.scriptable);
        text_up_title.setTypeface(Method.scriptable);

        if (pref.getBoolean(pref_check, false)) {
            editText_email.setText(pref.getString(pref_email, null));
            editText_password.setText(pref.getString(pref_password, null));
            checkBox.setChecked(true);
        } else {
            editText_email.setText("");
            editText_password.setText("");
            checkBox.setChecked(false);
        }

        checkBox.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                Log.d("SmoothCheckBox", String.valueOf(isChecked));
                if (isChecked) {
                    editor.putString(pref_email, editText_email.getText().toString());
                    editor.putString(pref_password, editText_password.getText().toString());
                    editor.putBoolean(pref_check, true);
                    editor.commit();
                } else {
                    editor.putBoolean(pref_check, false);
                    editor.commit();
                }
            }
        });

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = editText_email.getText().toString();
                password = editText_password.getText().toString();

                editText_email.clearFocus();
                editText_password.clearFocus();
                imm.hideSoftInputFromWindow(editText_email.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editText_password.getWindowToken(), 0);

                login(checkBox);
            }
        });

        textView_signup_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        button_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Method.loginBack) {
                    Method.loginBack = false;
                    onBackPressed();
                } else {
                    startActivity(new Intent(Login.this, MainActivity.class));
                    finish();
                }

            }
        });

        textView_forget_password_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, ForgetPassword.class));
            }
        });

    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void login(SmoothCheckBox checkBox) {

        editText_email.setError(null);
        editText_password.setError(null);

        if (!isValidMail(email) || email.isEmpty()) {
            editText_email.requestFocus();
            editText_email.setError(getResources().getString(R.string.please_enter_email));
        } else if (password.isEmpty()) {
            editText_password.requestFocus();
            editText_password.setError(getResources().getString(R.string.please_enter_password));
        } else {
            if (method.isNetworkAvailable()) {
                login(email, password, checkBox);
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }

        }
    }

    public void login(final String sendEmail, final String sendPassword, final SmoothCheckBox checkBox) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Login.this));
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
                        String msg = object.getString("msg");

                        if (success.equals("1")) {
                            String user_id = object.getString("user_id");
                            String name = object.getString("name");
                            String user_image = object.getString("user_image");
                            method.editor.putBoolean(method.pref_login, true);
                            method.editor.putString(method.profileId, user_id);
                            method.editor.putString(method.userName, name);
                            method.editor.putString(method.userEmail, sendEmail);
                            method.editor.putString(method.userPassword, sendPassword);
                            method.editor.putString(method.userImage, user_image);
                            method.editor.commit();
                            if (checkBox.isChecked()) {
                                editor.putString(pref_email, editText_email.getText().toString());
                                editor.putString(pref_password, editText_password.getText().toString());
                                editor.putBoolean(pref_check, true);
                                editor.commit();
                            }

                            editText_email.setText("");
                            editText_password.setText("");

                            if (Method.loginBack) {
                                Method.loginBack = false;
                                Events.Login loginNotify = new Events.Login("");
                                GlobalBus.getBus().post(loginNotify);
                                onBackPressed();
                            } else {
                                startActivity(new Intent(Login.this, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finishAffinity();
                            }


                        } else {
                            method.alertBox(msg);
                        }

                    }

                    progressDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.contact_msg));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

}
