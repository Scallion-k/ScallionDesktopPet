package com.desktop.ultraman;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

public class EUtil {

    public static ActivityManager activityManager;
    //获取属于桌面应用的应用包名称

    public static List<String> getHomes(Context context){
        List<String> names=new ArrayList<>();
        //PackageManager获取手机端已安装文件信息
        PackageManager packageManager=context.getPackageManager();//获取手机内所有应用
        //属性
        //Intent用于各大组件连接，传递对象
        Intent intent=new Intent(Intent.ACTION_MAIN);
        //Action属性代表系统要执行的动作,ACTION_MAIN：应用程序入口点
        intent.addCategory(Intent.CATEGORY_HOME);
        //Category属性用于指定动作Action被执行的环境, CATEGORY_HOME：随系统启动而运行
        List<ResolveInfo> resolveInfos=packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);//返回所有成功匹配的Activity信息
        //ResolveInfo信息类，读应用信息
        for(ResolveInfo info:resolveInfos){
            names.add(info.activityInfo.packageName);
        }
        return names;
    }
    public static boolean isHome(Context context){
        //判断当前界面是否桌面
        if (activityManager==null){
            activityManager= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        String topActivity =
                activityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        //getRunningTasks读取顶层应用，涉及隐私被禁止使用,会有bug
        return getHomes(context).contains(topActivity);
    }
    public static float getV(float x){ //暂未理解
        return (float)(Math.sqrt(5f/1000f)*x*x);
    }

    public static float getAngle(int total,int index){
        return (float) Math.toRadians(180.0/(total-1)*(index)+180);
    }
}
