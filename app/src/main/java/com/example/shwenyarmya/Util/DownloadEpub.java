package com.example.shwenyarmya.Util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.example.shwenyarmya.DataBase.DatabaseHandler;
import com.example.shwenyarmya.R;
import com.folioreader.FolioReader;
import com.folioreader.model.HighLight;
import com.folioreader.model.ReadPosition;
import com.folioreader.model.ReadPositionImpl;
import com.folioreader.util.OnHighlightListener;
import com.folioreader.util.ReadPositionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class DownloadEpub {

    private Activity activity;
    private DatabaseHandler db;
    private ProgressDialog progressDialog;
    private OkHttpClient client;
    private static final String CANCEL_TAG = "c_tag";

    public DownloadEpub(Activity activity) {
        this.activity = activity;
        db = new DatabaseHandler(activity);
    }

    public void pathEpub(String path, String bookId) {
        // declare the dialog as a member field of your activity

        progressDialog = new ProgressDialog(activity);

        String file_path = activity.getExternalCacheDir().getAbsolutePath();
        String file_name = "file" + bookId + ".epub";
        File fileOpen = new File(file_path, file_name);

        if (fileOpen.exists()) {
            openBook(fileOpen.toString(), bookId);
        } else {
            client = new OkHttpClient();
            Request.Builder builder = new Request.Builder()
                    .url(path)
                    .addHeader("Accept-Encoding", "identity")
                    .get()
                    .tag(CANCEL_TAG);

            Call call = client.newCall(builder.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("TAG", "=============onFailure===============");
                    e.printStackTrace();
                    Log.d("error_downloading", e.toString());
                    progressDialog.dismiss();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.e("TAG", "=============onResponse===============");
                    Log.e("TAG", "request headers:" + response.request().headers());
                    Log.e("TAG", "response headers:" + response.headers());
                    assert response.body() != null;
                    ResponseBody responseBody = ProgressHelper.withProgress(response.body(), new ProgressUIListener() {

                        //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                        @Override
                        public void onUIProgressStart(long totalBytes) {
                            super.onUIProgressStart(totalBytes);
                            Log.e("TAG", "onUIProgressStart:" + totalBytes);

                            progressDialog.setMessage(activity.getResources().getString(R.string.please_wait));
                            progressDialog.setCancelable(false);
                            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getResources().getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    fileOpen.delete();
                                    dialog.dismiss();
                                    if (client != null) {
                                        for (Call call : client.dispatcher().runningCalls()) {
                                            if (call.request().tag().equals(CANCEL_TAG))
                                                call.cancel();
                                        }
                                    }
                                    Log.d("call_cancel", "call_cancel");
                                }
                            });
                            progressDialog.show();

                        }

                        @Override
                        public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                            Log.e("TAG", "=============start===============");
                            Log.e("TAG", "numBytes:" + numBytes);
                            Log.e("TAG", "totalBytes:" + totalBytes);
                            Log.e("TAG", "percent:" + percent);
                            Log.e("TAG", "speed:" + speed);
                            Log.e("TAG", "============= end ===============");
                        }

                        //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                        @Override
                        public void onUIProgressFinish() {
                            super.onUIProgressFinish();
                            Log.e("TAG", "onUIProgressFinish:");
                            progressDialog.dismiss();
                            openBook(fileOpen.toString(), bookId);
                        }
                    });

                    try {

                        BufferedSource source = responseBody.source();
                        File outFile = new File(file_path + "/" + file_name);
                        BufferedSink sink = Okio.buffer(Okio.sink(outFile));
                        source.readAll(sink);
                        sink.flush();
                        source.close();

                    } catch (Exception e) {
                        Log.d("show_data", e.toString());
                    }
                }
            });
        }

    }

    private void openBook(String path, String id) {

        final FolioReader folioReader = FolioReader.getInstance(activity);
        folioReader.setOnHighlightListener(new OnHighlightListener() {
            @Override
            public void onHighlight(HighLight highlight, HighLight.HighLightAction type) {

            }
        });

        if (db.checkIdEpub(id)) {
            folioReader.openBook(path);
        } else {

            try {
                String string = db.getEpub(id);
                JSONObject jsonObject = new JSONObject(string);
                String bookId = jsonObject.getString("bookId");
                String chapterId = jsonObject.getString("chapterId");
                String chapterHref = jsonObject.getString("chapterHref");
                int chapterIndex = Integer.parseInt(jsonObject.getString("chapterIndex"));
                boolean usingId = Boolean.parseBoolean(jsonObject.getString("usingId"));
                String value = jsonObject.getString("value");

                ReadPosition readPosition = new ReadPositionImpl(bookId, chapterId,
                        chapterHref, chapterIndex, usingId, value);
                folioReader.setReadPosition(readPosition);
                folioReader.openBook(path);

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("error_load", e.getMessage());
            }
        }

        folioReader.setReadPositionListener(new ReadPositionListener() {
            @Override
            public void saveReadPosition(ReadPosition readPosition) {
                Log.d("readPosition", readPosition.toJson());
                if (db.checkIdEpub(id)) {
                    db.addEpub(id, readPosition.toJson());
                } else {
                    db.updateEpub(id, readPosition.toJson());
                }
            }
        });

    }

}
