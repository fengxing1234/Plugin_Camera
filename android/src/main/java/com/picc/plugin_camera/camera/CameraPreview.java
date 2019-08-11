package com.picc.plugin_camera.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * surfaceholder是控制surface的一个抽象接口，它能够控制surface的尺寸和格式，修改surface的像素，监视surface的变化等等
 * <p>
 * 自定义相机的预览图像由于对更新速度和帧率要求比较高，所以比较适合用surfaceview来显示。
 * <p>
 * surface是指向屏幕窗口原始图像缓冲区（raw buffer）的一个句柄，通过它可以获得这块屏幕上对应的canvas，进而完成在屏幕上绘制View的工作。通过surfaceHolder可以将Camera和surface连接起来，当camera和surface连接后，camera获得的预览帧数据就可以通过surface显示在屏幕上了。
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CameraPreview.class.getSimpleName();
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context) {
        super(context);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    public void setCamera(Camera camera) {
        if (camera == mCamera) {
            return;
        }
        stopPreviewAndFreeCamera();
        Log.d(TAG, "setCamera: " + camera);
        this.mCamera = camera;
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                Log.d(TAG, "setCamera: " + e.getMessage());
                e.printStackTrace();
            }
            mCamera.startPreview();
        }
    }

    /**
     * 在surface创建后立即被调用。
     * 在开发自定义相机时，可以通过重载这个函数调用camera.open()、camera.setPreviewDisplay()，
     * 来实现获取相机资源、连接camera和surface等操作。
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: 创建surfaceCreated");
//        //Surface已创建，现在告诉相机在哪里绘制预览。
//        try {
//            //设置预览方向 如果不设置默认横向
//            mCamera.setDisplayOrientation(90);
//            //绑定绘制预览图像的surface。
//            mCamera.setPreviewDisplay(holder);
//            //开始预览，将camera底层硬件传来的预览帧数据显示在绑定的surface上。
//            mCamera.startPreview();
//        } catch (IOException e) {
//            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
//        }
    }

    /**
     * 在surface发生format或size变化时调用。在开发自定义相机时，
     * 可以通过重载这个函数调用camera.startPreview来开启相机预览，
     * 使得camera预览帧数据可以传递给surface，从而实时显示相机预览图像。
     *
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: surface发生变化");
        Log.d(TAG, "format: " + format + " width = " + width + " height = " + height);
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        //如果您的预览可以更改或旋转，请在此处理这些事件。确保在调整大小或重新格式化之前停止预览。
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        //在进行更改之前停止预览
        // stop preview before making changes
        try {
            //停止预览，关闭camra底层的帧数据传递以及surface上的绘制。
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        //设置预览大小，并在此处进行任何调整大小，旋转或重新格式化更改
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    /**
     * 在surface销毁之前被调用。
     * 在开发自定义相机时，
     * 可以通过重载这个函数调用camera.stopPreview()，camera.release()
     * 来实现停止相机预览及释放相机资源等操作。
     *
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed: surface销毁");
        // empty. Take care of releasing the Camera preview in your activity.
//        mCamera.stopPreview();
//        mCamera.release();
//        mCamera = null;
//        mHolder = null;
        stopPreviewAndFreeCamera();
    }

    private void stopPreviewAndFreeCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
