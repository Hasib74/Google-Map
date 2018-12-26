package com.example.dcl.androiduberapplication;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.example.dcl.androiduberapplication.Common.Common;
import com.example.dcl.androiduberapplication.Helper.DirectionJSONParser;
import com.example.dcl.androiduberapplication.Model.FCMResponse;
import com.example.dcl.androiduberapplication.Model.Notification;
import com.example.dcl.androiduberapplication.Model.Sender;
import com.example.dcl.androiduberapplication.Model.Token;
import com.example.dcl.androiduberapplication.Remort.IFCMService;
import com.example.dcl.androiduberapplication.Remort.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.dcl.androiduberapplication.Common.Common.mLastLocation;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback ,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private GoogleMap mMap;

    Marker driverMarker;
    private Polyline direction;
    IGoogleAPI mService;

    IFCMService FCmServices;
    GeoFire geoFire;


    Double riderLat,riderLng;
    private final int MY_PERMISSION_REQUEST_CODE=7000;
    private final int PLAY_SERVICE_RES_REQUEST=7001;
    private final int UPDATE_INTERVAL=5000;
    private final int FASTER_INTERVAL=5001;
    private final int DISPLACEMENT=10;

    private LocationRequest mLocationrequest;
    private GoogleApiClient mGoogleApiClient;

    private Circle riderMarket;
    String customerId;

    Button btnStartTrip;

    Location pickUpLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        FCmServices=Common.getFCMService();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        riderLat=getIntent().getDoubleExtra("lat",-1.0);
        riderLng=getIntent().getDoubleExtra("lng",-1.0);

        setUpLoaction();

        Toast.makeText(getApplicationContext(),"Locatio :="+riderLat+","+riderLng,Toast.LENGTH_LONG).show();
        customerId=getIntent().getStringExtra("customer");
        mService= Common.getGoogleApi();

        btnStartTrip=findViewById(R.id.btnStartTrip);
        btnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnStartTrip.getText().toString().equals("START TRIP")){
                    pickUpLocation=Common.mLastLocation;
                    Toast.makeText(getApplicationContext(),""+pickUpLocation,Toast.LENGTH_LONG).show();

                    btnStartTrip.setText("DROP OFF HERE");
                }else if (btnStartTrip.getText().toString().equals("DROP OFF HERE")){
                    calculateCashFree(pickUpLocation,Common.mLastLocation);
                }
            }
        });

    }

    private void calculateCashFree(final Location pickUpLocation, Location mLastLocation) {




        String requestApi=null;

        try {
            /*requestApi="https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin"+pickUpLocation.getLatitude()+","+pickUpLocation.getLongitude()+"&"+
                    "destination="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&"+
                    "key="+Common.currentApi.getApi_key();*/
            requestApi="https://maps.googleapis.com/maps/api/directions/json?origin="+pickUpLocation.getLatitude()+","+pickUpLocation.getLongitude()+"&destination="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&key="+Common.currentApi.getApi_key();



            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {

                      /*  new ParserTask().execute(response.body().toString());*/

                      JSONObject jsonObject=new JSONObject(response.body().toString());
                      Toast.makeText(getApplicationContext(),""+jsonObject,Toast.LENGTH_LONG).show();
                      JSONArray routes=jsonObject.getJSONArray("routes");
                      JSONObject object=routes.getJSONObject(0);
                      JSONArray legs=object.getJSONArray("legs");
                      JSONObject legsObject=legs.getJSONObject(0);
                      JSONObject distance=legsObject.getJSONObject("distance");
                      String distance_text=distance.getString("text");
                      Double distance_value=Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));

                        JSONObject time=legsObject.getJSONObject("duration");
                        String time_text=time.getString("text");
                        Double time_value=Double.parseDouble(time_text.replaceAll("[^0-9\\\\.]+",""));

                        Intent in =new Intent(DriverTracking.this,TripDetail.class);
                        in.putExtra("start_address",legsObject.getString("start_address"));
                        in.putExtra("end_address",legsObject.getString("end_address"));
                        in.putExtra("time",String.valueOf(time_value));
                        in.putExtra("distance",String.valueOf(distance_value));
                        in.putExtra("total",Common.getPrice(distance_value,time_value));
                        in.putExtra("location_start",String.format("%f,%f",pickUpLocation.getLatitude(),pickUpLocation.getLongitude()));
                        in.putExtra("location_end",String.format("%f,%f",Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));

                        startActivity(in);
                        finish();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }catch (Exception e){

        }




    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        riderMarket=mMap.addCircle(new CircleOptions()
        .center(new LatLng(riderLat,riderLng))
        .radius(10)
        .strokeColor(Color.BLUE)
        .fillColor(0x220000FF)
        .strokeWidth(5.0f));

        geoFire=new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_tb1));
        GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(riderLat,riderLng),0.1f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendArrivedNotification(customerId);
                btnStartTrip.setEnabled(true);
               // btnStartTrip.setText("DROP OFF ");
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
   public void  sendArrivedNotification(String customerId){
       Token token=new Token(customerId);
       Notification notification=new Notification("Arrived",String.format("This driver %s has arrived at your",Common.currentUser.getName()));
       Sender sender=new Sender(token.getToken(),notification);

       FCmServices.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
           @Override
           public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
               if (response.body().success!=1){
                   Toast.makeText(DriverTracking.this,"Failed",Toast.LENGTH_LONG).show();
               }
           }

           @Override
           public void onFailure(Call<FCMResponse> call, Throwable t) {

           }
       });

    }
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        mLastLocation= LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation!=null){

                final double latitude=mLastLocation.getLatitude();
                final double longtitude=mLastLocation.getLongitude();

                if (driverMarker!=null){
                    driverMarker.remove();
                }

                driverMarker=mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude,longtitude))
                         .title("You")
                         .icon(BitmapDescriptorFactory.defaultMarker()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longtitude),17.0f));


                if (direction != null){
                    direction.remove();
                }

            getDireaction();

        }


    }

    private void getDireaction() {
       LatLng currentPosition=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

        String requestApi=null;

        try {
          /*  requestApi="https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin"+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+riderLat+","+riderLng+"&"+
                    "key="+Common.currentApi.getApi_key();*/
            requestApi="https://maps.googleapis.com/maps/api/directions/json?origin="+currentPosition.latitude+","+currentPosition.longitude+"&destination="+riderLat+","+riderLng+"&key="+Common.currentApi.getApi_key();



            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    Log.d("VALUE",response.body().toString());
                    try {

                        new ParserTask().execute(response.body().toString());


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }catch (Exception e){

        }
    }

    private void setUpLoaction() {

            if (checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();

                displayLocation();


        }
    }

    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        mLocationrequest=new LocationRequest();
        mLocationrequest.setInterval(UPDATE_INTERVAL);
        mLocationrequest.setFastestInterval(FASTER_INTERVAL);
        mLocationrequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationrequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int requestCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (requestCode!=ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(requestCode)){
                GooglePlayServicesUtil.getErrorDialog(requestCode,this,PLAY_SERVICE_RES_REQUEST).show();
            }else {
                Toast.makeText(this,"This Device dose not supported",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void startLocationUpdate() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationrequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation=location;
        displayLocation();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
     //ProgressDialog mDialog=new ProgressDialog(getApplicationContext());
    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        /*    mDialog.setMessage("Please Wait...");
            mDialog.show();
*/
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            List<List<HashMap<String, String>>> routes=null;
            JSONObject jsonObject;
            try{
                 jsonObject=new JSONObject(strings[0]);
                 DirectionJSONParser parser=new DirectionJSONParser();
                 routes=parser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
           // super.onPostExecute(lists);
         //  mDialog.dismiss();

            ArrayList<LatLng> points = new ArrayList<LatLng>();;
            PolylineOptions polylineOptions = new PolylineOptions();;

           /* ArrayList points=null;
            PolylineOptions polylineOptions=null;*/

            for (int i=0;i<lists.size();i++){
             points=new ArrayList();
             polylineOptions=new PolylineOptions();

             List<HashMap<String,String>> paths=lists.get(i);

             for (int j=0;j<paths.size();j++){
                 HashMap<String,String> point=paths.get(j);

                 double lat=Double.parseDouble(point.get("lat"));
                 double lng=Double.parseDouble(point.get("lng"));

                 LatLng position=new LatLng(lat,lng);

                 points.add(position);
             }

             polylineOptions.addAll(points);
             polylineOptions.width(10);
             polylineOptions.color(Color.RED);

             polylineOptions.geodesic(true);
            }
            if (direction!=null){
                direction=mMap.addPolyline(polylineOptions);
            }

        }
    }


}
