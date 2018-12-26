package com.example.dcl.androiduberapplication;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.dcl.androiduberapplication.Common.Common;
import com.example.dcl.androiduberapplication.Model.API;
import com.example.dcl.androiduberapplication.Model.Token;
import com.example.dcl.androiduberapplication.Remort.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.dcl.androiduberapplication.Common.Common.mLastLocation;

public class Welcome extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{
    private GoogleMap mMap;

    private final int MY_PERMISSION_REQUEST_CODE=7000;
    private final int PLAY_SERVICE_RES_REQUEST=7001;
    private final int UPDATE_INTERVAL=5000;
    private final int FASTER_INTERVAL=5001;
    private final int DISPLACEMENT=10;

    private LocationRequest mLocationrequest;
    private GoogleApiClient mGoogleApiClient;

    private  String API_KEY;

    DatabaseReference driver,onlineRef,currentUserRef,api_key_ref;
    GeoFire geoFire;
    Marker mCurrent;
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    Button present_place;



    //car Animation

    private List<LatLng> polyLineList;
    private Marker carMarker;
    private  float v;
    private  double lat,lng;
    private Handler handler;
    private LatLng startPosition,endPosition,currentPosition;
    private  int index,next;

    private  String destination;
    private PolylineOptions polylineOptions,backPolylineoptions;
    private Polyline blackPolyline,greyPolyline;
    private PlaceAutocompleteFragment place;
    AutocompleteFilter typeFilter;
    DrawerLayout drawerLayout;
    ImageView pp;


    private IGoogleAPI mService;

