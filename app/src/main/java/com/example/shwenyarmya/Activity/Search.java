package com.example.shwenyarmya.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.example.shwenyarmya.Adapter.BookAdapterGV;
import com.example.shwenyarmya.Adapter.BookAdapterLV;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.SubCategoryList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Search extends AppCompatActivity {

    private Method method;
    public Toolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public List<SubCategoryList> subCategoryLists;
    private boolean isView = true;
    private String id, search, type;
    private BookAdapterGV searchAdapterGV;
    private BookAdapterLV searchAdapterLV;
    private ImageView imageView_gridView, imageView_listView;
    private TextView textViewCount,textView_noData;
    LinearLayout linearLayout;
    private LayoutAnimationController animation;
    private InterstitialAdView interstitialAdView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        interstitialAdView = new InterstitialAdView() {
            @Override
            public void position(int position, String type, String id, String title, String fileType, String fileUrl) {
                startActivity(new Intent(Search.this, BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
            }
        };
        method = new Method(Search.this, interstitialAdView);
        method.forceRTLIfSupported(getWindow());

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(Search.this, resId);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        search = intent.getStringExtra("search");
        type = intent.getStringExtra("type");

        subCategoryLists = new ArrayList<>();

        toolbar = findViewById(R.id.toolbar_search);
        toolbar.setTitle(search);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        imageView_gridView = findViewById(R.id.imageView_gridView_search);
        imageView_listView = findViewById(R.id.imageView_listView_search);

        progressBar = findViewById(R.id.progressbar_search);
        textView_noData = findViewById(R.id.textView_search);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh_search);
        recyclerView = findViewById(R.id.recyclerView_search);
        textViewCount = findViewById(R.id.textView_number_ofItem_search);
        linearLayout = findViewById(R.id.linearLayout_search);

        if (Method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
        } else {
            method.showNonPersonalizedAds(linearLayout);
        }

        imageView_listView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Search.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.toolbar);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (method.isNetworkAvailable()) {
                    search(isView);
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        imageView_gridView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isView = false;
                imageView_gridView.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid_hov));
                imageView_listView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list));
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(Search.this, 3);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setFocusable(false);
                if (method.isNetworkAvailable()) {
                    search(isView);
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
            }
        });

        imageView_listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isView = true;
                imageView_gridView.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid));
                imageView_listView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Search.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setFocusable(false);
                if (method.isNetworkAvailable()) {
                    search(isView);
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
            }
        });

        if (method.isNetworkAvailable()) {
            search(isView);
            isView = true;
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void search(final boolean isTrue) {

        subCategoryLists.clear();
        progressBar.setVisibility(View.VISIBLE);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Search.this));
        if (type.equals("category_search")) {
            jsObj.addProperty("category_id", id);
        } else if (type.equals("author_search")) {
            jsObj.addProperty("author_id", id);
        }
        jsObj.addProperty("search_text", search);
        jsObj.addProperty("method_name", "get_search_books");
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
                        String cat_id = object.getString("cat_id");
                        String book_title = object.getString("book_title");
                        String book_description = object.getString("book_description");
                        String book_cover_img = object.getString("book_cover_img");
                        String book_bg_img = object.getString("book_bg_img");
                        String book_file_type = object.getString("book_file_type");
                        String total_rate = object.getString("total_rate");
                        String rate_avg = object.getString("rate_avg");
                        String book_views = object.getString("book_views");
                        String author_id = object.getString("author_id");
                        String author_name = object.getString("author_name");
                        String category_name = object.getString("category_name");

                        subCategoryLists.add(new SubCategoryList(id, cat_id, book_title, book_description, book_cover_img, book_bg_img, book_file_type, total_rate, rate_avg, book_views, author_id, author_name, category_name));

                    }
                    String count = subCategoryLists.size() + " " + getResources().getString(R.string.iteam);
                    textViewCount.setText(count);
                    textViewCount.setTypeface(Method.scriptable);

                    if(subCategoryLists.size()==0){
                        textView_noData.setVisibility(View.VISIBLE);
                    }else {
                        textView_noData.setVisibility(View.GONE);
                        if (isTrue) {
                            searchAdapterLV = new BookAdapterLV(Search.this, subCategoryLists, "search", interstitialAdView);
                            recyclerView.setAdapter(searchAdapterLV);
                        } else {
                            searchAdapterGV = new BookAdapterGV(Search.this, subCategoryLists, "search", interstitialAdView);
                            recyclerView.setAdapter(searchAdapterGV);
                            recyclerView.setLayoutAnimation(animation);
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

}
