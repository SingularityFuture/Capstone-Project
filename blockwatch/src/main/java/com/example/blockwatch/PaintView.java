package com.example.blockwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import java.util.Arrays;

/**
 * Created by Michael on 2/6/2017.
 *
 * @see <a href="http://stackoverflow.com/questions/15739009/draw-text-inside-an-arc-using-canvas/19352717?noredirect=1#comment71429997_19352717">Draw text inside an arc using canvas</a>
 */

public class PaintView extends View {
    private static final String TXHASH = "b5357533bf43d6793aa24d91d6a01055128bff64730627bbb3a512b04d2e9043";
    private Paint mPaintText;
    private Rect mOval;
    private float mTextHeight = 100.0f;
    int temp = 0;
    int outerCircleCount = 30;
    float outerArcSweep =360/outerCircleCount ;
    int innerCircleCount = 24;
    float innerArcSweep =360/innerCircleCount ;
    int firstCircleCount = 10;
    float firstArcSweep = 360/firstCircleCount;
    float outerInitialArcs[]; float innerInitialArcs[]; float firstInitialArcs[];
    float outerDegreesArray[]; float innerDegreesArray[]; float firstDegreesArray[];

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

        outerInitialArcs = new float[(int) outerCircleCount];
        Arrays.fill(outerInitialArcs, outerArcSweep);
        outerDegreesArray = calculateData(outerInitialArcs);
        innerInitialArcs = new float[(int) innerCircleCount];
        Arrays.fill(innerInitialArcs, innerArcSweep);
        innerDegreesArray = calculateData(innerInitialArcs);
        firstInitialArcs = new float[(int) firstCircleCount];
        Arrays.fill(firstInitialArcs, firstArcSweep);
        firstDegreesArray = calculateData(firstInitialArcs);
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
        radius *= 0.9; // 1 will put the text in the border, 0 will put the text in the center. Play with this to set the distance of your text.

        // Outer circle
        //set text Size
        mPaintText.setTextSize(mTextHeight);
        for (int i = 0; i < outerDegreesArray.length; i++)
        {
            if (i > 0)
                temp += (int) outerDegreesArray[i - 1];
            float medianAngle = (temp + (outerDegreesArray[i] / 2f)) * (float)Math.PI / 180f; // this angle will place the text in the center of the arc.
            canvas.drawText(String.valueOf(TXHASH.charAt(i)), (float)(centerX + (radius * Math.cos(medianAngle))), (float)(centerY + (radius * Math.sin(medianAngle))), mPaintText);
        }
        // Inner circle
        temp = 0;
        //set text Size
        mPaintText.setTextSize(mTextHeight-15);
        for (int i = 0; i < innerDegreesArray.length; i++)
        {
            if (i > 0)
                temp += (int) innerDegreesArray[i - 1];
            float medianAngle = (temp + (innerDegreesArray[i] / 2f)) * (float)Math.PI / 180f; // this angle will place the text in the center of the arc.
            canvas.drawText(String.valueOf(TXHASH.charAt(i+outerCircleCount)), (float)(centerX + (radius*0.65 * Math.cos(medianAngle))), (float)(centerY + (radius*0.65 * Math.sin(medianAngle))), mPaintText);
        }
        // First circle
        temp = 0;
        //set text Size
        mPaintText.setTextSize(mTextHeight-25);
        for (int i = 0; i < firstDegreesArray.length; i++)
        {
            if (i > 0)
                temp += (int) firstDegreesArray[i - 1];
            float medianAngle = (temp + (firstDegreesArray[i] / 2f)) * (float)Math.PI / 180f; // this angle will place the text in the center of the arc.
            canvas.drawText(String.valueOf(TXHASH.charAt(i+outerCircleCount+innerCircleCount)), (float)(centerX + (radius*0.25 * Math.cos(medianAngle))), (float)(centerY + (radius*0.25 * Math.sin(medianAngle))), mPaintText);
        }
    }

    private float[] calculateData(float[] initialArcs) {
        float total = 0;
        float[] degreeResults = new float[initialArcs.length];
        for (int i = 0; i < initialArcs.length; i++) total += initialArcs [i];
        for (int i = 0; i < initialArcs.length; i++) {
            degreeResults[i] = 360 * (initialArcs[i] / total);
        }
        return degreeResults;
    }
}