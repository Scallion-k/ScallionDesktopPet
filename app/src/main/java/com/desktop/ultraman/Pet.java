package com.desktop.ultraman;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Message;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.desktop.ultraman.function.MusicPlayer;
import com.desktop.ultraman.function.MusicView;
import com.desktop.ultraman.function.WeatherGet;
import com.desktop.ultraman.function.WeatherView;

import pl.droidsonroids.gif.GifDrawable;


//Android资源文件分类：res目录下可编译资源文件，assets目录下原生资源文件
//通过AssetManager工具类对assets进行访问
public class Pet extends View {
    final Point size = new Point();  //保存屏幕宽高
    private int petW = 0; //Pet的宽度
    public static final String STATUS = "status";
    public static final int STATUS_SHOW = 100;
    public static final int STATUS_HIDE = 101;
    private static final int HANDLE_CHECK_ACTIVITY = 200;

    public static final float K = 4000f;//比例因子

    public static final int TIMER_START = 10001;//Pet动作开始
    public static final int TIMER_STOP = 10002;//Pet动作结束
    public static final int MOV_LEFT = 10006;//Pet左移动
    public static final int MOV_RIGHT = 10007;//Pet右移动
    public static final int MOV_FALL = 10008;//Pet摔倒
    public static final int MOV_SIT = 10009;//Pet坐下
    public static final int MOV_FLY = 10010;//Pet飞行

    private boolean isAdd = false;//Pet是否添加到桌面
    private View petView;//petView：Pet的视图
    ImageView petImView; //加载Pet图像视图
    private boolean isPushing = false; //是否被拖动状态
    private Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams petParams, ropeParams, buttonParams, musicParams, weatherParams;
    //petParams:petView视图的参数； ropeParams:ropeView视图的参数； buttonParams:buttonView视图的参数
    //musicParams:musicView视图的参数； weatherParams:weaterView视图的参数

    private String walkToLeftPath;
    private String walkToRightPath;
    private String[] sitPath;
    private String pushPath;
    private String flyPath;
    private String dropdownPath;
    private String singPath;
    private String stayPath;

    private int randomDir = 1;

    private LineView ropeView; //ropeView: 绘制的绳子视图

    private GifDrawable sitAnimation, movToLeftGif, movToRightGif,
            hangUpGif, flyGif, fallGif,stayGif,singGif;//静止动画

    private float elasticX, elasticY;//elastic:有弹力的，弹力绳的滑动距离

    private MenuView menuView;// 按钮菜单显示视图
    private MusicView musicView;// 播放音乐舞台视图
    private WeatherView weatherView;// 显示天气情况视图
    private WeatherGet weatherGet;

    public Pet(final Context context, ImageView petImView, View petView) { //初始化各参数
        super(context);
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        petParams = new WindowManager.LayoutParams();
        ropeParams = new WindowManager.LayoutParams();
        buttonParams = new WindowManager.LayoutParams();
        musicParams = new WindowManager.LayoutParams();
        weatherParams=new WindowManager.LayoutParams();
        windowManager.getDefaultDisplay().getSize(size); //获取屏幕宽和高
        this.petImView = petImView;
        this.petView = petView;
        ropeView = new LineView(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void touch() {  //监听触控时候的状态并执行事件
        petView.setOnTouchListener(new OnTouchListener() {
            int lastX, lastY, dx, dy;
            int paramX, paramY;
            long downTime, upTime;

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mHandler.sendEmptyMessage(Pet.TIMER_STOP);
                        downTime = System.currentTimeMillis();
                        lastX = (int) event.getRawX();
                        //getX():自身组件左上角为原点；getRawX():屏幕左上角为原点
                        lastY = (int) event.getRawY();
                        paramX = petParams.x;
                        paramY = petParams.y;
                        ropeView.setVisibility(VISIBLE);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        HangUp();
                        isPushing = true;
                        dx = (int) (event.getRawX() - lastX);
                        dy = (int) (event.getRawY() - lastY);
                        petParams.x = paramX + dx;
                        petParams.y = paramY + dy; //将改变的xy坐标值修改到参数里
                        //设置绳子的控制点，即移动Pet在绳子上的所在位置,绳子绘制在LineView里实现
                        float controlX = (petParams.x - petParams.width * 1.1f) - ropeParams.x + size.x * 0.5f;
                        float controlY = (petParams.y + petParams.height * 1.5f) - ropeParams.y;
                        ropeView.setControlPoint(controlX, controlY);
                        windowManager.updateViewLayout(petView, petParams);//绘制移动时的Pet图像
                        break;
                    case MotionEvent.ACTION_UP:
                        ropeView.setVisibility(GONE);
                        isPushing = false;
                        upTime = System.currentTimeMillis();
                        elasticX = petParams.x > 0 ? EUtil.getV(petParams.x) : -EUtil.getV(petParams.x);
                        float temp_x = (petParams.y + petParams.height * 1.5f) - ropeParams.y;
                        elasticY = -EUtil.getV(temp_x);
                        if (Math.abs(event.getRawX() - lastX) < getScaleX() &&
                                Math.abs(event.getRawY() - lastY) < getScaleY()) {
                            mHandler.sendEmptyMessage(TIMER_STOP);
                            Mov_Stay();
                            createButtonView(event.getRawX(), event.getRawY());
                        } else {
                            if (elasticY < 0 && temp_x > 0)
                                mHandler.sendEmptyMessage(Pet.MOV_FLY);
                            else mHandler.sendEmptyMessage(Pet.TIMER_START);
                        }
                        break;
                }
                return true;
            }
        });

    }

