package com.example.shwenyarmya.Util;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.shwenyarmya.R;

import java.io.File;
import java.io.IOException;

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

public class DownloadService extends Service {

    NotificationCompat.Builder myNotify;
    private String downloadUrl, file_path, file_name;
    RemoteViews rv;
    OkHttpClient client;
    public static final String ACTION_STOP = "com.mydownload.action.STOP";
    public static final String ACTION_START = "com.mydownload.action.START";
    private String NOTIFICATION_CHANNEL_ID = "download_ch_1";
    private static final String CANCEL_TAG = "c_tag";
    NotificationManager mNotificationManager;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            int progress = Integer.parseInt(message.obj.toString());
            switch (message.what) {
                case 1:
                    rv.setTextViewText(R.id.nf_title, getString(R.string.app_name));
                    rv.setProgressBar(R.id.progress, 100, progress, false);
                    rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.downloading) + " " + "(" + progress + " %)");
                    myNotify.setCustomContentView(rv);
                    startForeground(1002, myNotify.build());
                    break;
                case 2:
                    stopForeground(true);
                    stopSelf();
                    break;
            }
            return false;
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myNotify = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        myNotify.setChannelId(NOTIFICATION_CHANNEL_ID);
        myNotify.setSmallIcon(R.mipmap.ic_launcher);
        myNotify.setTicker(getResources().getString(R.string.downloading));
        myNotify.setWhen(System.currentTimeMillis());
        myNotify.setOnlyAlertOnce(true);

        rv = new RemoteViews(getPackageName(),
                R.layout.my_custom_notification);
        rv.setTextViewText(R.id.nf_title, getString(R.string.app_name));
        rv.setProgressBar(R.id.progress, 100, 0, false);
        rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.downloading) + " " + "(0%)");

        Intent closeIntent = new Intent(this, DownloadService.class);
        closeIntent.setAction(ACTION_STOP);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);
        // rv.setOnClickPendingIntent(R.id.nf_close, pcloseIntent);

        myNotify.setCustomContentView(rv);
        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Download";// The user-visible name of the channel.
            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(mChannel);
        }
        startForeground(1002, myNotify.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent.getAction() != null && intent.getAction().equals(ACTION_START)) {
            downloadUrl = intent.getStringExtra("downloadUrl");
            file_path = intent.getStringExtra("file_path");
            file_name = intent.getStringExtra("file_name");
            init();
        }
        if (intent.getAction() != null && intent.getAction().equals(ACTION_STOP)) {
            stopForeground(true);
            stopSelf();
            if (client != null) {
                for (Call call : client.dispatcher().runningCalls()) {
                    if (call.request().tag().equals(CANCEL_TAG))
                        call.cancel();
                }
            }
        }
        return START_STICKY;
    }


    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                client = new OkHttpClient();
                Request.Builder builder = new Request.Builder()
                        .url(downloadUrl)
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
                        Method.isDownload = true;
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
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.downloading), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                                Log.e("TAG", "=============start===============");
                                Log.e("TAG", "numBytes:" + numBytes);
                                Log.e("TAG", "totalBytes:" + totalBytes);
                                Log.e("TAG", "percent:" + percent);
                                Log.e("TAG", "speed:" + speed);
                                Log.e("TAG", "============= end ===============");
                                Message msg = mHandler.obtainMessage();
                                msg.what = 1;
                                msg.obj = (int) (100 * percent) + "";
                                mHandler.sendMessage(msg);
                            }

                            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressFinish() {
                                super.onUIProgressFinish();
                                Log.e("TAG", "onUIProgressFinish:");
                                Message msg = mHandler.obtainMessage();
                                msg.what = 2;
                                msg.obj = 0 + "";
                                mHandler.sendMessage(msg);
                                Method.isDownload = true;
                                // Toast.makeText(getApplicationContext(), "End download", Toast.LENGTH_SHORT).show();
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
        }).start();
    }
}
