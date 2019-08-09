package com.picc.plugin_camera.camera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
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

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
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
        boolean on = false;
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            String flashMode = parameters.getFlashMode();
            on = !flashMode.equals(Camera.Parameters.FLASH_MODE_ON);
        }
        if (!on) {
            turnOnFlash();
        } else {
            turnOffFlash();
        }

    }

    public void turnOnFlash() {
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void turnOffFlash() {
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setPreview(CameraPreview preview) {
        this.mPreview = preview;
    }

    public void startPreview() {
        Log.d(TAG, "startPreview: " + mCamera);
        setCameraDisplayOrientation(mCurrentCameraId, mCamera);
        setCameraSize();
        setParameterZoom();
        mPreview.setCamera(mCamera);
        safeToTakePicture = true;
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
        //mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();

        Camera.Size pictureSize = parameters.getPictureSize();
        Log.d(TAG, "pictureSize = height: " + pictureSize.height + "  width : " + pictureSize.width);

        Camera.Size previewSize = parameters.getPreviewSize();
        Log.d(TAG, "previewSize = height: " + previewSize.height + "  width : " + previewSize.width);

        //height: 2448  width : 3264 未设置前 图片的宽高
        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        for (int i = 0; i < pictureSizes.size(); i++) {
            Camera.Size size = pictureSizes.get(i);
            Log.d(TAG, "height: " + size.height + "  width : " + size.width);
        }
        Camera.Size picSize = getProperSize(pictureSizes, ((float) 1920) / 1080);
        if (picSize != null) {
            //parameters.setPictureSize(picSize.width, picSize.height);
            Log.d(TAG, "picSize1 = height: " + picSize.height + "  width : " + picSize.width);
        } else {
            picSize = parameters.getPictureSize();
            Log.d(TAG, "picSize2 = height: " + picSize.height + "  width : " + picSize.width);
        }

        /*获取摄像头支持的PreviewSize列表*/
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        for (int i = 0; i < pictureSizes.size(); i++) {
            Camera.Size size = previewSizeList.get(i);
            Log.d(TAG, "previewSize = height: " + size.height + "  width : " + size.width);
        }
        Camera.Size preSize = getProperSize(previewSizeList, ((float) 1920) / 1080);
        if (null != preSize) {
            Log.d("preSize", preSize.width + "," + preSize.height);
            //parameters.setPreviewSize(preSize.width, preSize.height);
        }

        /*根据选出的PictureSize重新设置SurfaceView大小*/
        float w = picSize.width;
        float h = picSize.height;
        mPreview.setLayoutParams(new FrameLayout.LayoutParams(1080, 1920));
        parameters.setJpegQuality(100); // 设置照片质量

        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }


        mCamera.setParameters(parameters);
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

    public void touchFocus(FocusCameraView focusView, float x, float y) {
        //checkUseFocus(x, y, focusView.getWidth(), focusView.getHeight());
        focusView.setTouchFocusRect(mCamera, x, y);
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

    /**
     * 预览图片的分辨率选择逻辑是：有1920*1080则选之，否则选硬件支持的最大的分辨率，且满足图片比例为16：9
     *
     * @param sizeList
     * @param screenResolution
     * @return
     */
    private static Point findBestPreviewSizeValue(List<Camera.Size> sizeList, Point screenResolution) {
//        int bestX = 0;
//        int bestY = 0;
//        int size = 0;
//        for (int i = 0; i < sizeList.size(); i++) {
//            // 如果有符合的分辨率，则直接返回
//            if (sizeList.get(i).width == DEFAULT_WIDTH && sizeList.get(i).height == DEFAULT_HEIGHT) {
//                Log.d(TAG, "get default preview size!!!");
//                return new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
//            }
//
//            int newX = sizeList.get(i).width;
//            int newY = sizeList.get(i).height;
//            int newSize = Math.abs(newX * newX) + Math.abs(newY * newY);
//            float ratio = (float) newY / (float) newX;
//            Log.d(TAG, newX + ":" + newY + ":" + ratio);
//            if (newSize >= size && ratio != 0.75) {  // 确保图片是16：9的
//                bestX = newX;
//                bestY = newY;
//                size = newSize;
//            } else if (newSize < size) {
//                continue;
//            }
//        }
//
//        if (bestX > 0 && bestY > 0) {
//            return new Point(bestX, bestY);
//        }
        return null;
    }


    /**
     * 在硬件支持的拍照图片分辨率列表中，拍照图片分辨率选择逻辑：
     * <p>
     * 有1920*1080则选之
     * 选择大于屏幕分辨率且图片比例为16:9的
     * 选择图片分辨率尽可能大且图片比例为16:9的
     *
     * @param sizeList
     * @param screenResolution
     * @return
     */
    private static Point findBestPictureSizeValue(List<Camera.Size> sizeList, Point screenResolution) {
//        List<Camera.Size> tempList = new ArrayList<>();
//
//        for (int i = 0; i < sizeList.size(); i++) {
//            // 如果有符合的分辨率，则直接返回
//            if (sizeList.get(i).width == DEFAULT_WIDTH && sizeList.get(i).height == DEFAULT_HEIGHT) {
//                Log.d(TAG, "get default picture size!!!");
//                return new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
//            }
//            if (sizeList.get(i).width >= screenResolution.x && sizeList.get(i).height >= screenResolution.y) {
//                tempList.add(sizeList.get(i));
//            }
//        }
//
//        int bestX = 0;
//        int bestY = 0;
//        int diff = Integer.MAX_VALUE;
//        if (tempList != null && tempList.size() > 0) {
//            for (int i = 0; i < tempList.size(); i++) {
//                int newDiff = Math.abs(tempList.get(i).width - screenResolution.x) + Math.abs(tempList.get(i).height - screenResolution.y);
//                float ratio = (float) tempList.get(i).height / tempList.get(i).width;
//                Log.d(TAG, "ratio = " + ratio);
//                if (newDiff < diff && ratio != 0.75) {  // 确保图片是16：9的
//                    bestX = tempList.get(i).width;
//                    bestY = tempList.get(i).height;
//                    diff = newDiff;
//                }
//            }
//        }
//
//        if (bestX > 0 && bestY > 0) {
//            return new Point(bestX, bestY);
//        } else {
//            return findMaxPictureSizeValue(sizeList);
//        }
        return null;
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
    public void orientationChanged(int orientation) {
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
