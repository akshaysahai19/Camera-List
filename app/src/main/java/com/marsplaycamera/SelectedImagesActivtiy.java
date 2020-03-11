package com.marsplaycamera;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.marsplaycamera.databinding.SelectedImagesLayoutBinding;
import com.marsplaycamera.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;

public class SelectedImagesActivtiy extends AppCompatActivity implements CustomCameraRecyclerAdapter.ImageListInterface {

    private SelectedImagesLayoutBinding layoutBinding;
    private CustomCameraRecyclerAdapter customCameraRecyclerAdapter;
    private ArrayList<String> image_paths = new ArrayList<>();
    private int selectedPos = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.selected_images_layout);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        image_paths = getIntent().getStringArrayListExtra("IMAGE_PATHS");
        setupRecyclerview();

        onClick();
    }

    private void onClick() {
        layoutBinding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.startUploadActivity(image_paths, SelectedImagesActivtiy.this);
                finish();
            }
        });
    }

    private void setupRecyclerview() {
        layoutBinding.recyclerview.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        customCameraRecyclerAdapter = new CustomCameraRecyclerAdapter(this, image_paths, 1);
        layoutBinding.recyclerview.setAdapter(customCameraRecyclerAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                image_paths.set(selectedPos, result.getUri().getPath());
                customCameraRecyclerAdapter.setImagePathList(image_paths);
            }
        }
    }


    @Override
    public void removeImage(int pos) {
        image_paths.remove(pos);
        if (image_paths.size() > 0) {
            customCameraRecyclerAdapter.setImagePathList(image_paths);
        } else {
            finish();
        }
    }

    @Override
    public void imageClicked(int pos) {
        selectedPos = pos;
        Utils.showOpDialog(image_paths, this, selectedPos);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
