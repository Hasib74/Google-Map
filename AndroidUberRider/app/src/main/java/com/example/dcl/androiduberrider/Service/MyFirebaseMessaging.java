package com.example.dcl.androiduberrider.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.dcl.androiduberrider.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification().getTitle().equals("Cancel")){
            Handler handler=new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),""+remoteMessage.getNotification().getBody(),Toast.LENGTH_LONG).show();
                }
            });
        }else if (remoteMessage.getNotification().getTitle().equals("Arrived")){
            showArrivedNotification(remoteMessage.getNotification().getBody());
        }

    }

    private void showArrivedNotification(String body) {
        PendingIntent contentIntent=PendingIntent.getActivity(getBaseContext(),0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Notification.Builder builder=new Notification.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent)
                .build();

        NotificationManager manager=(NotificationManager)getBaseContext().getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());
    }

}
