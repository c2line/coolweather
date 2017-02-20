package com.c2line.coolweather.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.c2line.coolweather.R;
import com.c2line.coolweather.db.Area;
import com.c2line.coolweather.gson.Forecast;
import com.c2line.coolweather.gson.Weather;
import com.c2line.coolweather.service.AutoUpdateService;
import com.c2line.coolweather.util.ActivityCollection;
import com.c2line.coolweather.util.HttpUtil;
import com.c2line.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.crud.DataSupport.where;

public class WeatherActivity extends BaseActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;


    private ImageButton navTool;

    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;

    public DrawerLayout drawerLayout;
    private Button navButton;
    private String mWeatherId;

    private PopupWindow mPopWindow;

    private long firstTime = 0;//记录退出程序的第一次点击时间






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //沉浸式体验
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        //初始化控件
        init();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        if(weatherString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气
            mWeatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.GONE);
            requestWeather(mWeatherId);
        }

        //下拉刷新
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        //加载背景图片
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

        //打开侧边栏
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //右侧工具选项
        navTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopWindow();
            }
        });


    }

    /**
     * 获取定位地址
     */
    private void requestLocation() {
        //定位
        LocationClient mLocationClient;
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        //定位设置option
        LocationClientOption mOption=new LocationClientOption();
        mOption.setIsNeedAddress(true);
        mLocationClient.setLocOption(mOption);

        //开始定位
        mLocationClient.start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(WeatherActivity.this,"必须同意所有权限才能使用本功能",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(WeatherActivity.this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    //显示弹出窗
    private void showPopWindow() {
        //设置contentView
        View contentView=LayoutInflater.from(WeatherActivity.this).inflate(R.layout.popup_tool,null);
        mPopWindow=new PopupWindow(contentView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT,true);
        mPopWindow.setContentView(contentView);
        mPopWindow.setTouchable(true);
        //设置各个选项的点击事件
        LinearLayout lSerach= (LinearLayout) contentView.findViewById(R.id.nav_search);
        LinearLayout lLocation= (LinearLayout) contentView.findViewById(R.id.nav_location);
        //点击菜单搜索选项
        lSerach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(WeatherActivity.this,SearchActivity.class);
                startActivityForResult(intent,1);
                mPopWindow.dismiss();
            }
        });

        //点击菜单定位选项
        lLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swipeRefresh.setRefreshing(true);
                requestWeatherByLocation();
                mPopWindow.dismiss();
            }
        });

        //这句同上面的 mPopWindow.setTouchable(true);同时使用处理popupwindow的隐藏问题
        mPopWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_menu));

        //显示PopupWindow
        mPopWindow.showAsDropDown(navTool);
    }


    //定位当前位置的城市并更新天气信息
    private void requestWeatherByLocation() {
        //判断权限
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(WeatherActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(WeatherActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WeatherActivity.this,permissions,1);
        }else{
            requestLocation();
        }





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                String cityId=data.getStringExtra("cityId");
                String cityName=data.getStringExtra("cityName");
                titleCity.setText(cityName);
                swipeRefresh.setRefreshing(true);
                requestWeather(cityId);
            }
        }
    }

    //加载网路图片
    private void loadBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    public void requestWeather(final String weatherId) {
        final String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=1a2225c7529d48f2aa99c911799007ce";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                Log.i("WeatherActivity",weatherUrl);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId=weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }else{

                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }

                });
            }
        });
    }



    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        if(weather!=null&&"ok".equals(weather.status)){

            String cityName=weather.basic.cityName;
            String updateTime=weather.basic.update.updateTime.split(" ")[1];
            String degree=weather.now.temperature+"℃";
            String weatherInfo=weather.now.more.info;
            titleCity.setText(cityName);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
            forecastLayout.removeAllViews();
            for(Forecast forecast:weather.forcastList){
                View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
                TextView dateText= (TextView) view.findViewById(R.id.date_text);
                TextView info= (TextView) view.findViewById(R.id.info_text);
                TextView maxText= (TextView) view.findViewById(R.id.max_text);
                TextView minText= (TextView) view.findViewById(R.id.min_text);
                dateText.setText(forecast.date);
                info.setText(forecast.more.info);
                maxText.setText(forecast.temperature.max+"℃");
                minText.setText(forecast.temperature.min+"℃");
                forecastLayout.addView(view);
            }

            if(weather.aqi!=null){
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }

            String comfort="舒适度: "+weather.suggestion.comfort.info;
            String carWash="洗车指数: "+weather.suggestion.carWash.info;
            String sport="运动建议: "+weather.suggestion.sport.info;
            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
            weatherLayout.setVisibility(View.VISIBLE);

            Intent intent=new Intent(this, AutoUpdateService.class);
            startService(intent);
        }else{
            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        weatherLayout= (ScrollView) findViewById(R.id.weather_layout);
        titleCity= (TextView) findViewById(R.id.title_city);
        titleUpdateTime= (TextView) findViewById(R.id.title_update_time);
        degreeText= (TextView) findViewById(R.id.degree_text);
        weatherInfoText= (TextView) findViewById(R.id.weather_info_text);
        forecastLayout= (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText= (TextView) findViewById(R.id.aqi_text);
        pm25Text= (TextView) findViewById(R.id.pm25_text);
        comfortText= (TextView) findViewById(R.id.comfort_text);
        carWashText= (TextView) findViewById(R.id.car_wash_text);
        sportText= (TextView) findViewById(R.id.sport_text);
        bingPicImg= (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh= (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        drawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton= (Button) findViewById(R.id.nav_button);
        navTool= (ImageButton) findViewById(R.id.nav_tool);

    }

    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.i("TAG","receive");
//            double[] mLocation=new double[2];
//            mLocation[0]=bdLocation.getLatitude();//纬度
//            mLocation[1]=bdLocation.getLongitude();//经度
            //定位到市
            String mLocationCountry=bdLocation.getCity().split("市")[0];

            //定位到区
            String mLocation=bdLocation.getDistrict();//


            if(mLocation!=null){
                if(mLocation.contains("区")){
                    mLocation=mLocation.split("区")[0];
                }
                if(mLocation.contains("县")){
                    mLocation=mLocation.split("县")[0];
                }
                List<Area> areas= where("city=?",mLocation).find(Area.class);
                if(areas.size()>0){
                    String mLocationCityId=areas.get(0).getCityId();
                    requestWeather(mLocationCityId);
                }
                if(areas.size()==0){
                    List<Area> areasCountry=DataSupport.where("country=?",mLocationCountry).find(Area.class);
                    if(areasCountry.size()>0){
                        String mLocationCityId=areasCountry.get(0).getCityId();
                        requestWeather(mLocationCityId);
                    }
                }

            }else{
                Toast.makeText(WeatherActivity.this,"定位失败,请稍后重试",Toast.LENGTH_SHORT).show();
            }
            Log.i("TAG",mLocation);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000){
                Toast.makeText(this,"再点一次退出程序",Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
                return true;
            }else{
                ActivityCollection.finishAllAcitivites();
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
