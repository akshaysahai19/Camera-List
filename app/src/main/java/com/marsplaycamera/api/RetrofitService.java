package com.marsplaycamera.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {


    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.flickr.com/services/rest/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();


    public static <S> S cteateService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }

}
