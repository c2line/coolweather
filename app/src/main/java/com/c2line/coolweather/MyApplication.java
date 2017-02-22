package com.c2line.coolweather;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePalApplication;


/**
 * Created by Administrator on 2016/12/23.
 */

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        context=getApplicationContext();
        LitePalApplication.initialize(context);//初始化LitePal框架

    }

    public static Context getContext(){
        return context;
    }
}
