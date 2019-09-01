package com.example.shwenyarmya.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.shwenyarmya.DataBase.DatabaseHandler;
import com.example.shwenyarmya.InterFace.InterstitialAdView;
import com.example.shwenyarmya.Item.DownloadList;
import com.example.shwenyarmya.Item.ScdList;
import com.example.shwenyarmya.Item.SubCategoryList;
import com.example.shwenyarmya.R;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import androidx.appcompat.app.AlertDialog;

public class Method {

    public Activity activity;
    public static boolean share = false, allowPermissionExternalStorage = false, loginBack = false, personalization_ad = false, isDownload = true;
    public static Bitmap mbitmap;
    private static File file;
    private String filename;
    private DatabaseHandler db;

    public static Typeface scriptable;

    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    private final String myPreference = "login";
    public String pref_login = "pref_login";
    private String firstTime = "firstTime";
    public String profileId = "profileId";
    public String userEmail = "userEmail";
    public String userPassword = "userPassword";
    public String userName = "userName";
    public String userImage = "userImage";
    public String show_login = "show_login";

    String storageFile;
    private InterstitialAdView interstitialAdView;

    public Method(Activity activity) {
        this.activity = activity;
        scriptable = Typeface.createFromAsset(activity.getAssets(), "fonts/Poppins-Medium_0.ttf");
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
        db = new DatabaseHandler(activity);
    }

    public Method(Activity activity, InterstitialAdView interstitialAdView) {
        this.activity = activity;
        this.interstitialAdView = interstitialAdView;
        scriptable = Typeface.createFromAsset(activity.getAssets(), "fonts/Poppins-Medium_0.ttf");
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
        db = new DatabaseHandler(activity);
    }

    public void login() {
        if (!pref.getBoolean(firstTime, false)) {
            editor.putBoolean(pref_login, false);
            editor.putBoolean(firstTime, true);
            editor.commit();
        }
    }

    //rtl
    public void forceRTLIfSupported(Window window) {
        if (activity.getResources().getString(R.string.isRTL).equals("true")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }
    }

    //network check
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //Database add to favourite
    public void addData(DatabaseHandler db, int position, List<ScdList> scdLists) {
        db.addDetail(new SubCategoryList(scdLists.get(position).getId(), "",
                scdLists.get(position).getBook_title(),
                scdLists.get(position).getBook_description(),
                scdLists.get(position).getBook_cover_img(),
                scdLists.get(position).getBook_bg_img(),
                scdLists.get(position).getBook_file_type(),
                scdLists.get(position).getTotal_rate(),
                scdLists.get(position).getRate_avg(),
                scdLists.get(position).getBook_views(), "",
                scdLists.get(position).getAuthor_name(), ""));
        Toast.makeText(activity, activity.getResources().getString(R.string.add_to_favourite), Toast.LENGTH_SHORT).show();
    }

    //Database add to continue
    public void addContinue(DatabaseHandler db, int position, List<SubCategoryList> subCategoryLists) {
        if (!db.checkId_ContinueBook(subCategoryLists.get(position).getId())) {
            db.deleteContinue_Book(subCategoryLists.get(position).getId());
        }
        db.addContinueBook(new SubCategoryList(subCategoryLists.get(position).getId(), "",
                subCategoryLists.get(position).getBook_title(),
                subCategoryLists.get(position).getBook_description(),
                subCategoryLists.get(position).getBook_cover_img(),
                subCategoryLists.get(position).getBook_bg_img(),
                subCategoryLists.get(position).getBook_file_type(),
                subCategoryLists.get(position).getTotal_rate(),
                subCategoryLists.get(position).getRate_avg(),
                subCategoryLists.get(position).getBook_views(), "",
                subCategoryLists.get(position).getAuthor_name(), ""));
    }


