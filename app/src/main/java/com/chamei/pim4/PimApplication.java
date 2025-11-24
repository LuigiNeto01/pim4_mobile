package com.chamei.pim4;

import android.app.Application;

import com.chamei.pim4.core.EnvConfig;

public class PimApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EnvConfig.init(this);
    }
}
