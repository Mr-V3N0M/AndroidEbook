package com.example.shwenyarmya.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.example.shwenyarmya.Activity.BookDetail;
import com.example.shwenyarmya.Activity.MainActivity;
import com.example.shwenyarmya.Activity.Search;
import com.example.shwenyarmya.Adapter.BookAdapterGV;
import com.example.shwenyarmya.Adapter.BookAdapterLV;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.SubCategoryList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Method;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cz.msebera.android.httpclient.Header;

public class LatestFragment extends Fragment {

    private Method method;
    private String type;
    private ProgressBar progressBar;
    private List<SubCategoryList> subCategoryLists;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageView imageView_gridView, imageView_listView;
    private BookAdapterLV latestAdapterLV;
    private BookAdapterGV latestAdapterGV;
    private boolean isView = true;
    private TextView textViewCount, textView_noData;
    private LayoutAnimationController animation;
    private InterstitialAdView interstitialAdView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.latest_fragment, container, false);

        subCategoryLists = new ArrayList<>();

        assert getArguments() != null;
        type = getArguments().getString("type");
        assert type != null;
        if (type.equals("latest")) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.latest));
        } else {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.popular_books));
        }

        interstitialAdView = new InterstitialAdView() {
            @Override
            public void position(int position, String type, String id, String title, String fileType, String fileUrl) {
                startActivity(new Intent(getActivity(), BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
            }
        };
        method = new Method(getActivity(), interstitialAdView);

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        imageView_gridView = view.findViewById(R.id.imageView_gridView_latest_fragment);
        imageView_listView = view.findViewById(R.id.imageView_listView_latest_fragment);

        progressBar = view.findViewById(R.id.progresbar_latest_fragment);
        textViewCount = view.findViewById(R.id.textView_number_ofItem_latest_fragment);
        textView_noData = view.findViewById(R.id.textView_latest_fragment);
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefresh_latest_fragment);

        recyclerView = view.findViewById(R.id.recyclerView_latest_fragment);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        imageView_listView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));

        mSwipeRefreshLayout.setColorSchemeResources(R.color.toolbar);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (method.isNetworkAvailable()) {
                    Latest(isView);
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
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setFocusable(false);
                if (method.isNetworkAvailable()) {
                    Latest(isView);
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
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setFocusable(false);
                if (method.isNetworkAvailable()) {
                    Latest(isView);
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
            }
        });

        if (method.isNetworkAvailable()) {
            Latest(isView);
            isView = true;
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
            progressBar.setVisibility(View.GONE);
        }

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.ic_searchView);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener((new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startActivity(new Intent(getActivity(), Search.class)
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

    }

    private void Latest(final boolean isTrue) {

        subCategoryLists.clear();

        progressBar.setVisibility(View.VISIBLE);

        if (getActivity() != null) {

            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            if(type.equals("latest")){
                jsObj.addProperty("method_name", "get_latest_books");
            }else {
                jsObj.addProperty("method_name", "get_popular_books");
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

                            subCategoryLists.add(new SubCategoryList(id, cat_id, book_title, book_description, book_cover_img, book_bg_img, book_file_type, total_rate, rate_avg, book_views, author_id, author_name, ""));

                        }

                        if (subCategoryLists.size() == 0) {
                            textView_noData.setVisibility(View.VISIBLE);
                        } else {
                            textView_noData.setVisibility(View.GONE);
                            if (isTrue) {
                                latestAdapterLV = new BookAdapterLV(getActivity(), subCategoryLists, type, interstitialAdView);
                                recyclerView.setAdapter(latestAdapterLV);
                            } else {
                                latestAdapterGV = new BookAdapterGV(getActivity(), subCategoryLists, type, interstitialAdView);
                                recyclerView.setAdapter(latestAdapterGV);
                                recyclerView.setLayoutAnimation(animation);
                            }
                        }

                        String count = subCategoryLists.size() + " " + getResources().getString(R.string.iteam);
                        textViewCount.setText(count);
                        textViewCount.setTypeface(Method.scriptable);

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
}
