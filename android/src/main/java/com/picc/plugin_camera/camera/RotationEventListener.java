package com.picc.plugin_camera.camera;

import android.content.Context;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;

import static android.view.OrientationEventListener.ORIENTATION_UNKNOWN;

public abstract class RotationEventListener {

    private static final String TAG = "RotationEventListener";
    private static final int THRESHOLD = 45;
    private final OrientationEventListener listener;
    private int mCurrentDegrees = -1;
    private int mThreshold = THRESHOLD;

    public RotationEventListener(final Context context) {

        listener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {
                //转换成顺时针方向
                int degrees = (360 - orientation) % 360;
                int rotation = getRotation(degrees);
                //只在角度变化时回调
                if (rotation != mCurrentDegrees && rotation != ORIENTATION_UNKNOWN) {
                    Log.d(TAG, "orientation : " + orientation + "  rotation = " + rotation);
                    onRotationChanged(orientation, rotation, mCurrentDegrees);
                    mCurrentDegrees = rotation;
                }

            }
        };
    }

    /**
     * 旋转的阀值
     *
     * @param threshold
     */
    public void setThreshold(int threshold) {
        if (threshold < 0 || threshold > 90) {
            return;
        }
        this.mThreshold = threshold;
    }

    public int getCurrentDegrees() {
        return mCurrentDegrees;
    }

    /**
     * @param orientation
     * @return
     */
    private int getRotation(int orientation) {
        if (orientation > 360 - mThreshold || orientation < mThreshold) { //0度
            return Surface.ROTATION_0;
        } else if (orientation > (90 - mThreshold) && orientation < (90 + mThreshold)) { //90度
            return Surface.ROTATION_90;
        } else if (orientation > 180 - mThreshold && orientation < 180 + mThreshold) { //180度
            return Surface.ROTATION_180;
        } else if (orientation > 270 - mThreshold && orientation < 270 + mThreshold) { //270度
            return Surface.ROTATION_270;
        } else {
            return ORIENTATION_UNKNOWN;
        }
    }


    public static int getDegrees(int rotation) {
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }

    /**
     * @param orientation 原始 角度 逆时针
     * @param newRotation 四个方向 目前在那个方向 对应  Surface.ROTATION_0
     * @param oldRotation
     */
    protected abstract void onRotationChanged(int orientation, int newRotation, int oldRotation);


    public void enable() {
        if (listener != null) {
            listener.enable();
        }
    }

    public void disable() {
        if (listener != null) {
            listener.disable();
        }
    }
}
