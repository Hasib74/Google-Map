package com.example.dcl.androiduberrider.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.dcl.androiduberrider.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
    View myView;

    public CustomInfoWindow(Context c) {
        myView= LayoutInflater.from(c)
                .inflate(R.layout.costom_rider_info_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView textPickUpTitle=(TextView)myView.findViewById(R.id.textPickUpInfo);
        textPickUpTitle.setText(marker.getTitle());

        TextView textPickUpSnippet=(TextView)myView.findViewById(R.id.textPickUpSnippet);
        textPickUpSnippet.setText(marker.getSnippet());

        return  myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
