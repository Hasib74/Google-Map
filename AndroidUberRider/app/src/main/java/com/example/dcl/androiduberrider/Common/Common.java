package com.example.dcl.androiduberrider.Common;

import com.example.dcl.androiduberrider.Model.API;
import com.example.dcl.androiduberrider.Model.Rider;
import com.example.dcl.androiduberrider.Remort.FCMClient;
import com.example.dcl.androiduberrider.Remort.IFCMService;
import com.example.dcl.androiduberrider.Remort.IGoogleAPI;
import com.example.dcl.androiduberrider.Remort.RetrofitClient;

public class Common {
    public  static Rider rider;

    public static final String driver_tb1="Drivers";
    public static final String user_driver_tb1="Users";
    public static final String user_rider_tb1="Riders";
    public static final String pickup_request_tb1="PickUpRequest";
    public static final String token_tb1="Tokens";


    public static final String baseUrl="https://maps.googleapis.com";
    public  static  final String fcmURL="https://fcm.googleapis.com/";
    public static  API api;

    private  static double base_fare=2.55;
    private  static double time_rate=0.35;
    private  static double distance_rate=1.75;

    public   static  double getPrice(double km,int min){

        return  (base_fare+(time_rate*min)+(distance_rate*km));
    }

    /*public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseUrl).create(IGoogleAPI.class);
    }*/


    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }


    public static IGoogleAPI getGoogleServices(){
        return RetrofitClient.getClient(baseUrl).create(IGoogleAPI.class);
    }

}
