package com.picc.plugin_camera.picture;

import android.content.Context;
import android.os.Environment;

import com.picc.plugin_camera.picture.type.PictureType;

import java.io.File;

public class Gallery {

    public static final int THUMBNAIL_SIZE = 256;
    public static final String THUMB_EXT = ".thumb";
    public static final String EXT = ".jpg";
    private final File parentDir;

    public Gallery(Context context, PictureType type) {
        parentDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    public File[] generateNewImageFile(String imgPath) {
        long date = System.currentTimeMillis();
        File dir = new File(parentDir, imgPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File pictureFile = new File(dir, date + EXT);
        File thumbFile = new File(dir, date + THUMB_EXT);
        return new File[]{pictureFile, thumbFile};
    }
}
