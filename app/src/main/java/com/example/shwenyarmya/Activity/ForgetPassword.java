package com.example.shwenyarmya.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
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
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ForgetPassword extends AppCompatActivity {

    private Method method;
    private ProgressDialog progressDialog;
    Toolbar toolbar;
    private InputMethodManager imm;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        method = new Method(ForgetPassword.this);
        method.forceRTLIfSupported(getWindow());

        progressDialog = new ProgressDialog(ForgetPassword.this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        toolbar = findViewById(R.id.toolbar_fp);
        toolbar.setTitle(getResources().getString(R.string.button_text_forgot_password));

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        final EditText editText_forgetPassword = findViewById(R.id.editText_fp);
        Button buttonForgetPassword = findViewById(R.id.button_fp);
        buttonForgetPassword.setTypeface(Method.scriptable);

        buttonForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editText_forgetPassword.clearFocus();
                imm.hideSoftInputFromWindow(editText_forgetPassword.getWindowToken(), 0);

                String stringForgetPassword = editText_forgetPassword.getText().toString();
                editText_forgetPassword.setError(null);
                if (!isValidMail(stringForgetPassword) || stringForgetPassword.isEmpty()) {
                    editText_forgetPassword.requestFocus();
                    editText_forgetPassword.setError(getResources().getString(R.string.please_enter_email));
                } else {
                    if (method.isNetworkAvailable()) {
                        editText_forgetPassword.setText("");
                        forgetPassword(stringForgetPassword);
                    } else {
                        method.alertBox(getResources().getString(R.string.internet_connection));
                    }
                }
            }
        });


    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public void forgetPassword(String sendEmail_forget_password) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(ForgetPassword.this));
        jsObj.addProperty("user_email", sendEmail_forget_password);
        jsObj.addProperty("method_name", "forgot_pass");
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
                            method.alertBox(msg);
                        } else {
                            method.alertBox(msg);
                        }

                    }

                    progressDialog.dismiss();

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        super.onBackPressed();
    }
}
