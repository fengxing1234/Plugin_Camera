package com.picc.plugin_camera.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PictureTask extends AsyncTask<byte[], Integer, String> {


    private static final String TAG = PictureTask.class.getSimpleName();

    /**
     * 压缩图片最大容量
     */
    public static final int COMPRESS_SIZE = 150;
    private static final int BYTE_MONAD = 1024;
    private int scaleHeight = 600;
    private int scaleWidth = 800;
    private boolean srcImg = true;

    private ImageWorkerContext mImageWorkerContext;

    public PictureTask(ImageWorkerContext imageWorkerContext) {
        this.mImageWorkerContext = imageWorkerContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mImageWorkerContext.proExecute();
        Log.d(TAG, "onPreExecute: ");
    }

    @Override
    protected String doInBackground(byte[]... bytes) {
        if (mImageWorkerContext.preCheck(bytes[0])) {
            return null;
        }
        byte[] data = bytes[0];

        File[] imageFile = mImageWorkerContext.createImageFile();
        File pictureFile = imageFile[0];
//        File thumbFile = imageFile[1];

        if (srcImg) {
            long startTime = System.currentTimeMillis();
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(pictureFile));
                bos.write(data);
                bos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "耗时: " + (System.currentTimeMillis() - startTime));
        } else {
            Bitmap bitmap = compressBitmap(bytes[0]);
            bitmap = scaleBitmap(bitmap);
            bitmapToFile(pictureFile.getAbsolutePath(), bitmap);
        }


        return null;
    }

    /**
     * Bitmap to File
     *
     * @param bitmap bitmap
     * @return file
     */
    public static File bitmapToFile(String dstFileName, Bitmap bitmap) {
        FileOutputStream fos = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
        while (bos.toByteArray().length / BYTE_MONAD > COMPRESS_SIZE) {
            bos.reset();
            quality -= 5;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
        }
        File imageFile = new File(dstFileName);
        try {
            fos = new FileOutputStream(imageFile);
            fos.write(bos.toByteArray(), 0, bos.toByteArray().length);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return imageFile;
    }


    /**
     * 缩放图片
     *
     * @param bitmap
     * @return
     */
    private Bitmap scaleBitmap(Bitmap bitmap) {
        //实际宽高
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        //断言width是长边
        if (width < height) {
            float temp = width;
            width = height;
            height = temp;
            double scale = height / width;
            //新的宽高
            float newHeight = scaleWidth;
            double newWidth = newHeight * scale;
            return Bitmap.createScaledBitmap(bitmap, (int) newWidth, (int) newHeight, false);
        }
        double scale = height / width;
        //新的宽高
        float newWidth = scaleWidth;
        double newHeight = newWidth * scale;

        return Bitmap.createScaledBitmap(bitmap, (int) newWidth, (int) newHeight, false);

    }

    /**
     * 解析图片
     *
     * @param data
     * @return
     */
    public Bitmap compressBitmap(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        if (imageHeight > imageWidth) {
            options.inSampleSize = calculateInSampleSize(options, scaleHeight,
                    scaleWidth);
        } else {
            options.inSampleSize = calculateInSampleSize(options, scaleWidth,
                    scaleHeight);
        }
        options.inJustDecodeBounds = false;
        Bitmap source = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        return source;
    }

    /**
     * 计算采样率
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d(TAG, "onPostExecute: " + s);
        mImageWorkerContext.onTaskComplete(s);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        Log.d(TAG, "onCancelled: ");
        super.onCancelled();
    }

    @Override
    protected void onCancelled(String s) {
        Log.d(TAG, "onCancelled: " + s);
        super.onCancelled(s);
    }
}
