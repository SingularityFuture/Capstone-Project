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
 *
 * Creates a view that holds the current transaction hash from the Blockchain for one minute
 * Displays the hash in a circle (will decide later to add colored arcs or not)
 * For information on calculations of arc angles to make each letter vertically oriented: *
 * @see <a href="http://stackoverflow.com/questions/15739009/draw-text-inside-an-arc-using-canvas/19352717?noredirect=1#comment71429997_19352717">Draw text inside an arc using canvas</a>
 */

public class PaintView extends View {
    private static final String TXHASH = "b5357533bf43d6793aa24d91d6a01055128bff64730627bbb3a512b04d2e9043"; // Dummy hash for now; 64 hexadecimal characters
    private Paint mPaintText; // Define the paint object to draw the letters and arcs
    private RectF mOvalFOuter; // Define the bounds of the outer circle
    private RectF mOvalFInner; // Define the bounds of the inner circle
    private RectF mOvalFFirst; // Define the bounds of the first circle
    private Rect bounds = new Rect(); // Define an object that will measure each character before determining its coordinates
    private float mTextHeight = 100.0f; // Define the text size here (might make it more dependent on screen later)
    int temp = 0; // Define variable used for calculating arc sweeps
    int[] circleCount = {30, 24, 10}; // Define how many characters will go in each circle, starting on the outside
    // Declare the arrays that will hold the initial arc sizes in each circle
    float outerDegreesArray[] = new float[circleCount[0]]; float innerDegreesArray[] = new float[circleCount[1]]; float firstDegreesArray[] = new float[circleCount[2]];

    private float radius; // Declare the radius of the largest circle
    private float radiusSqueeze = 0.85f; // 1 will put the text in the border, 0 will put the text in the center. Play with this to set the distance of your text.
    private float centerX; // Declare the center x coordinate of all the circles
    private float centerY; // Declare the center y coordinate of all the circles
    private float screenWidth; // Declare the width of the screen
    private float screenHeight; // Declare the height of the screen
    private float actionBarHeight; // Declare the height of the screen



    private int[] COLORS = { Color.YELLOW, Color.GREEN, Color.WHITE,
            Color.CYAN, Color.RED }; // Define a set of colors used for drawing arcs around each character

