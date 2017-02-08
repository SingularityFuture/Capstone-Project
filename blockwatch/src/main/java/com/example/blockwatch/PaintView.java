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
    private float mTextWidth= 70.0f;
    private float mTextHeight = 70.0f;

    private final int mCanvasWidth = 400;

    private Paint myPaint;
    private Rect myRect;

    private Rect mDrawRect;


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
        mPaintText.setTextSize(mTextHeight);

        myPaint = new Paint();
        myPaint.setColor(Color.rgb(0, 255, 0));
        myPaint.setStrokeWidth(100);

        myRect = new Rect();
        myRect.set(0,0,200,200);

        //mTextWidth = mTextHeight = mPaintText.getTextSize();
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
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		//Get size requested and size mode
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width, height = 0;

		//Determine Width
		switch(widthMode){
		case MeasureSpec.EXACTLY:
			width = widthSize;
            //width = mCanvasWidth;
			break;
		case MeasureSpec.AT_MOST:
			width = Math.min(mCanvasWidth, widthSize);
			break;
		case MeasureSpec.UNSPECIFIED:
		default:
			width = mCanvasWidth;
			break;
		}

		//Determine Height
		switch(heightMode){
		case MeasureSpec.EXACTLY:
			height = heightSize;
			break;
		case MeasureSpec.AT_MOST:
			height = Math.min(mCanvasWidth, heightSize);
			break;
		case MeasureSpec.UNSPECIFIED:
		default:
			height = mCanvasWidth;
			break;
		}
		setMeasuredDimension(width, height);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
/*        int parentWidth = oldw;
        int parentHeight = oldh;
        int ww=w;
        int hh=h;
        this.setMeasuredDimension(w, h);*/
        super.onSizeChanged(w,h,oldw,oldh);

        myRect.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Draw Text on Canvas
        //canvas.drawRGB(255,0,0);
        // Draw the label text
        //canvas.drawText("Test", 400, 400, mPaintText);
        canvas.drawRect(myRect, myPaint);
        //canvas.drawTextOnPath(Text, myArc, 0, 20, mPaintText);
        //invalidate();
    }
}