package com.example.dcl.androiduberapplication.Service;

import android.content.Intent;

import com.example.dcl.androiduberapplication.CustommerCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        LatLng customer_location=new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);
        Intent in=new Intent(getBaseContext(), CustommerCall.class);

        in.putExtra("lat",customer_location.latitude);
        in.putExtra("lng",customer_location.longitude);
        in.putExtra("customer",remoteMessage.getNotification().getTitle());
        in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
    }
}
