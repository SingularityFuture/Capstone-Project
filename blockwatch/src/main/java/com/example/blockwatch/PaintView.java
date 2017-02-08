package com.example.blockwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Michael on 2/6/2017.
 */

public class PaintView extends View {
    private static final String Text = "Welcome To Hamad's Blog";
    private Path myArc;
    private Paint mPaintText;
    private float mTextHeight = 60.0f;

    public PaintView(Context context) {
        super(context);
        //create Path object
        myArc = new Path();
        //create RectF Object
        RectF oval = new RectF(300,500,700,900);
        //add Arc in Path with start angle -90 and sweep angle 360
        myArc.addArc(oval, 0, 360);
        //create paint object
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        //set style
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        //set color
        mPaintText.setColor(Color.BLACK);
        //set text Size
        mPaintText.setTextSize(mTextHeight);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawTextOnPath(Text, myArc, 0, 20, mPaintText);
    }
}