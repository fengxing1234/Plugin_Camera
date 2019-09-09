package com.picc.plugin_camera.camera;

import android.graphics.Bitmap;

import java.io.File;

public interface ImageWorkerContext {

    /**
     * 对数据和上下文环境进行检查，表示当前数据是否要保存为照片
     *
     * @param data
     * @return
     */
    boolean preCheck(byte[] data);

    /**
     * 返回一个长度为 2 的文件数组，分别表示照片和缩略图
     *
     * @return
     */
    File[] createImageFile();

    /**
     * 通知上下文文件保存成功
     *
     * @param image
     * @param thumb
     */
    void onFileSaved(File image, File thumb);

    /**
     * 任务执行完毕
     *
     * @param s
     */
    void onTaskComplete(String s);

    /**
     * 预执行
     */
    void proExecute();

    int getDegrees();

    void onThumbnailSaveDone(Bitmap thumbnail, File thumb);
}
