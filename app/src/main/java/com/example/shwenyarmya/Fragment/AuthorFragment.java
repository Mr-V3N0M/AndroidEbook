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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
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
import com.example.shwenyarmya.Activity.AuthorByList;
import com.example.shwenyarmya.Activity.MainActivity;
import com.example.shwenyarmya.Activity.Search;
import com.example.shwenyarmya.Adapter.AuthorAdapter;
import com.example.shwenyarmya.Adapter.NothingSelectedSpinnerAdapter;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.AuthorList;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cz.msebera.android.httpclient.Header;

public class AuthorFragment extends Fragment {

    private Method method;
    private ProgressBar progressBar;
    private TextView textView_noData;
    private List<AuthorList> authorLists;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AuthorAdapter authorAdapter;
    private ArrayList<String> mAuthorName;
    private LayoutAnimationController animation;
    private InterstitialAdView interstitialAdView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.category_fragment, container, false);

        MainActivity.toolbar.setTitle(getResources().getString(R.string.author));

        authorLists = new ArrayList<>();
        mAuthorName = new ArrayList<>();

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        interstitialAdView = new InterstitialAdView() {
            @Override
            public void position(int position, String type, String id, String title, String fileType, String fileUrl) {
                startActivity(new Intent(getActivity(), AuthorByList.class)
                        .putExtra("title", title)
                        .putExtra("id", id)
                        .putExtra("type", type));
            }
        };
        method = new Method(getActivity(), interstitialAdView);

        progressBar = view.findViewById(R.id.progressbar_category_fragment);
        textView_noData = view.findViewById(R.id.textView_category);
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefresh_category_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_category_fragment);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.toolbar);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (method.isNetworkAvailable()) {
                    Author();
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        if (method.isNetworkAvailable()) {
            Author();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
            progressBar.setVisibility(View.GONE);
        }

        setHasOptionsMenu(true);
        return view;
    }

    private void showSearch() {

        final Dialog mDialog = new Dialog(requireActivity(), R.style.Theme_AppCompat_Translucent);
        mDialog.setContentView(R.layout.search_dialog);
        final EditText edtSearch = mDialog.findViewById(R.id.edt_name);
        Button btnSubmit = mDialog.findViewById(R.id.btn_submit);
        ImageView close = mDialog.findViewById(R.id.txtClose);
        final Spinner spinnerAuthor = mDialog.findViewById(R.id.spName);
        TextView text_search_title = mDialog.findViewById(R.id.text_search_title);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireActivity(), R.layout.spinner_item, mAuthorName);
        typeAdapter.setDropDownViewResource(R.layout.spinner_item);

        spinnerAuthor.setAdapter(
                new NothingSelectedSpinnerAdapter(typeAdapter,
                        R.layout.contact_spinner_row_nothing_selected,
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
                    Toast.makeText(requireActivity(), getString(R.string.please_sel_author), Toast.LENGTH_SHORT).show();
                } else {
                    if (!search.isEmpty()) {
                        Intent intent = new Intent(requireActivity(), Search.class);
                        intent.putExtra("id", authorLists.get(spinnerAuthor.getSelectedItemPosition() - 1).getAuthor_id());
                        intent.putExtra("search", search);
                        intent.putExtra("type", "author_search");
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
                if(authorLists.size()!=0){
                    showSearch();
                }else {
                    method.alertBox(getResources().getString(R.string.wrong));
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void Author() {

        authorLists.clear();
        mAuthorName.clear();

        progressBar.setVisibility(View.VISIBLE);

        if (getActivity() != null) {

            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("method_name", "get_author");
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
                            String author_id = object.getString("author_id");
                            String author_name = object.getString("author_name");
                            String author_image = object.getString("author_image");
                            mAuthorName.add(object.getString("author_name"));

                            authorLists.add(new AuthorList(author_id, author_name, author_image));

                        }

                        if(authorLists.size()==0){
                            textView_noData.setVisibility(View.VISIBLE);
                        }else {
                            textView_noData.setVisibility(View.GONE);
                            authorAdapter = new AuthorAdapter(getActivity(), authorLists, "author", interstitialAdView);
                            recyclerView.setAdapter(authorAdapter);
                            recyclerView.setLayoutAnimation(animation);
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
