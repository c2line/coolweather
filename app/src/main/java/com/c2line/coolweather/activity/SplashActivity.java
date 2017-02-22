package com.c2line.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.c2line.coolweather.MainActivity;
import com.c2line.coolweather.R;
import com.c2line.coolweather.util.ActivityCollection;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends BaseActivity {
    private ViewPager mViewPager;
    private Button mButton;
    private List<ImageView> mList;
    private static int images[] = {R.mipmap.ic_back, R.mipmap.ic_home};

    private boolean isFirstUse = false;//记录是不是第一次

    LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_splash);
        ActivityCollection.addActivity(this);
        isFirst();
        initData();

    }

    //判断是不是第一次使用
    private void isFirst() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        isFirstUse = preferences.getBoolean("isFirstUse", false);
        if (isFirstUse){
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
        }else{
            editor.putBoolean("isFirstUse",true);
            editor.apply();
        }
    }

    private void initData() {
        mButton = (Button) findViewById(R.id.btn_start);
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_point);
        mViewPager = (ViewPager) findViewById(R.id.vp_splash);
        mList = new ArrayList<ImageView>();
        for (int i = 0; i < images.length; i++){
            ImageView imageView=new ImageView(this);
            imageView.setImageResource(images[i]);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            mList.add(imageView);
        }

        //初始化小点
        for (int j = 0; j < images.length; j++){
            View point = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
            if (j != 0){
                params.leftMargin = 15;
            }
            point.setLayoutParams(params);
            point.setBackgroundResource(R.drawable.shape_dot_normal);
            mLinearLayout.addView(point);
        }
        mLinearLayout.getChildAt(0).setBackgroundResource(R.drawable.shape_dot_selecte);

        mViewPager.setAdapter(new GuideViewPagerAdapter());
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < images.length; i++){
                    if (i == position){
                        mLinearLayout.getChildAt(i).setBackgroundResource(R.drawable.shape_dot_selecte);
                    }else{
                        mLinearLayout.getChildAt(i).setBackgroundResource(R.drawable.shape_dot_normal);
                    }
                }
                mButton.setVisibility(position == images.length-1 ? View.VISIBLE:View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private class GuideViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            if (mList != null){
                return mList.size();
            }
            return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ImageView iv = mList.get(position);
            container.removeView(iv);

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView iv = mList.get(position);
            container.addView(iv);
            return iv;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollection.removeActivity(this);
    }
}
