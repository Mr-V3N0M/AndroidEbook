package com.example.shwenyarmya.Fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.example.shwenyarmya.Activity.CategoryByList;
import com.example.shwenyarmya.Activity.MainActivity;
import com.example.shwenyarmya.Activity.Search;
import com.example.shwenyarmya.Adapter.CategoryAdapter;
import com.example.shwenyarmya.Adapter.NothingSelectedSpinnerAdapter;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.CategoryList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cz.msebera.android.httpclient.Header;

public class CategoryFragment extends Fragment {

    private Method method;
    private ProgressBar progressBar;
    private TextView textView_noData;
    private List<CategoryList> categoryLists;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CategoryAdapter categoryAdapter;
    private ArrayList<String> mCategoryName;
    private InterstitialAdView interstitialAdView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.category_fragment, container, false);

        MainActivity.toolbar.setTitle(getResources().getString(R.string.category));

        categoryLists = new ArrayList<>();
        mCategoryName = new ArrayList<>();

        interstitialAdView = new InterstitialAdView() {
            @Override
            public void position(int position, String type, String id, String title, String fileType, String fileUrl) {
                startActivity(new Intent(getActivity(), CategoryByList.class)
                        .putExtra("title", title)
                        .putExtra("id", id)
                        .putExtra("type", type));
            }
        };
        method = new Method(getActivity(), interstitialAdView);

        textView_noData = view.findViewById(R.id.textView_category);
        progressBar = view.findViewById(R.id.progressbar_category_fragment);
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefresh_category_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_category_fragment);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.setFocusable(false);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.toolbar);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (method.isNetworkAvailable()) {
                    Category();
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        if (method.isNetworkAvailable()) {
            Category();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
            progressBar.setVisibility(View.GONE);
        }

        setHasOptionsMenu(true);
        return view;
    }

    private void showSearch() {
        final Dialog mDialog = new Dialog(requireActivity(), R.style.Theme_AppCompat_Translucent);
        mDialog.setContentView(R.layout.search_cat_dialog);
        final EditText edtSearch = mDialog.findViewById(R.id.edt_name);
        Button btnSubmit = mDialog.findViewById(R.id.btn_submit);
        ImageView close = mDialog.findViewById(R.id.txtClose);
        final Spinner spinnerAuthor = mDialog.findViewById(R.id.spName);
        TextView text_search_title = mDialog.findViewById(R.id.text_search_title);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireActivity(), R.layout.spinner_item, mCategoryName);
        typeAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerAuthor.setAdapter(
                new NothingSelectedSpinnerAdapter(typeAdapter,
                        R.layout.contact_spinner_row_nothing_selected_cat,
                        // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                        getActivity()));

        edtSearch.setTypeface(Method.scriptable);
        btnSubmit.setTypeface(Method.scriptable);
        text_search_title.setTypeface(Method.scriptable);

        spinnerAuthor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.search_text));
                    ((TextView) parent.getChildAt(0)).setTextSize(14);
                    ((TextView) parent.getChildAt(0)).setTypeface(Method.scriptable);
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.search_text));
                    ((TextView) parent.getChildAt(0)).setTextSize(14);
                    ((TextView) parent.getChildAt(0)).setTypeface(Method.scriptable);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = edtSearch.getText().toString();
                if (spinnerAuthor.getSelectedItemPosition() == 0) {
                    Toast.makeText(requireActivity(), getString(R.string.please_sel_category), Toast.LENGTH_SHORT).show();
                } else {
                    if (!search.isEmpty()) {
                        Intent intent = new Intent(requireActivity(), Search.class);
                        intent.putExtra("id", categoryLists.get(spinnerAuthor.getSelectedItemPosition() - 1).getCid());
                        intent.putExtra("search", search);
                        intent.putExtra("type", "category_search");
                        startActivity(intent);
                        mDialog.dismiss();
                    } else {
                        edtSearch.setError(getResources().getString(R.string.please_enter_book));
                    }

                }

            }
        });
        mDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_author, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.ic_searchView:
                if (categoryLists.size() != 0) {
                    showSearch();
                } else {
                    method.alertBox(getResources().getString(R.string.wrong));
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void Category() {

        categoryLists.clear();
        mCategoryName.clear();

        progressBar.setVisibility(View.VISIBLE);

        if (getActivity() != null) {

            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("method_name", "get_category");
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
                            String cid = object.getString("cid");
                            String category_name = object.getString("category_name");
                            String total_books = object.getString("total_books");
                            String cat_image = object.getString("category_image");
                            mCategoryName.add(object.getString("category_name"));

                            categoryLists.add(new CategoryList(cid, category_name, total_books, cat_image));

                        }

                        if (categoryLists.size() == 0) {
                            textView_noData.setVisibility(View.VISIBLE);
                        } else {
                            textView_noData.setVisibility(View.GONE);
                            categoryAdapter = new CategoryAdapter(getActivity(), categoryLists, "category", interstitialAdView);
                            recyclerView.setAdapter(categoryAdapter);
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

}
