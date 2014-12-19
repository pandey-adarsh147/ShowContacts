package com.augy.showcontacts;

import android.app.Application;

import com.squareup.otto.Bus;

/**
 * Created by adarshpandey on 12/19/14.
 */
public class MainApplication extends Application {

    private static MainApplication application;

    private Bus mBus = new Bus();

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static Bus getEventBus() {
        return application.mBus;
    }
}
