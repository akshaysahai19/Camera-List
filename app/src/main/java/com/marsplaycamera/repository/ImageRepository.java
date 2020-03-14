package com.marsplaycamera.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

public class ImageRepository {

    private static ImageRepository imageRepository;
    private MutableLiveData<ArrayList<String>> imagesModelList = new MutableLiveData<>();

    public static ImageRepository getInstance() {
        if (imageRepository == null) {
            imageRepository = new ImageRepository();
        }
        return imageRepository;
    }

    public MutableLiveData<ArrayList<String>> getImagesModelList() {
        return imagesModelList;
    }

    public void removeImagesModel(int pos) {
        ArrayList<String> images = imagesModelList.getValue();
        images.remove(pos);
        imagesModelList.setValue(images);
    }

    public void addImages(ArrayList<String> images) {
        ArrayList<String> imageList = imagesModelList.getValue();
        if (imageList == null) {
            imageList = new ArrayList<>();
        }
        imageList.addAll(images);
        imagesModelList.setValue(imageList);
    }

    public void updateImage(String path, int pos){
        ArrayList<String> imageList = imagesModelList.getValue();
        imageList.set(pos,path);
        imagesModelList.setValue(imageList);
    }


}
