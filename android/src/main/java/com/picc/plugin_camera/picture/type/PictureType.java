package com.picc.plugin_camera.picture.type;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.picc.plugin_camera.InitManager;
import com.picc.plugin_camera.picture.Picture;
import com.picc.plugin_camera.picture.Picture_;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

public class PictureType {

    public static final String TAG = "PictureType";
    public static final int NO_LIMIT = Integer.MAX_VALUE;

    public final int limit;

    public String photoType;
    public String name;
    public List<PictureType> children;
    public PictureType parent;

    public PictureType() {
        limit = NO_LIMIT;
    }


    public PictureType(String photoType, String name, int limit) {
        this.photoType = photoType;
        this.name = name;
        this.limit = limit;
        Log.d(TAG, "PictureType: ");
    }

    public void addChild(PictureType type) {
        if (children == null) {
            children = new ArrayList<>();
        }
        Log.d(TAG, "addChild: " + this);
        type.parent = this;
        children.add(type);
    }

    public boolean removeChild(PictureType type) {
        return children.remove(type);
    }

    public int getChildrenSize() {
        return children == null ? 0 : children.size();
    }

    public PictureType getChild(int index) {
        return children.get(index);
    }

    public Picture addPicture(Context context, File src, File thumb) {
        Picture p = new Picture();
        p.imageUri = Uri.fromFile(src).toString();
        p.thumbUri = Uri.fromFile(thumb).toString();
        p.photoSize = src.length();
        p.typeName = name;
        p.photoType = photoType;

        p.cameraType = "android";
        p.manufacturer = Build.MANUFACTURER;
        populatePicture(p);
        Box<Picture> pictureBox = InitManager.getInstance().getBoxStore().boxFor(Picture.class);
        long put = pictureBox.put(p);
        return p;
    }

    public void addPicture(Picture p) {
        p.typeName = name;
        p.photoType = photoType;
        populatePicture(p);
        Box<Picture> pictureBox = InitManager.getInstance().getBoxStore().boxFor(Picture.class);
        long put = pictureBox.put(p);
    }

    public void deleteAll() {
        Box<Picture> pictureBox = InitManager.getInstance().getBoxStore().boxFor(Picture.class);
        QueryBuilder<Picture> filter = filter(pictureBox.query());
        filter.build().remove();
    }

    public List<Picture> queryList() {
        Box<Picture> pictureBox = InitManager.getInstance().getBoxStore().boxFor(Picture.class);
        QueryBuilder<Picture> filter = filter(pictureBox.query());
        return filter.build().find();
    }

    public long count() {
        Box<Picture> pictureBox = InitManager.getInstance().getBoxStore().boxFor(Picture.class);
        QueryBuilder<Picture> filter = filter(pictureBox.query());
        return filter.build().count();
    }

    protected void populatePicture(Picture p) {
        Log.d(TAG, "populatePicture: ");
    }

    protected int getRemainSpace(int size) {
        Log.d(TAG, "getRemainSpace: ");
        return limit == NO_LIMIT ? NO_LIMIT : limit - size;
    }

    protected QueryBuilder<Picture> filter(QueryBuilder<Picture> builder) {
        Log.d(TAG, "filter: ");
        return builder.equal(Picture_.photoType, photoType);
    }

    public boolean isLeaf() {
        return children == null || children.size() == 0;
    }

    public void clearChildren() {
        if (children != null) {
            children.clear();
        }
    }

}
