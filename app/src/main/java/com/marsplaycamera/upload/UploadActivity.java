package com.marsplaycamera.upload;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.marsplaycamera.custom_camera.CustomCameraActivity;
import com.marsplaycamera.repository.ImageRepository;
import com.marsplaycamera.viewmodel.ImageViewModel;
import com.marsplaycamera.R;
import com.marsplaycamera.databinding.ImageListActivityBinding;
import com.marsplaycamera.image_adapter.ImageRecyclerViewAdapter;
import com.marsplaycamera.utils.UploadDialog;
import com.marsplaycamera.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity implements ImageRecyclerViewAdapter.ImageModelInterface,
        UploadDialog.UploadDialogInterface {

    private ImageRecyclerViewAdapter imageRecyclerViewAdapter;
    private ImageViewModel imageViewModel;
    private ImageListActivityBinding layoutBinding;
    private static final int CAMERA_CODE = 1001;
    private static final int GALLERY_CODE = 1002;
    private int selectedPos = 10001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.image_list_activity);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        imageViewModel = ViewModelProviders.of(this).get(ImageViewModel.class);
        imageViewModel.init();
        imageRecyclerViewAdapter = new ImageRecyclerViewAdapter(new ArrayList<String>(), this);
        updateView();
        observeAdapter();
        onClicks();
    }

    private void onClicks() {
        layoutBinding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCameraGalleryDialog();
            }
        });

        layoutBinding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadDialog uploadDialog = new UploadDialog(UploadActivity.this, UploadActivity.this);
                uploadDialog.show();
            }
        });
    }

    private void observeAdapter() {
        imageViewModel.getMutableLiveData().observe(this, new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> imagesModels) {
                if (imagesModels != null) {
                    if (layoutBinding.upload.getVisibility() == View.GONE) {
                        layoutBinding.noImage.setVisibility(View.GONE);
                        layoutBinding.upload.setVisibility(View.VISIBLE);
                    }
                    imageRecyclerViewAdapter.setImagesModelArrayList(imagesModels);
                }
            }
        });
    }

    private void updateView() {
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.image_list_activity);
        layoutBinding.imageRecyclerview.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        layoutBinding.imageRecyclerview.setAdapter(imageRecyclerViewAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CODE) {
                ImageRepository.getInstance().addImages(data.getStringArrayListExtra("IMAGE_PATHS"));
            } else if (requestCode == GALLERY_CODE) {
                ArrayList<String> image_paths = new ArrayList<>();
                ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);

                for (int i = 0; i < files.size(); i++) {
                    String path = files.get(i).getPath();
                    image_paths.add(path);
                }
                ImageRepository.getInstance().addImages(image_paths);

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                ImageRepository.getInstance().updateImage(result.getUri().getPath(), selectedPos);
//                imgs_path_array.set(selectedPos, result.getUri().getPath());
            }
        }

    }

    private void showCameraGalleryDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setCancelable(false)
                .setTitle("Choose From")
                .setPositiveButton("Camera", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();

                        Intent intent = new Intent(UploadActivity.this, CustomCameraActivity.class);
                        startActivityForResult(intent, CAMERA_CODE);

                    }
                }).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();

                Intent intent = new Intent(UploadActivity.this, FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
                        .setCheckPermission(true)
                        .setShowImages(true)
                        .setShowVideos(false)
                        .enableImageCapture(false)
                        .enableVideoCapture(false)
                        .setMaxSelection(10)
                        .setSkipZeroSizeFiles(true)
                        .build());
                startActivityForResult(intent, GALLERY_CODE);


            }
        });

        builder.create().show();
    }


    @Override
    public void deleteImage(int pos) {
        imageViewModel.deleteModel(pos);
    }

    @Override
    public void showImageDialog(int pos) {
        selectedPos = pos;
        Utils.showOpDialog(ImageRepository.getInstance().getImagesModelList().getValue()
                .get(pos), this, pos);
    }

    @Override
    public void uploaded() {
        Toast.makeText(UploadActivity.this, "Image Upload successful", Toast.LENGTH_SHORT).show();
        finish();
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
