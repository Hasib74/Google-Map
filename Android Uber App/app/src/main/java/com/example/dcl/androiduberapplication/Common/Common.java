package com.example.dcl.androiduberapplication.Common;

import android.location.Location;

import com.example.dcl.androiduberapplication.Model.API;
import com.example.dcl.androiduberapplication.Model.User;
import com.example.dcl.androiduberapplication.Remort.FCMClient;
import com.example.dcl.androiduberapplication.Remort.IFCMService;
import com.example.dcl.androiduberapplication.Remort.IGoogleAPI;
import com.example.dcl.androiduberapplication.Remort.RetrofitClient;

public class Common {
    public static String currentToken="";
    public static final String driver_tb1="Drivers";
    public static final String user_driver_tb1="Users";
    public static final String user_rider_tb1="Riders";
    public static final String pickup_request_tb1="PickUpRequest";
    public static final String token_tb1="Tokens";

    public static API currentApi;

    public static User currentUser;

    public static Location mLastLocation;
    public static  int IMAGE_PICK_UP=1000;

    public  static  final String baseUrl="https://maps.googleapis.com";
    public  static  final String fcmUrl="https://fcm.googleapis.com/";

    public   static  double base_fare=2.55;
    public   static  double time_rate=0.35;
    public   static double distance_rate=1.75;

    public   static  double getPrice(double km,Double min){

        return  (base_fare+(time_rate*min)+(distance_rate*km));
    }

    public static IGoogleAPI getGoogleApi(){
        return RetrofitClient.getClient(baseUrl).create(IGoogleAPI.class);
    }
    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmUrl).create(IFCMService.class);
    }


}
