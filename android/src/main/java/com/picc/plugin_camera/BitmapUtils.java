package com.picc.plugin_camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {

    /**
     * 保存相片格式
     */
    private static final Bitmap.CompressFormat PICTURE_FORMAT = Bitmap.CompressFormat.JPEG;

    private static final String TAG = BitmapUtils.class.getSimpleName();

    /**
     * 默认压缩质量为80
     */
    private static final int quality = 80;
    //120kb 图片大小不得超过120kb
    public static final int BUFFER_SIZE = 150 * 1024;

    /**
     * 默认的图片处理方式
     */
    private static Bitmap.Config bitmapConfig = Bitmap.Config.RGB_565;

    /**
     * 图片比例 4：3
     */
    private static final int REQUEST_HEIGHT = 1280;
    private static final int REQUEST_WIDTH = 960;


    public static Bitmap bytes2Bitmap(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        //当系统内存不够时候图片自动被回收
        options.inInputShareable = true;
        options.inSampleSize = calculateInSampleSize(options, REQUEST_WIDTH, REQUEST_HEIGHT);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        return bitmap;
    }

    /**
     * 计算采样率
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.d(TAG, String.format("(outWidth = %s, outHeight = %s)", width, height));
        Log.d(TAG, String.format("(reqWidth = %s, reqHeight = %s)", reqWidth, reqHeight));
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(TAG, "calculateInSampleSize: inSampleSize = " + inSampleSize);
        return inSampleSize;
    }


    /**
     * 旋转bitmap
     *
     * @param bitmap
     * @param degrees
     * @return
     */
    public static Bitmap bitmapRotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.d(TAG, "bitmapRotate: width = " + width + "  height = " + height + " degrees = " + degrees);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    public static void saveBitmap(Bitmap bitmap, File file) {
        saveBitmap(bitmap, file, quality);
    }

    public static void saveBitmap(Bitmap bitmap, File file, int quality) {
        saveBitmap(bitmap, file, quality, true);
    }

    /**
     * 保存图片
     *
     * @param bitmap
     * @param file    保存路径
     * @param quality 图片质量
     * @param recycle 是否回收图片
     */
    public static void saveBitmap(Bitmap bitmap, File file, int quality, boolean recycle) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            boolean compress = bitmap.compress(PICTURE_FORMAT, quality, out);
            Log.d(TAG, "saveBitmap: ");
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (recycle && bitmap != null) {
                bitmap.recycle();
            }
        }
    }


    /**
     * 压缩图片并保存目录
     *
     * @param bitmap
     * @param file
     */
    public static void compressAndSaveBitmap(Bitmap bitmap, File file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;//个人喜欢从80开始,
        bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
        Log.i(TAG, "质量 100 = " + baos.toByteArray().length / 1024 + "KB");

        while (baos.size() > BUFFER_SIZE) {
            baos.reset();
            options -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        Log.i(TAG, "质量 " + options + " = " + baos.toByteArray().length / 1024 + "KB");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
