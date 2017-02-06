package com.example.blockwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/**
 * Created by Michael on 2/6/2017.
 */

public class PaintView extends View {
    private static final String Text = "Welcome To Hamad's Blog";
    private Path myArc;
    private Paint mPaintText;

    public PaintView(Context context) {
        super(context);
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
        mPaintText.setColor(Color.WHITE);
        //set text Size
        mPaintText.setTextSize(20f);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Draw Text on Canvas
        canvas.drawTextOnPath(Text, myArc, 0, 20, mPaintText);
        invalidate();
    }
}