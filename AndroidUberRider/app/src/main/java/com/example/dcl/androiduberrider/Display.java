package com.example.dcl.androiduberrider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.dcl.androiduberrider.Common.Common;
import com.example.dcl.androiduberrider.Helper.CustomInfoWindow;
import com.example.dcl.androiduberrider.Model.API;
import com.example.dcl.androiduberrider.Model.FCMResponse;
import com.example.dcl.androiduberrider.Model.Notification;
import com.example.dcl.androiduberrider.Model.Sender;
import com.example.dcl.androiduberrider.Model.Token;
import com.example.dcl.androiduberrider.Remort.IFCMService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Display extends FragmentActivity implements OnMapReadyCallback,NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private LocationRequest mLocationrequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private GeoFire geoFire;
    private Marker mCurrent,markerDestination,driverMarker;

    private FirebaseDatabase db;
    private DatabaseReference ref;
    private FirebaseAuth auth;
    DatabaseReference driver;
    boolean submitPressed;
    AutocompleteFilter typeFilter;



    private final int MY_PERMISSION_REQUEST_CODE=7000;
    private final int PLAY_SERVICE_RES_REQUEST=7001;
    private final int UPDATE_INTERVAL=5000;
    private final int FASTER_INTERVAL=5001;
    private final int DISPLACEMENT=10;

    String mPlaceLocation,mPlaceDestination;


    ImageView imgeExpandable;
    BottomSheetRiderFragment mBottomSheet;
    Button btnPickUpRequest;
    public static String API_KEY;
    public  static  DatabaseReference api_key_ref;

    boolean isDriverFound=false;
    String driverId="";
    int radius=1;
    int distance=3;
    IFCMService mServer;
    DatabaseReference driverAvilable;
    PlaceAutocompleteFragment place_location,place_destination;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ref=FirebaseDatabase.getInstance().getReference("Drivers");
        geoFire=new GeoFire(ref);
        auth=FirebaseAuth.getInstance();
        //mBottomSheet=new BottomSheetRiderFragment();

        //Toast.makeText(getApplicationContext(),"KEY  :="+Common.api.getApi_key(),Toast.LENGTH_LONG).show();

        setUpLocation();
        updateFirebaseToken();

        mServer=Common.getFCMService();
        typeFilter=new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        place_location=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_location);
        place_destination=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destination);

        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceLocation =place.getAddress().toString();
                mMap.clear();
                mCurrent=mMap.addMarker(new MarkerOptions()
                .position(place.getLatLng())
                .icon(BitmapDescriptorFactory.defaultMarker())
                .title("Pick Here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));
            }

            @Override
            public void onError(Status status) {

            }
        });
        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceDestination=place.getAddress().toString();
                mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                       );

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));


                Handler handler=new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BottomSheetRiderFragment mBottomFragment=BottomSheetRiderFragment.newInstance(mPlaceLocation,mPlaceDestination,false);
                        mBottomFragment.show(getSupportFragmentManager(),mBottomFragment.getTag());
                    }
                });




            }

            @Override
            public void onError(Status status) {

            }
        });

        imgeExpandable=(ImageView)findViewById(R.id.imgExpandable);
       // mBottomSheet=BottomSheetRiderFragment.newInstance("Rider bottom sheet");

       /* imgeExpandable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // mBottomSheet.show(getSupportFragmentManager(),mBottomSheet.getTag());

            }
        });*/

        btnPickUpRequest=(Button)findViewById(R.id.btnPickUpRequest);
        btnPickUpRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),""+isDriverFound,Toast.LENGTH_LONG).show();
                if (!isDriverFound){
                    requestPickUpHere(Common.rider.getEmail());
                }else {
                    sendRequestToDrivr(driverId);
                }

            }
        });


        api_key_ref=FirebaseDatabase.getInstance().getReference("API");

        api_key_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                API api=dataSnapshot.getValue(API.class);

                API_KEY=api.getApi_key();

                Common.api=api;
                Toast.makeText(getApplicationContext(),"KEY :="+API_KEY,Toast.LENGTH_LONG).show();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

      //  displayLocation();
}
    private void updateFirebaseToken() {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference(Common.token_tb1);
        Token token=new Token(FirebaseInstanceId.getInstance().getToken());


        tokens.child(Common.rider.getEmail())
                .setValue(token);

    }

    private void sendRequestToDrivr(String driverId) {
       DatabaseReference tokens =FirebaseDatabase.getInstance().getReference(Common.token_tb1);
       tokens.orderByKey().equalTo(driverId)
               .addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                       for (DataSnapshot postSnapShort:dataSnapshot.getChildren()){
                           Token token=postSnapShort.getValue(Token.class);

                           String json_lat_lng=new Gson().toJson(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                           Toast.makeText(getApplicationContext(),"json := "+json_lat_lng,Toast.LENGTH_LONG).show();
                           String riderToken=FirebaseInstanceId.getInstance().getToken();

                           Notification data=new Notification(riderToken,json_lat_lng);
                           Sender content=new Sender(token.getToken(),data);

                           mServer.sendMessage(content).enqueue(new Callback<FCMResponse>() {
                               @Override
                               public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                   if (response.body().success==1){
                                       Toast.makeText(getApplicationContext(),"Request Successfully",Toast.LENGTH_LONG).show();
                                   }else {
                                       Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();
                                   }
                               }

                               @Override
                               public void onFailure(Call<FCMResponse> call, Throwable t) {

                               }
                           });


                       }
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });
    }

    private void requestPickUpHere(String email) {
        DatabaseReference dbRequest=FirebaseDatabase.getInstance().getReference(Common.pickup_request_tb1);//"PickUpRequest"
        GeoFire mGeoFire=new GeoFire(dbRequest);
        mGeoFire.setLocation(email, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Toast.makeText(getApplicationContext(),"Successfully Add",Toast.LENGTH_LONG).show();
            }
        });

        if (mCurrent.isVisible())
            mCurrent.remove();

        mCurrent=mMap.addMarker(new MarkerOptions()
        .title("Pick Up Here")
        .snippet("")
        .position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        mCurrent.showInfoWindow();


        btnPickUpRequest.setText("Getting Your Driver...");

        findDriver();


    }

    private void findDriver() {
        DatabaseReference drivers=FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
        GeoFire mfire=new GeoFire(drivers);
        GeoQuery geoQuery=mfire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!isDriverFound){
                    isDriverFound=true;
                    driverId=key;
                    btnPickUpRequest.setText("Call to driver");
                    Toast.makeText(getApplicationContext(),""+key,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!isDriverFound && radius<3){
                    radius++;
                    findDriver();
                }else {
                    Toast.makeText(getApplicationContext(),"No driver is avilabe",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();

            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();

                    }
                }
        }

    }
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation!=null){

            LatLng center=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

            LatLng norhSide= SphericalUtil.computeOffset(center,100000,0);
            LatLng southSide= SphericalUtil.computeOffset(center,100000,180);

            LatLngBounds bounds=LatLngBounds.builder()
                               .include(norhSide)
                               .include(southSide)
                               .build();

            place_location.setBoundsBias(bounds);
            place_location.setFilter(typeFilter);

            place_destination.setBoundsBias(bounds);
            place_location.setFilter(typeFilter);


            final double latitude=mLastLocation.getLatitude();
            final double longtitude=mLastLocation.getLongitude();
            Toast.makeText(getApplicationContext(),""+latitude+","+longtitude,Toast.LENGTH_LONG).show();
            driverAvilable=FirebaseDatabase.getInstance().getReference(Common.driver_tb1);

            driverAvilable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    loadAvilableDriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

                    if (mCurrent!=null){
                        Toast.makeText(getApplicationContext(),"Sorry",Toast.LENGTH_LONG).show();
                        mCurrent.remove();
                    }
                        Toast.makeText(getApplicationContext(),""+latitude+""+longtitude,Toast.LENGTH_LONG).show();
                        mCurrent=mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude,longtitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location))

                                .title("You"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longtitude),15.0f));

                        loadAvilableDriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                    }

    }

    private void loadAvilableDriver(final LatLng location) {

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location)
                .title("You"));

        final DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
        GeoFire gf=new GeoFire(driverLocation);
        GeoQuery geoQuery=gf.queryAtLocation(new GeoLocation(location.latitude,location.longitude),distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
               FirebaseDatabase.getInstance().getReference(Common.user_rider_tb1)//User
                       .child(key)
                       .addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(DataSnapshot dataSnapshot) {


                              driverMarker= mMap.addMarker(new MarkerOptions()
                                       .position(new LatLng(location.latitude,location.longitude))
                                       .flat(true)
                                       .title(Common.rider.getName())
                                      // .snippet("Phone :"+rider.getPhone())
                                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                               );
                           }

                           @Override
                           public void onCancelled(DatabaseError databaseError) {

                           }
                       });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                 if (distance<=3){
                     distance++;
                     loadAvilableDriver(location);
                 }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
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
        if (requestCode!= ConnectionResult.SUCCESS){
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




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdates();

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationrequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        /*try{
            boolean isSuccess=googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this,R.raw.my_map_style)
            );
            if(!isSuccess){
                Log.e("ERROR","Map style load failed");
            }
        }catch (Exception e){

        }*/
        mMap=googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
           @Override
           public void onMapClick(final LatLng latLng) {
               if (markerDestination!=null){
                   markerDestination.remove();
               }
               markerDestination=mMap.addMarker(new MarkerOptions()
               .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                       .position(latLng)
                       .title("Destination")
               );
               mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));

               Handler handler=new Handler(Looper.getMainLooper());
               handler.post(new Runnable() {
                   @Override
                   public void run() {
                       BottomSheetRiderFragment mBottomFragment=BottomSheetRiderFragment.newInstance(String.format("%f,%f",mLastLocation.getLatitude(),mLastLocation.getLongitude()),String.format("%f,%f",latLng.latitude,latLng.longitude),true);
                       mBottomFragment.show(getSupportFragmentManager(),mBottomFragment.getTag());
                   }
               });


           }
       });

    }



}



