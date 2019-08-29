package com.picc.plugin_camera.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.picc.plugin_camera.R;


public class SimpleCameraActivity extends Activity {

    private static final String TAG = SimpleCameraActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE_CAMERA = 0x10;

    private CameraOperationHelper mCameraHelper;
    private FocusCameraView focusView;


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
        focusView = (FocusCameraView) findViewById(R.id.over_camera_view);
        previewContainer.addView(preview);
        mCameraHelper = CameraOperationHelper.getInstance(this);
        mCameraHelper.setPreview(preview);
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
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCameraHelper.touchFocus(focusView, event.getX(), event.getY());
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (checkPermission()) {
            initCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        mCameraHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mCameraHelper.releaseCamera();
        mCameraHelper = null;
    }

    private String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private boolean checkPermission() {
        //是否需要校验
        boolean checkNeed = false;
        for (int i = 0; i < permissions.length; i++) {
            boolean b = ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "b : " + b);
            checkNeed = checkNeed | b;
            Log.d(TAG, "checkPermission: " + checkNeed);
        }
        if (checkNeed) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this).setTitle("应用需要摄像头权限才能拍摄照片").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(SimpleCameraActivity.this,
                                permissions,
                                PERMISSIONS_REQUEST_CODE_CAMERA);
                    }
                }).setNegativeButton("离开", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();

            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        PERMISSIONS_REQUEST_CODE_CAMERA);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initCamera();
                } else {
                    new AlertDialog.Builder(this).setTitle("无法打开摄像头!").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    private void initCamera() {
        mCameraHelper.onResume();
    }

}
