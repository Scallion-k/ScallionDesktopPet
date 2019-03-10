package com.desktop.ultraman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
public class MenuView extends View {

    private Context mContext;
    private Bitmap drawBmp;
    private int drawwidth = 200, drawheight = 200;
    private float init_x = 0, init_y = 0;
    private int buttonCount = 4;
    private List<drawBitmap> bitmaps = new ArrayList<>(5);
    private int[] buttonImage=new int[]{R.drawable.button_cloud,R.drawable.button_music,
            R.drawable.button_bug,R.drawable.button_exit};

    public MenuView(Context context, float x, float y) {
        super(context);
        this.mContext = context;
        this.init_x = x;
        this.init_y = y;
        init();
    }

    private void init() {
        for (int i = 0; i < buttonCount; i++) {
            drawBmp = BitmapFactory.decodeResource(getResources(), buttonImage[i]);
            bitmaps.add(new drawBitmap(drawBmp, init_x, init_y));
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(init_x, init_y);
        for (int i = 0; i < buttonCount; i++) {
            canvas.save();
            Log.i("debug.draw", "the button index: " + i);
            Rect rect = new Rect(0, 0, bitmaps.get(i).getBmpWidth(), bitmaps.get(i).getBmpHeight());
            Rect drawrect = new Rect(0, 0,
                    (int) (bitmaps.get(i).getBmpWidth()/(1.5)),
                    (int) (bitmaps.get(i).getBmpHeight()/(1.5)));
            float disX = (float) (drawwidth * (Math.cos(EUtil.getAngle(buttonCount, i))));
            float disY = (float) (drawheight * (Math.sin(EUtil.getAngle(buttonCount, i))));
            canvas.translate(disX, disY);
            canvas.drawBitmap(bitmaps.get(i).getBitmap(), rect, drawrect, new Paint());
            bitmaps.get(i).setX(disX + init_x);
            bitmaps.get(i).setY(disY + init_y);
            canvas.restore();
        }
    }

    public int judgebutton(float x, float y) {//返回第几个按钮
        for(int i=0;i<buttonCount;i++){
            if(x<(bitmaps.get(i).getX()+bitmaps.get(i).getBmpWidth()/2)&&
                    x>(bitmaps.get(i).getX())&&
                    y<(bitmaps.get(i).getY()+bitmaps.get(i).getBmpHeight())&&
                    y>(bitmaps.get(i).getY()))
                return i;
        }
        return -1;
    }

    private class drawBitmap {
        private Bitmap bitmap;
        private float x = 0, y = 0;
        private int bmpWidth, bmpHeight;

        private drawBitmap(Bitmap bitmap, float x, float y) {
            this.bitmap = bitmap;
            this.bmpWidth = bitmap.getWidth();
            this.bmpHeight = bitmap.getHeight();
            this.x = x;
            this.y = y;
        }

        private int getBmpHeight() {
            return bmpHeight;
        }

        private int getBmpWidth() {
            return bmpWidth;
        }

        private Bitmap getBitmap() {
            return bitmap;
        }

        private void setX(float x) {
            this.x = x;
        }

        private void setY(float y) {
            this.y = y;
        }

        private float getX() {
            return x;
        }

        private float getY() {
            return y;
        }
    }
}



