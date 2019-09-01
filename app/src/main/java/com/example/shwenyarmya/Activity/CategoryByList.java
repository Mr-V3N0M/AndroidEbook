package com.example.shwenyarmya.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class CategoryByList extends AppCompatActivity {

    private Method method;
    public Toolbar toolbar;
    private String toolbarTitle, id, type;
    private ProgressBar progressBar;
    private List<SubCategoryList> subCategoryLists;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BookAdapterLV latestAdapterLV;
    private BookAdapterGV latestAdapterGV;
    private boolean isView = true;
    private TextView textViewCount,textView_noData;
    private LinearLayout linearLayout;
    private ImageView imageView_gridView, imageView_listView;
    private LayoutAnimationController animation;
    private InterstitialAdView interstitialAdView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);

        subCategoryLists = new ArrayList<>();

        interstitialAdView = new InterstitialAdView() {
            @Override
            public void position(int position, String type, String id, String title, String fileType, String fileUrl) {
                startActivity(new Intent(CategoryByList.this, BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
            }
        };
        method = new Method(CategoryByList.this, interstitialAdView);
        method.forceRTLIfSupported(getWindow());

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(CategoryByList.this, resId);

        Intent intent = getIntent();
        toolbarTitle = intent.getStringExtra("title");
        id = intent.getStringExtra("id");
        type = intent.getStringExtra("type");

        toolbar = findViewById(R.id.toolbar_sub_category);
        toolbar.setTitle(toolbarTitle);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        imageView_gridView = findViewById(R.id.imageView_gridView_sub_category);
        imageView_listView = findViewById(R.id.imageView_listView_sub_category);

        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh_sub_category);
        progressBar = findViewById(R.id.progressbar_sub_category);
        textView_noData = findViewById(R.id.textView_sub_category);
        recyclerView = findViewById(R.id.recyclerView_sub_category);
        textViewCount = findViewById(R.id.textView_number_ofItem_sub_category);
        linearLayout = findViewById(R.id.linearLayout_sub_category);

        if (Method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
        } else {
            method.showNonPersonalizedAds(linearLayout);
        }

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(CategoryByList.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        imageView_listView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));

        mSwipeRefreshLayout.setColorSchemeResources(R.color.toolbar);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callData();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        imageView_gridView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isView = false;
                imageView_gridView.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid_hov));
                imageView_listView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list));
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(CategoryByList.this, 3);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setFocusable(false);
                callData();
            }
        });

        imageView_listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isView = true;
                imageView_gridView.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid));
                imageView_listView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(CategoryByList.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setFocusable(false);
                callData();
            }
        });

        isView = true;
        callData();


    }

    public void callData() {
        if (method.isNetworkAvailable()) {
            subCategory(isView);
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.ic_searchView);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener((new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startActivity(new Intent(CategoryByList.this, Search.class)
                        .putExtra("id", "0")
                        .putExtra("search", query)
                        .putExtra("type", "normal"));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        }));


        return true;

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

    public void subCategory(final boolean isTrue) {

        subCategoryLists.clear();

        progressBar.setVisibility(View.VISIBLE);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(CategoryByList.this));
        jsObj.addProperty("cat_id", id);
        jsObj.addProperty("method_name", "get_cat_id");
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
                        String book_title = object.getString("book_title");
                        String book_description = object.getString("book_description");
                        String book_cover_img = object.getString("book_cover_img");
                        String book_file_type = object.getString("book_file_type");
                        String total_rate = object.getString("total_rate");
                        String rate_avg = object.getString("rate_avg");
                        String book_views = object.getString("book_views");
                        String author_name = object.getString("author_name");

                        subCategoryLists.add(new SubCategoryList(id, "", book_title, book_description, book_cover_img, "", book_file_type, total_rate, rate_avg, book_views, "", author_name, ""));

                    }

                    String count = subCategoryLists.size() + " " + getResources().getString(R.string.iteam);
                    textViewCount.setText(count);
                    textViewCount.setTypeface(Method.scriptable);

                    if(subCategoryLists.size()==0){
                        textView_noData.setVisibility(View.VISIBLE);
                    }else {
                        textView_noData.setVisibility(View.GONE);
                        if (isTrue) {
                            latestAdapterLV = new BookAdapterLV(CategoryByList.this, subCategoryLists, "cat_by_list", interstitialAdView);
                            recyclerView.setAdapter(latestAdapterLV);
                        } else {
                            latestAdapterGV = new BookAdapterGV(CategoryByList.this, subCategoryLists, "cat_by_list", interstitialAdView);
                            recyclerView.setAdapter(latestAdapterGV);
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
