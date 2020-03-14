package com.marsplaycamera.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marsplaycamera.repository.ImageRepository;
import com.marsplaycamera.R;
import com.marsplaycamera.databinding.UploadDialogLayoutBinding;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UploadDialog extends AppCompatDialog {

    private UploadDialogLayoutBinding uploadDialogLayoutBinding;
    private StorageReference storageReference;
    private ArrayList<String> img_path_list;
    private UploadDialogInterface uploadDialogInterface;

    public interface UploadDialogInterface{
        void uploaded();
    }

    public UploadDialog(Context context, Activity activity) {
        super(context);
        img_path_list = ImageRepository.getInstance().getImagesModelList().getValue();
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        uploadDialogLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.upload_dialog_layout, null, false);
        setContentView(uploadDialogLayoutBinding.getRoot());

        uploadDialogInterface = (UploadDialogInterface) activity;

        setCancelable(false);

        initializeUpload();
    }

    private void initializeUpload() {
        storageReference = FirebaseStorage.getInstance()
                .getReferenceFromUrl("gs://marsplay-camera.appspot.com");
        uploadAll();
    }

    private void uploadAll() {
        for (int i = 0; i < img_path_list.size(); i++) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap image = BitmapFactory.decodeFile(img_path_list.get(i), bmOptions);
            uploadFile(image, i);
        }

    }

    private void uploadFile(Bitmap bitmap, final int i) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] data = baos.toByteArray();

        final StorageReference storageRef = storageReference.child("Uploads")
                .child("pic" + i + ".jpg");

        UploadTask uploadTask = storageRef.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    Map<String, String> map = new HashMap<>();
                    map.put("path", "" + downloadUri);
                    FirebaseFirestore.getInstance().collection("Images")
                            .document("Upload").collection("Testing")
                            .add(map);

                    System.out.println("image index:" + i);
                    System.out.println("image file:" + downloadUri);

                    if (i == img_path_list.size() - 1) {
                        uploadDialogInterface.uploaded();
                        dismiss();
                    }

                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

}
