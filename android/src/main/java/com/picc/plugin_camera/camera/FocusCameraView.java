package com.picc.plugin_camera.camera;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 对焦矩形框
 */
public class FocusCameraView extends View {

    private static final String TAG = FocusCameraView.class.getSimpleName();
    public int mEdgeWidth;
    //聚焦矩形框 半径
    public int mRectRadius;
    //边线长度
    public int mEdgeLength;

    private Paint mPaint;
    private Rect touchFocusRect;
    private Rect targetFocusRect;

    public FocusCameraView(Context context) {
        this(context, null);
    }

    public FocusCameraView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusCameraView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //画笔设置
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mEdgeLength = 20;
        //mRectRadius = (int) dipToPixels(getContext(), 100);
        mRectRadius = 200;
        Log.d(TAG, "init: " + mRectRadius);
        //mRectRadius = 100;
        mEdgeWidth = 2;
    }

    public void setColor(int color) {
        if (mPaint != null) {
            mPaint.setColor(color);
        }
    }

    //对焦并绘制对焦矩形框
    public void setTouchFocusRect(Camera camera, float x, float y) {
        Log.d(TAG, "X : " + x + " Y = " + y);
        //以焦点为中心
        touchFocusRect = new Rect((int) (x - mRectRadius), (int) (y - mRectRadius), (int) (x + mRectRadius), (int) (y + mRectRadius));
        Log.d(TAG, "touchFocusRect: " + touchFocusRect);
        //对焦区域
        targetFocusRect = new Rect(
                clamp((touchFocusRect.left * 2000 / this.getWidth() - 1000), -1000, 1000),
                clamp((touchFocusRect.top * 2000 / this.getHeight() - 1000), -1000, 1000),
                clamp((touchFocusRect.right * 2000 / this.getWidth() - 1000), -1000, 1000),
                clamp((touchFocusRect.bottom * 2000 / this.getHeight() - 1000), -1000, 1000));
        Log.d(TAG, "targetFocusRect : " + targetFocusRect);
        doTouchFocus(camera, targetFocusRect);//对焦
        postInvalidate();//刷新界面，调用onDraw(Canvas canvas)函数绘制矩形框
    }

    private FocusCallback mFocusCallback;

    public void setFocusCallback(FocusCallback callback) {
        this.mFocusCallback = callback;
    }

    //不大于最大值，不小于最小值
    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }


    //设置camera参数，并完成对焦
    public void doTouchFocus(Camera camera, Rect tfocusRect) {
        if (camera == null) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        int maxNumFocusAreas = parameters.getMaxNumFocusAreas();
        Log.d(TAG, "maxNumFocusAreas: " + maxNumFocusAreas);
        if (maxNumFocusAreas <= 0) {
            disDrawTouchFocusRect();
            //大部分手机这个方法的返回值，前置摄像头都是0，后置摄像头都是1，说明前置摄像头一般不支持设置聚焦，而后置摄像头一般也只支持单个区域的聚焦。
            return;
        }
        try {
            camera.cancelAutoFocus();
            final List<Camera.Area> focusList = new ArrayList<>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);//相机参数：对焦区域
            focusList.add(focusArea);

            Camera.Parameters para = camera.getParameters();
            para.setFocusAreas(focusList);
            int maxNumMeteringAreas = para.getMaxNumMeteringAreas();
            Log.d(TAG, "maxNumMeteringAreas : " + maxNumMeteringAreas);
            if (maxNumMeteringAreas > 0) {
                para.setMeteringAreas(focusList);
            }
            final String currentFocusMode = parameters.getFocusMode();
            Log.d(TAG, "doTouchFocus: " + currentFocusMode);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            camera.setParameters(para);//相机参数生效
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    Log.d(TAG, "onAutoFocus: " + success);
                    if (success) {
                        startAnimator();
                    } else {
                        disDrawTouchFocusRect();
                    }

                    //回调后 还原模式
                    Camera.Parameters params = camera.getParameters();
                    params.setFocusMode(currentFocusMode);
                    camera.setParameters(params);
                    if (mFocusCallback != null) {
                        mFocusCallback.focusComplete(success);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAnimator() {
        animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "onAnimationStart: ");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "onAnimationEnd: ");
                disDrawTouchFocusRect();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).alpha(0.5f).setDuration(1000);
    }

    //对焦完成后，清除对焦矩形框
    public void disDrawTouchFocusRect() {
        touchFocusRect = null;//将对焦区域设置为null，刷新界面后对焦框消失
        postInvalidate();//刷新界面，调用onDraw(Canvas canvas)函数
    }


    @Override
    protected void onDraw(Canvas canvas) {
        drawTouchFocusRect(canvas);
        super.onDraw(canvas);
    }

    private void drawTouchFocusRect(Canvas canvas) {
        Log.d(TAG, "drawTouchFocusRect: " + touchFocusRect);
        if (null != touchFocusRect) {
            //根据对焦区域targetFocusRect，绘制自己想要的对焦框样式，本文在矩形四个角取L形状
            //左下角
            canvas.drawRect(touchFocusRect.left - 2, touchFocusRect.bottom, touchFocusRect.left + mEdgeLength, touchFocusRect.bottom + 2, mPaint);
            canvas.drawRect(touchFocusRect.left - 2, touchFocusRect.bottom - mEdgeLength, touchFocusRect.left, touchFocusRect.bottom, mPaint);
            //左上角
            canvas.drawRect(touchFocusRect.left - 2, touchFocusRect.top - 2, touchFocusRect.left + mEdgeLength, touchFocusRect.top, mPaint);
            canvas.drawRect(touchFocusRect.left - 2, touchFocusRect.top, touchFocusRect.left, touchFocusRect.top + mEdgeLength, mPaint);
            //右上角
            canvas.drawRect(touchFocusRect.right - mEdgeLength, touchFocusRect.top - 2, touchFocusRect.right + 2, touchFocusRect.top, mPaint);
            canvas.drawRect(touchFocusRect.right, touchFocusRect.top, touchFocusRect.right + 2, touchFocusRect.top + mEdgeLength, mPaint);
            //右下角
            canvas.drawRect(touchFocusRect.right - mEdgeLength, touchFocusRect.bottom, touchFocusRect.right + 2, touchFocusRect.bottom + 2, mPaint);
            canvas.drawRect(touchFocusRect.right, touchFocusRect.bottom - mEdgeLength, touchFocusRect.right + 2, touchFocusRect.bottom, mPaint);
        }
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public interface FocusCallback {
        void focusComplete(boolean succeed);
    }
}
