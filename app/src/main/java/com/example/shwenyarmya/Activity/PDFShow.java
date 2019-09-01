package com.example.shwenyarmya.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;
import com.example.shwenyarmya.DataBase.DatabaseHandler;
import com.example.shwenyarmya.R;
import com.example.shwenyarmya.Util.Method;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class PDFShow extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    private Method method;
    private PDFView pdfView;
    private DatabaseHandler db;
    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;
    private String id, uri, type;
    private InputStream is;
    public Toolbar toolbar;
    private ProgressDialog progressDialog;

    private static final String TAG = PDFShow.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);

        method = new Method(PDFShow.this);
        method.forceRTLIfSupported(getWindow());

        db = new DatabaseHandler(PDFShow.this);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        uri = intent.getStringExtra("link");
        String toolbarTitle = intent.getStringExtra("toolbarTitle");
        type = intent.getStringExtra("type");

        progressDialog = new ProgressDialog(PDFShow.this);

        toolbar = findViewById(R.id.toolbar_pdfView);
        toolbar.setTitle(toolbarTitle);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LinearLayout linearLayout = findViewById(R.id.linearLayout_pdfView);
        if (Method.personalization_ad) {
            method.showPersonalizedAds(linearLayout);
        } else {
            method.showNonPersonalizedAds(linearLayout);
        }

        pdfView = findViewById(R.id.pdfView_activity);
        pdfView.setBackgroundColor(Color.LTGRAY);

        if (type.equals("link")) {
            if (!db.checkIdPdfBook(id)) {
                new PdfAsyncTask().execute(uri, db.getPdf(id), String.valueOf(true));
            } else {
                new PdfAsyncTask().execute(uri, String.valueOf(0), String.valueOf(true));
                db.addPdf(id, String.valueOf(0));
            }
        } else {
            File file = new File(uri);
            if (!db.checkIdPdfBook(id)) {
                displayFromFile(file, Integer.parseInt(db.getPdf(id)), true);
            } else {
                displayFromFile(file, 0, false);
                db.addPdf(id, String.valueOf(0));
            }
        }

    }

    public void displayFromUri(InputStream inputStream, int pagePosition, boolean isRead) {
        pdfView.fromStream(inputStream)
                .defaultPage(pagePosition)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
    }

    public void displayFromFile(File file, int pagePosition, boolean isRead) {
        pdfView.fromFile(file)
                .defaultPage(pagePosition)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        db.updatePdf(id, String.valueOf(page));
    }

    @Override
    public void loadComplete(int nbPages) {
        progressDialog.dismiss();
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());
    }

    @Override
    public void onPageError(int page, Throwable t) {
        progressDialog.dismiss();
        Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPicker();
            }
        }
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, "error_launchPicker", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class PdfAsyncTask extends AsyncTask<String, String, String> {

        boolean isRead;
        String pagePosition;

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                URL myURL = new URL(strings[0]);
                pagePosition = strings[1];
                isRead = Boolean.parseBoolean(strings[2]);
                HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                Log.d("uri", uri);

            } catch (Exception e) {
                Log.d("toast", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            displayFromUri(is, Integer.parseInt(pagePosition), isRead);
            super.onPostExecute(s);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
