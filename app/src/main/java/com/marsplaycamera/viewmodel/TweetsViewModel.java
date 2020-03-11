package com.marsplaycamera.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.joshtest.model.TweetsResponse;
import com.joshtest.repository.TweetsRepository;

public class TweetsViewModel extends ViewModel {

    private MutableLiveData<TweetsResponse> mutableLiveData;
    private TweetsRepository tweetsRepository;

    public void init() {
        if (mutableLiveData != null) {
            return;
        }
        tweetsRepository = TweetsRepository.getInstance();
    }

    public LiveData<TweetsResponse> getTweetsRepository() {
        return mutableLiveData;
    }

    public void fetchTweets(String hashtag, long page) {
        mutableLiveData = tweetsRepository.getNews(hashtag, page);
    }

}
