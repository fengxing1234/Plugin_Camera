package com.picc.plugin_camera.camera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;

import com.picc.plugin_camera.ScreenUtils;

import java.io.File;
import java.util.List;

import static android.view.OrientationEventListener.ORIENTATION_UNKNOWN;

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
            try {
                PictureTask pictureTask = new PictureTask(imageWorkerContext);
                pictureTask.execute(data);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //连拍功能
                mCamera.stopPreview();// 关闭预览
                mCamera.startPreview();// 开启预览
                safeToTakePicture = true;
            }
        }
    };

    private ImageWorkerContext imageWorkerContext = new ImageWorkerContext() {
        @Override
        public boolean preCheck(byte[] data) {
            return false;
        }

        @Override
        public File[] createImageFile() {
            return generateNewImageFile();
        }

        @Override
        public void onFileSaved(File image, File thumb) {

        }

        @Override
        public void onTaskComplete(String s) {

        }

        @Override
        public void proExecute() {

        }
    };

    /**
     * 文件保存位置
     *
     * @return
     */
    private File[] generateNewImageFile() {
        File images = mContext.getExternalFilesDir("images");
        File file = new File(images, System.currentTimeMillis() + ".png");
        return new File[]{file};
    }

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

    public void setPreview(CameraPreview preview) {
        this.mPreview = preview;
    }

    public void releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "releaseCamera: " + e.getMessage());
        }
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
        setFlushMode(Camera.Parameters.FLASH_MODE_TORCH);
    }

    /**
     * 闪光模式
     * <p>
     * 如果摄像头不支持这些参数都会出错的，所以设置的时候一定要判断是否支持
     *
     * @param flushMode
     */
    public void setFlushMode(String flushMode) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes != null && supportedFlashModes.contains(flushMode)) {
            parameters.setFlashMode(flushMode);
        }
        mCamera.setParameters(parameters);
    }

    public void startPreview() {
        Log.d(TAG, "startPreview: " + mCamera);
        setCameraDisplayOrientation(mCurrentCameraId, mCamera);
        setCameraSize();
        setCameraOther();
        mPreview.setCamera(mCamera);
        safeToTakePicture = true;
    }

    private void setCameraOther() {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPictureFormat(ImageFormat.JPEG); // 设置拍照图片格式
        parameters.setExposureCompensation(0); // 设置曝光强度
        mCamera.setParameters(parameters);
    }

    private void setParameterZoom() {
        if (mCamera == null) {
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        int zoom = parameters.getZoom();
        parameters.setZoom(1);
        mCamera.setParameters(parameters);
        Log.d(TAG, "setParameterZoom: " + zoom);
    }

    public void setCameraSize() {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        //设置预览尺寸
        Point previewResolution = CameraUtils.findBestPreviewResolution(parameters, ScreenUtils.getScreenRawPixels(mContext), ScreenUtils.getScreenOrientation(mContext), ScreenUtils.getRotation(mContext));
        parameters.setPreviewSize(previewResolution.x, previewResolution.y);

        //设置图片尺寸
        Point pictureResolution = CameraUtils.findBestPictureResolution2(parameters, ScreenUtils.getScreenRawPixels(mContext), ScreenUtils.getScreenOrientation(mContext), ScreenUtils.getRotation(mContext));
        Log.d(TAG, "setCameraSize: " + pictureResolution.x + "," + pictureResolution.y);
        parameters.setPictureSize(pictureResolution.x, pictureResolution.y);


        mCamera.setParameters(parameters);
    }

    /**
     * 触摸对焦 定点对焦
     *
     * @param focusView
     * @param x
     * @param y
     */
    public void touchFocus(FocusCameraView focusView, float x, float y) {
        //checkUseFocus(x, y, focusView.getWidth(), focusView.getHeight());
        focusView.setTouchFocusRect(mCamera, x, y);
    }


    /**
     * 设置预览方向
     * https://glumes.com/post/android/android-camera-aspect-ratio--and-orientation/
     *
     * @param cameraId
     * @param camera
     */
    private void setCameraDisplayOrientation(int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
        Log.d(TAG, "setCameraDisplayOrientation: " + rotation);
        int degrees = RotationEventListener.getDegrees(rotation);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * 拍摄相片旋转问题
     *
     * @param
     * @param orientation
     */
    public void setOrientationChanged(int orientation) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (orientation == ORIENTATION_UNKNOWN) {
            return;
        }
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(mCurrentCameraId, info);

        orientation = (orientation + 45) / 90 * 90;
        int rotation = 0;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {  // back-facing camera
            rotation = (info.orientation + orientation) % 360;
        }
        parameters.setRotation(rotation);
        mCamera.setParameters(parameters);
    }

}
