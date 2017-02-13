package com.example.blockwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import java.util.Arrays;

/**
 * Created by Michael on 2/6/2017.
 */

public class PaintView extends View {
    private static final String TXHASH = "b5357533bf43d6793aa24d91d6a01055128bff64730627bbb3a512b04d2e9043";
    private Paint mPaintText;
    private Paint mPaintOval;
    private Paint mPaintCircle;
    private Rect mOval;
    private RectF mOvalF;
    private float mTextHeight = 70.0f;
    int temp = 0;
    float outerCircleCount = 30;
    float outerArcSweep =360/outerCircleCount ;
    float innerCircleCount = 24;
    float innerArcSweep =360/innerCircleCount ;
    float firstCircleCount = 10;
    float firstArcSweep = 360/firstCircleCount;
    float values[];
    float value_degrees[];

    private int[] COLORS = { Color.YELLOW, Color.GREEN, Color.WHITE,
            Color.CYAN, Color.RED };


    private float left = 0f;
    private float top = 0f;
    private float radius;
    private float centerX;
    private float centerY;
    private float screenWidth;
    private float screenHeight;
    private float actionBarHeight;

    public PaintView(Context context) {
        super(context);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,metrics);
        }
        else {
            actionBarHeight = 168;
        }
        screenWidth = metrics.widthPixels; // Measure the screen width.
        screenHeight = metrics.heightPixels; // Measure the screen height.

        radius = Math.min(screenWidth,screenHeight) / 2;

        mOval = new Rect(Math.round(left),Math.round(top + actionBarHeight),Math.round(left+radius*2),Math.round(top+actionBarHeight+radius*2));
        mOvalF = new RectF(Math.round(left),Math.round(top + actionBarHeight),Math.round(left+radius*2),Math.round(top+actionBarHeight+radius*2));
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

        mPaintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintCircle.setColor(Color.RED);
        mPaintCircle.setStyle(Paint.Style.FILL);

        mPaintOval=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOval.setColor(Color.CYAN);
        mPaintOval.setStyle(Paint.Style.FILL);
        values = new float[(int) outerCircleCount];
        Arrays.fill(values, outerArcSweep);
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
        temp = 0;
        centerX = (mOval.right - mOval.left) / 2;
        centerY = (mOval.bottom - mOval.top) / 2 + actionBarHeight;
        canvas.drawCircle(centerX,centerY,radius/6,mPaintCircle); // See where the center is!

        radius *= 0.95; // 1 will put the text in the border, 0 will put the text in the center. Play with this to set the distance of your text.

        for (int i = 0; i < value_degrees.length; i++)
        {
            if (i > 0)
                temp += (int) value_degrees[i - 1];


            mPaintText.setColor(COLORS[i%5]);
            canvas.drawArc(mOvalF, temp, value_degrees[i], true, mPaintText);

            mPaintText.setColor(Color.BLACK);

            float medianAngle = (temp + (value_degrees[i] / 2f)) * (float)Math.PI / 180f; // this angle will place the text in the center of the arc.

            canvas.drawText(String.valueOf(TXHASH.charAt(i)), (float)(centerX + (radius * Math.cos(medianAngle))), (float)(centerY + (radius * Math.sin(medianAngle))), mPaintText);
        }
    }

    private float[] calculateData(float[] values) {
        float total = 0;
        value_degrees = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            total += values [i];
        }
        for (int i = 0; i < values.length; i++) {
            value_degrees [i] = 360 * (values [i] / total);
        }
        return value_degrees ;
    }
}