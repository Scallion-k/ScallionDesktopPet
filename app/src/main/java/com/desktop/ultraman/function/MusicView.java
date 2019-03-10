package com.desktop.ultraman.function;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.desktop.ultraman.R;

public class MusicView extends View {
    private Context context;
    private float init_x,init_y;
    private Bitmap stagebitmap;
    private int width,height;


    public MusicView(Context context, float x, float y,int w,int h){
        super(context);
        this.context=context;
        this.init_x=x;
        this.init_y=y;
        this.width=w;
        this.height=h;
        stagebitmap=BitmapFactory.decodeResource(getResources(), R.drawable.stage);
    }
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(init_x,init_y);
        Rect rect=new Rect(0,0,stagebitmap.getWidth(),stagebitmap.getHeight());
        Rect drawrect=new Rect(0,0,width,height);
                //stagebitmap.getWidth(),stagebitmap.getHeight());
        canvas.drawBitmap(stagebitmap,rect,drawrect,new Paint());
    }

    public boolean judgeTouch(float x,float y){
        if(x<(init_x+stagebitmap.getWidth())&&x>(init_x)
                &&y<(init_y+stagebitmap.getHeight())&&y>init_y)
            return false;
        return true;
    }
}
