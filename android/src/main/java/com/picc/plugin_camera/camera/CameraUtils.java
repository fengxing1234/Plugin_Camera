package com.picc.plugin_camera.camera;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CameraUtils {

    private static final String TAG = CameraUtils.class.getSimpleName();
    /**
     * 最小预览界面的分辨率
     */
    private static final int MIN_PREVIEW_PIXELS = 480 * 320;
    /**
     * 最大宽高比差
     */
    private static final double MAX_ASPECT_DISTORTION = 0.15;


    public static String getFlashModeName(String flashMode) {
        switch (flashMode) {
            case Camera.Parameters.FLASH_MODE_AUTO:
                return "自动";
            case Camera.Parameters.FLASH_MODE_ON:
                return "打开";
            case Camera.Parameters.FLASH_MODE_OFF:
                return "关闭";
            case Camera.Parameters.FLASH_MODE_TORCH:
                return "长亮";
            case Camera.Parameters.FLASH_MODE_RED_EYE:
                return "红眼";
            default:
                return null;
        }
    }


    /**
     * 找出最适合的预览界面分辨率
     *
     * @return
     */
    public static Point findBestPreviewResolution(Camera.Parameters cameraParameters, Point screenResolution, int screenOrientation, int cameraOrientation) {
        Camera.Size defaultPreviewResolution = cameraParameters.getPreviewSize(); //默认的预览尺寸
        Log.d(TAG, "camera default resolution " + defaultPreviewResolution.width + "x" + defaultPreviewResolution.height);
        List<Camera.Size> rawSupportedSizes = cameraParameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            Log.w(TAG, "Device returned no supported preview sizes; using default");
            return new Point(defaultPreviewResolution.width, defaultPreviewResolution.height);
        }
        // 按照分辨率从大到小排序
        List<Camera.Size> supportedPreviewResolutions = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });
        printlnSupportedPreviewSize(supportedPreviewResolutions);
        // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
        // 由于camera的分辨率是width>height，这里先判断我们的屏幕和相机的角度是不是相同的方向(横屏 or 竖屏),然后决定比较的时候要不要先交换宽高值
        boolean isCandidatePortrait = screenOrientation % 180 != cameraOrientation % 180;
        double screenAspectRatio = (double) screenResolution.x / (double) screenResolution.y;
        // 移除不符合条件的分辨率
        Iterator<Camera.Size> it = supportedPreviewResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;
            // 移除低于下限的分辨率，尽可能取高分辨率
            if (width * height < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }
            //移除宽高比差异较大的
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }
            // 找到与屏幕分辨率完全匹配的预览界面分辨率直接返回
            if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
                Point exactPoint = new Point(width, height);
                Log.d(TAG, "found preview resolution exactly matching screen resolutions: " + exactPoint);
                return exactPoint;
            }
        }
        // 如果没有找到合适的，并且还有候选的像素，则设置其中最大比例的，对于配置比较低的机器不太合适
        if (!supportedPreviewResolutions.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewResolutions.get(0);
            Point largestSize = new Point(largestPreview.width, largestPreview.height);
            Log.d(TAG, "using largest suitable preview resolution: " + largestSize);
            return largestSize;
        }
        // 没有找到合适的，就返回默认的
        Point defaultResolution = new Point(defaultPreviewResolution.width, defaultPreviewResolution.height);
        Log.d(TAG, "No suitable preview resolutions, using default: " + defaultResolution);
        return defaultResolution;
    }

    private static void printlnSupportedPreviewSize(List<Camera.Size> supportedPreviewSizes) {
        Log.d(TAG, "--------------------Support Preview Size--------------------");
        for (int i = 0; i < supportedPreviewSizes.size(); i++) {
            Log.d(TAG, String.format("(%s,%s)", supportedPreviewSizes.get(i).width, supportedPreviewSizes.get(i).height));
        }
        Log.d(TAG, "------------------------------------------------------------");
    }

    public static Point findBestPictureResolution(Camera.Parameters cameraParameters, Point screenResolution) {
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值

        StringBuilder picResolutionSb = new StringBuilder();
        for (Camera.Size supportedPicResolution : supportedPicResolutions) {
            picResolutionSb.append(supportedPicResolution.width).append('x').append(supportedPicResolution.height).append(" ");
        }
        Log.d(TAG, "Supported picture resolutions: " + picResolutionSb);

        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();
        Log.d(TAG, "default picture resolution " + defaultPictureResolution.width + "x" + defaultPictureResolution.height);

        // 排序
        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<Camera.Size>(supportedPicResolutions);
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) screenResolution.x / (double) screenResolution.y;
        Iterator<Camera.Size> it = sortedSupportedPicResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然后在比较宽高比
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，对于照片，则取其中最大比例的，而不是选择与屏幕分辨率相同的
        if (!sortedSupportedPicResolutions.isEmpty()) {
            Camera.Size largestPreview = sortedSupportedPicResolutions.get(0);
            Point largestSize = new Point(largestPreview.width, largestPreview.height);
            Log.d(TAG, "using largest suitable picture resolution: " + largestSize);
            return largestSize;
        }

        // 没有找到合适的，就返回默认的
        Point defaultResolution = new Point(defaultPictureResolution.width, defaultPictureResolution.height);
        Log.d(TAG, "No suitable picture resolutions, using default: " + defaultResolution);

        return defaultResolution;
    }

    /**
     * 屏幕尺寸
     * @param cameraParameters
     * @param screenResolution
     * @param screenOrientation
     * @param cameraOrientation
     * @return
     */
    public static Point findBestPictureResolution2(Camera.Parameters cameraParameters, Point screenResolution, int screenOrientation, int cameraOrientation) {
        Camera.Size defaultPreviewResolution = cameraParameters.getPictureSize(); //默认的预览尺寸
        Log.d(TAG, "camera default resolution " + defaultPreviewResolution.width + "x" + defaultPreviewResolution.height);
        List<Camera.Size> rawSupportedSizes = cameraParameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            Log.w(TAG, "Device returned no supported preview sizes; using default");
            return new Point(defaultPreviewResolution.width, defaultPreviewResolution.height);
        }
        // 按照分辨率从大到小排序
        List<Camera.Size> supportedPreviewResolutions = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });
        printlnSupportedPreviewSize(supportedPreviewResolutions);
        // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
        // 由于camera的分辨率是width>height，这里先判断我们的屏幕和相机的角度是不是相同的方向(横屏 or 竖屏),然后决定比较的时候要不要先交换宽高值
        boolean isCandidatePortrait = screenOrientation % 180 != cameraOrientation % 180;
        double screenAspectRatio = (double) screenResolution.x / (double) screenResolution.y;
        // 移除不符合条件的分辨率
        Iterator<Camera.Size> it = supportedPreviewResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;
            // 移除低于下限的分辨率，尽可能取高分辨率
            if (width * height < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }
            //移除宽高比差异较大的
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }
            // 找到与屏幕分辨率完全匹配的预览界面分辨率直接返回
            if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
                Point exactPoint = new Point(width, height);
                Log.d(TAG, "found preview resolution exactly matching screen resolutions: " + exactPoint);
                return exactPoint;
            }
        }
        // 如果没有找到合适的，并且还有候选的像素，则设置其中最大比例的，对于配置比较低的机器不太合适
        if (!supportedPreviewResolutions.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewResolutions.get(0);
            Point largestSize = new Point(largestPreview.width, largestPreview.height);
            Log.d(TAG, "using largest suitable preview resolution: " + largestSize);
            return largestSize;
        }
        // 没有找到合适的，就返回默认的
        Point defaultResolution = new Point(defaultPreviewResolution.width, defaultPreviewResolution.height);
        Log.d(TAG, "No suitable preview resolutions, using default: " + defaultResolution);
        return defaultResolution;
    }

    /**
     * 获取最适合屏幕的照片 尺寸
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for ( Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }


    /**
     * 设置 SurfaceView 的大小
     *
     * @param displaySize
     * @param screenOrientation
     * @param previewSize
     * @param cameraOrientation
     * @return
     */
    public static Rect getSurfaceViewSize(Point displaySize, int screenOrientation, Point previewSize, int cameraOrientation) {
        Point previewSize2 = new Point();
        //方向不一致则交换
        if (screenOrientation % 180 != cameraOrientation % 180) {
            previewSize2.set(previewSize.y, previewSize.x);
        } else {
            previewSize2 = previewSize;
        }
        int width = displaySize.x;
        int height = previewSize2.y * displaySize.x / previewSize2.x;
        int left = 0;
        int right = width;
        int top = 0;
        int bottom = top + height;
        return new Rect(left, top, right, bottom);
    }
}
