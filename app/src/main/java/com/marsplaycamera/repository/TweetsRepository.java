package com.marsplaycamera.repository;

import androidx.lifecycle.MutableLiveData;

import com.joshtest.api.RetrofitService;
import com.joshtest.api.TweetsApi;
import com.joshtest.model.TweetsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TweetsRepository {

    private static TweetsRepository tweetsRepository;
    private MutableLiveData<TweetsResponse> tweetsResponse = new MutableLiveData<>();

    public static TweetsRepository getInstance(){
        if (tweetsRepository == null){
            tweetsRepository = new TweetsRepository();
        }
        return tweetsRepository;
    }

    private TweetsApi tweetsApi;

    public TweetsRepository(){
        tweetsApi = RetrofitService.cteateService(TweetsApi.class);
    }

    public MutableLiveData<TweetsResponse> getNews(String hashTag, long page){
        tweetsApi.getNewsList(hashTag, page).enqueue(new Callback<TweetsResponse>() {
            @Override
            public void onResponse(Call<TweetsResponse> call,
                                   Response<TweetsResponse> response) {
                if (response.isSuccessful()){
                    tweetsResponse.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<TweetsResponse> call, Throwable t) {
                tweetsResponse.postValue(null);
            }
        });
        return tweetsResponse;
    }

}
