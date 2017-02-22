package com.c2line.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.c2line.coolweather.activity.BaseActivity;
import com.c2line.coolweather.activity.WeatherActivity;
import com.c2line.coolweather.util.ActivityCollection;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCollection.addActivity(this);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("weather",null)!=null){
            Intent intent=new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollection.removeActivity(this);
    }
}
