package com.marsplaycamera.api;

import com.joshtest.model.TweetsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TweetsApi {

    @Headers("Content-Type: application/json")
    @POST("image/{id}")
    Call<TweetsResponse> getNewsList(@Path("id") String transaction_id);

//    https://api.twitter.com/1.1/search/tweets.json?q=%23test&page=2

}
