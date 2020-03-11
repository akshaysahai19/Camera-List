package com.marsplaycamera.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class GenericFileProvider extends FileProvider {

    public static File captureImage(Context context, Bitmap bitmap, String fileName) {
        File directory = context.getExternalFilesDir("");
        File file = new File(directory, fileName);
        if (!file.exists()) {
            Log.d("path", file.toString());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                Log.d("Closed", "FileOutPutStream");
            } catch (java.io.IOException e) {
                e.printStackTrace();
                Log.d("Failed", "FileOutPutStream");
            }
        }
        return file;
    }


}
