package com.picc.plugin_camera.picture.type;

import android.util.Log;

import com.picc.plugin_camera.picture.Picture;
import com.picc.plugin_camera.picture.PictureManager;
import com.picc.plugin_camera.picture.Picture_;

import io.objectbox.query.QueryBuilder;

/**
 * 任务类型
 */
public class CaseType extends PictureType {

    private static final String TAG = "CaseType";

    public CaseType() {

    }

    public CaseType(String photoType, String name, int limit) {
        super(photoType, name, limit);
    }

    public CaseRootType getRootCase() {
        return PictureManager.getInstance().getRootCase();
    }

    @Override
    protected QueryBuilder<Picture> filter(QueryBuilder<Picture> builder) {
        Log.d(TAG, "filter: ");
        return super.filter(builder)
                .equal(Picture_.taskId, (getTaskId()))
                .equal(Picture_.registerNo, getRegisterNo())
                .equal(Picture_.scheduleType, getScheduleType())
                ;
    }

    @Override
    protected void populatePicture(Picture p) {
        Log.d(TAG, "populatePicture: ");
        super.populatePicture(p);
        p.taskId = getTaskId();
        p.scheduleType = getScheduleType();
        CaseType parent = (CaseType) this.parent;
        StringBuilder builder = new StringBuilder();
        while (parent != getRootCase()) {
            builder.insert(0, '-');
            builder.insert(0, parent.name);
            parent = (CaseType) parent.parent;
        }
        p.checkPhotoDescription = builder.toString();
    }

    @Override
    protected int getRemainSpace(int size) {
        Log.d(TAG, "getRemainSpace: ");
        int remain = super.getRemainSpace(size);
        int caseRemain = getRootCase().limit - getRootCase().getCasePictureCount();
        return Math.min(remain, caseRemain);
    }

    private String getScheduleType() {
        return getRootCase().getScheduleType();
    }

    private String getRegisterNo() {
        return getRootCase().getRegisterNo();
    }

    private String getTaskId() {
        return getRootCase().getTaskId();
    }
}
