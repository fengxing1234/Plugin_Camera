package com.picc.plugin_camera.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

import com.picc.plugin_camera.R;


public class SimpleCameraActivity extends Activity {

    private static final String TAG = SimpleCameraActivity.class.getSimpleName();

    private CameraOperationHelper mCameraHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera_activity);

        CameraPreview preview = new CameraPreview(this);
        FrameLayout previewContainer = (FrameLayout) findViewById(R.id.camera_preview);
        previewContainer.addView(preview);

        mCameraHelper = CameraOperationHelper.getInstance(this);
        Camera camera = mCameraHelper.safeOpenCamera();
        mCameraHelper.setPreview(preview);
        new IOrientationEventListener(this);

//        mCameraHelper.startPreview();
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        //这个是实现相机拍照的主要方法，包含了三个回调参数。shutter是快门按下时的回调，raw是获取拍照原始数据的回调，jpeg是获取经过压缩成jpg格式的图像数据的回调。
                        mCameraHelper.takePicture();
                    }
                }
        );

        Button btnSwitchCamera = findViewById(R.id.btn_switch_camera);
        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraHelper.doSwitchCamera();
            }
        });

        Button btnFlushMode = findViewById(R.id.btn_flush_mode);
        btnFlushMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraHelper.doSwitchFlush();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        mCameraHelper.safeOpenCamera();
        mCameraHelper.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        mCameraHelper.releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mCameraHelper.releaseCamera();
        mCameraHelper = null;
    }

}
