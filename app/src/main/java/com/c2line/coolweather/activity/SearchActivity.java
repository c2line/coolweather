package com.c2line.coolweather.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.c2line.coolweather.R;
import com.c2line.coolweather.db.Area;

import org.litepal.crud.DataSupport;

import java.util.List;

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
                Area mArea=queryCity();//从数据库中查询城市名字相对应的cityId
                if(mArea!=null){
                    dialog.dismiss();
                    result.setText(mArea.getCity());
                    cityId=mArea.getCityId();

                }else{
                    dialog.dismiss();
                    result.setText("没有相应的城市");

                }
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
        List<Area> areas= DataSupport.where("cityNamePy=?",cityName.getText().toString().trim()).find(Area.class);
        if(areas.size()>0){
            result=areas.get(0);
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    //显示加载等待
    private  void showDialog(){
        dialog=new ProgressDialog(this);
        dialog.setMessage("搜索中...");
        dialog.setCancelable(false);
        dialog.show();
    }

    private void init() {
        cityName= (EditText) findViewById(R.id.city_name);
        search= (ImageButton) findViewById(R.id.search);
        result= (TextView) findViewById(R.id.search_result);

        //向数据库中导入数据
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                copy();
//            }
//        }).start();

    }

//    private void copy() {
//        BufferedReader br = null;
//        try {
//            InputStreamReader isr = new InputStreamReader(getResources().getAssets().open("cityid.txt"), "UTF-8");
//            br = new BufferedReader(isr);
//            while (true) {
//                String strLine = br.readLine();
//                if (TextUtils.isEmpty(strLine)) {
//                    break;
//                }
//                String[] cityInfos = strLine.trim().split(",");
//                Log.i("DataBase",cityInfos[2].toString());
//                if (cityInfos != null && cityInfos.length > 0) {
//                    Area area = new Area();
//                    area.setCityId(cityInfos[0]);
//                    area.setCityNamePy(cityInfos[1]);
//                    area.setCity(cityInfos[2]);
//                    area.setCountry(cityInfos[3]);
//                    area.setProvince(cityInfos[4]);
//                    area.save();
//
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                br.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
}
