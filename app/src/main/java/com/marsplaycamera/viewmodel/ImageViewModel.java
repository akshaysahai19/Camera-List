package com.marsplaycamera.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.marsplaycamera.repository.ImageRepository;

import java.util.ArrayList;

public class ImageViewModel extends ViewModel {

    private MutableLiveData<ArrayList<String>> mutableLiveData;

    public void init(){
        if (mutableLiveData != null) {
            return;
        }
        mutableLiveData = ImageRepository.getInstance().getImagesModelList();
    }

    public LiveData<ArrayList<String>> getMutableLiveData() {
        return mutableLiveData;
    }

    public void deleteModel(int pos) {
        ImageRepository.getInstance().removeImagesModel(pos);
    }
}
