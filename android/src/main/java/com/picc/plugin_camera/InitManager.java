package com.picc.plugin_camera;

import android.content.Context;

import com.picc.plugin_camera.picture.MyObjectBox;
import com.picc.plugin_camera.picture.PictureManager;

import io.objectbox.BoxStore;

public class InitManager {


    private BoxStore boxStore;
    private PictureManager pictureManager;
    private Context mContext;

    private InitManager() {

    }

    private volatile static InitManager instance;

    public static InitManager getInstance() {
        if (instance == null) {
            synchronized (InitManager.class) {
                if (instance == null) {
                    instance = new InitManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        this.mContext = context;
        pictureManager = PictureManager.initManager(context);
    }

    public BoxStore getBoxStore() {
        if (boxStore == null) {
            boxStore = MyObjectBox.builder().androidContext(mContext).build();
        }
        return boxStore;
    }

    public PictureManager getPictureManager() {
        return pictureManager;
    }
}
