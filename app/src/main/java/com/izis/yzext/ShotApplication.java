package com.izis.yzext;

import android.app.Application;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;

public class ShotApplication extends Application {
    public static int userId = 0;

    private int result;
    private Intent intent;
    private MediaProjectionManager mMediaProjectionManager;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public MediaProjectionManager getmMediaProjectionManager() {
        return mMediaProjectionManager;
    }

    public void setMediaProjectionManager(MediaProjectionManager mMediaProjectionManager) {
        this.mMediaProjectionManager = mMediaProjectionManager;
    }
}