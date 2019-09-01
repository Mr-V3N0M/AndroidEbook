package com.example.shwenyarmya.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.shwenyarmya.Adapter.BookAdapterLV;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.SubCategoryList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Method;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import at.blogc.android.views.ExpandableTextView;
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class AuthorByList extends AppCompatActivity {

    private Method method;
    public Toolbar toolbar;
    private String title, id, type;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<SubCategoryList> subCategoryLists;
    private BookAdapterLV bookAdapterLV;
    private ImageView imageViewAuthor;
    private TextView textViewAuthorName, textViewCity, textView_noData;
    private String author_id, author_name, author_city, author_desc, author_image;
    private LinearLayout linearLayout;
    private TextView text_view_more, text_represent;
    private ExpandableTextView textViewDescription;
    private InterstitialAdView interstitialAdView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_author);

        subCategoryLists = new ArrayList<>();

        interstitialAdView = new InterstitialAdView() {
            @Override
            public void position(int position, String type, String id, String title, String fileType, String fileUrl) {
                startActivity(new Intent(AuthorByList.this, BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
            }
        };
        method = new Method(AuthorByList.this, interstitialAdView);
        method.forceRTLIfSupported(getWindow());

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        id = intent.getStringExtra("id");
        type = intent.getStringExtra("type");

        toolbar = findViewById(R.id.toolbar_sub_author);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh_sub_author);
        progressBar = findViewById(R.id.progressbar_sub_author);
        recyclerView = findViewById(R.id.recyclerView_sub_author);
        imageViewAuthor = findViewById(R.id.imageView_sub_author);
        textViewAuthorName = findViewById(R.id.textView_name_sub_author);
        textViewCity = findViewById(R.id.textView_subName_sub_author);
        textViewDescription = findViewById(R.id.textViewDes_sub_author);
        linearLayout = findViewById(R.id.linearLayout_sub_author);
        text_view_more = findViewById(R.id.textView_more_sub_author);
        text_represent = findViewById(R.id.text_represent);
        textView_noData = findViewById(R.id.textView_sub_author);

        text_represent.setTypeface(Method.scriptable);
        text_view_more.setTypeface(Method.scriptable);

        if (Method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
        } else {
            method.showNonPersonalizedAds(linearLayout);
        }

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AuthorByList.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);
        recyclerView.setNestedScrollingEnabled(false);

        textViewDescription.setAnimationDuration(750L);

        // set interpolators for both expanding and collapsing animations
        textViewDescription.setInterpolator(new OvershootInterpolator());

        textViewDescription.setExpandInterpolator(new OvershootInterpolator());
        textViewDescription.setCollapseInterpolator(new OvershootInterpolator());

        text_view_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                text_view_more.setText(textViewDescription.isExpanded() ? R.string.text_load_more : R.string.text_load_less);
                textViewDescription.toggle();
            }
        });

        text_view_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (textViewDescription.isExpanded()) {
                    textViewDescription.collapse();
                    text_view_more.setText(R.string.text_load_more);
                } else {
                    textViewDescription.expand();
                    text_view_more.setText(R.string.text_load_less);
                }
            }
        });


        mSwipeRefreshLayout.setColorSchemeResources(R.color.toolbar);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (method.isNetworkAvailable()) {
                    subCategory();
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });


        if (method.isNetworkAvailable()) {
            subCategory();
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
                startActivity(new Intent(AuthorByList.this, Search.class)
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

    public void subCategory() {

        subCategoryLists.clear();

        progressBar.setVisibility(View.VISIBLE);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(AuthorByList.this));
        jsObj.addProperty("author_id", id);
        jsObj.addProperty("method_name", "get_author_id");
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant_Api.url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                Log.d("Response", new String(responseBody));
                String res = new String(responseBody);

                try {
                    JSONObject jsonObject = new JSONObject(res);

                    author_id = jsonObject.getString("author_id");
                    author_name = jsonObject.getString("author_name");
                    author_city = jsonObject.getString("author_city_name");
                    author_desc = jsonObject.getString("author_description");
                    author_image = jsonObject.getString("author_image");

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
                    if (!author_image.equals("")) {
                        Picasso.get().load(author_image)
                                .placeholder(R.drawable.placeholder_portable)
                                .into(imageViewAuthor);
                    }

                    textViewAuthorName.setText(author_name);
                    textViewAuthorName.setTypeface(Method.scriptable);
                    textViewCity.setText(author_city);
                    textViewDescription.setText(Html.fromHtml(author_desc));

                    if (subCategoryLists.size() == 0) {
                        textView_noData.setVisibility(View.VISIBLE);
                    } else {
                        textView_noData.setVisibility(View.GONE);
                        bookAdapterLV = new BookAdapterLV(AuthorByList.this, subCategoryLists, "author_by_list", interstitialAdView);
                        recyclerView.setAdapter(bookAdapterLV);
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
