package com.example.blockwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
    //private static final String TXHASH = "b5357533bf43d6793aa24d91d6a01055128bff64730627bbb3a512b04d2e9043"; // Dummy hash for now; 64 hexadecimal characters
    private String currentHash;
    private Paint mPaintText; // Define the paint object to draw the letters and arcs
    private Rect bounds = new Rect(); // Define an object that will measure each character before determining its coordinates
    private float mTextHeight = 100.0f; // Define the text size here (might make it more dependent on screen later)
    private int[] circleCount = {30, 24, 10}; // Define how many characters will go in each circle, starting on the outside
    private int numberOfCircles = circleCount.length; // Define the number of circles
    private RectF[] mOvalsF = new RectF[circleCount.length]; // Define the array of bounding rectangles for all the circles of the watch
    // Declare the array list that will hold the initial arc sizes in each circle
    private List<float[]> degreesArray= new ArrayList<>(Arrays.asList(new float[circleCount[0]], new float[circleCount[1]], new float[circleCount[2]]));

    private float radius; // Declare the radius of the largest circle
    private float[] radiusSqueeze = {0.85f, 0.7f, 0.65f}; // 1 will put the text in the border, 0 will put the text in the center. Play with this to set the distance of your text.
    private float[] circleSpacing = {0.95f, 0.65f, 0.25f}; // Define the spacing of the circles used in the bounding rectangles
    private float centerX; // Declare the center x coordinate of all the circles
    private float centerY; // Declare the center y coordinate of all the circles
    float actionBarHeight; // Declare the height of the toolbar
    private float statusBarHeight; // Declare the height of the status bar
    private int[] startAngle = {0, 0, 0}; // Set the initial angles to 0; allows you to change any one later if you want it to move
    private int[] second = {0, 0, 0}; // Initialize second counter for all circles; only inner one should change
    Calendar mCalendar;
    String hour;
    String minute;
    String currentChar;
    List<Boolean> hourDrawn = new ArrayList<>(Arrays.asList(false, false));;
    List<Boolean> minuteDrawn = new ArrayList<>(Arrays.asList(false, false));