    public PaintView(Context context) {
        super(context); // Call the superclass's (View) constructor
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE); // Get the window manager
        DisplayMetrics metrics = new DisplayMetrics(); // Declare a metrics object
        wm.getDefaultDisplay().getMetrics(metrics); // Get the metrics of the window
        // Calculate ActionBar height
        TypedValue tv = new TypedValue(); // Declare a typed value
        if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) // Get the action bar size attribute
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,metrics); // Put it in this variable if it exists
        }
        else {
            actionBarHeight = 168; // Otherwise declare a default value
        }
        screenWidth = metrics.widthPixels; // Measure the screen width.
        screenHeight = metrics.heightPixels; // Measure the screen height.

        radius = (Math.min(screenWidth,screenHeight - actionBarHeight) / 2); // Measure the radius of the screen using the smallest dimension taking into account the action bar height
        //mOvalFOuter = new RectF(Math.round(left),Math.round(top + actionBarHeight),Math.round(left+radius*2),Math.round(top+actionBarHeight+radius*2)); // Declare the bounding rectangle for the largest circle of the watch
        centerX = screenWidth / 2; // Measure the center x coordinate
        centerY = radius + actionBarHeight; // Measure the center y coordinate of this rectangle taking into account the action bar height
        // Declare the bounding rectangle for the largest circle of the watch
        mOvalFOuter = new RectF(Math.round(centerX - radius*0.95),Math.round(centerY - radius*0.95),Math.round(centerX + radius*0.95),Math.round(centerY + radius*0.95));
        // Declare the bounding rectangle for the inner circle of the watch
        mOvalFInner = new RectF(Math.round(centerX - radius*0.65),Math.round(centerY - radius*0.65),Math.round(centerX + radius*0.65),Math.round(centerY + radius*0.65));
        // Declare the bounding rectangle for the first circle of the watch
        mOvalFFirst = new RectF(Math.round(centerX - radius*0.25),Math.round(centerY - radius*0.25),Math.round(centerX + radius*0.25),Math.round(centerY + radius*0.25));

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG); // Create paint object
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE); // Set style
        mPaintText.setColor(Color.BLACK); // Set color
        mPaintText.setTextSize(mTextHeight); // Set text size
        mPaintText.setTextAlign(Paint.Align.CENTER); // Set alignment

        Arrays.fill(outerDegreesArray, 360/circleCount[0]); // Fill the arrays with the degrees for each arc
        Arrays.fill(innerDegreesArray, 360/circleCount[1]);
        Arrays.fill(firstDegreesArray, 360/circleCount[2]);
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
        // Outer circle
        temp = 0;
        mPaintText.setTextSize(mTextHeight); // Set text size
        for (int i = 0; i < outerDegreesArray.length; i++)
        {
            if (i > 0)
                temp += (int) outerDegreesArray[i - 1];
            mPaintText.setColor(COLORS[i%5]);
            canvas.drawArc(mOvalFOuter, temp, outerDegreesArray[i], true, mPaintText);
            mPaintText.setColor(Color.BLACK);
            mPaintText.getTextBounds(String.valueOf(TXHASH.charAt(i)), 0, String.valueOf(TXHASH.charAt(i)).length(), bounds); // Measure the text
            float medianAngle = (temp + (outerDegreesArray[i] / 2f)) * (float)Math.PI / 180f; // this angle will place the text in the center of the arc.
            canvas.drawText(String.valueOf(TXHASH.charAt(i)), (float)(centerX + (radius*radiusSqueeze * Math.cos(medianAngle))), (float)(centerY + (radius*radiusSqueeze * Math.sin(medianAngle)))+ bounds.height() * 0.5f, mPaintText);
        }
        // Inner circle
        temp = 0;
        mPaintText.setTextSize(mTextHeight-15); // Set text size
        for (int i = 0; i < innerDegreesArray.length; i++)
        {
            if (i > 0)
                temp += (int) innerDegreesArray[i - 1];
            mPaintText.setColor(COLORS[(i+1)%5]);
            canvas.drawArc(mOvalFInner, temp, innerDegreesArray[i], true, mPaintText);
            mPaintText.setColor(Color.BLACK);
            mPaintText.getTextBounds(String.valueOf(TXHASH.charAt(i+circleCount[0])), 0, String.valueOf(TXHASH.charAt(i+circleCount[0])).length(), bounds); // Measure the text
            float medianAngle = (temp + (innerDegreesArray[i] / 2f)) * (float)Math.PI / 180f; // This angle will place the text in the center of the arc.
            canvas.drawText(String.valueOf(TXHASH.charAt(i+circleCount[0])), (float)(centerX + (radius*radiusSqueeze*0.65 * Math.cos(medianAngle))), (float)(centerY + (radius*radiusSqueeze*0.65 * Math.sin(medianAngle)))+ bounds.height() * 0.5f, mPaintText);
        }
        // First circle
        temp = 0;
        mPaintText.setTextSize(mTextHeight-25); // Set text size
        for (int i = 0; i < firstDegreesArray.length; i++)
        {
            if (i > 0)
                temp += (int) firstDegreesArray[i - 1];
            mPaintText.setColor(COLORS[(i+2)%5]);
            canvas.drawArc(mOvalFFirst, temp, firstDegreesArray[i], true, mPaintText);
            mPaintText.setColor(Color.BLACK);
            mPaintText.getTextBounds(String.valueOf(TXHASH.charAt(i+circleCount[0]+circleCount[1])), 0, String.valueOf(TXHASH.charAt(i+circleCount[0]+circleCount[1])).length(), bounds); // Measure the text
            float medianAngle = (temp + (firstDegreesArray[i] / 2f)) * (float)Math.PI / 180f; // This angle will place the text in the center of the arc.
            canvas.drawText(String.valueOf(TXHASH.charAt(i+circleCount[0]+circleCount[1])), (float)(centerX + (radius*radiusSqueeze*0.25 * Math.cos(medianAngle))), (float)(centerY + (radius*radiusSqueeze*0.25 * Math.sin(medianAngle)))+ bounds.height() * 0.5f, mPaintText);
        }
    }
}