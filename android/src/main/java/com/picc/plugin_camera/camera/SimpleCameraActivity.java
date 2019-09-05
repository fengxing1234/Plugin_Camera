package com.picc.plugin_camera.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.picc.plugin_camera.CircularImageView;
import com.picc.plugin_camera.R;


public class SimpleCameraActivity extends Activity {

    private static final String TAG = SimpleCameraActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE_CAMERA = 0x10;

    private CameraOperationHelper mCameraHelper;
    private FocusCameraView focusView;
    private ImageView ivBack;
    private ImageView ivSwitchCamera;
    private ImageView ivFlushMode;
    private ImageView ivImportImage;
    private CircularImageView ivThumbnail;
    private ImageButton ivTakePicture;
    private ImageButton ivPictureType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.camera_activity);
        initView();
        if (checkPermission()) {
            initCamera();
        }
    }

    private void initCamera() {
        CameraPreview preview = new CameraPreview(this);
        FrameLayout previewContainer = (FrameLayout) findViewById(R.id.camera_preview);
        focusView = (FocusCameraView) findViewById(R.id.over_camera_view);
        previewContainer.addView(preview);
        mCameraHelper = CameraOperationHelper.getInstance(this);
        mCameraHelper.setICameraCallback(new CameraOperationHelper.ICameraCallback() {
            @Override
            public void onCameraReady() {
                Log.d(TAG, "onCameraReady: ");
                setupFlushIcon();
            }

            @Override
            public void onRotationChanged(int orientation, int newRotation, int oldRotation) {
                int degrees = RotationEventListener.getDegrees(newRotation);
                ivSwitchCamera.animate().rotation(degrees).start();
                ivFlushMode.animate().rotation(degrees).start();
                ivImportImage.animate().rotation(degrees).start();
                ivThumbnail.animate().rotation(degrees).start();
                ivTakePicture.animate().rotation(degrees).start();
                ivPictureType.animate().rotation(degrees).start();
            }
        });
        mCameraHelper.init(preview);

    }

    private void initView() {
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivSwitchCamera = findViewById(R.id.iv_switch_camera);
        ivFlushMode = findViewById(R.id.iv_flush_mode);
        ivImportImage = findViewById(R.id.iv_import_image);
        ivThumbnail = findViewById(R.id.iv_thumbnail);
        ivTakePicture = findViewById(R.id.iv_take_picture);
        ivPictureType = findViewById(R.id.iv_picture_type);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraHelper.doSwitchCamera();
            }
        });
        ivFlushMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraHelper.doSwitchFlush();
                setupFlushIcon();
            }
        });
        ivImportImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ivTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraHelper.doTakePicture();
            }
        });
        ivPictureType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void setupFlushIcon() {
        ivFlushMode.setImageDrawable(getFlushModeDrawable());
    }

    private Drawable getFlushModeDrawable() {
        Log.d(TAG, "getFlushModeDrawable: " + mCameraHelper);
        if (Camera.Parameters.FLASH_MODE_ON.equals(mCameraHelper.getFlushMode())) {
            return getResources().getDrawable(R.drawable.icon_flush_open);
        }
        if (Camera.Parameters.FLASH_MODE_OFF.equals(mCameraHelper.getFlushMode())) {
            return getResources().getDrawable(R.drawable.icon_flush_mode);
        }
        return getResources().getDrawable(R.drawable.icon_flush_mode);
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
        mCameraHelper.onResume();
        setupFlushIcon();
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
        mCameraHelper.release();
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
}
