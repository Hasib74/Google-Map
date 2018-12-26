package com.example.dcl.androiduberrider;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dcl.androiduberrider.Common.Common;
import com.example.dcl.androiduberrider.Remort.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.font.TextAttribute;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {
   String mTag;
   String mLocation,mDestination;


   IGoogleAPI mService;
    TextView total;
    TextView myLocation;
    TextView destination;

    boolean isTapOnMap;

   public static BottomSheetRiderFragment newInstance(String location,String destination,boolean isTapOnMap){
       BottomSheetRiderFragment f=new BottomSheetRiderFragment();
       Bundle args=new Bundle();
       args.putString("location",location);
       args.putString("destination",destination);
       args.putBoolean("isTapOnMap",isTapOnMap);
       f.setArguments(args);
       return f;
   }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mTag=getArguments().getString("TAG");
        mLocation=getArguments().getString("location");
        mDestination=getArguments().getString("destination");
        isTapOnMap=getArguments().getBoolean("isTapOnMap");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

     View v=inflater.inflate(R.layout.bottom_sheet_rider,container,false);
         myLocation=v.findViewById(R.id.my_location);
         destination=v.findViewById(R.id.destination_location);
        total=v.findViewById(R.id.total);

        mService= Common.getGoogleServices();
        if (!isTapOnMap){
             myLocation.setText(mLocation);
             destination.setText(mDestination);
        }


        getPrice(mLocation,mDestination);



     return  v;
    }

    private void getPrice(final String mLocation, final String mDestination) {

        Toast.makeText(getContext(),""+Common.api.getApi_key(),Toast.LENGTH_LONG).show();


        //Toast.makeText(getContext(),""+Common.api.getApi_key(),Toast.LENGTH_LONG).show();

       String requestUrl=null;
       try{

           //https://maps.googleapis.com/maps/api/directions/json?origin=41.43206,-81.38992&destination=42.43206,-82.38992&key=AIzaSyBCekDRuzJCie4ijrMyra1T5-B70odCwzY
          /* requestUrl="https://maps.googleapis.com/maps/api/direction/json?"+
                   "mode=driving&"
                   +"transit_routing_preference=less_driving&"
                   +"origin="+mLocation+"&"
                   +"destination="+mDestination+"&"
                   +"key="+Common.api.getApi_key();*/
         /*  requestUrl="https://maps.googleapis.com/maps/api/direction/json?"
                   +"origin="+mLocation+"&"
                   +"destination="+mDestination+"&"
                   +"key="+Common.api.getApi_key();
*/

           requestUrl="https://maps.googleapis.com/maps/api/directions/json?origin="+mLocation+"&destination="+mDestination+"&key="+Common.api.getApi_key();

           mService.getPath(requestUrl)
                   .enqueue(new Callback<String>() {
                       @Override
                       public void onResponse(Call<String> call, Response<String> response) {
                           try {


                           //    Toast.makeText(getContext(),""+mLocation+","+mDestination,Toast.LENGTH_LONG).show();

                               JSONObject jsonObject=new JSONObject(response.body().toString());
                               JSONArray jsonArray=jsonObject.getJSONArray("routes");

                               JSONObject object=jsonArray.getJSONObject(0);

                               JSONArray legs=object.getJSONArray("legs");

                               JSONObject legsObject=legs.getJSONObject(0);

                               JSONObject distination=legsObject.getJSONObject("distance");
                               String distance_txt=distination.getString("text");

                               Double distance_value=Double.parseDouble(distance_txt.replaceAll("[^0-9\\\\.]+",""));

                               JSONObject time=legsObject.getJSONObject("duration");
                               String time_txt=time.getString("text");

                               Integer time_value= Integer.parseInt(time_txt.replaceAll("\\D+",""));
                               String final_calculation=String.format("%s +%s = $%.2f",distance_txt,time_txt,Common.getPrice(distance_value,time_value));

                               total.setText(final_calculation);


                               if (isTapOnMap){
                                   String start_address=legsObject.getString("start_address");
                                   String end_address=legsObject.getString("end_address");


                                   Toast.makeText(getContext(),"start"+start_address+"  end"+end_address,Toast.LENGTH_LONG).show();

                                   myLocation.setText(start_address);
                                   destination.setText(end_address);

                               }


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
}
