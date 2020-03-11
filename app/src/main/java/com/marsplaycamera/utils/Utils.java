package com.marsplaycamera.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.marsplaycamera.upload.UploadActivity;
import com.marsplaycamera.image_operation.PreviewZoomActivity;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;

public class Utils {

    public static void showOpDialog(final ArrayList<String> imgs_path_array, final Activity activity, final int pos) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(activity);

        builder.setCancelable(false)
                .setTitle("Select Operation")
                .setMessage("What operation, would you like to perform with the image?")
                .setPositiveButton("Crop", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();

                        File file = new File(imgs_path_array.get(pos));
                        CropImage.activity(Uri.fromFile(file))
                                .start(activity);

                    }
                }).setNegativeButton("Zoom", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();

                Intent intent = new Intent(activity, PreviewZoomActivity.class);
                intent.putExtra("IMAGE_PATH", imgs_path_array.get(pos));
                activity.startActivity(intent);

            }
        }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

            }
        });

        builder.create().show();
    }

    public static void startUploadActivity(ArrayList<String> img_path, Activity activity) {
        Intent intent = new Intent(activity, UploadActivity.class);
        intent.putExtra("IMAGE_PATHS", img_path);
        activity.startActivity(intent);
    }

}
