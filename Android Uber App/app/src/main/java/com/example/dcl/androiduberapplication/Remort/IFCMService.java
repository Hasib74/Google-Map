package com.example.dcl.androiduberapplication.Remort;

import com.example.dcl.androiduberapplication.Model.FCMResponse;
import com.example.dcl.androiduberapplication.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAr0Nxd3w:APA91bH84CVp_MxaQACK7xndbjx4kATqz3BsbuHwkE-p4TbNqM6wre1ow0BYW0JD5XmApX54ZdQ5NBmEBLGH366xEekPWXnqDKir4lqYqN5h2BUHZVNabEA2EP3sYK9LpvOJ26lt82U_"
    })
    @POST("fcm/send")
    Call<FCMResponse>sendMessage(@Body Sender body);
}
