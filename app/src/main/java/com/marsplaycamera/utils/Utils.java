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

    public static void showOpDialog(final String path, final Activity activity, final int pos) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(activity);

        builder.setCancelable(false)
                .setTitle("Select Operation")
                .setMessage("What operation, would you like to perform with the image?")
                .setPositiveButton("Crop", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();

                        File file = new File(path);
                        CropImage.activity(Uri.fromFile(file))
                                .start(activity);

                    }
                }).setNegativeButton("Zoom", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                dialog.dismiss();

                Intent intent = new Intent(activity, PreviewZoomActivity.class);
                intent.putExtra("IMAGE_PATH", path);
                activity.startActivity(intent);

            }
        }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

            }
        });

        builder.create().show();
    }


}
