package com.picc.plugin_camera.camera;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.picc.plugin_camera.BitmapUtils;

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
    private int scaleHeight = 1960;
    private int scaleWidth = 1080;
    private boolean srcImg = false;

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
        String[] split = pictureFile.getAbsolutePath().split(".png");
        String rawFile = split[0] + "_raw.png";
        saveRawPicture(data, new File(rawFile));
        if (srcImg) {

        } else {
            saveCompressPicture(data, pictureFile);
        }


        return null;
    }

    private void saveCompressPicture(byte[] data, File pictureFile) {
        Bitmap bitmap = BitmapUtils.bytes2Bitmap(data);
        //int degrees = mImageWorkerContext.getDegrees();
        //bitmap = BitmapUtils.bitmapRotate(bitmap, degrees);
        BitmapUtils.compressAndSaveBitmap(bitmap, pictureFile);
        //BitmapUtils.saveBitmap(bitmap, pictureFile, 100, true);
    }

    /**
     * 保存原图 不做压缩
     *
     * @param data
     * @param pictureFile
     */
    private void saveRawPicture(byte[] data, File pictureFile) {
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