    //get screen width
    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) activity
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        columnWidth = point.x;
        return columnWidth;
    }

    /*<---------------------- share & download and method ---------------------->*/

    public void share_save(String image, String bookName, String bookAuthor, String description, String bookLink) {
        new DownloadImageTask().execute(image, bookName, bookAuthor, description, bookLink);
    }

    @SuppressLint("StaticFieldLeak")
    public class DownloadImageTask extends AsyncTask<String, String, String> {

        String bookName, bookAuthor, bookDescription, bookLink;

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                bookName = params[1];
                bookAuthor = params[2];
                bookDescription = params[3];
                bookLink = params[4];
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                mbitmap = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                // Log exception
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (share) {
                ShareImage(mbitmap, bookName, bookAuthor, bookDescription, bookLink);
                share = false;
            }
            super.onPostExecute(s);
        }
    }

    @SuppressLint("IntentReset")
    private void ShareImage(Bitmap finalBitmap, String bookName, String bookAuthor, String description, String bookLink) {

        String root = activity.getExternalCacheDir().getAbsolutePath();
        String fname = "Image_share" + ".jpg";
        file = new File(root, fname);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri contentUri = Uri.fromFile(file);
        Log.d("url", String.valueOf(contentUri));

        if (contentUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setData(contentUri);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(bookName)
                    + "\n" + "\n" + Html.fromHtml(bookAuthor)
                    + "\n" + "\n" + Html.fromHtml(description)
                    + "\n" + "\n" + "http://play.google.com/store/apps/details?id=" + activity.getPackageName());
            activity.startActivity(Intent.createChooser(shareIntent, "Choose an app"));
        }
    }

    /*<---------------------- share & download and method ---------------------->*/

    /*<---------------------- download book ---------------------->*/

    public void download(String id, String bookName, String bookImage, String bookAuthor, String bookUrl, String type) {

        if (type.equals("epub")) {
            storageFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AndroidEBook/" + "filename-" + id + ".epub";

        } else {
            storageFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AndroidEBook/" + "filename-" + id + ".pdf";

        }
        File file = new File(storageFile);
        if (!file.exists()) {

            Method.isDownload = false;
            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AndroidEBook/");
            if (!root.exists()) {
                root.mkdirs();
            }
            if (type.equals("epub")) {
                filename = "filename-" + id + ".epub";
            } else {
                filename = "filename-" + id + ".pdf";
            }
            Intent serviceIntent = new Intent(activity, DownloadService.class);
            serviceIntent.setAction(DownloadService.ACTION_START);
            serviceIntent.putExtra("downloadUrl", bookUrl);
            serviceIntent.putExtra("file_path", root.toString());
            serviceIntent.putExtra("file_name", filename);
            activity.startService(serviceIntent);
        } else {
            if (type.equals("epub")) {
                filename = "filename-" + id + ".epub";
            } else {
                filename = "filename-" + id + ".pdf";
            }

            Toast.makeText(activity, activity.getResources().getString(R.string.you_have_allready_download_book), Toast.LENGTH_SHORT).show();
        }

        new DownloadImage().execute(bookImage, id, bookName, bookAuthor, type);

    }

    @SuppressLint("StaticFieldLeak")
    public class DownloadImage extends AsyncTask<String, String, String> {

        private String id, bookName, bookAuthor, type;
        Bitmap bitmapDownload;

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                id = params[1];
                bookName = params[2];
                bookAuthor = params[3];
                type = params[4];
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmapDownload = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                // Log exception
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            downloadImage(bitmapDownload, id, bookName, bookAuthor, type);

            super.onPostExecute(s);
        }

    }

    private void downloadImage(Bitmap bitmap, String id, String bookName, String bookAuthor, String type) {

        String filePath = null;

        String iconsStoragePath = Environment.getExternalStorageDirectory() + "/AndroidEBook/";
        File sdIconStorageDir = new File(iconsStoragePath);

        //create storage directories, if they don't exist
        sdIconStorageDir.mkdirs();

        try {
            String fname = "Image-" + id;
            filePath = sdIconStorageDir.toString() + "/" + fname + ".jpg";
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);

            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

            //choose another format if PNG doesn't suit you
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
        } catch (IOException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
        }
        if (db.checkIdDownloadBook(id)) {
            db.addDownload(new DownloadList(id, bookName, filePath, bookAuthor, iconsStoragePath + filename));
        }

    }

    /*<---------------------- download book ---------------------->*/

    //---------------Interstitial Ad---------------//

    public void interstitialAdShow(final int position, final String type, final String id, final String title, final String fileType, final String fileUrl) {

        if (Constant_Api.aboutUsList != null) {
            if (Constant_Api.aboutUsList.isInterstital_ad()) {
                Constant_Api.AD_COUNT = Constant_Api.AD_COUNT + 1;
                if (Constant_Api.AD_COUNT == Constant_Api.AD_COUNT_SHOW) {
                    Constant_Api.AD_COUNT = 0;
                    final InterstitialAd interstitialAd = new InterstitialAd(activity);
                    AdRequest adRequest;
                    if (personalization_ad) {
                        adRequest = new AdRequest.Builder()
                                .build();
                    } else {
                        Bundle extras = new Bundle();
                        extras.putString("npa", "1");
                        adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                .build();
                    }
                    interstitialAd.setAdUnitId(Constant_Api.aboutUsList.getInterstital_ad_id());
                    interstitialAd.loadAd(adRequest);
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            interstitialAd.show();
                        }

                        public void onAdClosed() {
                            interstitialAdView.position(position, type, id, title, fileType, fileUrl);
                            super.onAdClosed();
                        }

                        @Override
                        public void onAdFailedToLoad(int i) {
                            Log.d("admob_error", String.valueOf(i));
                            interstitialAdView.position(position, type, id, title, fileType, fileUrl);
                            super.onAdFailedToLoad(i);
                        }

                    });
                } else {
                    interstitialAdView.position(position, type, id, title, fileType, fileUrl);
                }
            } else {
                interstitialAdView.position(position, type, id, title, fileType, fileUrl);
            }
        } else {
            interstitialAdView.position(position, type, id, title, fileType, fileUrl);
        }
    }

    //---------------Interstitial Ad---------------//

    //---------------Banner Ad---------------//

    public void showNonPersonalizedAds(LinearLayout linearLayout) {

        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        if (Constant_Api.aboutUsList != null) {
            if (Constant_Api.aboutUsList.isBanner_ad()) {
                AdView adView = new AdView(activity);
                AdRequest adRequest = new AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                        .build();
                adView.setAdUnitId(Constant_Api.aboutUsList.getBanner_ad_id());
                adView.setAdSize(AdSize.BANNER);
                linearLayout.addView(adView);
                adView.loadAd(adRequest);
            } else {
                linearLayout.setVisibility(View.GONE);
            }
        } else {
            linearLayout.setVisibility(View.GONE);
        }
    }

    public void showPersonalizedAds(LinearLayout linearLayout) {

        if (Constant_Api.aboutUsList != null) {
            if (Constant_Api.aboutUsList.isBanner_ad()) {
                AdView adView = new AdView(activity);
                AdRequest adRequest = new AdRequest.Builder()
                        .build();
                adView.setAdUnitId(Constant_Api.aboutUsList.getBanner_ad_id());
                adView.setAdSize(AdSize.BANNER);
                linearLayout.addView(adView);
                adView.loadAd(adRequest);
            } else {
                linearLayout.setVisibility(View.GONE);
            }
        } else {
            linearLayout.setVisibility(View.GONE);
        }
    }

    //---------------Banner Ad---------------//


    //alert message box
    public void alertBox(String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(activity.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    // view format
    public static String Format(Integer number) {
        String[] suffix = new String[]{"k", "m", "b", "t"};
        int size = (number.intValue() != 0) ? (int) Math.log10(number) : 0;
        if (size >= 3) {
            while (size % 3 != 0) {
                size = size - 1;
            }
        }
        double notation = Math.pow(10, size);
        String result = (size >= 3) ? +(Math.round((number / notation) * 100) / 100.0d) + suffix[(size / 3) - 1] : +number + "";
        return result;
    }

}
