package com.picc.plugin_camera.camera;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;

import com.picc.plugin_camera.picture.Gallery;
import com.picc.plugin_camera.utils.BitmapUtils;

import java.io.File;

public class PictureTask extends AsyncTask<byte[], Integer, String> {


    private static final String TAG = PictureTask.class.getSimpleName();

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
        File thumb = imageFile[1];


//        String[] split = pictureFile.getAbsolutePath().split(".png");
//        String rawFile = split[0] + "_raw.png";
//        FileUtils.saveBytesForFile(data, new File(rawFile));

        Bitmap bitmap = BitmapUtils.bytes2Bitmap(data);
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, Gallery.THUMBNAIL_SIZE, Gallery.THUMBNAIL_SIZE);
        BitmapUtils.saveBitmap(thumbnail, thumb.getAbsoluteFile(), 60, false);
        mImageWorkerContext.onThumbnailSaveDone(thumbnail, thumb);
        BitmapUtils.compressAndSaveBitmap(bitmap, pictureFile);
        return null;
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
