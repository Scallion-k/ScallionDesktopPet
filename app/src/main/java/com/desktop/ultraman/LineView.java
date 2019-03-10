package com.desktop.ultraman;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;

public class LineView extends View {
    // 绘制绳子视图

    PointF controlPoint, leftPoint, rightPoint; //两端点的绘制及扯绳子中间的控制点
    Paint paintArc = new Paint(); //绘制弧线
    Paint paintLine = new Paint(); //绘制直线

    public LineView(Context context) {
        super(context);
        controlPoint = new PointF(getMeasuredWidth() * 0.5f, 0);
        //getWidth():View在设定好布局之后整个View的宽度; 一般在view布局后呈现出来想获取宽度时
        //getMeasuredWidth():对View上的内容进行测量后得到的View内容占据的宽度;
        // 自定义view重写onLayout时、动态加载view想获得view原始宽度时应用
        leftPoint = new PointF(getMeasuredWidth() * 0.1f, 0);
        rightPoint = new PointF(getMeasuredWidth() * 0.9f, 0);
        paintArc.setColor(getResources().getColor(R.color.colorAccent));
        paintArc.setStyle(Paint.Style.STROKE);
        paintArc.setStrokeWidth(10);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(10);
    }

    public void setControlPoint(float x, float y) {
        this.controlPoint.x = x * 2;
        this.controlPoint.y = y * 2;
        postInvalidate();//postInvalidate(),invalidate():用来更新当前View
        //invalidate()在UI线程中刷新View,postInvalidate()在非UI线程中刷新View,底层使用Handler，指定一个延迟时间
    }

    @Override
    protected void onDraw(Canvas canvas) { //绘制两个点
        super.onDraw(canvas);
        leftPoint.x = getMeasuredWidth() * 0.25f;
        rightPoint.x = getMeasuredWidth() * 0.75f;
        if (controlPoint.x == 0 || controlPoint.y < 30) {
            controlPoint.y = 30;
            controlPoint.x = getMeasuredWidth() * 0.5f;
        }
        canvas.drawCircle(leftPoint.x, leftPoint.y + 10, 10, paintArc);
        canvas.drawCircle(rightPoint.x, rightPoint.y + 10, 10, paintArc);
        Path linePath = new Path();//绘制线条
        linePath.moveTo(leftPoint.x, leftPoint.y + 10); //设置下一个图形的开始点
        linePath.quadTo(controlPoint.x, controlPoint.y + 20, rightPoint.x, rightPoint.y + 10);
        //绘制“贝塞尔曲线”，x1y1为控制点坐标值，x2y2为终点坐标值
        canvas.drawPath(linePath, paintLine);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }
}
