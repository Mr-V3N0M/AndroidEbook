package com.example.shwenyarmya.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Register extends AppCompatActivity {

    private Method method;
    private EditText editText_name, editText_email, editText_password, editText_phoneNo;
    private String name, email, password, phoneNo;
    private InputMethodManager imm;
    private ProgressDialog progressDialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        method = new Method(Register.this);
        method.forceRTLIfSupported(getWindow());

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        progressDialog = new ProgressDialog(Register.this);

        editText_name = findViewById(R.id.editText_name_register);
        editText_email = findViewById(R.id.editText_email_register);
        editText_password = findViewById(R.id.editText_password_register);
        editText_phoneNo = findViewById(R.id.editText_phoneNo_register);
        Button button_submit = findViewById(R.id.button_submit);
        TextView textView_login = findViewById(R.id.textView_login_register);
        TextView text_reg_title = findViewById(R.id.text_reg_title);
        TextView text_sign_title = findViewById(R.id.text_sign_title);

        text_reg_title.setTypeface(Method.scriptable);
        textView_login.setTypeface(Method.scriptable);
        button_submit.setTypeface(Method.scriptable);
        text_sign_title.setTypeface(Method.scriptable);

        textView_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
                finishAffinity();
            }
        });


        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = editText_name.getText().toString();
                email = editText_email.getText().toString();
                password = editText_password.getText().toString();
                phoneNo = editText_phoneNo.getText().toString();

                form();

            }
        });

    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void form() {

        editText_name.setError(null);
        editText_email.setError(null);
        editText_password.setError(null);
        editText_phoneNo.setError(null);

        if (name.equals("") || name.isEmpty()) {
            editText_name.requestFocus();
            editText_name.setError(getResources().getString(R.string.please_enter_name));
        } else if (!isValidMail(email) || email.isEmpty()) {
            editText_email.requestFocus();
            editText_email.setError(getResources().getString(R.string.please_enter_email));
        } else if (password.equals("") || password.isEmpty()) {
            editText_password.requestFocus();
            editText_password.setError(getResources().getString(R.string.please_enter_password));
        } else if (phoneNo.equals("") || phoneNo.isEmpty()) {
            editText_phoneNo.requestFocus();
            editText_phoneNo.setError(getResources().getString(R.string.please_enter_phone));
        } else {

            editText_name.clearFocus();
            editText_email.clearFocus();
            editText_password.clearFocus();
            editText_phoneNo.clearFocus();
            imm.hideSoftInputFromWindow(editText_name.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editText_email.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editText_password.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editText_phoneNo.getWindowToken(), 0);

            if (method.isNetworkAvailable()) {
                register(name, email, password, phoneNo);
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }

        }
    }

    public void register(String sendName, String sendEmail, String sendPassword, String sendPhone) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Register.this));
        jsObj.addProperty("name", sendName);
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("password", sendPassword);
        jsObj.addProperty("phone", sendPhone);
        jsObj.addProperty("method_name", "user_register");
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

                            Toast.makeText(Register.this, msg, Toast.LENGTH_SHORT).show();

                            editText_name.setText("");
                            editText_email.setText("");
                            editText_password.setText("");
                            editText_phoneNo.setText("");

                            startActivity(new Intent(Register.this, Login.class));
                            finishAffinity();

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
