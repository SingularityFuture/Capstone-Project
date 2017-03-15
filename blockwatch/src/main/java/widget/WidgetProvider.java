package widget;

/**
 * Created by Michael on 3/15/2017.
 */

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.example.blockwatch.MainActivity;
import com.example.blockwatch.R;

/**
 * Provider for a widget showing current transaction information.
 */
public class WidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Perform this loop procedure for each widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_small;
            int iconResource = R.drawable.blockwatchicon;
            RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
            String dummyHash = "b5357533bf43d6793aa24d91d6a01055128bff64730627bbb3a512b04d2e9043";
            String dummyIP = "88.88.88.88";
            String description = context.getString(R.string.Transaction);

            // Add the data to the RemoteViews
            views.setImageViewResource(R.id.widget_icon, iconResource);
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }
            views.setTextViewText(R.id.transactionHash, dummyHash);
            views.setTextViewText(R.id.transactionRelayedBy, dummyIP);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
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
