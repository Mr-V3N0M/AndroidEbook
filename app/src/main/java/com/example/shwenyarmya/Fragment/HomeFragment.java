package com.example.shwenyarmya.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.shwenyarmya.Activity.AuthorByList;
import com.example.shwenyarmya.Activity.BookDetail;
import com.example.shwenyarmya.Activity.CategoryByList;
import com.example.shwenyarmya.Activity.MainActivity;
import com.example.shwenyarmya.Activity.Search;
import com.example.shwenyarmya.Adapter.BookHomeAdapterGV;
import com.example.shwenyarmya.Adapter.CategoryHomeAdapter;
import com.example.shwenyarmya.Adapter.HomeAuthorAdapter;
import com.example.shwenyarmya.DataBase.DatabaseHandler;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.AuthorList;
import com.example.shwenyarmya.Item.CategoryList;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cz.msebera.android.httpclient.Header;

public class HomeFragment extends Fragment {


    private Method method;
    private ProgressBar progressBar;
    private List<SubCategoryList> continueList;
    private List<SubCategoryList> sliderList;
    private List<SubCategoryList> latestList;
    private List<SubCategoryList> mostPopularList;
    private List<CategoryList> categoryLists;
    private List<AuthorList> authorLists;
    private BookHomeAdapterGV latestAdapter;
    private BookHomeAdapterGV popularAdapter;
    private BookHomeAdapterGV continueAdapter;
    private HomeAuthorAdapter authorAdapter;
    private SliderLayout mDemoSlider;
    private CategoryHomeAdapter categoryAdapter;
    private InterstitialAdView interstitialAdView;
    private InputMethodManager imm;
    private DatabaseHandler db;
    private EditText editText_search;
    private LinearLayout linearLayout_continue;
    private RecyclerView recyclerViewContinue, recyclerViewLatest, recyclerViewPopular, recyclerViewCategory, recyclerViewAuthor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.home_fragment, container, false);

        MainActivity.toolbar.setTitle(getResources().getString(R.string.home));

        db = new DatabaseHandler(getActivity());

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        interstitialAdView = new InterstitialAdView() {
            @Override
            public void position(int position, String type, String id, String title, String fileType, String fileUrl) {
                if (type.equals("home_cat")) {
                    startActivity(new Intent(getActivity(), CategoryByList.class)
                            .putExtra("title", title)
                            .putExtra("id", id)
                            .putExtra("type", type));
                } else if (type.equals("home_author")) {
                    startActivity(new Intent(getActivity(), AuthorByList.class)
                            .putExtra("title", title)
                            .putExtra("id", id)
                            .putExtra("type", type));
                } else {
                    startActivity(new Intent(getActivity(), BookDetail.class)
                            .putExtra("bookId", id)
                            .putExtra("position", position)
                            .putExtra("type", type));
                }
            }
        };
        method = new Method(requireActivity(), interstitialAdView);

        continueList = new ArrayList<>();
        sliderList = new ArrayList<>();
        latestList = new ArrayList<>();
        mostPopularList = new ArrayList<>();
        categoryLists = new ArrayList<>();
        authorLists = new ArrayList<>();

        DatabaseHandler db = new DatabaseHandler(getActivity());
        continueList.clear();
        continueList = db.getContinueBook("4");

        progressBar = view.findViewById(R.id.progreesbar_home_fragment);
        linearLayout_continue = view.findViewById(R.id.linearLayout_continue_home_fragment);
        recyclerViewContinue = view.findViewById(R.id.recyclerViewContinue_home_fragment);
        recyclerViewLatest = view.findViewById(R.id.recyclerViewLatest_home_fragment);
        recyclerViewPopular = view.findViewById(R.id.recyclerViewPopular_home_fragment);
        recyclerViewCategory = view.findViewById(R.id.recyclerViewCat_home_fragment);
        recyclerViewAuthor = view.findViewById(R.id.recyclerViewAuthor_home_fragment);
        mDemoSlider = view.findViewById(R.id.custom_indicator_home_fragment);

        LinearLayout linearLayout = view.findViewById(R.id.linearLayout_adView_home_fragment);

        if (Method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
        } else {
            method.showNonPersonalizedAds(linearLayout);
        }

        View viewContinue = view.findViewById(R.id.viewContinue_home_fragment);
        View viewCat = view.findViewById(R.id.viewCat_home_fragment);
        View viewLatest = view.findViewById(R.id.viewLatest_home_fragment);
        View viewPopular = view.findViewById(R.id.viewPopular_home_fragment);
        View viewAuthor = view.findViewById(R.id.viewAuthor_home_fragment);

        if (getResources().getString(R.string.isRTL).equals("true")) {
            viewContinue.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white_rtl));
            viewCat.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white_rtl));
            viewLatest.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white_rtl));
            viewPopular.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white_rtl));
            viewAuthor.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white_rtl));
        } else {
            viewContinue.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white));
            viewCat.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white));
            viewLatest.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white));
            viewPopular.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white));
            viewAuthor.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white));
        }

        TextView text_home_cat = view.findViewById(R.id.text_home_cat);
        TextView text_home_latest = view.findViewById(R.id.text_home_latest);
        TextView text_home_popular = view.findViewById(R.id.text_home_popular);
        text_home_cat.setTypeface(Method.scriptable);
        text_home_latest.setTypeface(Method.scriptable);
        text_home_popular.setTypeface(Method.scriptable);

        linearLayout_continue.setVisibility(View.GONE);

        recyclerViewContinue.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager_Continue = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewContinue.setLayoutManager(layoutManager_Continue);
        recyclerViewContinue.setFocusable(false);

        recyclerViewLatest.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerLatest = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewLatest.setLayoutManager(layoutManagerLatest);
        recyclerViewLatest.setFocusable(false);

        recyclerViewPopular.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerPopular = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewPopular.setLayoutManager(layoutManagerPopular);
        recyclerViewPopular.setFocusable(false);

        recyclerViewCategory.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerCat = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCategory.setLayoutManager(layoutManagerCat);
        recyclerViewCategory.setFocusable(false);

        recyclerViewAuthor.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerAuthor = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewAuthor.setLayoutManager(layoutManagerAuthor);
        recyclerViewAuthor.setFocusable(false);

        editText_search = view.findViewById(R.id.editText_home_fragment);
        ImageView imageView_search = view.findViewById(R.id.imageView_search_home_fragment);
        Button button_continue = view.findViewById(R.id.button_continue_home_fragment);
        Button button_latest = view.findViewById(R.id.button_latest_home_fragment);
        Button button_popular = view.findViewById(R.id.button_popular_home_fragment);
        Button button_cat = view.findViewById(R.id.button_latest_home_fragment_cat);
        Button button_author = view.findViewById(R.id.button_author_home_fragment);

        button_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main, new ContinueFragment(), "continue").commit();
            }
        });

        button_latest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatestFragment latestFragment = new LatestFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "latest");
                latestFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main,
                        latestFragment, "latest").commit();
            }
        });

        button_cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main, new CategoryFragment(), "category").commit();
            }
        });

        button_popular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatestFragment latestFragment = new LatestFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "popular");
                latestFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main,
                        latestFragment, "latest").commit();
            }
        });

        button_author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.framlayout_main, new AuthorFragment(), "author").commit();
            }
        });

        editText_search.setTypeface(null);

        editText_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                }
                return false;
            }
        });


        imageView_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        if (method.isNetworkAvailable()) {
            Home();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
            progressBar.setVisibility(View.GONE);
        }

        setHasOptionsMenu(true);

        return view;
    }

    public void search() {

        String search = editText_search.getText().toString();
        //do something
        if (!search.isEmpty() || !search.equals("")) {

            editText_search.clearFocus();
            imm.hideSoftInputFromWindow(editText_search.getWindowToken(), 0);

            startActivity(new Intent(getActivity(), Search.class)
                    .putExtra("id", "0")
                    .putExtra("search", search)
                    .putExtra("type", "normal"));
        } else {
            if (getActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
            method.alertBox(getResources().getString(R.string.wrong));
        }

    }

    private void Home() {

        progressBar.setVisibility(View.VISIBLE);

        if (getActivity() != null) {

            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("method_name", "get_home");
            params.put("data", API.toBase64(jsObj.toString()));
            client.post(Constant_Api.url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    Log.d("Response", new String(responseBody));
                    String res = new String(responseBody);

                    try {
                        JSONObject jsonObject = new JSONObject(res);

                        JSONObject jsonObjectBook = jsonObject.getJSONObject(Constant_Api.tag);

                        JSONArray jsonArray = jsonObjectBook.getJSONArray("featured_books");
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

                            sliderList.add(new SubCategoryList(id, cat_id, book_title, book_description, book_cover_img, book_bg_img, book_file_type, total_rate, rate_avg, book_views, author_id, author_name, ""));

                        }

                        JSONArray jsonArrayLatest = jsonObjectBook.getJSONArray("latest_books");
                        for (int i = 0; i < jsonArrayLatest.length(); i++) {

                            JSONObject object = jsonArrayLatest.getJSONObject(i);
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

                            latestList.add(new SubCategoryList(id, cat_id, book_title, book_description, book_cover_img, book_bg_img, book_file_type, total_rate, rate_avg, book_views, author_id, author_name, ""));

                        }

                        JSONArray jsonArrayPopular = jsonObjectBook.getJSONArray("popular_books");
                        for (int i = 0; i < jsonArrayPopular.length(); i++) {

                            JSONObject object = jsonArrayPopular.getJSONObject(i);
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

                            mostPopularList.add(new SubCategoryList(id, cat_id, book_title, book_description, book_cover_img, book_bg_img, book_file_type, total_rate, rate_avg, book_views, author_id, author_name, ""));

                        }

                        JSONArray jsonArrayCat = jsonObjectBook.getJSONArray("category_list");
                        for (int i = 0; i < jsonArrayCat.length(); i++) {

                            JSONObject object = jsonArrayCat.getJSONObject(i);
                            String cid = object.getString("cid");
                            String category_name = object.getString("category_name");
                            String total_books = object.getString("total_books");
                            String cat_image = object.getString("category_image");

                            categoryLists.add(new CategoryList(cid, category_name, total_books, cat_image));

                        }

                        JSONArray jsonArrayAuthor = jsonObjectBook.getJSONArray("author_list");
                        for (int i = 0; i < jsonArrayAuthor.length(); i++) {

                            JSONObject object = jsonArrayAuthor.getJSONObject(i);
                            String author_id = object.getString("author_id");
                            String author_name = object.getString("author_name");
                            String author_image = object.getString("author_image");

                            authorLists.add(new AuthorList(author_id, author_name, author_image));

                        }

                        if (continueList.size() != 0) {
                            linearLayout_continue.setVisibility(View.VISIBLE);
                            continueAdapter = new BookHomeAdapterGV(getActivity(), continueList, "home_continue", interstitialAdView);
                            recyclerViewContinue.setAdapter(continueAdapter);
                        } else {
                            linearLayout_continue.setVisibility(View.GONE);
                        }

                        latestAdapter = new BookHomeAdapterGV(getActivity(), latestList, "home_latest", interstitialAdView);
                        recyclerViewLatest.setAdapter(latestAdapter);

                        popularAdapter = new BookHomeAdapterGV(getActivity(), mostPopularList, "home_most", interstitialAdView);
                        recyclerViewPopular.setAdapter(popularAdapter);

                        categoryAdapter = new CategoryHomeAdapter(getActivity(), categoryLists, "home_cat", interstitialAdView);
                        recyclerViewCategory.setAdapter(categoryAdapter);

                        authorAdapter = new HomeAuthorAdapter(getActivity(), authorLists, "home_author", interstitialAdView);
                        recyclerViewAuthor.setAdapter(authorAdapter);

                        for (int i = 0; i < sliderList.size(); i++) {
                            TextSliderView textSliderView = new TextSliderView(getActivity());
                            // initialize a SliderLayout
                            textSliderView
                                    .name(sliderList.get(i).getBook_title())
                                    .sub_name(sliderList.get(i).getAuthor_name())
                                    .rating(sliderList.get(i).getRate_avg())
                                    .ratingView(sliderList.get(i).getTotal_rate())
                                    .image(sliderList.get(i).getBook_bg_img())
                                    .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                        @Override
                                        public void onSliderClick(BaseSliderView slider) {
                                            method.addContinue(db, mDemoSlider.getCurrentPosition(), sliderList);
                                            startActivity(new Intent(getActivity(), BookDetail.class)
                                                    .putExtra("bookId", sliderList.get(mDemoSlider.getCurrentPosition()).getId())
                                                    .putExtra("position", mDemoSlider.getCurrentPosition())
                                                    .putExtra("type", "slider"));
                                        }
                                    })
                                    .setScaleType(BaseSliderView.ScaleType.Fit);
                            mDemoSlider.addSlider(textSliderView);
                        }

                        if (getResources().getString(R.string.isRTL).equals("true")) {
                            mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Left_Bottom);
                        } else {
                            mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
                        }
                        mDemoSlider.getPagerIndicator().setDefaultIndicatorColor(getResources().getColor(R.color.selectedColor)
                                , getResources().getColor(R.color.unselectedColor));
                        mDemoSlider.setCustomAnimation(new DescriptionAnimation());

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
