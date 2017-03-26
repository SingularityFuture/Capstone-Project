package com.example.blockwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.example.blockwatch.R.string.hour_one_color;
import static com.example.blockwatch.R.string.hour_two_color;
import static com.example.blockwatch.R.string.minute_one_color;
import static com.example.blockwatch.R.string.minute_two_color;
import static com.example.blockwatch.R.string.show_time_preference_key;

/**
 * Created by Michael on 2/6/2017.
 * <p>
 * Creates a view that holds the current transaction hash from the Blockchain for one minute
 * Displays the hash in a circle (will decide later to add colored arcs or not)
 * For information on calculations of arc angles to make each letter vertically oriented: *
 *
 * @see <a href="http://stackoverflow.com/questions/15739009/draw-text-inside-an-arc-using-canvas/19352717?noredirect=1#comment71429997_19352717">Draw text inside an arc using canvas</a>
 */

public class PaintView extends View {
    private final float firstHourTextScale = 2.2f;
    private final float secondHourTextScale = 1.8f;
    private final float firstMinuteTextScale = 1.6f;
    private final float secondMinuteTextScale = 1.05f;
    float actionBarHeight; // Declare the height of the toolbar
    Calendar mCalendar;
    String hour;
    String minute;
    String currentChar;
    List<Boolean> hourDrawn = new ArrayList<>(Arrays.asList(false, false));
    List<Boolean> minuteDrawn = new ArrayList<>(Arrays.asList(false, false));
    boolean isTablet = getResources().getBoolean(R.bool.isTablet); // Detect whether we are in tablet mode, which will affect the drawing size and coordinates
    Handler handler = new Handler(); // Looper.getMainLooper()
    private String currentHash;
    private Paint mPaintText; // Define the paint object to draw the letters and arcs
    private Rect bounds = new Rect(); // Define an object that will measure each character before determining its coordinates
    private float mTextHeight = 100.0f; // Define the text size here (might make it more dependent on screen later)
    private int[] circleCount = {30, 24, 10}; // Define how many characters will go in each circle, starting on the outside
    private int numberOfCircles = circleCount.length; // Define the number of circles
    private RectF[] mOvalsF = new RectF[circleCount.length]; // Define the array of bounding rectangles for all the circles of the watch
    // Declare the array list that will hold the initial arc sizes in each circle
    private List<float[]> degreesArray = new ArrayList<>(Arrays.asList(new float[circleCount[0]], new float[circleCount[1]], new float[circleCount[2]]));
    private float radius; // Declare the radius of the largest circle
    private float[] radiusSqueeze = {0.85f, 0.7f, 0.65f}; // 1 will put the text in the border, 0 will put the text in the center. Play with this to set the distance of your text.
    private float[] circleSpacing = {0.95f, 0.65f, 0.25f}; // Define the spacing of the circles used in the bounding rectangles
    private float centerX; // Declare the center x coordinate of all the circles
    private float centerY; // Declare the center y coordinate of all the circles
    private float statusBarHeight; // Declare the height of the status bar
    private int[] startAngle = {0, 0, 0}; // Set the initial angles to 0; allows you to change any one later if you want it to move
    private int[] second = {0, 0, 0}; // Initialize second counter for all circles; only inner one should change
    Runnable moveInnerDial = new Runnable() {
        public void run() {
            if (second[1] == circleCount[1]) {
                second[1] = 0; // Reset to 0 every full revolution
                startAngle[1] = 0; // Reset to 0 every full revolution
            } else {
                startAngle[1] = second[1] * (360 / circleCount[1]); // Move the starting angle by the size of one arc for this circle
                second[1]++; // Otherwise increment the second count
            }
            invalidate(); // Redraw the canvas
            handler.postDelayed(this, 1000); // Redraw every second
        }
    };
    private int[] COLORS = {Color.BLACK, Color.WHITE}; // Create a simpler version here
    private int charCount;
    private int circle;
    private int currentAngle;
    private float medianAngle;
    private boolean timeDigit;
    private int[] charIndices = {0, 0, 0, 0};
    private int j;
    private int[] circleIndices = {0, 0, 0, 0};
    private int[] indexInCircle = {0, 0, 0, 0};
    private int[] timeAngles = {0, 0, 0, 0};
    private int am_pm;
    int hourOneColor = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getResources().getString(hour_one_color), ContextCompat.getColor(getContext(),R.color.red));
    int hourTwoColor = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getResources().getString(hour_two_color), ContextCompat.getColor(getContext(),R.color.red));
    int minuteOneColor = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getResources().getString(minute_one_color), ContextCompat.getColor(getContext(),R.color.red));
    int minuteTwoColor = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getResources().getString(minute_two_color), ContextCompat.getColor(getContext(),R.color.red));
    boolean showTimeBelowWheel= PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getResources().getString(show_time_preference_key), true);

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
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, metrics); // Put it in this variable if it exists
        } else {
            actionBarHeight = 168; // Otherwise declare a default value
        }
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android"); // Find the height of the status bar
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId); // If it exists, put it here
        }
        if (!isTablet) { // If it's not a tablet, have this fill the whole screen
            radius = (Math.min(metrics.widthPixels, (metrics.heightPixels - actionBarHeight - statusBarHeight)) / 2); // Measure the radius of the screen using the smallest dimension taking into account the action bar height
            centerX = metrics.widthPixels / 2; // Measure the center x coordinate
        } else { // Otherwise, push it over to the left half of the screen
            radius = (Math.min(metrics.widthPixels, (metrics.heightPixels - actionBarHeight - statusBarHeight)) / 2.5f); // Measure the radius of the screen using the smallest dimension taking into account the action bar height
            centerX = metrics.widthPixels / 4; // Measure the center x coordinate
        }
        // For some reason, adding actionBarHeight to the centerY coordinate pushes it too far down, even though it's required when computing the radius size
        centerY = (metrics.heightPixels + statusBarHeight) / 2; // Measure the center y coordinate of this rectangle taking into account the action bar height

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG); // Create paint object
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE); // Set style
        mPaintText.setTextSize(mTextHeight); // Set text size
        mPaintText.setTextAlign(Paint.Align.CENTER); // Set alignment

        for (int i = 0; i < numberOfCircles; i++) {
            Arrays.fill(degreesArray.get(i), 360 / circleCount[i]); // Fill the arrays with the degrees for each arc
            // Declare the bounding rectangles for all the circles of the watch
            mOvalsF[i] = new RectF(Math.round(centerX - radius * circleSpacing[i]), Math.round(centerY - radius * circleSpacing[i]), Math.round(centerX + radius * circleSpacing[i]), Math.round(centerY + radius * circleSpacing[i]));
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
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCalendar = Calendar.getInstance();
        hour = String.valueOf(mCalendar.get(Calendar.HOUR));  // Get the hour
        am_pm = mCalendar.get(Calendar.AM_PM);
        if (hour.equals("0")) {
            hour = "12"; // Change the hour from 0 to 12 to fix Java notation
        }
        minute = String.valueOf(mCalendar.get(Calendar.MINUTE)); // Get the minute
        if (minute.length() == 1) {
            minute = "0" + minute; // If the minute is less than 9, add a 0 in the front of it.
        }

        // Reset everything to false so it redraws the time correctly
        hourDrawn.set(0, false);
        hourDrawn.set(1, false);
        minuteDrawn.set(0, false);
        minuteDrawn.set(1, false);
        // Clear background with white rectangle since some former clock digits will extend past the circle
        mPaintText.setColor(Color.WHITE);
        canvas.drawRect(mOvalsF[0].left - 200, mOvalsF[0].top - 150, mOvalsF[0].right + 200, mOvalsF[0].bottom + 300, mPaintText); // Add white rectangle to back
        mPaintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)); // Make the hash normal text style
        mPaintText.setShadowLayer(0, 0, 0, Color.BLACK); // Nullify the shadow layer

        // All circles
        // Keep track of the character index
        charCount = 0;
        for (circle = 0; circle < numberOfCircles; circle++) { // Go through each circle
            // Start off the angle count the current moved angle
            currentAngle = startAngle[circle];
            // Add 'shadow' highlight to bottom right of each circle
            mPaintText.setColor(Color.BLACK);
            canvas.drawOval(mOvalsF[circle].left + 10, mOvalsF[circle].top + 10, mOvalsF[circle].right + 10, mOvalsF[circle].bottom + 10, mPaintText); // See if you can see an oval in the background
            for (int i = 0; i < degreesArray.get(circle).length; i++) // For each angle in the current circle
            {
                timeDigit = false;
                mPaintText.setTextSize(mTextHeight - circle * 20); // Set text size, decreasing with each circle
                if (i > 0) // Initial angles will be 0, or directly to the right
                    currentAngle += (int) degreesArray.get(circle)[i - 1]; // Get the current starting angle
                mPaintText.setColor(COLORS[(i + circle) % COLORS.length]); // Set the color based on the color array, with an offset for each additional circle and an offset for the second count if the circle is keeping track of it
                canvas.drawArc(mOvalsF[circle], currentAngle, degreesArray.get(circle)[i], true, mPaintText); // Draw an arc behind the current character
                currentChar = String.valueOf(currentHash.charAt(charCount)); // Get the current character in the hash
                /* This angle will place the text in the center of the arc.
                * @see <a href="http://stackoverflow.com/questions/15739009/draw-text-inside-an-arc-using-canvas/19352717?noredirect=1#comment71429997_19352717">Draw text inside an arc using canvas</a> */
                // Go halfway between the current starting angle and the next angle, and convert to radians
                medianAngle = (currentAngle + (degreesArray.get(circle)[i] / 2f)) * (float) Math.PI / 180f;
                // Set the color of the text to opposite of the background color
                mPaintText.setColor(COLORS[(i + circle + 1) % COLORS.length]); // Set the color based on the color array, with an offset for each additional circle and an offset for the second count if the circle is keeping track of it
                if (currentChar.equals(String.valueOf(hour.charAt(0))) && !hourDrawn.get(0)) {
                    hourDrawn.set(0, true); // Track that you've already drawn it
                    timeDigit = true; // Mark that we will not draw this time digit on this round of drawing
                    charIndices[0] = charCount; // Keep track of where each digit occurs in the total hash
                    circleIndices[0] = circle; // Keep track of which circle the digit is in
                    indexInCircle[0] = i; //  Keep track of where it occurs in the current circle too
                    timeAngles[0] = currentAngle; // Keep track of which angle you're at
                } else if (hour.length() == 2 && !hourDrawn.get(1)) {
                    if (currentChar.equals(String.valueOf(hour.charAt(1)))) { // If the current character matches the second number in the hour
                        hourDrawn.set(1, true); // Track that you've already drawn it
                        timeDigit = true; // Mark that we will not draw this time digit on this round of drawing
                        charIndices[1] = charCount; // Keep track of where each digit occurs in the total hash
                        circleIndices[1] = circle; // Keep track of which circle the digit is in
                        indexInCircle[1] = i; //  Keep track of where it occurs in the current circle too
                        timeAngles[1] = currentAngle; // Keep track of which angle you're at
                    }
                } else if (currentChar.equals(String.valueOf(minute.charAt(0))) && !minuteDrawn.get(0)) {
                    minuteDrawn.set(0, true); // Track that you've already drawn it
                    timeDigit = true; // Mark that we will not draw this time digit on this round of drawing
                    charIndices[2] = charCount; // Keep track of where each digit occurs in the total hash
                    circleIndices[2] = circle; // Keep track of which circle the digit is in
                    indexInCircle[2] = i; //  Keep track of where it occurs in the current circle too
                    timeAngles[2] = currentAngle; // Keep track of which angle you're at
                } else if (currentChar.equals(String.valueOf(minute.charAt(1))) && !minuteDrawn.get(1)) {
                    minuteDrawn.set(1, true); // Track that you've already drawn it
                    timeDigit = true; // Mark that we will not draw this time digit on this round of drawing
                    charIndices[3] = charCount; // Keep track of where each digit occurs in the total hash
                    circleIndices[3] = circle; // Keep track of which circle the digit is in
                    indexInCircle[3] = i; //  Keep track of where it occurs in the current circle too
                    timeAngles[3] = currentAngle; // Keep track of which angle you're at
                }
                // Measure the current character in the string
                mPaintText.getTextBounds(String.valueOf(currentHash.charAt(charCount)), 0, String.valueOf(currentHash.charAt(charCount)).length(), bounds); // Measure the text
                if (!timeDigit) { // Only draw the character if it's not a digit for the time
                    canvas.drawText(currentChar, (float) (centerX + (radius * radiusSqueeze[circle] * circleSpacing[circle] * Math.cos(medianAngle))), (float) (centerY + (radius * radiusSqueeze[circle] * circleSpacing[circle] * Math.sin(medianAngle))) + bounds.height() * 0.5f, mPaintText);
                }
                charCount++; // Increment the character count
            }
        }

        // Now go through all the time circles and draw them on top, if there was one in the hash
        // Must be done this way so that they can overlay what was drawn previous and not be drawn over by an arc
        for (j = 0; j < 4; j++) {
            currentAngle = timeAngles[j];
            medianAngle = (currentAngle + (degreesArray.get(circleIndices[j])[indexInCircle[j]] / 2f)) * (float) Math.PI / 180f;
            currentChar = String.valueOf(currentHash.charAt(charIndices[j])); // Get the current character in the hash
            if (j == 0 && hourDrawn.get(0)) { // If you're on the current digit of the time, and you found it up above while looping through the hash
                // Draw a circle behind the current digit to make it stand out
                if(hour.length() == 2) {
                    mPaintText.setColor(hourOneColor); // Use the first color in the preference if there are two digits in the hour
                } else if (hour.length() == 1) {
                    mPaintText.setColor(hourTwoColor); // Make sure to use the 2nd hour color if there is only one digit in the hour
                }
                canvas.drawCircle((float) (centerX + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.cos(medianAngle))), (float) (centerY + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.sin(medianAngle))), mTextHeight * firstHourTextScale, mPaintText);
            } else if (j == 1 && hour.length() == 2 && hourDrawn.get(1)) { // If you're on the current digit of the time, and you found it up above while looping through the hash
                // Draw a circle behind the current digit to make it stand out
                mPaintText.setColor(hourTwoColor); // Change the color to red
                canvas.drawCircle((float) (centerX + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.cos(medianAngle))), (float) (centerY + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.sin(medianAngle))), mTextHeight * secondHourTextScale, mPaintText);
            } else if (j == 2 && minuteDrawn.get(0)) { // If you're on the current digit of the time, and you found it up above while looping through the hash
                // Draw a circle behind the current digit to make it stand out
                mPaintText.setColor(minuteOneColor); // Change the color
                canvas.drawCircle((float) (centerX + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.cos(medianAngle))), (float) (centerY + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.sin(medianAngle))), mTextHeight * firstMinuteTextScale, mPaintText);
            } else if (j == 3 && minuteDrawn.get(1)) { // If you're on the current digit of the time, and you found it up above while looping through the hash
                // Draw a circle behind the current digit to make it stand out
                mPaintText.setColor(minuteTwoColor); // Change the color
                canvas.drawCircle((float) (centerX + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.cos(medianAngle))), (float) (centerY + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.sin(medianAngle))), mTextHeight * secondMinuteTextScale, mPaintText);
            }
        }

        // Now go through all the time digits and draw them on top of the circles, if there was one in the hash
        // Must be done this way so that they can overlay what was drawn previous and not be drawn over by an arc or circle
        mPaintText.setColor(Color.WHITE); // Set the color to white
        for (j = 0; j < 4; j++) {
            currentAngle = timeAngles[j];
            medianAngle = (currentAngle + (degreesArray.get(circleIndices[j])[indexInCircle[j]] / 2f)) * (float) Math.PI / 180f;
            currentChar = String.valueOf(currentHash.charAt(charIndices[j])); // Get the current character in the hash
            if (j == 0 && hourDrawn.get(0)) { // If you're on the current digit of the time, and you found it up above while looping through the hash
                mPaintText.setTextSize(mTextHeight * firstHourTextScale); // Make the hour really big
                mPaintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Make the hours bold
                mPaintText.setShadowLayer(6, 6, 6, Color.BLACK); // Create a shadow for the digits
                if (hour.length() == 1) {
                    currentChar = currentChar + ":"; // Put a colon after the hour if it's only one number
                }
                // Measure the current character in the string
                mPaintText.getTextBounds(currentChar, 0, currentChar.length(), bounds); // Measure the text
                canvas.drawText(currentChar, (float) (centerX + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.cos(medianAngle))), (float) (centerY + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.sin(medianAngle))) + bounds.height() * 0.5f, mPaintText);
            } else if (j == 1 && hour.length() == 2 && hourDrawn.get(1)) { // If you're on the current digit of the time, and you found it up above while looping through the hash
                mPaintText.setTextSize(mTextHeight * secondHourTextScale); // Make the hour really big
                mPaintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Make the hours bold
                mPaintText.setShadowLayer(6, 6, 6, Color.BLACK); // Create a shadow for the digits
                // Measure the current character in the string
                mPaintText.getTextBounds(currentChar, 0, currentChar.length(), bounds); // Measure the text
                currentChar = currentChar + ":"; // Put a colon after the hour
                canvas.drawText(currentChar, (float) (centerX + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.cos(medianAngle))), (float) (centerY + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.sin(medianAngle))) + bounds.height() * 0.5f, mPaintText);
            } else if (j == 2 && minuteDrawn.get(0)) { // If you're on the current digit of the time, and you found it up above while looping through the hash
                mPaintText.setTextSize(mTextHeight * firstMinuteTextScale); // Make the minute a bit bigger
                mPaintText.setShadowLayer(3, 2, 2, Color.BLACK); // Create a shadow for the digits
                mPaintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)); // Make the minutes italic
                // Measure the current character in the string
                mPaintText.getTextBounds(currentChar, 0, currentChar.length(), bounds); // Measure the text
                canvas.drawText(currentChar, (float) (centerX + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.cos(medianAngle))), (float) (centerY + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.sin(medianAngle))) + bounds.height() * 0.5f, mPaintText);
            } else if (j == 3 && minuteDrawn.get(1)) { // If you're on the current digit of the time, and you found it up above while looping through the hash
                mPaintText.setTextSize(mTextHeight * secondMinuteTextScale); // Make the minute a bit bigger
                mPaintText.setShadowLayer(3, 2, 2, Color.BLACK); // Create a shadow for the digits
                // Measure the current character in the string
                mPaintText.getTextBounds(currentChar, 0, currentChar.length(), bounds); // Measure the text
                canvas.drawText(currentChar, (float) (centerX + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.cos(medianAngle))), (float) (centerY + (radius * radiusSqueeze[circleIndices[j]] * circleSpacing[circleIndices[j]] * Math.sin(medianAngle))) + bounds.height() * 0.5f, mPaintText);
            }
        }

        // In case something wasn't drawn
        if (!hourDrawn.get(0)) {
            currentAngle = 225;
            medianAngle = (currentAngle) * (float) Math.PI / 180f;
            currentChar = String.valueOf(hour.charAt(0)); // Get the current character in the hash
            mPaintText.setTextSize(mTextHeight * firstHourTextScale); // Make the hour really big
            if (hour.length() == 1) {
                currentChar = currentChar + ":"; // Put a colon after the hour if it's only one number
            }
            // Measure the current character in the string
            mPaintText.getTextBounds(currentChar, 0, currentChar.length(), bounds); // Measure the text
            mPaintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Make the hours bold
            // Draw a circle behind the current digit to make it stand out
            if(hour.length() == 2) {
                mPaintText.setColor(hourOneColor); // Use the first color in the preference if there are two digits in the hour
            } else if (hour.length() == 1) {
                mPaintText.setColor(hourTwoColor); // Make sure to use the 2nd hour color if there is only one digit in the hour
            }
            canvas.drawCircle((float) (centerX + (radius * Math.cos(medianAngle))), (float) (centerY + (radius * Math.sin(medianAngle))), mTextHeight * firstHourTextScale, mPaintText);
            mPaintText.setColor(Color.WHITE);
            canvas.drawText(currentChar, (float) (centerX + (radius * Math.cos(medianAngle))), (float) (centerY + (radius * Math.sin(medianAngle))) + bounds.height() * 0.5f, mPaintText);
        }
        // In case something wasn't drawn
        if (!hourDrawn.get(1) && hour.length() == 2) {
            currentAngle = 315;
            medianAngle = (currentAngle) * (float) Math.PI / 180f;
            currentChar = String.valueOf(hour.charAt(1)); // Get the current character in the hash
            mPaintText.setTextSize(mTextHeight * secondHourTextScale); // Make the hour really big
            currentChar = currentChar + ":"; // Put a colon after the hour if it's only one number
            // Measure the current character in the string
            mPaintText.getTextBounds(currentChar, 0, currentChar.length(), bounds); // Measure the text
            mPaintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Make the hours bold
            // Draw a circle behind the current digit to make it stand out
            mPaintText.setColor(hourTwoColor);
            canvas.drawCircle((float) (centerX + (radius * Math.cos(medianAngle))), (float) (centerY + (radius * Math.sin(medianAngle))), mTextHeight * secondHourTextScale, mPaintText);
            mPaintText.setColor(Color.WHITE);
            canvas.drawText(currentChar, (float) (centerX + (radius * Math.cos(medianAngle))), (float) (centerY + (radius * Math.sin(medianAngle))) + bounds.height() * 0.5f, mPaintText);
        }
        // In case something wasn't drawn
        if (!minuteDrawn.get(0)) {
            currentAngle = 135;
            medianAngle = (currentAngle) * (float) Math.PI / 180f;
            currentChar = String.valueOf(minute.charAt(0)); // Get the current character in the hash
            mPaintText.setTextSize(mTextHeight * firstMinuteTextScale); // Make the hour really big
            // Measure the current character in the string
            mPaintText.getTextBounds(currentChar, 0, currentChar.length(), bounds); // Measure the text
            // Draw a circle behind the current digit to make it stand out
            mPaintText.setColor(minuteOneColor);
            mPaintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)); // Make the minutes italic
            canvas.drawCircle((float) (centerX + (radius * Math.cos(medianAngle))), (float) (centerY + (radius * Math.sin(medianAngle))), mTextHeight * firstMinuteTextScale, mPaintText);
            mPaintText.setColor(Color.WHITE);
            canvas.drawText(currentChar, (float) (centerX + (radius * Math.cos(medianAngle))), (float) (centerY + (radius * Math.sin(medianAngle))) + bounds.height() * 0.5f, mPaintText);
        }
        // In case something wasn't drawn
        if (!minuteDrawn.get(1)) {
            currentAngle = 45;
            medianAngle = (currentAngle) * (float) Math.PI / 180f;
            currentChar = String.valueOf(minute.charAt(1)); // Get the current character in the hash
            mPaintText.setTextSize(mTextHeight * secondMinuteTextScale); // Make the hour really big
            // Measure the current character in the string
            mPaintText.getTextBounds(currentChar, 0, currentChar.length(), bounds); // Measure the text
            // Draw a circle behind the current digit to make it stand out
            mPaintText.setColor(minuteTwoColor);
            mPaintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)); // Make the minutes italic
            canvas.drawCircle((float) (centerX + (radius * Math.cos(medianAngle))), (float) (centerY + (radius * Math.sin(medianAngle))), mTextHeight * secondMinuteTextScale, mPaintText);
            mPaintText.setColor(Color.WHITE);
            canvas.drawText(currentChar, (float) (centerX + (radius * Math.cos(medianAngle))), (float) (centerY + (radius * Math.sin(medianAngle))) + bounds.height() * 0.5f, mPaintText);
        }
        if(showTimeBelowWheel) {
            String fullTime = am_pm == Calendar.AM ? hour + ":" + minute + " AM" : hour + ":" + minute + " PM"; // Put the full time here
            mPaintText.setColor(Color.BLACK);
            mPaintText.setTextSize(mTextHeight);
            mPaintText.setShadowLayer(0, 0, 0, Color.BLACK); // Nullify the shadow layer
            canvas.drawText(fullTime, centerX, centerY + radius + mTextHeight * 0.7f, mPaintText); // Draw the full time to make it easier
        }
    }
}