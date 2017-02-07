package com.example.blockwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Michael on 2/6/2017.
 */

public class PaintView extends View {
    private static final String Text = "Welcome To Hamad's Blog";
    private Path myArc;
    private Paint mPaintText;
    private float mTextWidth= 24.0f;
    private float mTextHeight = 24.0f;


    public PaintView(Context context) {
/*        super(context);
        //create Path object
        myArc = new Path();
        //create RectF Object
        RectF oval = new RectF(50,100,200,250);
        //add Arc in Path with start angle -180 and sweep angle 200
        myArc.addArc(oval, -180, 200);
        //create paint object
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        //set style
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        //set color
        mPaintText.setColor(Color.BLACK);
        //set text Size
        mPaintText.setTextSize(200f);
        // Get the text size
        mTextWidth = mPaintText.getTextSize();
        this.setWillNotDraw(false);*/
        super(context);

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setColor(Color.BLACK);
        //mPaintText.setTextSize(mTextHeight);

        mTextWidth = mTextHeight = mPaintText.getTextSize();
        //mTextWidth = mPaintText.getTextWidths("a",);
        //this.setWillNotDraw(false);

    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = MeasureSpec.getSize(w) - (int) mTextWidth + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(MeasureSpec.getSize(minh) - (int) mTextWidth, heightMeasureSpec, 0);

        setMeasuredDimension(1000, 1000);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Draw Text on Canvas
        // Draw the label text
        canvas.drawText("Test", 100, 100, mPaintText);
        canvas.drawRGB(255,0,0);
        //canvas.drawTextOnPath(Text, myArc, 0, 20, mPaintText);
        //invalidate();
    }
}