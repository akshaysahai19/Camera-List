package com.marsplaycamera;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.marsplaycamera.databinding.ActivityMainBinding;
import com.marsplaycamera.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_CODE = 1001;
    private static final int GALLERY_CODE = 1002;
    ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        onClicks();
    }

    private void onClicks() {
        activityMainBinding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showCameraGalleryDialog();
            }
        });

        activityMainBinding.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void showCameraGalleryDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setCancelable(false)
                .setTitle("Choose From")
                .setPositiveButton("Camera", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();

                        Intent intent = new Intent(MainActivity.this, CustomCameraActivity.class);
                        startActivityForResult(intent, CAMERA_CODE);

                    }
                }).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();

                Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CODE) {
                Utils.startUploadActivity(data.getStringArrayListExtra("IMAGE_PATHS"),this);
            } else if (requestCode == GALLERY_CODE) {
                ArrayList<String> image_paths = new ArrayList<>();
                ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);

                for (int i = 0; i < files.size(); i++) {
                    String path = files.get(i).getPath();
                    image_paths.add(path);
                }
                startSelectedScreen(image_paths);

            }
        }

    }


    private void startSelectedScreen(ArrayList<String> image_paths) {
        if (image_paths.size() > 0) {
            Intent intent = new Intent(MainActivity.this, SelectedImagesActivtiy.class);
            intent.putExtra("IMAGE_PATHS", image_paths);
            startActivity(intent);
        }
    }
}

