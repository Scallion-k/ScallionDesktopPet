package com.desktop.ultraman.function;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.desktop.ultraman.R;

public class WeatherView extends View {
    private Context context;
    private float init_x,init_y;
    private Bitmap showbitmap;
    private String stringText="没有联网";
    private TextPaint textPaint;
    private float distance=0;
    private float rotate,ratio;//旋转角度，放大角度
    private WeatherGet weatherGet;
    public WeatherView(Context context,float x,float y,WeatherGet weather){
        super(context);
        this.context=context;
        this.init_x=x;
        this.init_y=y;
        this.weatherGet=weather;
        showbitmap=BitmapFactory.decodeResource(getResources(), R.drawable.blank);
        textPaint=new TextPaint();
        init();
        textAnimation();
    }
    private void init(){
        textPaint.setStrokeWidth(10);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(50);
       // textPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics=textPaint.getFontMetrics();
        distance=(fontMetrics.bottom-fontMetrics.top)/2-fontMetrics.bottom;
        stringText=weatherGet.getText();
    }

    private void textAnimation(){
        //ObjectAnimator objtAnimatorR;
        //objtAnimatorR=ObjectAnimator.ofFloat()
        ValueAnimator rotataAni=ValueAnimator.ofFloat(0,360);
        rotataAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                rotate= (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator ratioAni=ValueAnimator.ofFloat(0, 0.7f);
        ratioAni.setInterpolator(new LinearInterpolator());
        ratioAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ratio= (float) valueAnimator.getAnimatedValue();
            }
        });
        AnimatorSet set=new AnimatorSet();
        set.setDuration(2000);
        set.play(rotataAni).with(ratioAni);
        set.start();
    }
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(init_x,init_y);
        canvas.rotate(rotate,showbitmap.getWidth()/2,showbitmap.getHeight()/2);
        Rect rect=new Rect(0,0,showbitmap.getWidth(),showbitmap.getHeight());
        Rect drawrect=new Rect(0,0,
                (int) (showbitmap.getWidth()*ratio), (int) (showbitmap.getHeight()*ratio));
        canvas.drawBitmap(showbitmap,rect,drawrect,new Paint());
        //canvas.drawBitmap(showbitmap,init_x,init_y,new Paint());
        //canvas.drawText(stringText,rect.centerX(),rect.centerY()+distance,textPaint);

        StaticLayout textLayout=new StaticLayout(stringText, textPaint,500,
                Layout.Alignment.ALIGN_CENTER,1f,0f,false);
        canvas.save();
        canvas.translate((showbitmap.getWidth()*ratio)/10,(showbitmap.getHeight()*ratio)/(2.5f));
        textLayout.draw(canvas);
        canvas.restore();
        canvas.translate(-init_x,-init_y);
    }
    public boolean judgeTouch(float x,float y){
        if(x<(init_x+showbitmap.getWidth())&&x>init_x
                &&y<(init_y+showbitmap.getHeight())&&y>init_y)
            return false;
        return true;
    }


}
