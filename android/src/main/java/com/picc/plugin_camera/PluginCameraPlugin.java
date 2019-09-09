package com.picc.plugin_camera;

import android.app.Activity;

import com.picc.plugin_camera.camera.SimpleCameraActivity;
import com.picc.plugin_camera.picture.PictureManager;
import com.picc.plugin_camera.picture.type.CaseRootType;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * PluginCameraPlugin
 */
public class PluginCameraPlugin implements MethodCallHandler {

    private Activity mActivity;
    private static final String START_CAMERA_ACTIVITY = "startCameraActivity";

    public PluginCameraPlugin(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        InitManager.getInstance().init(registrar.context());
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "plugin_camera");
        channel.setMethodCallHandler(new PluginCameraPlugin(registrar.activity()));
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case START_CAMERA_ACTIVITY:
                startCameraActivity();
                break;
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            default:
                result.notImplemented();
                break;
        }

    }

    public void startCameraActivity() {
        PictureManager.getInstance().setTask("123456", "RDZA778899", "1");
        SimpleCameraActivity.startSimpleCameraActivity(mActivity, CaseRootType.SURVEY_NAME);
    }
}