    Runnable drawPathRunable=new Runnable() {
        @Override
        public void run() {
            if (index<polyLineList.size()-1)
            {
                index++;
                next=index+1;
            }
            if (index<polyLineList.size()-1)
            {
                startPosition=polyLineList.get(index);
                endPosition=polyLineList.get(next);
            }

            ValueAnimator valueAnimator=ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v=valueAnimator.getAnimatedFraction();
                    lng=v*endPosition.longitude+(1-v)*startPosition.longitude;
                    lat=v*endPosition.latitude+(1-v)*startPosition.latitude;
                    LatLng newPos=new LatLng(lat,lng);

                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f,0.5f);
                    carMarker.setRotation(getBearing(startPosition,newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                            .target(newPos)
                            .zoom(15.5f)
                            .build()

                    ));
                }
            });

            valueAnimator.start();
            handler.postDelayed(this,3000);
        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat=Math.abs(startPosition.latitude-endPosition.latitude);
        double lng=Math.abs(startPosition.longitude-endPosition.longitude);

        if (startPosition.latitude<endPosition.latitude && startPosition.longitude<endPosition.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat)));

        else if (startPosition.latitude>=endPosition.latitude && startPosition.longitude<endPosition.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+90);

        else if (startPosition.latitude>=endPosition.latitude && startPosition.longitude>=endPosition.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
        else if (startPosition.latitude<endPosition.latitude && startPosition.longitude>=endPosition.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+270);

        return  -1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
        driver= FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
        onlineRef=FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef=FirebaseDatabase.getInstance().getReference(Common.driver_tb1)
                .child(Common.currentUser.getName());

        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        geoFire=new GeoFire(driver);
        setUpLoaction();

        mService=Common.getGoogleApi();

        updateFirebaseTken();

        mService= Common.getGoogleApi();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        location_switch=findViewById(R.id.location_switch);
        drawerLayout=findViewById(R.id.drawable);
        present_place=findViewById(R.id.prest_place);
        present_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });


        ImageView tooleBtn=findViewById(R.id.openNav);

        pp=findViewById(R.id.pp);
        pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });


        tooleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        api_key_ref=FirebaseDatabase.getInstance().getReference("API");

        api_key_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                API api=dataSnapshot.getValue(API.class);

                API_KEY=api.getApi_key();
                Toast.makeText(getApplicationContext(),"KEY :="+API_KEY,Toast.LENGTH_LONG).show();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {



            }
        });





        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline){
                    FirebaseDatabase.getInstance().goOnline();
                    startLocationUpdate();
                    displayLocation();
                    Toast.makeText(getApplicationContext(),"Online",Toast.LENGTH_LONG).show();

                }else{
                    FirebaseDatabase.getInstance().goOffline();
                    mMap.clear();
                 //   handler.removeCallbacks(drawPathRunable);
                    stopLocationUpdate();
                    mCurrent.remove();
                    Toast.makeText(getApplicationContext(),"Ofline",Toast.LENGTH_LONG).show();
                }

            }
        });

        polyLineList=new ArrayList<>();
        typeFilter=new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();
        place=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        place.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (location_switch.isChecked()){

                    destination=place.getAddress().toString();
                    destination=destination.replace(" ","+");
                    getDireaction();
                }else {
                    Toast.makeText(getApplicationContext(),"Please change your status to ONLINE",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Status status) {
             Toast.makeText(getApplicationContext(),""+status.toString(),Toast.LENGTH_LONG).show();
            }
        });

       // btnGeo=findViewById(R.id.btnGo);
        /*edtPlace=findViewById(R.id.editPlace);

        btnGeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destination=edtPlace.getText().toString();
                destination=destination.replace("","+");

                getDireaction();
            }
        });*/





    }

    private void chooseImage() {
        Intent in=new Intent();
        in.setType("image/*");
        in.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(in,"Select Picture: "),Common.IMAGE_PICK_UP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode== Common.IMAGE_PICK_UP && resultCode==RESULT_OK && data!=null&& data.getData()!=null){

            Uri saveUri=data.getData();

            if (saveUri!=null){

                final ProgressDialog pd=new ProgressDialog(this);
                pd.setMessage("Uploading...");
                pd.show();

                String imageName= UUID.randomUUID().toString();

                final StorageReference imageFolder=storageReference.child("images/"+imageName);

                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Map<String,Object> avaterUpdate=new HashMap<>();
                                        avaterUpdate.put("imageUrl",uri.toString());

                                        DatabaseReference driverInformation=FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1).child(Common.currentUser.getName());
                                        driverInformation.updateChildren(avaterUpdate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Toast.makeText(getApplicationContext(),"Successfuly Update",Toast.LENGTH_LONG).show();
                                                        }else {
                                                            Toast.makeText(getApplicationContext(),"Failed Update",Toast.LENGTH_LONG).show();

                                                        }

                                                    }
                                                });

                                    }
                                });
                            }
                        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        pd.setMessage("Uploaded "+progress+"%");

                        Picasso.get().load(Common.currentUser.getImageUrl()).into(pp);


                    }
                });


            }
        }
    }

    private void updateFirebaseTken() {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference(Common.token_tb1);
        Token token=new Token(FirebaseInstanceId.getInstance().getToken());


            tokens.child(Common.currentUser.getName())
                    .setValue(token);

    }

    private void getDireaction() {
        currentPosition=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

        String requestApi=null;

        try {

            requestApi="https://maps.googleapis.com/maps/api/directions/json?origin="+currentPosition.latitude+","+currentPosition.longitude+"&destination="+destination+"&key="+Common.currentApi.getApi_key();



           /* requestApi="https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin"+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+destination+"&"+
                    "key="+API_KEY;*/


            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {


                        Log.d("Show_Data",response.body().toString()+"\n"+currentPosition.latitude+","+currentPosition.longitude+"&destination="+destination+"&key="+Common.currentApi.getApi_key());

                        Toast.makeText(getApplicationContext(),""+response.body().toString(),Toast.LENGTH_LONG).show();

                        JSONObject jsonObject=new JSONObject(response.body().toString());
                        JSONArray jsonArray=jsonObject.getJSONArray("routes");

                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject route=jsonArray.getJSONObject(i);
                            JSONObject poly=route.getJSONObject("overview_polyline");
                            String polyline=  poly.getString("points");
                            polyLineList=decodePoly(polyline);
                        }
                        LatLngBounds.Builder builder=new LatLngBounds.Builder();
                       for (LatLng latLng:polyLineList){
                           builder.include(latLng);
                       }
                       LatLngBounds bounds=builder.build();
                       CameraUpdate mCameraUpdate=CameraUpdateFactory.newLatLngBounds(bounds,2);

                       mMap.animateCamera(mCameraUpdate);

                       polylineOptions=new PolylineOptions();
                       polylineOptions.color(Color.GRAY);
                       polylineOptions.width(5);
                       polylineOptions.startCap(new SquareCap());
                       polylineOptions.endCap(new SquareCap());
                       polylineOptions.jointType(JointType.ROUND);
                       polylineOptions.addAll(polyLineList);
                       greyPolyline =mMap.addPolyline(polylineOptions);

                        backPolylineoptions=new PolylineOptions();
                        backPolylineoptions.color(Color.GRAY);
                        backPolylineoptions.width(5);
                        backPolylineoptions.startCap(new SquareCap());
                        backPolylineoptions.endCap(new SquareCap());
                        backPolylineoptions.jointType(JointType.ROUND);
                        backPolylineoptions.addAll(polyLineList);
                        blackPolyline =mMap.addPolyline(polylineOptions);


                        mMap.addMarker(new MarkerOptions()
                                      .position(polyLineList.get(polyLineList.size()-1))
                                      .title("Pickup Location"));

                        ValueAnimator polyLineAnimation =ValueAnimator.ofInt(0,100);
                        polyLineAnimation.setDuration(1000);
                        polyLineAnimation.setInterpolator(new LinearInterpolator());
                        polyLineAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                List<LatLng> points=greyPolyline.getPoints();
                                int percentValue=(int) valueAnimator.getAnimatedValue();
                                int size=points.size();
                                int newPoints=(int)(size*(percentValue/100.0f));
                                List<LatLng> p=points.subList(0,newPoints);
                                blackPolyline.setPoints(p);
                            }
                        });
                        polyLineAnimation.start();
                        carMarker=mMap.addMarker(new MarkerOptions().position(currentPosition)
                        .flat(false)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                        handler=new Handler();
                        index=-1;
                        next=1;
                        handler.post(drawPathRunable);

                    } catch (JSONException e) {
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

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void setUpLoaction() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
           ActivityCompat.requestPermissions(this,new String[]{
                   Manifest.permission.ACCESS_COARSE_LOCATION,
                   Manifest.permission.ACCESS_FINE_LOCATION
           },MY_PERMISSION_REQUEST_CODE);
        }
        else {
            if (checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
               /* if (location_switch.isChecked()){*/
                    displayLocation();
               // }
            }
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

    private void stopLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation!=null){
            if (location_switch.isChecked()){
                final double latitude=mLastLocation.getLatitude();
                final double longtitude=mLastLocation.getLongitude();

                LatLng center=new LatLng(latitude,longtitude);
                LatLng northSide= SphericalUtil.computeOffset(center,100000,0);
                LatLng southSide=SphericalUtil.computeOffset(center,100000,180);

                LatLngBounds  bounds=LatLngBounds.builder()
                                     .include(northSide)
                                     .include(southSide)
                                     .build();

                place.setBoundsBias(bounds);
                place.setFilter(typeFilter);

                geoFire.setLocation(Common.currentUser.getName(), new GeoLocation(latitude, longtitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (mCurrent!=null){
                            mCurrent.remove();
                        }else {
                            mCurrent=mMap.addMarker(new MarkerOptions()

                            .position(new LatLng(latitude,longtitude))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                    .title("You"));

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longtitude),15.0f));

                        }
                    }
                });
            }
        }

    }

    private void roterMarker(final Marker mCurrent, final float i, GoogleMap mMap) {
        final Handler handler=new Handler();
        final long start= SystemClock.uptimeMillis();
        final float startRotation=mCurrent.getRotation();
        final long duration=1500;

        final Interpolator interpolator=new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed=SystemClock.uptimeMillis()-start;
                float t=interpolator.getInterpolation((float)elapsed/duration);
                float rot=t*i+(1-t)*startRotation;
                mCurrent.setRotation(-rot>180?rot/2:rot);
                if (t<1.0){
                    handler.postDelayed(this,16);
                }
            }
        });

    }

    private void startLocationUpdate() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationrequest,this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

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

    @Override
    public void onLocationChanged(Location location) {
            mLastLocation=location;
            displayLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

            switch (requestCode){
                case MY_PERMISSION_REQUEST_CODE:
                    if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                        if (checkPlayServices()) {
                            buildGoogleApiClient();
                            createLocationRequest();
                            if (location_switch.isChecked()) {
                                displayLocation();

                            }
                        }
                    }
            }
    }
}
