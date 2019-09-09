package com.picc.plugin_camera.picture.type;

public class OfflineType extends PictureType {

    public static final String OFFLINE_GALLERY = "离线拍照模式";
    public static final int OFFLINE_LIMIT = 100;

    public OfflineType() {
        super(OFFLINE_GALLERY, OFFLINE_GALLERY, OFFLINE_LIMIT);
    }
}
