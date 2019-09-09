package com.picc.plugin_camera.picture;

import android.content.Context;

import com.picc.plugin_camera.picture.type.CaseRootType;
import com.picc.plugin_camera.picture.type.OfflineType;
import com.picc.plugin_camera.picture.type.PictureType;

import java.util.LinkedList;

public class PictureManager {

    private static PictureManager singleton;

    private Context context;
    private final CaseRootType caseRootType;
    private final OfflineType offlineType;
    private final PictureType root;

    private PictureManager(Context context) {
        this.context = context;
        root = new PictureType();
        caseRootType = new CaseRootType();
        offlineType = new OfflineType();
        root.addChild(caseRootType);
        root.addChild(offlineType);
    }

    public static PictureManager initManager(Context context) {
        return singleton = new PictureManager(context);
    }

    public static PictureManager getInstance() {
        return singleton;
    }

    public CaseRootType getRootCase() {
        return caseRootType;
    }

    public OfflineType getOfflineCase() {
        return offlineType;
    }

    public void setTask(String taskId, String registerNo, String scheduleType) {
        caseRootType.setTask(taskId, registerNo, scheduleType);
    }

    public PictureType findPictureType(String type) {
        LinkedList<PictureType> list = new LinkedList<>();
        list.push(root);
        while (!list.isEmpty()) {
            PictureType pictureType = list.poll();
            if (type.equals(pictureType.name)) {
                return pictureType;
            }
            if (!pictureType.isLeaf()) {
                for (PictureType chile : pictureType.children) {
                    list.push(chile);
                }
            }
        }
        return null;
    }

}
