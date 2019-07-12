package com.example.cuanbo.Tool;

import android.app.Application;
import android.content.Context;

/**
 * Created by xww on 18/3/19.
 */

/**
 * 用于获取全局Context
 *
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
