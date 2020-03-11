package com.marsplaycamera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marsplaycamera.databinding.CameraLayoutBinding;
import com.marsplaycamera.image_operation.PreviewZoomActivity;
import com.marsplaycamera.utils.GenericFileProvider;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class CustomCameraActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnTouchListener, SensorEventListener, CustomCameraRecyclerAdapter.ImageListInterface {

    CameraLayoutBinding cameraLayoutBinding;
    private static final int CAMERA_REQUEST_CODE = 101;
    String cameraId;
    Size previewSize;
    CameraCaptureSession cameraCaptureSession;
    CaptureRequest captureRequest;
    CaptureRequest.Builder captureRequestBuilder;


    CameraManager cameraManager;
    int cameraFacing_back, cameraFacing_front;
    TextureView.SurfaceTextureListener surfaceTextureListener;
    HandlerThread backgroundThread;
    Handler backgroundHandler;
    CameraDevice.StateCallback stateCallback;
    CameraDevice cameraDevice;
    boolean isFrontCameraSelected = false;
    boolean isFlashOn = false;
    File galleryFolder;
    CameraCharacteristics cameraCharacteristics;

    ArrayList<String> imgs_path_array;
    CustomCameraRecyclerAdapter customCameraRecyclerAdapter;

    public float finger_spacing = 0;
    public int zoom_level = 1;


    SensorManager sensorManager;
    Sensor mSensorOrientation;
    public static final int UPSIDE_DOWN = 3;
    public static final int LANDSCAPE_RIGHT = 4;
    public static final int PORTRAIT = 1;
    public static final int LANDSCAPE_LEFT = 2;
    public int mOrientationDeg; //last rotation in degrees
    public int mOrientationRounded; //last orientation int from above
    private static final int _DATA_X = 0;
    private static final int _DATA_Y = 1;
    private static final int _DATA_Z = 2;
    private int ORIENTATION_UNKNOWN = -1;
    int tempOrientRounded = 0;
    private long mLastClickTime = 0;
    private int selectedPos = 1001;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraLayoutBinding = DataBindingUtil.setContentView(this, R.layout.camera_layout);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_CODE);
        imgs_path_array = new ArrayList<>();

        clickListener();
        setupRecyclerView();
        cameraSetup();
    }

    private void cameraSetup() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraFacing_back = CameraCharacteristics.LENS_FACING_BACK;
        cameraFacing_front = CameraCharacteristics.LENS_FACING_FRONT;
        cameraLayoutBinding.texture.setOnTouchListener(this);

        surfaceTextureListener = new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                setUpCamera(cameraFacing_back);
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };

        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice cameraDevice) {
                CustomCameraActivity.this.cameraDevice = cameraDevice;
                createPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice) {
                cameraDevice.close();
                CustomCameraActivity.this.cameraDevice = null;
            }

            @Override
            public void onError(CameraDevice cameraDevice, int error) {
                cameraDevice.close();
                CustomCameraActivity.this.cameraDevice = null;
            }
        };


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, mSensorOrientation, SensorManager.SENSOR_DELAY_NORMAL);


    }

    private void clickListener() {
        cameraLayoutBinding.customCameraCapture.setOnClickListener(this);
        cameraLayoutBinding.customCameraDone.setOnClickListener(this);
        cameraLayoutBinding.customCameraClose.setOnClickListener(this);
        cameraLayoutBinding.customCameraFlash.setOnClickListener(this);
        cameraLayoutBinding.customCameraRotate.setOnClickListener(this);
    }

    private void setupRecyclerView() {
        cameraLayoutBinding.recyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        customCameraRecyclerAdapter = new CustomCameraRecyclerAdapter(this, imgs_path_array);
        cameraLayoutBinding.recyclerview.setAdapter(customCameraRecyclerAdapter);
    }

    private void setUpCamera(int cam_face_id) {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        cam_face_id) {
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//                    previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    previewSize = chooseOptimalSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class),
                            cameraLayoutBinding.texture.getWidth(), cameraLayoutBinding.texture.getHeight());
                    Log.d("Sizes - " + streamConfigurationMap.getOutputSizes(SurfaceTexture.class).toString(), "Ok?");
                    Log.d("Width - " + previewSize.getWidth() + ", Height : " + previewSize.getHeight(), "Ok?");
                    this.cameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void openCamera() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openBackgroundThread() {
        backgroundThread = new HandlerThread("camera_background_thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }


    @Override
    protected void onResume() {
        super.onResume();
        openBackgroundThread();
        if (cameraLayoutBinding.texture.isAvailable()) {
            setUpCamera(cameraFacing_back);
            openCamera();
        } else {
            cameraLayoutBinding.texture.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
        closeBackgroundThread();
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }


    private void createPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = cameraLayoutBinding.texture.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (cameraDevice == null) {
                                return;
                            }

                            try {
                                captureRequest = captureRequestBuilder.build();
                                CustomCameraActivity.this.cameraCaptureSession = cameraCaptureSession;
                                CustomCameraActivity.this.cameraCaptureSession.setRepeatingRequest(captureRequest,
                                        null, backgroundHandler);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    }, backgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Size chooseOptimalSize(Size[] outputSizes, int width, int height) {
        double preferredRatio = height / (double) width;
        Size currentOptimalSize = outputSizes[0];
        double currentOptimalRatio = currentOptimalSize.getWidth() / (double) currentOptimalSize.getHeight();
        for (Size currentSize : outputSizes) {
            double currentRatio = currentSize.getWidth() / (double) currentSize.getHeight();
            if (Math.abs(preferredRatio - currentRatio) <
                    Math.abs(preferredRatio - currentOptimalRatio)) {
                currentOptimalSize = currentSize;
                currentOptimalRatio = currentRatio;
            }
        }
        return currentOptimalSize;
    }


    private void lock() {

        try {

            cameraCaptureSession.capture(captureRequestBuilder.build(),
                    null, backgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void unlock() {
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                    null, backgroundHandler);
//            customCameraRecyclerAdapter = new CustomCameraRecyclerAdapter(this, imgs_path_array);
//            customCameraRecyclerAdapter.notifyDataSetChanged();
//            custom_camera_recyclerview.setAdapter(customCameraRecyclerAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        try {

            if (cameraId != null) {

                CameraManager manager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                float maxzoom = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 5;

                Rect m = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                int action = event.getAction();
                float current_finger_spacing;

                if (event.getPointerCount() > 1) {
                    // Multi touch logic
                    current_finger_spacing = getFingerSpacing(event);
                    if (finger_spacing != 0) {
                        if (current_finger_spacing > finger_spacing && maxzoom > zoom_level) {
                            zoom_level++;
                        } else if (current_finger_spacing < finger_spacing && zoom_level > 1) {
                            zoom_level--;
                        }
                        int minW = (int) (m.width() / maxzoom);
                        int minH = (int) (m.height() / maxzoom);
                        int difW = m.width() - minW;
                        int difH = m.height() - minH;
                        int cropW = difW / 100 * (int) zoom_level;
                        int cropH = difH / 100 * (int) zoom_level;
                        cropW -= cropW & 3;
                        cropH -= cropH & 3;
                        Rect zoom = new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH);
                        captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
                    }
                    finger_spacing = current_finger_spacing;
                } else {
                    if (action == MotionEvent.ACTION_UP) {
                        //single touch logic
                    }
                }

                try {
                    CameraCaptureSession.CaptureCallback captureCallbackHandler = new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);

                            if (request.getTag() == "FOCUS_TAG") {
                                //the focus trigger is complete -
                                //resume repeating (preview surface will get frames), clear AF trigger
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
                                try {
                                    cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                            super.onCaptureFailed(session, request, failure);
                            Log.e("TAG", "Manual AF failure: " + failure);
                        }
                    };
                    cameraCaptureSession
                            .setRepeatingRequest(captureRequestBuilder.build(), captureCallbackHandler, null);
                } catch (CameraAccessException | NullPointerException | IllegalStateException e) {
                    e.printStackTrace();
                }
            } else {
                finish();
                startActivity(getIntent());
            }
        } catch (Exception e) {
            throw new RuntimeException("can not access camera.", e);
        }

        return true;
    }

    //Determine the space between the first two fingers
    @SuppressWarnings("deprecation")
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }


    private boolean isMeteringAreaAFSupported() {
        return cameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) >= 1;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.custom_camera_close:
                closeCamera();
                finish();
                break;
            case R.id.custom_camera_done:

                if (imgs_path_array.size() > 0) {
//                    Intent intent = new Intent(CustomCameraActivity.this, DescriptionActivity.class);
//                    intent.putStringArrayListExtra("data", imgs_path_array);
//                    intent.putExtra(SurveyActivity.EXISTING_TAGS, existing_tags);
//                    intent.putExtra("type", "image");
//                    startActivityForResult(intent, 101);
                } else {
                    finish();
                }


                break;
            case R.id.custom_camera_rotate:
                if (isFrontCameraSelected) {
                    isFrontCameraSelected = false;
                    setUpCamera(cameraFacing_back);
                    closeCamera();
                    openCamera();
                } else {
                    isFrontCameraSelected = true;
                    setUpCamera(cameraFacing_front);
                    closeCamera();
                    openCamera();
                }
                break;
            case R.id.custom_camera_flash:
                if (isFlashOn) {
                    isFlashOn = false;
                    try {
                        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                        cameraLayoutBinding.customCameraFlash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off_black_24dp));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    isFlashOn = true;
                    try {
                        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                        cameraLayoutBinding.customCameraFlash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on_black_24dp));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case R.id.custom_camera_capture:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                cameraLayoutBinding.customCameraDone.setVisibility(View.VISIBLE);

                lock();

                String fileName = System.currentTimeMillis() + ".jpg";
                String img_path = GenericFileProvider.captureImage(CustomCameraActivity.this,
                        cameraLayoutBinding.texture.getBitmap(), fileName).getPath();
                System.out.println("File Path - " + img_path);
                updateList(img_path);

                unlock();

                break;


        }
    }

    private void updateList(String img_path) {
        imgs_path_array.add(img_path);
        customCameraRecyclerAdapter.setImagePathList(imgs_path_array);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imgs_path_array.set(selectedPos, result.getUri().getPath());
                customCameraRecyclerAdapter.setImagePathList(imgs_path_array);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        {

            float[] values = sensorEvent.values;
            int orientation = ORIENTATION_UNKNOWN;
            float X = -values[_DATA_X];
            float Y = -values[_DATA_Y];
            float Z = -values[_DATA_Z];
            float magnitude = X * X + Y * Y;
            // Don't trust the angle if the magnitude is small compared to the y value
            if (magnitude * 4 >= Z * Z) {
                float OneEightyOverPi = 57.29577957855f;
                float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
                orientation = 90 - (int) Math.round(angle);
                // normalize to 0 - 359 range
                while (orientation >= 360) {
                    orientation -= 360;
                }
                while (orientation < 0) {
                    orientation += 360;
                }
            }
            if (orientation != mOrientationDeg) {
                mOrientationDeg = orientation;
                //figure out actual orientation
                if (orientation == -1) {//basically flat

                } else if (orientation <= 45 || orientation > 315) {//round to 0
                    tempOrientRounded = 1;//portrait
                } else if (orientation > 45 && orientation <= 135) {//round to 90
                    tempOrientRounded = 2; //lsleft
                } else if (orientation > 135 && orientation <= 225) {//round to 180
                    tempOrientRounded = 3; //upside down
                } else if (orientation > 225 && orientation <= 315) {//round to 270
                    tempOrientRounded = 4;//lsright
                }


            }

            if (mOrientationRounded != tempOrientRounded) {
                //Orientation changed, handle the change here
                mOrientationRounded = tempOrientRounded;

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void removeImage(int pos) {
        imgs_path_array.remove(pos);
        customCameraRecyclerAdapter.setImagePathList(imgs_path_array);
    }

    @Override
    public void imageClicked(int pos) {
        selectedPos = pos;
//        File file = new File(imgs_path_array.get(pos));
//        CropImage.activity(Uri.fromFile(file))
//                .start(this);
        lock();
        showOpDialog();
    }

    private void showOpDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setCancelable(false)
                .setTitle("Select Operation")
                .setMessage("What operation, would you like to perform with the image?")
                .setPositiveButton("Crop", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        File file = new File(imgs_path_array.get(selectedPos));
                        CropImage.activity(Uri.fromFile(file))
                                .start(CustomCameraActivity.this);

                    }
                }).setNegativeButton("Zoom", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                Intent intent = new Intent(CustomCameraActivity.this, PreviewZoomActivity.class);
                intent.putExtra("image_path", imgs_path_array.get(selectedPos));
                startActivity(intent);

            }
        }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                unlock();
                dialog.dismiss();

            }
        });

        builder.create().show();
    }


}