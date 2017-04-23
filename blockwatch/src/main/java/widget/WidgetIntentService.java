package widget;

/**
 * Created by Michael on 3/15/2017.
 */

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import com.singularityfuture.blockwatch.MainActivity;
import com.singularityfuture.blockwatch.R;

import org.json.JSONException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import data.BlockContract;
import utilities.TransactionJsonUtils;

/**
 * IntentService which handles updating the widget with the latest data
 */
public class WidgetIntentService extends IntentService {
    private static final String[] TRANSACTION_COLUMNS = {
            BlockContract.BlockEntry.COLUMN_PRICE,
            BlockContract.BlockEntry.COLUMN_PRICE_HISTORY
    };
    // These indices must match the projection
    private static final int INDEX_COLUMN_PRICE = 0;
    private static final int INDEX_COLUMN_PRICE_HISTORY = 1;
    String formattedCurrentPrice = "";
    String priceHistoryJSON = "";
    double[][] price_array = new double[365][365]; // Declare the array of historical prices
    String formattedPercentageChange = "";
    double firstNonZero; // Holder for yesterday's price

    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                WidgetProvider.class));

        // Get data from the ContentProvider
        Cursor data = getContentResolver().query(BlockContract.BlockEntry.CONTENT_URI, TRANSACTION_COLUMNS, null,
                null, null);
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the weather data from the Cursor
        if (!data.isNull(0)) {
            NumberFormat formatter = new DecimalFormat("#0.00");
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);
            formattedCurrentPrice = "$" + formatter.format(data.getDouble(INDEX_COLUMN_PRICE)); // Get the current price
        }
        if (!data.isNull(1)) {
            priceHistoryJSON = data.getString(INDEX_COLUMN_PRICE_HISTORY); // Get the price history
            try { // Try parsing the JSON to get the price array
                price_array = TransactionJsonUtils.getHistoricalPricesFromJson(priceHistoryJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            NumberFormat percentFormat = NumberFormat.getPercentInstance();
            percentFormat.setMinimumFractionDigits(2);
            percentFormat.setMaximumFractionDigits(2);
            int i;
            firstNonZero = data.getDouble(INDEX_COLUMN_PRICE); // Initialize yesterday's price to today's in case you have trouble finding it below for some reason
            for (i=price_array.length-1; i>0; i--) {
                // Turn your data into Entry objects
                if (price_array[i][1] != 0) {
                    firstNonZero=price_array[i][1];
                    break;
                }
            }
            formattedPercentageChange = "("+percentFormat.format((data.getDouble(INDEX_COLUMN_PRICE) - firstNonZero)/firstNonZero)+")"; // Get the price percentage change from yesterday
        }
        String description = getApplicationContext().getString(R.string.Transaction);

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_small;
            int iconResource = R.drawable.blockwatchicon;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            int img;
            views.setTextViewText(R.id.widget_current_price, formattedCurrentPrice);
            views.setTextViewText(R.id.widget_percentage_change, formattedPercentageChange);
            if (data.getDouble(INDEX_COLUMN_PRICE) < firstNonZero) { // If today's price is currently less than yesterday's closing price
                // Add the data to the RemoteViews
                views.setTextColor(R.id.widget_current_price, ContextCompat.getColor(getApplicationContext(), R.color.md_red_500)); // Color the price red
                views.setTextColor(R.id.widget_percentage_change, ContextCompat.getColor(getApplicationContext(), R.color.md_red_500)); // Color the price red
                img = R.mipmap.trending_down;
            } else {
                // Add the data to the RemoteViews
                views.setTextColor(R.id.widget_current_price, ContextCompat.getColor(getApplicationContext(), R.color.md_light_green_500)); // Color the price red
                views.setTextColor(R.id.widget_percentage_change, ContextCompat.getColor(getApplicationContext(), R.color.md_light_green_500)); // Color the price red
                img = R.mipmap.trending_up;
            }
            views.setImageViewResource(R.id.widget_trend, img);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }
            data.close();

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget, description);
    }
}