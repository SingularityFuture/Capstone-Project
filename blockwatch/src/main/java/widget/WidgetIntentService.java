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
import android.widget.RemoteViews;

import com.example.blockwatch.MainActivity;
import com.example.blockwatch.R;

import data.BlockContract;

/**
 * IntentService which handles updating the widget with the latest data
 */
public class WidgetIntentService extends IntentService {
    private static final String[] TRANSACTION_COLUMNS = {
            BlockContract.BlockEntry.COLUMN_HASH,
            BlockContract.BlockEntry.COLUMN_RELAYED_BY
    };
    // These indices must match the projection
    private static final int INDEX_TRANSACTION_HASH = 0;
    private static final int INDEX_RELAYED_BY = 1;

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
        String transactionHash = data.getString(INDEX_TRANSACTION_HASH);
        String relayedBy = data.getString(INDEX_RELAYED_BY);
        data.close();
        String description = getApplicationContext().getString(R.string.Transaction);

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_small;
            int iconResource = R.drawable.blockwatchicon;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setImageViewResource(R.id.widget_icon, iconResource);
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }
            views.setTextViewText(R.id.transactionHash, transactionHash);
            views.setTextViewText(R.id.transactionRelayedBy, relayedBy);

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
        views.setContentDescription(R.id.widget_icon, description);
    }
}