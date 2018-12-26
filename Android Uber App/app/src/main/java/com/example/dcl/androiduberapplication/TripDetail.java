package com.example.dcl.androiduberapplication;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.dcl.androiduberapplication.Common.Common;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;

public class TripDetail extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView txtdate,txtFee,txtBaseFare,txtTime,txtDisatance,txtEstimatedPayout,txtFrom,txtTo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        txtdate=findViewById(R.id.current_date);
        txtFee=findViewById(R.id.base_fare_rate);
        txtTime=findViewById(R.id.time);
        txtDisatance=findViewById(R.id.distance);
        txtEstimatedPayout=findViewById(R.id.estimated_payout);
        txtFrom=findViewById(R.id.form);
        txtTo=findViewById(R.id.to);
        txtBaseFare=findViewById(R.id.base_fire);

        settingInformation();



    }

    private void settingInformation() {
        if(getIntent()!=null){
            Calendar calendar=Calendar.getInstance();
            String date=String.format("%s, %d/%d",converToDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH));

           txtdate.setText(date);

            txtFee.setText(String.format("$ %.2f",getIntent().getDoubleExtra("total",0.0)));
            txtEstimatedPayout.setText(String.format("$ %.2f",getIntent().getDoubleExtra("total",0.0)));
            txtBaseFare.setText(String.format("$ %.2f", Common.base_fare));
            txtTime.setText(String.format("%s min",getIntent().getStringExtra("time")));
            txtDisatance.setText(String.format("%s km",getIntent().getStringExtra("distance")));
            txtFrom.setText(getIntent().getStringExtra("start_address"));
            txtTo.setText(getIntent().getStringExtra("end_address"));

            String[] location_end=getIntent().getStringExtra("location_end").split(",");

            Log.d("Location_end", String.valueOf(location_end));

           //LatLng dropOff=new LatLng(Double.parseDouble(location_end[0]),Double.parseDouble(location_end[1]));
            /*mMap.addMarker(new MarkerOptions()
            .position(dropOff)
             .title("Drop Off Here")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));*/

           // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dropOff,12.0f));

        }
    }

    private String converToDayOfWeek(int day) {

        switch (day){
            case Calendar.SUNDAY:
                return "SUNDAY";
            case Calendar.MONDAY:
                return "MONDAY";
            case Calendar.WEDNESDAY:
                return "WEDNESDAY";
            case Calendar.THURSDAY:
                return "THURSDAY";
            case Calendar.FRIDAY:
                return "FRIDAY";
            case Calendar.SATURDAY:
                return "SATURDAY";
            case Calendar.TUESDAY:
                return "TUESDAY";


                default:
                    return "UNK";

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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
