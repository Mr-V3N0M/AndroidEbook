package com.example.shwenyarmya.Util;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;
import com.example.shwenyarmya.Activity.SplashScreen;
import com.example.shwenyarmya.R;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.core.app.NotificationCompat;

public class NotificationExtenderExample extends NotificationExtenderService {

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    String message, bigpicture, title, bookid, url;
    private String NOTIFICATION_CHANNEL_ID = "android_e_book";

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {

        title = receivedResult.payload.title;
        message = receivedResult.payload.body;
        bigpicture = receivedResult.payload.bigPicture;

        Log.d("notificationData", receivedResult.toString());

        try {
            Log.d("android_bookid", receivedResult.payload.additionalData.getString("book_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            bookid = receivedResult.payload.additionalData.getString("book_id");
            url = receivedResult.payload.additionalData.getString("external_link");
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendNotification();
        return true;
    }

    @SuppressLint("WrongConstant")
    private void sendNotification() {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent;
        if (bookid.equals("0") && !url.equals("false") && !url.trim().isEmpty()) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
        } else {
            intent = new Intent(this, SplashScreen.class);
            intent.putExtra("bookid", bookid);
        }

        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Android E Book";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSound(uri)
                .setAutoCancel(true)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setLights(Color.RED, 800, 800);

        mBuilder.setSmallIcon(getNotificationIcon(mBuilder));

        mBuilder.setContentTitle(title);
        mBuilder.setTicker(message);

        if (bigpicture != null) {
            mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(bigpicture)).setSummaryText(message));
        } else {
            //mBuilder.setContentText(message);
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(getColour());
            return R.drawable.ic_stat_onesignal_default;
        } else {
            return R.mipmap.ic_launcher;
        }
    }

    private int getColour() {
        return 0x3F51B5;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
}