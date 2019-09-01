package com.example.shwenyarmya.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shwenyarmya.Adapter.BookAdapterGV;
import com.example.shwenyarmya.Adapter.CommentAdapter;
import com.example.shwenyarmya.DataBase.DatabaseHandler;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.CommentList;
import com.example.shwenyarmya.Item.ScdList;
import com.example.shwenyarmya.Item.SubCategoryList;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.API;
import com.example.shwenyarmya.Util.Constant_Api;
import com.example.shwenyarmya.Util.DownloadEpub;
import com.example.shwenyarmya.Util.Events;
import com.example.shwenyarmya.Util.GlobalBus;
import com.example.shwenyarmya.Util.Method;
import com.github.ornolfr.ratingview.RatingView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import cz.msebera.android.httpclient.Header;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class BookDetail extends AppCompatActivity {

    public Toolbar toolbar;
    private String bookId, type;
    private int rate;
    private Method method;
    private ImageView imageView, imageView_CoverBook, imageViewRating;
    private RecyclerView recyclerViewComment, recyclerViewRelatedBook;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private List<ScdList> scdLists;
    private DatabaseHandler db;
    private CommentAdapter commentAdapter;
    private RelativeLayout linearLayoutRelatedBook;
    private TextView textView_noData;
    private TextView textViewNoBookFound;
    private TextView textViewNoCommentFound;
    private TextView textViewBookName;
    private TextView textViewAuthor;
    private TextView textViewRating;
    private TextView textView_view;
    private WebView webView;
    private RatingView ratingBar;
    private Button buttonAllComment;
    private Button buttonViewAll;
    private InputMethodManager inputMethodManager;
    private int position = 0;
    private ImageView imageView_Send;
    private EditText editText;
    private LinearLayout lay_main, lay_favorite, lay_download, lay_read, lay_report;
    private ImageView image_favorite;
    private InterstitialAdView interstitialAdView;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scd);

        GlobalBus.getBus().register(this);

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        type = intent.getStringExtra("type");
        position = intent.getIntExtra("position", 0);

        scdLists = new ArrayList<>();

        db = new DatabaseHandler(BookDetail.this);
        progressDialog = new ProgressDialog(BookDetail.this);

        interstitialAdView = new InterstitialAdView() {
            @Override
            public void position(int position, String type, String id, String title, String fileType, String fileUrl) {
                startActivity(new Intent(BookDetail.this, BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
                finish();
            }
        };
        method = new Method(BookDetail.this, interstitialAdView);
        method.forceRTLIfSupported(getWindow());

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        toolbar = findViewById(R.id.toolbar_scd);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        lay_main = findViewById(R.id.main_content);
        textView_noData = findViewById(R.id.textView_noData_scd);
        imageView = findViewById(R.id.imageView_scd);
        imageView_CoverBook = findViewById(R.id.imageView_book_scd);
        imageViewRating = findViewById(R.id.imageView_rating_scd);
        buttonAllComment = findViewById(R.id.button_allComment_scd);
        buttonViewAll = findViewById(R.id.button_viewall_scd);
        webView = findViewById(R.id.webView_scd);
        textViewBookName = findViewById(R.id.textView_bookName_scd);
        textViewAuthor = findViewById(R.id.textView_authorName_scd);
        progressBar = findViewById(R.id.progresbar_scd);
        recyclerViewRelatedBook = findViewById(R.id.recyclerView_relatedBook_scd);
        recyclerViewComment = findViewById(R.id.recyclerView_comment_scd);
        linearLayoutRelatedBook = findViewById(R.id.linearLayout_relatedBook_scd);
        textViewNoBookFound = findViewById(R.id.textView_noBookFound_scd);
        textViewNoCommentFound = findViewById(R.id.textView_noComment_scd);
        textViewRating = findViewById(R.id.textView_ratingCount_scd);
        textView_view = findViewById(R.id.textView_view_scd);
        ratingBar = findViewById(R.id.ratingBar_scd);
        imageView_Send = findViewById(R.id.imageView_dialogBox_comment);
        editText = findViewById(R.id.editText_dialogbox_comment);
        image_favorite = findViewById(R.id.image_favorite_scd);

        lay_main.setVisibility(View.GONE);

        LinearLayout linearLayout = findViewById(R.id.linearLayout_scd);
        LinearLayout linearLayout_ad_scd = findViewById(R.id.linearLayout_adView_scd);

        lay_report = findViewById(R.id.lay_report_scd);

        TextView text_desc_title = findViewById(R.id.text_desc_title_scd);
        TextView text_cmt_title = findViewById(R.id.text_cmt_title_scd);
        TextView text_related_title = findViewById(R.id.text_related_title_scd);

        ImageView imageView_comment = findViewById(R.id.imageView_comment_scd);
        View view_related = findViewById(R.id.view_related_bg_scd);

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

        if (getResources().getString(R.string.isRTL).equals("true")) {
            view_related.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white_rtl));
        } else {
            view_related.setBackground(getResources().getDrawable(R.drawable.bg_gradient_white));
        }

        textViewBookName.setSelected(true);
        textViewBookName.setTypeface(Method.scriptable);
        text_desc_title.setTypeface(Method.scriptable);
        text_cmt_title.setTypeface(Method.scriptable);
        textViewNoCommentFound.setTypeface(Method.scriptable);
        text_related_title.setTypeface(Method.scriptable);

        if (Method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
            method.showPersonalizedAds(linearLayout_ad_scd);
        } else {
            method.showNonPersonalizedAds(linearLayout);
            method.showNonPersonalizedAds(linearLayout_ad_scd);
        }

        linearLayoutRelatedBook.setVisibility(View.VISIBLE);
        textViewNoBookFound.setVisibility(View.GONE);
        textViewNoCommentFound.setVisibility(View.GONE);

        recyclerViewRelatedBook.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerRelatedBook = new LinearLayoutManager(BookDetail.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewRelatedBook.setLayoutManager(layoutManagerRelatedBook);
        recyclerViewRelatedBook.setFocusable(false);
        recyclerViewRelatedBook.setNestedScrollingEnabled(false);

        recyclerViewComment.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BookDetail.this);
        recyclerViewComment.setLayoutManager(layoutManager);
        recyclerViewComment.setFocusable(false);
        recyclerViewComment.setNestedScrollingEnabled(false);

        lay_download = findViewById(R.id.lay_download);
        lay_favorite = findViewById(R.id.lay_favorite);
        lay_read = findViewById(R.id.lay_read);

        if (method.isNetworkAvailable()) {
            scd();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    @Subscribe
    public void getComment(Events.Comment comment) {
        if (bookId.equals(comment.getBook_id())) {
            scdLists.get(0).getCommentLists().add(0, new CommentList(comment.getUser_id(),
                    comment.getUser_name(), comment.getUser_image(), comment.getUser_name(),
                    comment.getComment_text(), comment.getComment_date()));
            if (commentAdapter != null) {
                commentAdapter.notifyDataSetChanged();
                String buttonTotal = getResources().getString(R.string.view_all) + " " + "(" + scdLists.get(0).getCommentLists().size() + ")";
                buttonAllComment.setText(buttonTotal);
            }
            if (scdLists.get(0).getCommentLists().size() != 0) {
                textViewNoCommentFound.setVisibility(View.GONE);
            }
        }
    }

    public void checkPer() {
        if ((ContextCompat.checkSelfPermission(BookDetail.this, "android.permission.WRITE_EXTERNAL_STORAGE" + "android.permission.WRITE_INTERNAL_STORAGE" + "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_INTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                Method.allowPermissionExternalStorage = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        boolean canUseExternalStorage = false;

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canUseExternalStorage = true;
                    Method.allowPermissionExternalStorage = true;
                }
                if (!canUseExternalStorage) {
                    Toast.makeText(BookDetail.this, getResources().getString(R.string.cannot_use_save_permission), Toast.LENGTH_SHORT).show();
                    Method.allowPermissionExternalStorage = false;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scd, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_share:
                if (Method.allowPermissionExternalStorage) {
                    if (scdLists.size() != 0) {
                        Method.share = true;
                        method.share_save(scdLists.get(0).getBook_cover_img(),
                                scdLists.get(0).getBook_title(),
                                scdLists.get(0).getAuthor_name(),
                                scdLists.get(0).getBook_description(),
                                scdLists.get(0).getBook_file_url());
                    }
                } else {
                    checkPer();
                }
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    // single book detail
    public void scd() {

        scdLists.clear();

        progressBar.setVisibility(View.VISIBLE);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("method_name", "get_single_book");
        params.put("data", API.toBase64(jsObj.toString()));
        client.post(Constant_Api.url, params, new AsyncHttpResponseHandler() {
            @SuppressLint({"SetTextI18n", "SetJavaScriptEnabled"})
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
                        String book_bg_img = object.getString("book_bg_img");
                        String book_file_type = object.getString("book_file_type");
                        String book_file_url = object.getString("book_file_url");
                        String total_rate = object.getString("total_rate");
                        String rate_avg = object.getString("rate_avg");
                        String book_views = object.getString("book_views");
                        String author_name = object.getString("author_name");

                        JSONArray jsonArrayRb = object.getJSONArray("related_books");
                        List<SubCategoryList> arrayListRb = new ArrayList<>();

                        if (jsonArrayRb.length() != 0) {

                            for (int j = 0; j < jsonArrayRb.length(); j++) {

                                JSONObject objectRb = jsonArrayRb.getJSONObject(j);
                                String idRb = objectRb.getString("id");
                                String book_titleRb = objectRb.getString("book_title");
                                String book_descriptionRb = objectRb.getString("book_description");
                                String book_cover_imgRb = objectRb.getString("book_cover_img");
                                String book_file_typeRb = objectRb.getString("book_file_type");
                                String total_rateRb = objectRb.getString("total_rate");
                                String rate_avgRb = objectRb.getString("rate_avg");
                                String book_viewsRb = objectRb.getString("book_views");
                                String author_nameRb = objectRb.getString("author_name");

                                arrayListRb.add(new SubCategoryList(idRb, "", book_titleRb, book_descriptionRb, book_cover_imgRb, "", book_file_typeRb, total_rateRb, rate_avgRb, book_viewsRb, "", author_nameRb, ""));

                            }
                        }

                        JSONArray jsonArrayComment = object.getJSONArray("user_comments");
                        List<CommentList> arrayList = new ArrayList<>();

                        if (jsonArrayComment.length() != 0) {

                            for (int j = 0; j < jsonArrayComment.length(); j++) {

                                JSONObject objectComment = jsonArrayComment.getJSONObject(j);

                                String user_id = objectComment.getString("user_id");
                                String book_id = objectComment.getString("post_id");
                                String user_name = objectComment.getString("user_name");
                                String comment_text = objectComment.getString("comment_text");
                                String user_profile = objectComment.getString("user_profile");
                                String comment_date = objectComment.getString("comment_date");

                                arrayList.add(new CommentList(user_id, user_name, user_profile, book_id, comment_text, comment_date));

                            }
                        }

                        scdLists.add(new ScdList(id, book_title, book_description, book_cover_img, book_bg_img, book_file_type, book_file_url, total_rate, rate_avg, book_views, author_name, arrayListRb, arrayList));

                    }

                    if (scdLists.size() != 0) {

                        lay_main.setVisibility(View.VISIBLE);
                        textView_noData.setVisibility(View.GONE);

                        int value = Integer.parseInt(scdLists.get(0).getBook_views());
                        value++;
                        scdLists.get(0).setBook_views(String.valueOf(value));

                        //update view count favourite
                        if (!db.checkId(bookId)) {
                            db.updateFavView(bookId, String.valueOf(value));
                        }
                        //update view count continue reading book
                        if (!db.checkId_ContinueBook(bookId)) {
                            db.updateContinueView(bookId, String.valueOf(value));
                        }

                        //check book favourite or not
                        if (db.checkId(bookId)) {
                            image_favorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav));
                        } else {
                            image_favorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav_hov));
                        }

                        toolbar.setTitle(scdLists.get(0).getBook_title());

                        Picasso.get().load(scdLists.get(0).getBook_cover_img())
                                .placeholder(R.drawable.placeholder_portable)
                                .into(imageView_CoverBook);

                        Picasso.get().load(scdLists.get(0).getBook_bg_img())
                                .placeholder(R.drawable.placeholder_portable)
                                .into(imageView);

                        WebSettings webSettings = webView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        webSettings.setPluginState(WebSettings.PluginState.ON);

                        webView.setBackgroundColor(Color.TRANSPARENT);
                        webView.setFocusableInTouchMode(false);
                        webView.setFocusable(false);
                        webView.getSettings().setDefaultTextEncodingName("UTF-8");
                        String mimeType = "text/html";
                        String encoding = "utf-8";
                        String htmlText = scdLists.get(0).getBook_description();

                        String text = "<html><head>"
                                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/Poppins-Medium_0.ttf\")}body{font-family: MyFont;color: #8b8b8b;line-height:1.6;font-size:13px}"
                                + "</style></head>"
                                + "<body>"
                                + htmlText
                                + "</body></html>";

                        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);

                        textViewBookName.setText(scdLists.get(0).getBook_title());
                        textViewAuthor.setText(getString(R.string.author_by) + " " + scdLists.get(0).getAuthor_name());
                        textViewRating.setText(scdLists.get(0).getTotal_rate());
                        textView_view.setText(Method.Format(Integer.parseInt(scdLists.get(0).getBook_views())));
                        ratingBar.setRating(Float.parseFloat(scdLists.get(0).getRate_avg()));

                        String buttonTotal = getResources().getString(R.string.view_all) + " " + "(" + scdLists.get(0).getCommentLists().size() + ")";
                        buttonAllComment.setText(buttonTotal);

                        //related book
                        if (scdLists.get(0).getSubCategoryLists().size() == 0) {
                            linearLayoutRelatedBook.setVisibility(View.GONE);
                            textViewNoBookFound.setVisibility(View.VISIBLE);
                        } else {
                            BookAdapterGV relatedBookAdapterGV = new BookAdapterGV(BookDetail.this, scdLists.get(0).getSubCategoryLists(), "related_scd", interstitialAdView);
                            recyclerViewRelatedBook.setAdapter(relatedBookAdapterGV);
                        }

                        //book comment
                        if (scdLists.get(0).getCommentLists().size() == 0) {
                            textViewNoCommentFound.setVisibility(View.VISIBLE);
                        }
                        commentAdapter = new CommentAdapter(BookDetail.this, scdLists.get(0).getCommentLists());
                        recyclerViewComment.setAdapter(commentAdapter);


                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Setting the intent for pdf reader
                                if (method.isNetworkAvailable()) {

                                    if (scdLists.get(0).getBook_file_url().contains(".epub")) {
                                        if (Method.allowPermissionExternalStorage) {
                                            DownloadEpub downloadEpub = new DownloadEpub(BookDetail.this);
                                            downloadEpub.pathEpub(scdLists.get(0).getBook_file_url(), scdLists.get(0).getId());
                                        } else {
                                            checkPer();
                                        }
                                    } else {
                                        startActivity(new Intent(BookDetail.this, PDFShow.class)
                                                .putExtra("id", scdLists.get(0).getId())
                                                .putExtra("link", scdLists.get(0).getBook_file_url())
                                                .putExtra("toolbarTitle", scdLists.get(0).getBook_title())
                                                .putExtra("type", "link"));
                                    }

                                } else {
                                    method.alertBox(getResources().getString(R.string.internet_connection));
                                }

                            }
                        });

                        lay_download.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (method.isNetworkAvailable()) {
                                    if (Method.allowPermissionExternalStorage) {
                                        if (Method.isDownload) {
                                            if (scdLists.get(0).getBook_file_url().contains(".epub")) {
                                                method.download(scdLists.get(0).getId(),
                                                        scdLists.get(0).getBook_title(),
                                                        scdLists.get(0).getBook_cover_img(),
                                                        scdLists.get(0).getAuthor_name(),
                                                        scdLists.get(0).getBook_file_url(), "epub");
                                            } else {
                                                method.download(scdLists.get(0).getId(),
                                                        scdLists.get(0).getBook_title(),
                                                        scdLists.get(0).getBook_cover_img(),
                                                        scdLists.get(0).getAuthor_name(),
                                                        scdLists.get(0).getBook_file_url(), "pdf");
                                            }
                                        } else {
                                            method.alertBox(getResources().getString(R.string.downloading_message));
                                        }

                                    } else {
                                        checkPer();
                                    }
                                } else {
                                    method.alertBox(getResources().getString(R.string.internet_connection));
                                }
                            }
                        });

                        lay_favorite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (db.checkId(scdLists.get(0).getId())) {
                                    method.addData(db, 0, scdLists);
                                    image_favorite.setImageResource(R.drawable.ic_fav_hov);
                                } else {
                                    db.deleteDetail(scdLists.get(0).getId());
                                    image_favorite.setImageResource(R.drawable.ic_fav);
                                    Toast.makeText(BookDetail.this, getResources().getString(R.string.remove_to_favourite), Toast.LENGTH_SHORT).show();
                                }
                                Events.Favourite favourite = new Events.Favourite("");
                                GlobalBus.getBus().post(favourite);
                            }
                        });

                        lay_read.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (method.isNetworkAvailable()) {
                                    if (scdLists.get(0).getBook_file_url().contains(".epub")) {
                                        if (Method.allowPermissionExternalStorage) {
                                            DownloadEpub downloadEpub = new DownloadEpub(BookDetail.this);
                                            downloadEpub.pathEpub(scdLists.get(0).getBook_file_url(), scdLists.get(0).getId());
                                        } else {
                                            checkPer();
                                        }
                                    } else {
                                        startActivity(new Intent(BookDetail.this, PDFShow.class)
                                                .putExtra("id", scdLists.get(0).getId())
                                                .putExtra("link", scdLists.get(0).getBook_file_url())
                                                .putExtra("toolbarTitle", scdLists.get(0).getBook_title())
                                                .putExtra("type", "link"));
                                    }

                                } else {
                                    method.alertBox(getResources().getString(R.string.internet_connection));
                                }
                            }
                        });

                        lay_report.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (method.pref.getBoolean(method.pref_login, false)) {
                                    Intent intent_report = new Intent(BookDetail.this, ReportBook.class);
                                    intent_report.putExtra("bookId", scdLists.get(0).getId());
                                    intent_report.putExtra("BookName", scdLists.get(0).getBook_title());
                                    intent_report.putExtra("BookAuthor", scdLists.get(0).getAuthor_name());
                                    intent_report.putExtra("BookImage", scdLists.get(0).getBook_cover_img());
                                    startActivity(intent_report);
                                } else {
                                    Method.loginBack = true;
                                    startActivity(new Intent(BookDetail.this, Login.class));
                                }
                            }
                        });

                        buttonAllComment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(BookDetail.this, AllComment.class)
                                        .putExtra("bookId", bookId)
                                        .putExtra("array", (Serializable) scdLists.get(0).getCommentLists()));
                            }
                        });

                        buttonViewAll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(BookDetail.this, RelatedBook.class)
                                        .putExtra("array", (Serializable) scdLists.get(0).getSubCategoryLists()));
                            }
                        });

                        imageViewRating.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (method.pref.getBoolean(method.pref_login, false)) {
                                    String userId = method.pref.getString(method.profileId, null);
                                    rating(userId, bookId);
                                } else {
                                    Method.loginBack = true;
                                    startActivity(new Intent(BookDetail.this, Login.class));
                                }
                            }
                        });

                        imageView_Send.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (method.pref.getBoolean(method.pref_login, false)) {

                                    editText.setError(null);
                                    String comment = editText.getText().toString();
                                    if (comment.isEmpty()) {
                                        editText.requestFocus();
                                        editText.setError(getResources().getString(R.string.please_enter_comment));
                                    } else {
                                        if (method.isNetworkAvailable()) {
                                            editText.clearFocus();
                                            editText.getText().clear();
                                            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                                            Comment(method.pref.getString(method.profileId, null), comment);
                                        } else {
                                            method.alertBox(getResources().getString(R.string.internet_connection));
                                        }

                                    }

                                } else {
                                    Method.loginBack = true;
                                    startActivity(new Intent(BookDetail.this, Login.class));
                                }
                            }
                        });

                    } else {
                        lay_main.setVisibility(View.GONE);
                        textView_noData.setVisibility(View.VISIBLE);
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

    public void rating(final String userId, final String bookId) {

        final Dialog dialog = new Dialog(BookDetail.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogbox_rating);
        dialog.getWindow().setLayout(ViewPager.LayoutParams.FILL_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        Button button = dialog.findViewById(R.id.button_dialogBox_rating);
        ImageView image_close = dialog.findViewById(R.id.image_close);
        final RatingView ratingBar = dialog.findViewById(R.id.ratingBar_dialogbox_comment);
        TextView text_rating_title = dialog.findViewById(R.id.text_rating_title);

        ratingBar.setRating(0);

        if (method.isNetworkAvailable()) {
            myRating(bookId, userId, ratingBar);
        }

        text_rating_title.setTypeface(Method.scriptable);
        ratingBar.setOnRatingChangedListener(new RatingView.OnRatingChangedListener() {
            @Override
            public void onRatingChange(float oldRating, float newRating) {
                rate = (int) newRating;
            }
        });

        image_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (method.isNetworkAvailable()) {
                    if (rate > 0) {
                        ratingSend(rate, userId, bookId);
                        dialog.dismiss();
                    } else {
                        method.alertBox(getResources().getString(R.string.rating_status));
                    }
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
            }
        });

        dialog.show();

    }

    //send user rating
    public void ratingSend(final int rate, String userId, final String bookId) {

        @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(BookDetail.this.getContentResolver(), Settings.Secure.ANDROID_ID);

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("post_id", bookId);
        jsObj.addProperty("device_id", deviceId);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("rate", rate);
        jsObj.addProperty("method_name", "book_rating");
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
                        String success = object.getString("success");
                        String msg = object.getString("msg");

                        if (success.equals("1")) {
                            String total_rate = object.getString("total_rate");
                            String rate_avg = object.getString("rate_avg");
                            scdLists.get(0).setRate_avg(String.valueOf(rate_avg));
                            scdLists.get(0).setTotal_rate(String.valueOf(total_rate));
                            ratingBar.setRating(Float.parseFloat(rate_avg));
                            textViewRating.setText(total_rate);

                            if (!db.checkId(bookId)) {
                                db.updateFavRate(bookId, total_rate, rate_avg);
                            }
                            if (!db.checkId_ContinueBook(bookId)) {
                                db.updateContinueRate(bookId, total_rate, rate_avg);
                            }

                        }

                        progressDialog.dismiss();

                        method.alertBox(msg);
                    }

                    progressDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.contact_msg));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressDialog.dismiss();
            }
        });
    }

    //get user rating
    private void myRating(String post_id, String user_id, final RatingView ratingView) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("method_name", "get_rating");
        jsObj.addProperty("post_id", post_id);
        jsObj.addProperty("user_id", user_id);
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
                        String user_rate = object.getString("user_rate");
                        String success = object.getString("success");

                        if (success.equals("1")) {
                            ratingView.setRating(Float.parseFloat(user_rate));
                        }

                    }
                    progressDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressDialog.dismiss();
            }
        });

    }

    //comment
    private void Comment(final String userId, final String comment) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("comment_text", comment);
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
                            Toast.makeText(BookDetail.this, msg, Toast.LENGTH_SHORT).show();
                            textViewNoCommentFound.setVisibility(View.GONE);
                            String userImage = method.pref.getString(method.userImage, null);
                            if (userImage == null) {
                                userImage = "";
                            }
                            scdLists.get(0).getCommentLists().add(0, new CommentList(method.pref.getString(method.profileId, null),
                                    method.pref.getString(method.userName, null),
                                    userImage,
                                    scdLists.get(0).getId(), comment, getResources().getString(R.string.today)));
                            commentAdapter.notifyDataSetChanged();
                            String buttonTotal = getResources().getString(R.string.view_all) + " " + "(" + scdLists.get(0).getCommentLists().size() + ")";
                            buttonAllComment.setText(buttonTotal);
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

    @Override
    protected void onDestroy() {
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }


}
