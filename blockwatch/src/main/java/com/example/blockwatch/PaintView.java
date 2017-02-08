package com.example.blockwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Michael on 2/6/2017.
 */

public class PaintView extends View {
    private static final String Text = "Welcome To Hamad's Blog";
    private Path myArc;
    private Paint mPaintText;
    private Rect mOval;
    private float mTextHeight = 60.0f;
    int temp = 0;
    float values[] = {130, 130, 130, 130, 130};
    float value_degrees[];

    private float centerX = 300;
    private float centerY = 300;
    private float radius = 100;

    public PaintView(Context context) {
        super(context);
        //create Path object
        myArc = new Path();
        //create RectF Object
        mOval = new Rect(300,500,700,900);
        //add Arc in Path with start angle -90 and sweep angle 360
        //myArc.addArc(mOval, 0, 360);
        //create paint object
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        //set style
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        //set color
        mPaintText.setColor(Color.BLACK);
        //set text Size
        mPaintText.setTextSize(mTextHeight);
        // Set alignment
        mPaintText.setTextAlign(Paint.Align.CENTER);
        value_degrees = calculateData(values);
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
        //canvas.drawTextOnPath(Text, myArc, 0, 20, mPaintText);
        //canvas.drawText(String.valueOf(Text.charAt(0)), (centerX - radius/ 2) + 35, (y + radius / 2) - 35, mPaintText);

        temp = 0;
        int centerX = (mOval.left + mOval.right) / 2;
        int centerY = (mOval.top + mOval.bottom) / 2;
        int radius = (mOval.right - mOval.left) / 2;

        radius *= 0.5; // 1 will put the text in the border, 0 will put the text in the center. Play with this to set the distance of your text.

        for (int i = 0; i < value_degrees.length; i++)
        {
            if (i > 0)
                temp += (int) value_degrees[i - 1];

            float medianAngle = (temp + (value_degrees[i] / 2f)) * (float)Math.PI / 180f; // this angle will place the text in the center of the arc.
            canvas.drawText(String.valueOf(Text.charAt(i)), (float)(centerX + (radius * Math.cos(medianAngle))), (float)(centerY + (radius * Math.sin(medianAngle))), mPaintText);
        }
    }

    private float[] calculateData(float[] value_degrees) {
        float total = 0;
        for (int i = 0; i < value_degrees .length; i++) {
            total += value_degrees [i];
        }
        for (int i = 0; i < value_degrees .length; i++) {
            value_degrees [i] = 360 * (value_degrees [i] / total);
        }
        return value_degrees ;
    }
}