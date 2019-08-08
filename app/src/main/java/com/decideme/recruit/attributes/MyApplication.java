package com.decideme.recruit.attributes;

import android.support.multidex.MultiDexApplication;

/**
 * Created by vivek_shah on 6/10/17.
 */
public class MyApplication extends MultiDexApplication {

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;
}
