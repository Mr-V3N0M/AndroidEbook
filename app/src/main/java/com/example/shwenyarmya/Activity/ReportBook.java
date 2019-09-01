package com.example.shwenyarmya.Activity;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ReportBook extends AppCompatActivity {

    public Toolbar toolbar;
    private String bookId;
    private TextView textViewBook, textViewAuthor, text_title;
    private RoundedImageView imageViewBook;
    private EditText editTextIssue;
    private Button buttonSubmit;
    private ProgressDialog progressDialog;
    private Method method;
    private InputMethodManager imm;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_book);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        toolbar = findViewById(R.id.toolbar_report_book);
        toolbar.setTitle(getResources().getString(R.string.report_book_toolbar_title));

        progressDialog = new ProgressDialog(ReportBook.this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        method = new Method(ReportBook.this);
        method.forceRTLIfSupported(getWindow());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        String bookName = intent.getStringExtra("BookName");
        String bookAuthor = intent.getStringExtra("BookAuthor");
        String bookImage = intent.getStringExtra("BookImage");

        textViewBook = findViewById(R.id.textView_title_report);
        textViewAuthor = findViewById(R.id.textView_author_report);
        imageViewBook = findViewById(R.id.imageView_report);
        editTextIssue = findViewById(R.id.editIssue);
        buttonSubmit = findViewById(R.id.btn_submit);
        text_title = findViewById(R.id.text_title);


        LinearLayout linearLayout = findViewById(R.id.linearLayout_report_book);

        if (Method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
        } else {
            method.showNonPersonalizedAds(linearLayout);
        }

        textViewBook.setTypeface(Method.scriptable);
        text_title.setTypeface(Method.scriptable);
        buttonSubmit.setTypeface(Method.scriptable);

        Picasso.get().load(bookImage)
                .placeholder(R.drawable.placeholder_portable)
                .into(imageViewBook);
        textViewAuthor.setText(getString(R.string.author_by) + "\u0020" + bookAuthor);
        textViewBook.setText(bookName);

        if (method.isNetworkAvailable()) {
            if (method.pref.getBoolean(method.pref_login, false)) {
                String userId = method.pref.getString(method.profileId, null);
                getReport(userId, bookId);
            } else {
                method.alertBox(getResources().getString(R.string.you_have_not_login));
            }
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editTextIssue.clearFocus();
                imm.hideSoftInputFromWindow(editTextIssue.getWindowToken(), 0);

                editTextIssue.setError(null);
                String report = editTextIssue.getText().toString();

                if (report.equals("") || report.isEmpty()) {
                    editTextIssue.requestFocus();
                    editTextIssue.setError(getResources().getString(R.string.please_enter_comment));
                } else {
                    if (method.isNetworkAvailable()) {

                        editTextIssue.clearFocus();
                        editTextIssue.getText().clear();

                        if (method.pref.getBoolean(method.pref_login, false)) {
                            String id = method.pref.getString(method.profileId, null);
                            String email = method.pref.getString(method.userEmail, null);
                            reportBook(id, email, bookId, report);
                        } else {
                            method.alertBox(getResources().getString(R.string.you_have_not_login));
                        }


                    } else {
                        method.alertBox(getResources().getString(R.string.internet_connection));
                    }
                }
            }
        });
    }

    //get book report
    public void getReport(String userId, String bookId) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(ReportBook.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("post_id", bookId);
        jsObj.addProperty("method_name", "get_report");
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
                        String id = object.getString("id");
                        String email = object.getString("email");
                        String report = object.getString("report");
                        String report_on = object.getString("report_on");

                        editTextIssue.setText(report);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressDialog.dismiss();
            }
        });
    }

    //send book report
    public void reportBook(String userId, String emailId, String sendBookId, String report) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(ReportBook.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("email", emailId);
        jsObj.addProperty("book_id", sendBookId);
        jsObj.addProperty("report", report);
        jsObj.addProperty("method_name", "book_report");
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
                            Toast.makeText(ReportBook.this, msg, Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }

                    }

                    progressDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.contact_msg));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressDialog.dismiss();
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
