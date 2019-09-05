package com.picc.plugin_camera.camera;

public interface ICamera {

    void init(CameraPreview preview);

    void onResume();

    void onPause();

    void doTakePicture();

    void doSwitchCamera();

    void doSwitchFlush();

    String getFlushMode();

    void release();
}