/*    private int[] COLORS = { Color.YELLOW, Color.GREEN, Color.WHITE,
            Color.CYAN, Color.RED }; // Define a set of colors used for drawing arcs around each character*/

    private int[] COLORS = {Color.BLACK, Color.WHITE}; // Create a simpler version here

    public PaintView(Context context, String currentHash) {
        super(context); // Call the superclass's (View) constructor
        this.currentHash = currentHash;
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
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android"); // Find the height of the status bar
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId); // If it exists, put it here
        }
        radius = (Math.min(metrics.widthPixels,(metrics.heightPixels - actionBarHeight - statusBarHeight)) / 2); // Measure the radius of the screen using the smallest dimension taking into account the action bar height
        centerX = metrics.widthPixels / 2; // Measure the center x coordinate
        // For some reason, adding actionBarHeight to the centerY coordinate pushes it too far down, even though it's required when computing the radius size
        centerY = (metrics.heightPixels + statusBarHeight) / 2; // Measure the center y coordinate of this rectangle taking into account the action bar height

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG); // Create paint object
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE); // Set style
        mPaintText.setTextSize(mTextHeight); // Set text size
        mPaintText.setTextAlign(Paint.Align.CENTER); // Set alignment

        for(int i=0;i<numberOfCircles;i++){
            Arrays.fill(degreesArray.get(i), 360/circleCount[i]); // Fill the arrays with the degrees for each arc
            // Declare the bounding rectangles for all the circles of the watch
            mOvalsF[i] = new RectF(Math.round(centerX - radius*circleSpacing[i]),Math.round(centerY - radius*circleSpacing[i]),Math.round(centerX + radius*circleSpacing[i]),Math.round(centerY + radius*circleSpacing[i]));
        }
        moveInnerDial.run(); //
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
        mCalendar = Calendar.getInstance();
        hour = String.valueOf(mCalendar.get(Calendar.HOUR));  // Get the hour
        if(hour.equals("0")){
            hour="12"; // Change the hour from 0 to 12 to fix Java notation
        }
        minute = String.valueOf(mCalendar.get(Calendar.MINUTE)); // Get the minute
        if(minute.length()==1){
            minute="0"+minute; // If the minute is less than 9, add a 0 in the front of it.
        }

        // Reset everything to false so it redraws the time correctly
        hourDrawn.set(0, false); hourDrawn.set(1, false);
        minuteDrawn.set(0, false); minuteDrawn.set(1, false);
        // Clear background with white rectangle since some former clock digits will extend past the circle
        mPaintText.setColor(Color.WHITE);
        canvas.drawRect(mOvalsF[0].left-3, mOvalsF[0].top-3, mOvalsF[0].right+3, mOvalsF[0].bottom+3, mPaintText); // Add white rectangle to back
        //canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);

        mPaintText.setAlpha(0);
        // All circles
        for(int circle=0;circle<numberOfCircles;circle++){ // Go through each circle
            if(circle!=1 && second[circle]!=0)
                continue; // If this is not the inner circle and you're not refreshing to mark a new minute, don't draw the circle
            int currentAngle = startAngle[circle]; // Start off the angle count the current moved angle
            // Add 'shadow' highlight to bottom right of each circle
            mPaintText.setColor(Color.BLACK);
            canvas.drawOval(mOvalsF[circle].left+10, mOvalsF[circle].top+10, mOvalsF[circle].right+10, mOvalsF[circle].bottom+10, mPaintText); // See if you can see an oval in the background
            for (int i = 0; i < degreesArray.get(circle).length; i++) // For each angle in the current circle
            {
                mPaintText.setTextSize(mTextHeight-circle*20); // Set text size, decreasing with each circle
                if (i > 0) // Initial angles will be 0, or directly to the right
                    currentAngle += (int) degreesArray.get(circle)[i - 1]; // Get the current starting angle
                mPaintText.setColor(COLORS[(i+circle)%COLORS.length]); // Set the color based on the color array, with an offset for each additional circle and an offset for the second count if the circle is keeping track of it
                canvas.drawArc(mOvalsF[circle], currentAngle, degreesArray.get(circle)[i], true, mPaintText); // Draw an arc behind the current character
            }
        }

        // All characters
        int charCount = 0; // Keep track of the character index
        for(int circle=0;circle<numberOfCircles;circle++){ // Go through each circle
            int currentAngle = startAngle[circle]; // Start off the angle count the current moved angle

            for (int i = 0; i < degreesArray.get(circle).length; i++) // For each character in the current circle
            {
                mPaintText.setAlpha(0);
                mPaintText.setShadowLayer(0,0,0,Color.TRANSPARENT);
                mPaintText.setTextSize(mTextHeight-circle*20); // Set text size, decreasing with each circle
                if (i > 0) // Initial angles will be 0, or directly to the right
                    currentAngle += (int) degreesArray.get(circle)[i - 1]; // Get the current starting angle
                mPaintText.setColor(COLORS[(i+circle+1)%COLORS.length]); // Make the text either black or white, the reverse of the arc color
                /* This angle will place the text in the center of the arc.
                * @see <a href="http://stackoverflow.com/questions/15739009/draw-text-inside-an-arc-using-canvas/19352717?noredirect=1#comment71429997_19352717">Draw text inside an arc using canvas</a> */
                float medianAngle = (currentAngle + (degreesArray.get(circle)[i] / 2f)) * (float) Math.PI / 180f;  // Go halfway between the current starting angle and the next angle, and convert to radians
                currentChar = String.valueOf(currentHash.charAt(charCount)); // Get the current character in the hash
                if(currentChar.equals(String.valueOf(hour.charAt(0))) && !hourDrawn.get(0)){
                    mPaintText.setTextSize(mTextHeight*2); // Make the hour really big
                    mPaintText.setColor(Color.RED); // Make the color different
                    hourDrawn.set(0, true); // Track that you've already drawn it
                    if(hour.length()==1){
                        currentChar=currentChar+":"; // Put a colon after the hour if it's only one number
                    }
                    mPaintText.setAlpha(255);
                    mPaintText.setShadowLayer(3,2,2,Color.BLACK);
                }
                else if(hour.length()==2 && !hourDrawn.get(1)){
                    if(currentChar.equals(String.valueOf(hour.charAt(1)))){ // If the current character matches the second number in the hour
                        mPaintText.setTextSize(mTextHeight*1.8f); // Make the hour really big
                        mPaintText.setColor(Color.RED);
                        hourDrawn.set(1, true);
                        currentChar=currentChar+":"; // Put a colon after the hour
                    }
                    mPaintText.setAlpha(255);
                    mPaintText.setShadowLayer(3,2,2,Color.BLACK);
                }
                else if(currentChar.equals(String.valueOf(minute.charAt(0))) && !minuteDrawn.get(0)){
                    mPaintText.setTextSize(mTextHeight*1.5f); // Make the hour really big
                    mPaintText.setColor(Color.RED); // Make the color different
                    minuteDrawn.set(0,true); // Track that you've already drawn it
                    mPaintText.setAlpha(255);
                    mPaintText.setShadowLayer(3,2,2,Color.BLACK);
                    currentChar=":"+currentChar; // Put a colon before the first minute to set it apart
                }
                else if(currentChar.equals(String.valueOf(minute.charAt(1))) && !minuteDrawn.get(1)){
                    mPaintText.setTextSize(mTextHeight*1.3f); // Make the hour really big
                    mPaintText.setColor(Color.RED); // Make the color different
                    minuteDrawn.set(1,true); // Track that you've already drawn it
                    mPaintText.setAlpha(255);
                    mPaintText.setShadowLayer(3,2,2,Color.BLACK);
                }
                // Measure the current character in the string
                mPaintText.getTextBounds(String.valueOf(currentHash.charAt(charCount)), 0, String.valueOf(currentHash.charAt(charCount)).length(), bounds); // Measure the text

                canvas.drawText(currentChar, (float)(centerX + (radius*radiusSqueeze[circle]*circleSpacing[circle] * Math.cos(medianAngle))), (float)(centerY + (radius*radiusSqueeze[circle]*circleSpacing[circle] * Math.sin(medianAngle)))+ bounds.height() * 0.5f, mPaintText);
                charCount++; // Increment the character count
            }
        }
    }

    Handler handler = new Handler(); // Looper.getMainLooper()
    Runnable moveInnerDial = new Runnable() {
        public void run() {
            if(second[1]==circleCount[1]) {
                second[1]=0; // Reset to 0 every full revolution
                startAngle[1] = 0; // Reset to 0 every full revolution
            }
            else {
                startAngle[1] = second[1] * (360/circleCount[1]); // Move the starting angle by the size of one arc for this circle
                second[1]++; // Otherwise increment the second count
            }
            invalidate(); // Redraw the canvas
            handler.postDelayed(this, 1000); // Redraw every second
        }
    };
}