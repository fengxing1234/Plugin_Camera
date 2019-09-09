package com.picc.plugin_camera.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class ScreenUtils {

    private static final String TAG = ScreenUtils.class.getSimpleName();


    /**
     * 获取的则是真正原始的屏幕尺寸，纯天然无公害的屏幕尺寸，二者获的Width相同，Height不同。
     * <p>
     * 官网描述的信息 相等 分辨率 2280*1080
     * onePlus6
     * width: 1080 height = 2280 density = 2.625 densityDpi = 420
     *
     * @param context
     */
    public static Point getScreenRawPixels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return null;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return null;
        }
        DisplayMetrics metric = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metric);
        int width = metric.widthPixels; // 宽度（PX）
        int height = metric.heightPixels; // 高度（PX）
        float density = metric.density; // 密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi;
        Log.d(TAG, "width: " + width + " height = " + height + " density = " + density + " densityDpi = " + densityDpi);
        Point point = new Point();
        point.x = width;
        point.y = height;
        return point;
    }

    /**
     * 去除虚拟按键后的尺寸
     * onePlus6
     * width: 1080 height = 2075 density = 2.625 densityDpi = 420
     *
     * @param context
     */
    public static Point getScreenPixels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return null;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return null;
        }
        DisplayMetrics metric = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels; // 宽度（PX）
        int height = metric.heightPixels; // 高度（PX）
        float density = metric.density; // 密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi;
        Log.d(TAG, "width: " + width + " height = " + height + " density = " + density + " densityDpi = " + densityDpi);
        Point point = new Point();
        point.x = width;
        point.y = height;
        return point;
    }

    /**
     * 判断Activity是横屏还是竖屏
     *
     * @param context
     * @return
     */
    public static int getScreenOrientation(Context context) {

        Configuration configuration = context.getResources().getConfiguration(); //获取设置的配置信息
        return configuration.orientation; //获取屏幕方向
    }

    /**
     * 返回屏幕自然方向
     *
     * @param context
     * @return
     */
    public static int getRotation(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager == null) {
            return -1;
        }
        return manager.getDefaultDisplay().getRotation();
    }
}
