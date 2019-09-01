package com.example.shwenyarmya.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;
import com.example.shwenyarmya.Adapter.AllCommentAdapter;
import com.example.shwenyarmya.Item.CommentList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Events;
import com.example.shwenyarmya.Util.GlobalBus;
import com.example.shwenyarmya.Util.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class AllComment extends AppCompatActivity {

    public Toolbar toolbar;
    private String bookId;
    private Method method;
    private AllCommentAdapter allCommentAdapter;
    private TextView textViewNoCommentFound;
    private EditText editTextComment;
    private InputMethodManager inputMethodManager;
    private LinearLayout linearLayout_all_comment;
    private ImageView floatingComment;
    private List<CommentList> commentLists;
    private InputMethodManager imm;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_comment);

        method = new Method(AllComment.this);
        method.forceRTLIfSupported(getWindow());

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        commentLists = (List<CommentList>) intent.getSerializableExtra("array");

        toolbar = findViewById(R.id.toolbar_all_comment);
        toolbar.setTitle(getResources().getString(R.string.toolbar_Title_AllComment));
        setSupportActionBar(toolbar);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LinearLayout linearLayout = findViewById(R.id.linearLayout_adView_all_comment);
        if (Method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
        } else {
            method.showNonPersonalizedAds(linearLayout);
        }

        textViewNoCommentFound = findViewById(R.id.textView_noComment_all_Comment);
        editTextComment = findViewById(R.id.editText_dialogbox_comment);
        RecyclerView recyclerView = findViewById(R.id.recyclerView_all_comment);
        ImageView imageViewSend = findViewById(R.id.imageView_dialogBox_comment);
        linearLayout_all_comment = findViewById(R.id.linearLayout_all_comment);
        floatingComment = findViewById(R.id.fab);

        ImageView imageView_comment = findViewById(R.id.imageView_all_comment);
        try {
            if (method.pref.getBoolean(method.pref_login, false)) {
                String imageUser = method.pref.getString(method.userImage, null);
                if (imageUser != null) {
                    Picasso.get().load(imageUser)
                            .placeholder(R.drawable.profile)
                            .into(imageView_comment);
                }
            }
        } catch (Exception e) {
            Log.d("error", e.toString());
        }

        textViewNoCommentFound.setVisibility(View.GONE);

        if (commentLists.size() == 0) {
            textViewNoCommentFound.setVisibility(View.VISIBLE);
        }

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AllComment.this);
        recyclerView.setLayoutManager(layoutManager);

        allCommentAdapter = new AllCommentAdapter(AllComment.this, commentLists);
        recyclerView.setAdapter(allCommentAdapter);

        floatingComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout_all_comment.setVisibility(View.VISIBLE);
                floatingComment.setVisibility(View.GONE);
            }
        });

        imageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (method.pref.getBoolean(method.pref_login, false)) {

                    editTextComment.clearFocus();
                    imm.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);

                    editTextComment.setError(null);
                    String comment = editTextComment.getText().toString();

                    if (comment.isEmpty()) {
                        editTextComment.requestFocus();
                        editTextComment.setError(getResources().getString(R.string.please_enter_comment));
                    } else {
                        linearLayout_all_comment.setVisibility(View.GONE);
                        floatingComment.setVisibility(View.VISIBLE);
                        if (method.isNetworkAvailable()) {
                            editTextComment.clearFocus();
                            editTextComment.getText().clear();
                            inputMethodManager.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);
                            Comment(method.pref.getString(method.profileId, null), comment);
                        } else {
                            method.alertBox(getResources().getString(R.string.internet_connection));
                        }
                    }


                } else {
                    Method.loginBack = true;
                    startActivity(new Intent(AllComment.this, Login.class));
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
    public void onBackPressed() {
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        super.onBackPressed();
    }

    public void Comment(final String userId, final String comment) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(AllComment.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("comment_text", comment);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("method_name", "user_comment");
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
                            Toast.makeText(AllComment.this, msg, Toast.LENGTH_SHORT).show();
                            textViewNoCommentFound.setVisibility(View.GONE);
                            String userImage = method.pref.getString(method.userImage, null);
                            if (userImage == null) {
                                userImage = "";
                            }
                            commentLists.add(0, new CommentList(method.pref.getString(method.profileId, null),
                                    method.pref.getString(method.userName, null),
                                    userImage,
                                    bookId,
                                    comment, getResources().getString(R.string.today)));
                            allCommentAdapter.notifyDataSetChanged();
                            Events.Comment commentNotify = new Events.Comment(method.pref.getString(method.profileId, null),
                                    method.pref.getString(method.userName, null),
                                    userImage,
                                    bookId,
                                    comment, getResources().getString(R.string.today));
                            GlobalBus.getBus().post(commentNotify);
                        }
                    }

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
}