    public void Hang() {
        setImageByGifDrawable(hangUpGif);
    }

    public void Mov_Sit() {
        int random = new Random().nextInt(getSitPath().length);
        try {
            sitAnimation = new GifDrawable(context.getAssets(), getSitPath()[random]);
            //从Assets中获取：new GifDrawable(getAssets(),"***.gif")
            //从drawable或raw中获取：new GifDrawable(getResources(),R.drawable.***)
            //从文件中读取：new GifDrawable(new File(getFilesDir(),"***.gif"))
            //从输入流中获取：new GifDrawable(new BufferedInputStream(new FileInputStream(gifFile),w*h))
        } catch (IOException e) {
            e.printStackTrace();
        }
        setImageByGifDrawable(sitAnimation);
        mHandler.sendEmptyMessage(Pet.MOV_SIT);
    }

    public void Mov_RunLeft() {
        setImageByGifDrawable(movToLeftGif);
        mHandler.sendEmptyMessage(Pet.MOV_LEFT);
    }

    public void Mov_Stay() {
        setImageByGifDrawable(stayGif);
        mHandler.sendEmptyMessage(Pet.TIMER_STOP);
    }

    public void Mov_Sing(){
        setImageByGifDrawable(singGif);
        mHandler.sendEmptyMessage(Pet.TIMER_STOP);
    }

    public void Mov_RunRight() {
        setImageByGifDrawable(movToRightGif);
        mHandler.sendEmptyMessage(Pet.MOV_RIGHT);
    }

    public void Mov_Fall() {
        setImageByGifDrawable(fallGif);
        mHandler.sendEmptyMessage(Pet.MOV_FALL);
    }

    public void Mov_Fly() {
        setImageByGifDrawable(flyGif);
    }

    public void HangUp() {
        if (isPushing)
            setImageByGifDrawable(hangUpGif);
    }

    private void setImageByGifDrawable(GifDrawable gifDrawable) {
        petImView.setImageDrawable(gifDrawable);
        float hangUpGifW = hangUpGif.getIntrinsicWidth();
        //getWidth():实际显示的宽度
        //getMeasureWidth():测量宽度，在布局之前计算出来
        //getIntrinsicWidth():原有宽度，辅助测量时候选择合适的展示宽度，单位是dp
        petParams.height = petParams.width = (int) (petW * (gifDrawable.getIntrinsicWidth() / hangUpGifW));
        petView.setVisibility(VISIBLE);
        windowManager.updateViewLayout(petView, petParams);//更新画布
        RelativeLayout.LayoutParams lineParams = (RelativeLayout.LayoutParams) petImView.getLayoutParams();
        lineParams.width = petParams.width;
        lineParams.height = petParams.height;
        petImView.setLayoutParams(lineParams);//使设置好的布局参数应用到控件
    }

    public void Run() {  //生成Gif动画
        try {
            hangUpGif = new GifDrawable(context.getAssets(), getPushPath());
            movToLeftGif = new GifDrawable(context.getAssets(), getWalkToLeftPath());
            movToRightGif = new GifDrawable(context.getAssets(), getWalkToRightPath());
            flyGif = new GifDrawable(context.getAssets(), getFlyPath());
            fallGif = new GifDrawable(context.getAssets(), getDropdownPath());
            stayGif=new GifDrawable(context.getAssets(),getStayPath());
            singGif=new GifDrawable(context.getAssets(),getSingPath());

        } catch (IOException e) {
            e.printStackTrace();
        }
        touch();
        mHandler.sendEmptyMessage(Pet.TIMER_START);
        createRopeView();
        createPetView();
    }

