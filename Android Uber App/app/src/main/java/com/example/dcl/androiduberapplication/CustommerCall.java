package com.example.dcl.androiduberapplication;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dcl.androiduberapplication.Common.Common;
import com.example.dcl.androiduberapplication.Model.FCMResponse;
import com.example.dcl.androiduberapplication.Model.Notification;
import com.example.dcl.androiduberapplication.Model.Sender;
import com.example.dcl.androiduberapplication.Model.Token;
import com.example.dcl.androiduberapplication.Remort.IFCMService;
import com.example.dcl.androiduberapplication.Remort.IGoogleAPI;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustommerCall extends AppCompatActivity {


    TextView txtTime,txtAddress,txtDistance;
    MediaPlayer mediaPlayer;

    IGoogleAPI mService;

    Button btnAccept,btnDecline;

    String customerId;
    IFCMService FCMService;

    Double lat,lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custommer_call);

        txtAddress=(TextView)findViewById(R.id.txtDaddress);
        txtDistance=(TextView)findViewById(R.id.txtDistance);
        txtTime=(TextView)findViewById(R.id.txtTime);
        btnAccept=findViewById(R.id.btnAccept);
        btnDecline=findViewById(R.id.btnCancel);

        mediaPlayer=MediaPlayer.create(this,R.raw.love);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        mService=Common.getGoogleApi();
        FCMService=Common.getFCMService();

        if (getIntent()!=null){
            lat=getIntent().getDoubleExtra("lat",-1.0);
            lng=getIntent().getDoubleExtra("lng",-1.0);
            customerId=getIntent().getStringExtra("customer");
            getDireaction(lat,lng);
        }

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent  intent =new Intent(CustommerCall.this,DriverTracking.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("customer",customerId);



                startActivity(intent);

                finish();
            }
        });
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(customerId)){
                    cancelBooking(customerId);
                }
            }
        });

    }

    private void cancelBooking(String customerId) {
        Token token=new Token(customerId);
        Notification notification=new Notification("Cancel","Driver has cancelled your Request");
        Sender sender=new Sender(token.getToken(),notification);
        FCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success==1){
                    Toast.makeText(getApplicationContext(),"Successfully send message",Toast.LENGTH_LONG).show();

                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void getDireaction(double lat,double lng) {

        String requestApi=null;

        try {
           /* requestApi="https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin"+ Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+Common.currentApi.getApi_key();*/

            requestApi="https://maps.googleapis.com/maps/api/directions/json?origin="+Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&destination="+lat+","+lng+"&key="+Common.currentApi.getApi_key();



            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {

                        //Toast.makeText(getApplicationContext(),""+response.body().toString(),Toast.LENGTH_LONG).show();
                       JSONObject jsonObject=new JSONObject(response.body().toString());

                       JSONArray routes=jsonObject.getJSONArray("routes");
                       JSONObject object=routes.getJSONObject(0);
                       JSONArray legs=object.getJSONArray("legs");

                       JSONObject legsObject=legs.getJSONObject(0);

                       JSONObject distance=legsObject.getJSONObject("distance");
                       txtDistance.setText(distance.getString("text"));

                        JSONObject time=legsObject.getJSONObject("duration");
                        txtDistance.setText(time.getString("text"));

                        String address=legsObject.getString("end_address");
                        txtDistance.setText(address);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                 Toast.makeText(getApplicationContext(),"Error "+t.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

}
