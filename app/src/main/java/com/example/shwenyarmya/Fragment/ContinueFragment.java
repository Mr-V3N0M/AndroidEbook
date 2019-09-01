package com.example.shwenyarmya.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.shwenyarmya.Activity.BookDetail;
import com.example.shwenyarmya.Activity.MainActivity;
import com.example.shwenyarmya.Activity.Search;
import com.example.shwenyarmya.Adapter.BookAdapterLV;
import com.example.shwenyarmya.DataBase.DatabaseHandler;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.SubCategoryList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.Method;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ContinueFragment extends Fragment {

    private Method method;
    private ProgressBar progressBar;
    private BookAdapterLV continueAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView textView_noData;
    public DatabaseHandler db;
    private List<SubCategoryList> subCategoryLists;
    private InterstitialAdView interstitialAdView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.favorite_fragment, container, false);

        MainActivity.toolbar.setTitle(getResources().getString(R.string.continue_book));

        db = new DatabaseHandler(getContext());
        subCategoryLists = new ArrayList<>();
        subCategoryLists.clear();
        subCategoryLists = db.getContinueBook("all");

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

        progressBar = view.findViewById(R.id.progressbar_category_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_category_fragment);
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefresh_category_fragment);
        textView_noData = view.findViewById(R.id.text_no_fav);

        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        progressBar.setVisibility(View.GONE);

        setData();

        mSwipeRefreshLayout.setColorSchemeResources(R.color.toolbar);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                subCategoryLists.clear();
                subCategoryLists = db.getContinueBook("all");
                setData();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setData() {
        if (subCategoryLists.size() == 0) {
            textView_noData.setVisibility(View.VISIBLE);
        } else {
            textView_noData.setVisibility(View.GONE);
            continueAdapter = new BookAdapterLV(getActivity(), subCategoryLists, "continue", interstitialAdView);
            recyclerView.setAdapter(continueAdapter);
        }
    }
}