    @SuppressWarnings("static-access")
    @SuppressLint("NewApi")
    private void createPetView() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            petParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        else
            petParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        petParams.format = PixelFormat.RGBA_8888;//设置图片格式
        //格式：效果为背景透明
        petParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        petW = size.x / 4;
        petParams.width = petW;
        petParams.height = petW;
        petParams.x = 0;
        petParams.y = 0;
        petView.setVisibility(VISIBLE);
        windowManager.addView(petView, petParams);
        isAdd = true;
    }

    @SuppressWarnings("static-access")
    @SuppressLint("NewApi")
    private void createRopeView() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            ropeParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        else
            ropeParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        ropeParams.format = PixelFormat.RGBA_8888;//RGBA_8888为android的一种32位颜色格式，RGBA分别用8位表示
        ropeParams.x = 0;
        ropeParams.y = (int) (size.y * 0.35f + size.x / 5);
        ropeParams.width = size.x;
        ropeParams.height = (int) (size.y * 0.15f);
        ropeView.setVisibility(GONE); //设置隐藏:GONE
        windowManager.addView(ropeView, ropeParams);
    }

    @SuppressWarnings("static-access")
    @SuppressLint({"NewApi", "ResourceType", "ClickableViewAccessibility"})
    private void createButtonView(float x, float y) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            buttonParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        else
            buttonParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        buttonParams.format = PixelFormat.RGBA_8888;
        // buttonParams.gravity = Gravity.START | Gravity.TOP;
        buttonParams.x = petParams.x;
        buttonParams.y = petParams.y;
        buttonParams.width = size.x;//buttonParams.WRAP_CONTENT;
        buttonParams.height = size.y;//buttonParams.WRAP_CONTENT;
        Log.i("debug.button", "params.x:" + buttonParams.x + "  params.y:" + buttonParams.y);
        Log.i("debug.button", "params.width:" + buttonParams.width +
                "  params.height:" + buttonParams.height);
        menuView = new MenuView(context, x - petParams.width / 2, y - petParams.height / 2);
        menuView.setVisibility(VISIBLE);
        menuView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        int index = menuView.judgebutton(motionEvent.getRawX(), motionEvent.getRawY());
                        buttonFunction(index, motionEvent.getRawX(), motionEvent.getRawY());
                        break;
                }
                return true;
            }
        });

        windowManager.addView(menuView, buttonParams);
        weatherGet=new WeatherGet(context);
        weatherGet.Getweather();
    }

    //按钮功能--------------------------------------------
    @SuppressLint("ClickableViewAccessibility")
    private void buttonFunction(int flag, float x, float y) {
        switch (flag) {
            case -1:
                windowManager.removeView(menuView);
                mHandler.sendEmptyMessage(TIMER_START);
                break;
            case 0:
                Log.i("debug.function", "function_1");
                windowManager.removeView(menuView);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    weatherParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                else
                    weatherParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                weatherParams.format = PixelFormat.RGBA_8888;
                weatherParams.x = petParams.x;
                weatherParams.y = petParams.y;
                weatherParams.width = size.x;//buttonParams.WRAP_CONTENT;
                weatherParams.height = size.y;//buttonParams.WRAP_CONTENT;
                weatherView = new WeatherView(context,
                        x - petParams.width / 2, y - petParams.height / 2,weatherGet);
                weatherView.setVisibility(VISIBLE);
                weatherView.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switch (motionEvent.getAction()){
                            case MotionEvent.ACTION_DOWN:
                                if(weatherView.judgeTouch(motionEvent.getRawX(),motionEvent.getRawY())){
                                    windowManager.removeView(weatherView);
                                    mHandler.sendEmptyMessage(TIMER_START);
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                break;
                        }
                        return true;
                    }
                });
                Hang();
                windowManager.addView(weatherView,weatherParams);
                break;
            case 1:
                Log.i("debug.function", "function_2");
                windowManager.removeView(menuView);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    musicParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                else
                    musicParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                musicParams.format = PixelFormat.RGBA_8888;
                musicParams.x = 0;//petParams.x;
                musicParams.y = 0;//petParams.y;
                musicParams.width = size.x;//buttonParams.WRAP_CONTENT;
                musicParams.height = size.y;//buttonParams.WRAP_CONTENT;
                Log.i("debug.music"," petX: "+petParams.x+"  petY"+petParams.y);
                musicView = new MusicView(context,
                        (float) (petParams.x+400), (float) (petParams.y+725),
                        petParams.width,petParams.height);
                musicView.setVisibility(VISIBLE);

                MusicPlayer.playVoice(context,1);
                musicView.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (musicView.judgeTouch(motionEvent.getRawX(), motionEvent.getRawY())) {
                                    windowManager.removeView(musicView);
                                    //windowManager.removeView(menuView);
                                    MusicPlayer.stopVoice();
                                    mHandler.sendEmptyMessage(TIMER_START);
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                break;
                        }
                        return true;
                    }
                });
                Mov_Sing();
                windowManager.addView(musicView, musicParams);
                break;
            case 2:
                MusicPlayer.playVoice(context,2);
                Log.i("debug.function", "function_3");
                break;
            case 3:
                mHandler.sendEmptyMessage(TIMER_STOP);
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
        }

    }

    //处理消息--------------------------------------------
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() { //消息处理机制，更新UI界面
        //处理消息队列上的消息
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_CHECK_ACTIVITY:
                    if (EUtil.isHome(context)) {  //判断当前是否桌面
                        if (!isAdd) {
                            windowManager.addView(petView, petParams);
                            isAdd = true;
                        }
                    } else {
                        if (isAdd) { //若不是桌面，若Pet已添加则移除
                            windowManager.removeView(petView);
                            isAdd = false;
                        }
                    }
                    mHandler.sendEmptyMessageDelayed(HANDLE_CHECK_ACTIVITY, 1000);
                    //sendEmptyMessageDelayed:在规定的延迟后不停的发消息
                    break;
                case Pet.TIMER_START://随机产生Pet的一种状态
                    mHandler.removeMessages(Pet.TIMER_START);
                    int movChoose = (int) (Math.random() * (11));
                    randomDir = Math.random() > 0.5 ? 1 : -1;
                    switch (movChoose) {
                        case 0:
                        case 1:
                        case 2:
                        case 8:
                            Mov_Sit();
                            break;
                        case 3:
                        case 4:
                        case 9:
                            mHandler.removeMessages(Pet.MOV_RIGHT);
                            mHandler.removeMessages(Pet.MOV_FALL);
                            Mov_RunLeft();
                            break;
                        case 5:
                        case 6:
                        case 10:
                            mHandler.removeMessages(Pet.MOV_LEFT);
                            mHandler.removeMessages(Pet.MOV_FALL);
                            Mov_RunRight();
                            break;
                        case 7:
                            mHandler.removeMessages(Pet.MOV_LEFT);
                            mHandler.removeMessages(Pet.MOV_RIGHT);
                            Mov_Fall();
                            break;
                    }
                    //在随机延迟后重新开始TimeStart选择动作状态,总时间
                    mHandler.sendEmptyMessageDelayed(Pet.TIMER_START, 2000 + (int) (Math.random() * 2) * 2000);
                    break;
                case Pet.TIMER_STOP://清除动作状态信息
                    petView.setRotation(0);
                    petView.setAlpha(1);
                    petView.setVisibility(VISIBLE);
                    mHandler.removeMessages(Pet.TIMER_START);
                    mHandler.removeMessages(Pet.MOV_LEFT);
                    mHandler.removeMessages(Pet.MOV_RIGHT);
                    mHandler.removeMessages(Pet.MOV_FALL);
                    mHandler.removeMessages(Pet.MOV_FLY);
                    mHandler.removeMessages(Pet.MOV_SIT);
                    break;
                case Pet.MOV_LEFT:
                    mHandler.removeMessages(Pet.MOV_LEFT);
                    petParams.y = petParams.y + randomDir * (int) (Math.random() * 3 + 1);//随机加上y值
                    petParams.x = petParams.x - (int) (Math.random() * 2 + 1); //向左扣去x坐标值
                    windowManager.updateViewLayout(petView, petParams);//更新宠物视图
                    //如果向左走到顶就往右
                    if (petParams.x - petW / 2 < (-400)) {
                        Mov_RunRight();
                    } else {
                        mHandler.sendEmptyMessageDelayed(Pet.MOV_LEFT, 50);
                    }
                    break;
                case Pet.MOV_RIGHT:
                    mHandler.removeMessages(Pet.MOV_RIGHT);
                    petParams.y = petParams.y + randomDir * (int) (Math.random() * 3 + 1);//随机加上y值
                    petParams.x = petParams.x + (int) (Math.random() * 2 + 1); //向左加上x坐标值
                    windowManager.updateViewLayout(petView, petParams);//更新宠物视图
                    //如果向左走到顶就往右
                    if (petParams.x > (400 - petW / 2)) {
                        Mov_RunLeft();
                    } else {
                        mHandler.sendEmptyMessageDelayed(Pet.MOV_RIGHT, 50);
                    }
                    break;
                case Pet.MOV_FALL:
                    mHandler.removeMessages(Pet.MOV_FALL);
                    petParams.y = petParams.y + (int) (Math.random() * 2 + 2);//降落
                    windowManager.updateViewLayout(petView, petParams);
                    mHandler.sendEmptyMessageDelayed(Pet.MOV_FALL, 10);
                    break;
                case Pet.MOV_SIT:
                    mHandler.removeMessages(Pet.MOV_LEFT);
                    mHandler.removeMessages(Pet.MOV_RIGHT);
                    mHandler.removeMessages(Pet.MOV_FALL);
                    break;
                case Pet.MOV_FLY: //暂未理解
                    Mov_Fly();
                    int flag = -1;
                    petParams.x = (int) (petParams.x - (elasticX * 0.02));
                    petParams.y = (int) (petParams.y + (elasticY * 0.02)); //根据弹力绳滑动长度按比例改变位置
                    if (Math.abs(elasticX) < 30) {
                        elasticX = 0;
                    } else {
                        elasticX = elasticX + (elasticX > 0 ? -K * 0.04f : K * 0.04f);
                    }
                    elasticY = elasticY + K * 0.05f;
                    if (petParams.x < (-400) || petParams.x > 400)
                        elasticX = -elasticX;
                    if (petParams.y + 800 < 0) { //如果飞到顶部，就飞出去了可以降落
                        flag = 0;
                        //petParams.x = 0;
                        petParams.y = -800;
                        petParams.height = petW;
                    }
                    if (petParams.y > 800) {
                        if (Math.abs(elasticY) < 600)
                            flag = 1;
                        else elasticY = -elasticY * 0.5f;
                    }
                    /*
                    if(petParams.y>(ropeParams.y-petW)){ //如果飞到底部，就不飞了
                        windowManager.updateViewLayout(petView, petParams);
                        sendEmptyMessage(TIMER_START);
                    }*/
                    if (flag == 0) { //飞出去了的状态描述，可修改
                        mHandler.removeMessages(Pet.MOV_FLY);
                        petView.setRotation(0);
                        petView.setAlpha(1);
                        Mov_Fall();
                        mHandler.postDelayed(new Runnable() { //5秒后重新回来
                            @Override
                            public void run() {
                                mHandler.sendEmptyMessage(TIMER_START);
                            }
                        }, 5000);
                    } else if (flag == 1) { //没飞出去的状态描述
                        petView.setRotation(0);
                        petView.setAlpha(1);
                        petView.setVisibility(VISIBLE);
                        //Mov_Fall();
                        windowManager.updateViewLayout(petView, petParams);
                        mHandler.sendEmptyMessage(TIMER_START);
                    } else { //正在飞的状态描述，在移动xy坐标的过程中修改方向
                        float rotation = (float) (Math.atan2(elasticY, -elasticX) * 180 / Math.PI) + 90;
                        //Log.e("rotation",""+rotation);
                        petView.setRotation(rotation);
                        windowManager.updateViewLayout(petView, petParams);
                        mHandler.sendEmptyMessageDelayed(Pet.MOV_FLY, 60);
                    }
                    break;
            }
        }
    };

    public String[] getSitPath() {
        return sitPath;
    }

    public void setSitPath(String[] sitPath) {
        this.sitPath = sitPath;
    }

    public void setDropdownPath(String dropdownPath) {
        this.dropdownPath = dropdownPath;
    }

    public String getDropdownPath() {
        return dropdownPath;
    }

    public void setWalkToLeftPath(String walkToLeftPath) {
        this.walkToLeftPath = walkToLeftPath;
    }

    public String getWalkToLeftPath() {
        return walkToLeftPath;
    }

    public void setWalkToRightPath(String walkToRightPath) {
        this.walkToRightPath = walkToRightPath;
    }

    public String getWalkToRightPath() {
        return walkToRightPath;
    }

    public void setPushPath(String pushPath) {
        this.pushPath = pushPath;
    }

    public String getPushPath() {
        return pushPath;
    }

    public void setFlyPath(String flyPath) {
        this.flyPath = flyPath;
    }

    public String getFlyPath() {
        return flyPath;
    }

    public void setStayPath(String stayPath) {
        this.stayPath = stayPath;
    }

    public String getStayPath() {
        return stayPath;
    }

    public void setSingPath(String singPath) {
        this.singPath = singPath;
    }

    public String getSingPath() {
        return singPath;
    }

    @Override
    public float getScaleX() {
        return super.getScaleX();
    }

    @Override
    public float getScaleY() {
        return super.getScaleY();
    }

}
