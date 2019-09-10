package com.picc.plugin_camera.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.picc.plugin_camera.R;
import com.picc.plugin_camera.picture.Gallery;
import com.picc.plugin_camera.picture.PictureManager;
import com.picc.plugin_camera.picture.type.OfflineType;
import com.picc.plugin_camera.utils.FakeBoldSpan;
import com.picc.plugin_camera.utils.Spanny;
import com.picc.plugin_camera.widgets.CircleImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SimpleCameraActivity extends Activity {

    private static final String TAG = SimpleCameraActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE_CAMERA = 0x10;
    public static final String PICTURE_TYPE = "Picture_Type";

    private CameraOperationHelper mCameraHelper;
    private FocusCameraView focusView;
    private ImageView ivBack;
    private ImageView ivSwitchCamera;
    private ImageView ivFlushMode;
    private ImageView ivImportImage;
    private CircleImageView ivThumbnail;
    private ImageButton ivTakePicture;
    private ImageButton ivPictureType;
    private String type;
    private Gallery mGalley;
    private boolean isOffline;

    public static void startSimpleCameraActivity(Context context, String type) {
        Intent intent = new Intent(context, SimpleCameraActivity.class);
        intent.putExtra(PICTURE_TYPE, type);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.camera_activity);
        mCameraHelper = CameraOperationHelper.getInstance(this);
        initData();
        initView();
        if (checkPermission()) {
            initCamera();
        }
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getStringExtra(PICTURE_TYPE);
            if (OfflineType.OFFLINE_GALLERY.equals(type)) {
                isOffline = true;
            }
        }
        mGalley = new Gallery(this, PictureManager.getInstance().findPictureType(type));

    }

    private void initCamera() {
        CameraPreview preview = new CameraPreview(this);
        FrameLayout previewContainer = (FrameLayout) findViewById(R.id.camera_preview);
        focusView = (FocusCameraView) findViewById(R.id.over_camera_view);
        previewContainer.addView(preview);
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

            @Override
            public File[] generateNewImageFile() {
                String imgPath;
                if (isOffline) {
                    imgPath = "offLineCamera";
                } else {
                    imgPath = PictureManager.getInstance().getRootCase().getRegisterNo();
                }
                return mGalley.generateNewImageFile(imgPath);
            }

            @Override
            public void onThumbnailSaveDone(final Bitmap thumbnail, File thumb) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivThumbnail.setBitmap(thumbnail);
                    }
                });
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
                showBottomSheetDialog();
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
                showSelectPictureType();
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

    private void showSelectPictureType() {
        final BottomSheetDialog bottomSheet = new BottomSheetDialog(this);//实例化BottomSheetDialog
        bottomSheet.setCancelable(true);//设置点击外部是否可以取消
        bottomSheet.setContentView(R.layout.camera_select_picture_type);//设置对框框中的布局
        bottomSheet.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView tvTitle = bottomSheet.findViewById(R.id.tv_title);
        tvTitle.setText(new Spanny().append("选择照片类型", new FakeBoldSpan(getResources().getColor(R.color.text_color_0))));
        RecyclerView recyclerView = bottomSheet.findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new SelectPictureTypeAdapter(getList()));
        bottomSheet.show();
    }

    public List<SelectPictureTypeData> getList() {
        List<SelectPictureTypeData> mData = new ArrayList<>();
        mData.add(getTestData("简易赔案协议书(2/10)", true, true));
        mData.add(getTestData("被保险人身份证明、房屋产权证或租赁合同(2/10)", false, true));
        mData.add(getTestData("银行账号(2/10)", false, true));
        mData.add(getTestData("保险单、保险凭证(2/10)", false, true));
        mData.add(getTestData("相关票据、事故证明(2/10)", false, true));
        mData.add(getTestData("其他(2/10)", false, false));
        return mData;
    }

    public SelectPictureTypeData getTestData(String type, boolean isSelected, boolean isMust) {
        SelectPictureTypeData data = new SelectPictureTypeData();
        data.isSelected = isSelected;
        data.pictureType = type;
        data.currentCount = 2;
        data.maxCount = 10;
        data.isMust = isMust;
        return data;
    }

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheet = new BottomSheetDialog(this);//实例化BottomSheetDialog
        bottomSheet.setCancelable(true);//设置点击外部是否可以取消
        bottomSheet.setContentView(R.layout.camera_select_picture_source);//设置对框框中的布局
        bottomSheet.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bottomSheet.findViewById(R.id.tv_source_offline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SimpleCameraActivity.this, "离线相册", Toast.LENGTH_SHORT).show();
                bottomSheet.cancel();
            }
        });

        bottomSheet.findViewById(R.id.tv_source_mobile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SimpleCameraActivity.this, "手机相册", Toast.LENGTH_SHORT).show();
                bottomSheet.cancel();
            }
        });
        bottomSheet.findViewById(R.id.tv_source_case_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SimpleCameraActivity.this, "案件相册", Toast.LENGTH_SHORT).show();
                bottomSheet.cancel();
            }
        });

        bottomSheet.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheet.cancel();
            }
        });
        bottomSheet.show();
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
