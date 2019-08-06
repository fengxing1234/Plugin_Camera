package com.picc.plugin_camera.camera;

import android.content.Context;
import android.util.Log;
import android.view.OrientationEventListener;

public class IOrientationEventListener extends OrientationEventListener {

    private static final String TAG = IOrientationEventListener.class.getSimpleName();

    public IOrientationEventListener(Context context) {
        super(context);
    }

    public IOrientationEventListener(Context context, int rate) {
        super(context, rate);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        Log.d(TAG, "onOrientationChanged: " + orientation);

    }
}
