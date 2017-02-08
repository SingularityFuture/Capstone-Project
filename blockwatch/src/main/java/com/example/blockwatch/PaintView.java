package com.example.blockwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Michael on 2/6/2017.
 */

public class PaintView extends View {
    private static final String TXHASH = "b5357533bf43d6793aa24d91d6a01055128bff64730627bbb3a512b04d2e9043";
    private Paint mPaintText;
    private Rect mOval;
    private float mTextHeight = 60.0f;
    int temp = 0;
    float values[] = {130,130,130,130,130,130,130,130,130,130,130,130,130,130,130,130,130,130,130,130,};
    float value_degrees[];

    private float left = 0f;
    private float top = 200f;
    private float radius = 1500f;
    private float centerX;
    private float centerY;


    public PaintView(Context context) {
        super(context);
        //create RectF Object
        mOval = new Rect(Math.round(left),Math.round(top),Math.round(left+radius),Math.round(top+radius));
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
        //Arrays.fill(values, 130);
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
        temp = 0;
        centerX = (mOval.right - mOval.left) / 2;
        centerY = (mOval.bottom - mOval.top) / 2;
        radius = (mOval.right - mOval.left) / 2;

        radius *= 0.5; // 1 will put the text in the border, 0 will put the text in the center. Play with this to set the distance of your text.

        for (int i = 0; i < value_degrees.length; i++)
        {
            if (i > 0)
                temp += (int) value_degrees[i - 1];

            float medianAngle = (temp + (value_degrees[i] / 2f)) * (float)Math.PI / 180f; // this angle will place the text in the center of the arc.
            canvas.drawText(String.valueOf(TXHASH.charAt(i)), (float)(centerX + (radius * Math.cos(medianAngle))), (float)(centerY + (radius * Math.sin(medianAngle))), mPaintText);
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