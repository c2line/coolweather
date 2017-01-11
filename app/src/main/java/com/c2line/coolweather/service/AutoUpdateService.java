package com.c2line.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.c2line.coolweather.db.Area;
import com.c2line.coolweather.gson.Weather;
import com.c2line.coolweather.util.HttpUtil;
import com.c2line.coolweather.util.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    public AutoUpdateService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        updateWeather();
        updateBingPic();
        copyData();

        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour=8*60*60*1000;//8小时的毫秒数
        long triggerAtTime= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    //像数据库写入数据
    private void copyData() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                copy();

            }
        }).start();
    }
    private void copy() {
        BufferedReader br = null;
        try {
            InputStreamReader isr = new InputStreamReader(getResources().getAssets().open("cityid.txt"), "UTF-8");
            br = new BufferedReader(isr);
            while (true) {
                String strLine = br.readLine();
                if (TextUtils.isEmpty(strLine)) {
                    break;
                }
                String[] cityInfos = strLine.trim().split(",");
                if (cityInfos != null && cityInfos.length > 0) {
                    Area area = new Area();
                    area.setCityId(cityInfos[0]);
                    area.setCityNamePy(cityInfos[1]);
                    area.setCity(cityInfos[2]);
                    area.setCountry(cityInfos[3]);
                    area.setProvince(cityInfos[4]);
                    area.save();

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 更新天气
     */
    private void updateWeather() {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weaterString=prefs.getString("weather",null);
        if(weaterString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weaterString);
            String weatherId=weather.basic.weatherId;
            String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=1a2225c7529d48f2aa99c911799007ce";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText=response.body().string();
                    Weather weather=Utility.handleWeatherResponse(responseText);
                    if(weather!=null&&"ok".equals(weather.status)){
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();

                    }
                }
            });
        }
    }

    /**
     * 更新图片
     */
    private void updateBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }
}
