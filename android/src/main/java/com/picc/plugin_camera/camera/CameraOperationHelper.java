package com.picc.plugin_camera.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * 相机操作功能类
 * 采用单例模式来统一管理相机资源，封装相机API的直接调用，
 * 并提供用于跟自定义相机Activity做UI交互的回调接口，
 * 其功能函数如下，主要有创建\释放相机，连接\开始\关闭预览界面，拍照，自动对焦，切换前后摄像头，切换闪光灯模式等
 */
public class CameraOperationHelper {

    private static final String TAG = CameraOperationHelper.class.getSimpleName();

    private volatile static CameraOperationHelper instance;

    private Context mContext;
    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean safeToTakePicture = false;
    protected int CAMERA_ID_FRONT;
    protected int CAMERA_ID_BACK;
    private int mCurrentCameraId;
    private boolean mCameraReady;


    private CameraOperationHelper(Context context) {
        this.mContext = context;
    }

    public static CameraOperationHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (CameraOperationHelper.class) {
                if (instance == null) {
                    instance = new CameraOperationHelper(context);
                }
            }
        }
        return instance;
    }

    private Camera.PictureCallback mPictureRow = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken: " + data);
        }
    };

    /**
     *
     */
    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.d(TAG, "onShutter: ");
//            AudioManager mgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
        }
    };

    /**
     * 创建jpeg图片回调数据对象
     */
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            } finally {
                //连拍功能
                mCamera.stopPreview();// 关闭预览
                mCamera.startPreview();// 开启预览
                safeToTakePicture = true;
            }
        }
    };

    /**
     * 切换相机 和 第一次创建相机 会调用 标示已经准备好了 可以拍了
     */
    private Camera.PreviewCallback mOneShotPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Log.d(TAG, "onPreviewFrame: ");
            mCameraReady = true;
        }
    };


    /**
     * 图片保存路径
     *
     * @param mediaTypeImage
     * @return
     */
    private File getOutputMediaFile(int mediaTypeImage) {
        File images = mContext.getExternalFilesDir("images");
        return new File(images, System.currentTimeMillis() + ".png");
    }

    /**
     * 安全的打开摄像头
     */
    public Camera safeOpenCamera() {
        try {
            initAvailableCameraId();
            boolean hasCamera = mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
            Log.d(TAG, "是否有相机资源: " + hasCamera);
            //获取camera实例。attempt to get a Camera instance
            if (hasCamera) {
                mCamera = Camera.open(mCurrentCameraId);
                mCamera.setOneShotPreviewCallback(mOneShotPreviewCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }

    private int faceBackCameraId;
    private int faceBackCameraOrientation;

    private int faceFrontCameraId;
    private int faceFrontCameraOrientation;

    private void getCameraInfo() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            //后置摄像头
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                faceBackCameraId = i;
                faceBackCameraOrientation = cameraInfo.orientation;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//前置摄像头
                faceFrontCameraId = i;
                faceFrontCameraOrientation = cameraInfo.orientation;
            }
        }
    }

    private void getCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        //Camera.Parameters.FLASH_MODE_AUTO 自动模式，当光线较暗时自动打开闪光灯；
        //Camera.Parameters.FLASH_MODE_OFF 关闭闪光灯；
        //Camera.Parameters.FLASH_MODE_ON 拍照时闪光灯；
        //Camera.Parameters.FLASH_MODE_RED_EYE 闪光灯参数，防红眼模式。
        String flashMode = parameters.getFlashMode();
        Log.d(TAG, "flashMode: " + flashMode);
        //Camera.Parameters.FOCUS_MODE_AUTO 自动对焦模式，摄影小白专用模式；
        //Camera.Parameters.FOCUS_MODE_FIXED 固定焦距模式，拍摄老司机模式；
        //Camera.Parameters.FOCUS_MODE_EDOF 景深模式，文艺女青年最喜欢的模式；
        //Camera.Parameters.FOCUS_MODE_INFINITY 远景模式，拍风景大场面的模式；
        //Camera.Parameters.FOCUS_MODE_MACRO 微焦模式，拍摄小花小草小蚂蚁专用模式；
        String focusMode = parameters.getFocusMode();
        Log.d(TAG, "focusMode: " + focusMode);
        //Camera.Parameters.SCENE_MODE_BARCODE 扫描条码场景，NextQRCode项目会判断并设置为这个场景；
        //Camera.Parameters.SCENE_MODE_ACTION 动作场景，就是抓拍跑得飞快的运动员、汽车等场景用的；
        //Camera.Parameters.SCENE_MODE_AUTO 自动选择场景；
        //Camera.Parameters.SCENE_MODE_HDR 高动态对比度场景，通常用于拍摄晚霞等明暗分明的照片；
        //Camera.Parameters.SCENE_MODE_NIGHT 夜间场景；
        String sceneMode = parameters.getSceneMode();
        Log.d(TAG, "sceneMode: " + sceneMode);

    }


    public void takePicture() {
        try {
            if (safeToTakePicture && mCameraReady) {
                mCamera.takePicture(mShutterCallback, mPictureRow, mPictureCallback);
                safeToTakePicture = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "takePicture: " + e.getMessage());
            releaseCamera();
            safeOpenCamera();
            startPreview();
        }

    }

    /**
     * 找出前置摄像头和后置摄像头对应的 id，
     * -1 表示不支持该摄像头
     */
    private void initAvailableCameraId() {
        CAMERA_ID_BACK = -1;
        CAMERA_ID_FRONT = -1;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                CAMERA_ID_BACK = i;
            }

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                CAMERA_ID_FRONT = i;
            }
        }
        Log.d(TAG, "CAMERA_ID_BACK: " + CAMERA_ID_BACK + "::: CAMERA_ID_FRONT : " + CAMERA_ID_FRONT);
    }

    /**
     * 切换前后摄像头
     */
    public void doSwitchCamera() {
        if (!mCameraReady) {
            return;
        }
        mCameraReady = false;
        if (mCurrentCameraId == CAMERA_ID_BACK) {
            mCurrentCameraId = CAMERA_ID_FRONT;
        } else {
            mCurrentCameraId = CAMERA_ID_BACK;
        }

        Log.d(TAG, "doSwitchCamera: " + mCurrentCameraId);
        releaseCamera();
        safeOpenCamera();
        startPreview();
    }


    public void doSwitchFlush() {

    }

    public void setPreview(CameraPreview preview) {
        this.mPreview = preview;
    }

    public void startPreview() {
        Log.d(TAG, "startPreview: " + mCamera);
        setCameraParameters();
        mPreview.setCamera(mCamera);
        safeToTakePicture = true;
    }

    public void setCameraParameters() {
        if (mCamera == null) {
            return;
        }
        mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();

        //height: 2448  width : 3264 未设置前 图片的宽高
        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        for (int i = 0; i < pictureSizes.size(); i++) {
            Camera.Size size = pictureSizes.get(i);
            Log.d(TAG, "height: " + size.height + "  width : " + size.width);
        }
        Camera.Size picSize = getProperSize(pictureSizes, ((float) 1920) / 1080);
        if (picSize != null) {
            parameters.setPictureSize(picSize.width, picSize.height);
            Log.d(TAG, "picSize1 = height: " + picSize.height + "  width : " + picSize.width);
        } else {
            picSize = parameters.getPictureSize();
            Log.d(TAG, "picSize2 = height: " + picSize.height + "  width : " + picSize.width);
        }

        /*获取摄像头支持的PreviewSize列表*/
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        Camera.Size preSize = getProperSize(previewSizeList, ((float) 1920) / 1080);
        if (null != preSize) {
            Log.d("preSize", preSize.width + "," + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }

        /*根据选出的PictureSize重新设置SurfaceView大小*/
        float w = picSize.width;
        float h = picSize.height;
        mPreview.setLayoutParams(new FrameLayout.LayoutParams(1080, 1920));
        parameters.setJpegQuality(100); // 设置照片质量
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCamera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
        //mCamera.setDisplayOrientation(0);
        mCamera.setParameters(parameters);

        // 连续对焦模式
        //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        //自动聚焦模式
        //Camera.Parameters.FOCUS_MODE_AUTO;
        //无穷远
        //Camera.Parameters.FOCUS_MODE_INFINITY;
        //微距
        //Camera.Parameters.FOCUS_MODE_MACRO;
        //固定焦距
        //Camera.Parameters.FOCUS_MODE_FIXED;


    }

    public Camera.Size getProperSize(List<Camera.Size> sizeList, float displayRatio) {
        //先对传进来的size列表进行排序
        Collections.sort(sizeList, new SizeComparator());

        Camera.Size result = null;
        for (Camera.Size size : sizeList) {
            float curRatio = ((float) size.width) / size.height;
            if (curRatio - displayRatio == 0) {
                result = size;
            }
        }
        if (null == result) {
            for (Camera.Size size : sizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 3f / 4) {
                    result = size;
                }
            }
        }
        return result;
    }

    public void orientationChanged(int orientation) {
        Log.d(TAG, "orientationChanged: " + orientation);
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        orientation = (orientation + 45) / 90 * 90;
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {
            rotation = (info.orientation + orientation) % 360;
        }
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setRotation(rotation);
            mCamera.setParameters(parameters);
        }
    }

    static class SizeComparator implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // TODO Auto-generated method stub
            Camera.Size size1 = lhs;
            Camera.Size size2 = rhs;
            if (size1.width < size2.width
                    || size1.width == size2.width && size1.height < size2.height) {
                return -1;
            } else if (!(size1.width == size2.width && size1.height == size2.height)) {
                return 1;
            }
            return 0;
        }

    }
}
