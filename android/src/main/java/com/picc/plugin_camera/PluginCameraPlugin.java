package com.picc.plugin_camera;

import android.app.Activity;
import android.content.Intent;

import com.picc.plugin_camera.camera.SimpleCameraActivity;

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
        Intent intent = new Intent(mActivity, SimpleCameraActivity.class);
        mActivity.startActivity(intent);
    }
}
