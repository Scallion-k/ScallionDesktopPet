package com.desktop.ultraman;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.desktop.ultraman.function.WeatherGet;

public class windowActivity extends Activity {
    boolean go=false;
    private static final int BAIDU_READ_PHONE_STATE =100;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);


        //Android 6.0判断用户是否授予定位权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        BAIDU_READ_PHONE_STATE);
            }
        }
        if(go)
            dismiss();
        else run();
        finish();
    }
     public void run(){
         Intent show=new Intent(this,WindowService.class);
         show.putExtra(Pet.STATUS,Pet.STATUS_SHOW);
         startService(show);
     }
     public void dismiss(){
        Intent hide=new Intent(this,WindowService.class);
        hide.putExtra(Pet.STATUS,Pet.STATUS_HIDE);
        startService(hide);
     }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            //requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case BAIDU_READ_PHONE_STATE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("debug.permission","success");
                    //获取到权限，做相应处理
                    //调用定位SDK应确保相关权限均被授权，否则会引起定位失败
                } else{
                    //没有获取到权限，做特殊处理
                    Log.i("debug.permission","error");
                }
                break;
            default:
                break;
        }
    }

}
