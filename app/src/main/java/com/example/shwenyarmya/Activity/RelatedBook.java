package com.example.shwenyarmya.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.shwenyarmya.Adapter.BookAdapterGV;
import com.example.shwenyarmya.Adapter.BookAdapterLV;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.SubCategoryList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.Method;

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
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class RelatedBook extends AppCompatActivity {

    private Method method;
    private ImageView imageView_gridView, imageView_listView;
    public Toolbar toolbar;
    private RecyclerView recyclerView;
    private List<SubCategoryList> subCategoryLists;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isView = true;
    private TextView textViewCount, textView_noData;
    private LinearLayout linearLayout;
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

        interstitialAdView = new InterstitialAdView() {
            @Override
            public void position(int position, String type, String id, String title, String fileType, String fileUrl) {
                startActivity(new Intent(RelatedBook.this, BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
            }
        };
        method = new Method(RelatedBook.this, interstitialAdView);
        method.forceRTLIfSupported(getWindow());

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(RelatedBook.this, resId);

        subCategoryLists = new ArrayList<>();
        subCategoryLists = (List<SubCategoryList>) getIntent().getSerializableExtra("array");

        toolbar = findViewById(R.id.toolbar_sub_category);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        imageView_gridView = findViewById(R.id.imageView_gridView_sub_category);
        imageView_listView = findViewById(R.id.imageView_listView_sub_category);

        ProgressBar progressBar = findViewById(R.id.progressbar_sub_category);
        textView_noData = findViewById(R.id.textView_sub_category);
        recyclerView = findViewById(R.id.recyclerView_sub_category);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh_sub_category);
        textViewCount = findViewById(R.id.textView_number_ofItem_sub_category);
        linearLayout = findViewById(R.id.linearLayout_sub_category);

        if (Method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
        } else {
            method.showNonPersonalizedAds(linearLayout);
        }

        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RelatedBook.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.toolbar);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapterSet(isView);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        imageView_listView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));

        imageView_gridView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isView = false;
                imageView_gridView.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid_hov));
                imageView_listView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list));
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(RelatedBook.this, 3);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setFocusable(false);
                adapterSet(isView);
            }
        });

        imageView_listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isView = true;
                imageView_gridView.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid));
                imageView_listView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RelatedBook.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setFocusable(false);
                adapterSet(isView);
            }
        });

        adapterSet(isView);
        isView = true;

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
                startActivity(new Intent(RelatedBook.this, Search.class)
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


    public void adapterSet(final boolean isTrue) {

        String count = subCategoryLists.size() + " " + getResources().getString(R.string.iteam);
        textViewCount.setText(count);
        textViewCount.setTypeface(Method.scriptable);

        if (subCategoryLists.size() == 0) {
            textView_noData.setVisibility(View.VISIBLE);
        } else {
            textView_noData.setVisibility(View.GONE);
            if (isTrue) {
                BookAdapterLV relatedBookAdapterLV = new BookAdapterLV(RelatedBook.this, subCategoryLists, "related", interstitialAdView);
                recyclerView.setAdapter(relatedBookAdapterLV);
            } else {
                BookAdapterGV relatedBookAdapterGV = new BookAdapterGV(RelatedBook.this, subCategoryLists, "related", interstitialAdView);
                recyclerView.setAdapter(relatedBookAdapterGV);
                recyclerView.setLayoutAnimation(animation);
            }
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
