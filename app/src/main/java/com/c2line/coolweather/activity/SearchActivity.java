package com.c2line.coolweather.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.c2line.coolweather.R;
import com.c2line.coolweather.db.Area;

import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity {

    private EditText cityName;
    private ImageButton search;
    private ProgressDialog dialog;
    private TextView result;

    private String cityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_search);
        init();
        clickEvent();

    }

    private void clickEvent() {
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Area mArea=queryCity();//从数据库中查询城市名字相对应的cityId
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(mArea!=null){
                                        dialog.dismiss();
                                        result.setText(mArea.getCity());
                                        cityId=mArea.getCityId();

                                    }else{
                                        dialog.dismiss();
                                        result.setText("没有相应的城市");
                                        result.setEnabled(false);

                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();




            }
        });

        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.putExtra("cityName",result.getText().toString().trim());
                intent.putExtra("cityId",cityId);
                SearchActivity.this.setResult(RESULT_OK,intent);
                SearchActivity.this.finish();
            }
        });
    }

    private Area queryCity() {
        Area result=null;
        String cityN=cityName.getText().toString().trim();
        Pattern patternPinyin=Pattern.compile("[a-zA-Z]");
        Pattern patternHanzi=Pattern.compile("^[\\u4e00-\\u9fa5]*$");
        List<Area> areas;
        Log.i("TAG",cityN);
        Log.i("TAG",patternPinyin.matcher(cityN).matches()+"");
        Log.i("TAG",patternHanzi.matcher(cityN).matches()+"");
        if(patternPinyin.matcher(cityN).matches()){
            areas= DataSupport.where("cityNamePy=?",cityN).find(Area.class);
            if(areas.size()>0){
                result=areas.get(0);
            }
        }
        if(patternHanzi.matcher(cityN).matches()){
            areas= DataSupport.where("city=?",cityN).find(Area.class);
            if(areas.size()>0){
                result=areas.get(0);
            }
        }
        return result;
    }

    //显示加载等待
    private  void showDialog(){
        dialog=new ProgressDialog(SearchActivity.this);
        dialog.setMessage("搜索中...");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void init() {
        cityName = (EditText) findViewById(R.id.city_name);
        search = (ImageButton) findViewById(R.id.search);
        result = (TextView) findViewById(R.id.search_result);

    }

}
