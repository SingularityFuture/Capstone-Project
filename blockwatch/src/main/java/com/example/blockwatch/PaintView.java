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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private Rect bounds = new Rect(); // Define an object that will measure each character before determining its coordinates
    private float mTextHeight = 100.0f; // Define the text size here (might make it more dependent on screen later)
    private int[] circleCount = {30, 24, 10}; // Define how many characters will go in each circle, starting on the outside
    private int numberOfCircles = circleCount.length; // Define the number of circles
    private RectF[] mOvalsF = new RectF[circleCount.length]; // Define the array of bounding rectangles for all the circles of the watch
    // Declare the array list that will hold the initial arc sizes in each circle
    private List<float[]> degreesArray= new ArrayList<>(Arrays.asList(new float[circleCount[0]], new float[circleCount[1]], new float[circleCount[2]]));

    private float radius; // Declare the radius of the largest circle
    private float radiusSqueeze = 0.85f; // 1 will put the text in the border, 0 will put the text in the center. Play with this to set the distance of your text.
    private float[] circleSpacing = {0.95f, 0.65f, 0.25f}; // Define the spacing of the circles
    private float centerX; // Declare the center x coordinate of all the circles
    private float centerY; // Declare the center y coordinate of all the circles
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
        radius = (Math.min(metrics.widthPixels,metrics.heightPixels - actionBarHeight) / 2); // Measure the radius of the screen using the smallest dimension taking into account the action bar height
        centerX = metrics.widthPixels / 2; // Measure the center x coordinate
        centerY = radius + actionBarHeight; // Measure the center y coordinate of this rectangle taking into account the action bar height

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG); // Create paint object
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE); // Set style
        mPaintText.setColor(Color.BLACK); // Set color
        mPaintText.setTextSize(mTextHeight); // Set text size
        mPaintText.setTextAlign(Paint.Align.CENTER); // Set alignment

        for(int i=0;i<numberOfCircles;i++){
            Arrays.fill(degreesArray.get(i), 360/circleCount[i]); // Fill the arrays with the degrees for each arc
            // Declare the bounding rectangles for all the circles of the watch
            mOvalsF[i] = new RectF(Math.round(centerX - radius*circleSpacing[i]),Math.round(centerY - radius*circleSpacing[i]),Math.round(centerX + radius*circleSpacing[i]),Math.round(centerY + radius*circleSpacing[i]));
        }
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
        // All circles
        for(int circle=0;circle<numberOfCircles;circle++){ // Go through each circle
            int startAngle = 0; // Set the initial angle to 0
            int charCount = 0; // Keep track of the character index
            mPaintText.setTextSize(mTextHeight-circle*15); // Set text size, decreasing with each circle
            for (int i = 0; i < degreesArray.get(circle).length; i++) // For each character in the current circle
            {
                if (i > 0) // Initial angle will be 0, or directly to the right
                    startAngle += (int) degreesArray.get(circle)[i - 1]; // Get the current starting angle
                mPaintText.setColor(COLORS[(i+circle)%COLORS.length]); // Set the color based on the color array, with an offset for each additional circle
                canvas.drawArc(mOvalsF[circle], startAngle, degreesArray.get(circle)[i], true, mPaintText); // Draw an arc behind the current character
                mPaintText.setColor(Color.BLACK); // Reset the color for the characters
                // Measure the current character in the string
                mPaintText.getTextBounds(String.valueOf(TXHASH.charAt(charCount)), 0, String.valueOf(TXHASH.charAt(charCount)).length(), bounds); // Measure the text
                /** This angle will place the text in the center of the arc.
                * @see <a href="http://stackoverflow.com/questions/15739009/draw-text-inside-an-arc-using-canvas/19352717?noredirect=1#comment71429997_19352717">Draw text inside an arc using canvas</a> */
                float medianAngle = (startAngle + (degreesArray.get(circle)[i] / 2f)) * (float) Math.PI / 180f;  // Go halfway between the current starting angle and the next angle, and convert to radians
                canvas.drawText(String.valueOf(TXHASH.charAt(charCount)), (float)(centerX + (radius*radiusSqueeze*circleSpacing[circle] * Math.cos(medianAngle))), (float)(centerY + (radius*radiusSqueeze*circleSpacing[circle] * Math.sin(medianAngle)))+ bounds.height() * 0.5f, mPaintText);
                charCount++; // Increment the character count
            }
        }
    }
}