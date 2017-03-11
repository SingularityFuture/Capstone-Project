package sync;

/**
 * Created by Michael on 3/11/2017.
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import com.example.blockwatch.BuildConfig;
import com.example.blockwatch.MainActivity;
import com.example.blockwatch.R;
import data.BlockContract;
import data.BlockExplorerClass;
import utilities.NetworkUtils;
import utilities.TransactionJsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class BlockwatchSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = BlockwatchSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the blockchain transaction, in seconds.
    // 5 seconds
    public static final int SYNC_INTERVAL = 5;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE= "longitude";
    private static final String RELAYED_BY = "relayed_by";
    private String currentHash;

    public BlockwatchSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        try {
            currentHash =  BlockExplorerClass.retrieveUnconfirmedTransactions(); // Update the watch at the beginning with a fresh hash
        }
        catch (Exception e){
            Log.d("Explorer error: ", e.getMessage());
        }

        try {
             /*
             * The getUrl method will return the URL that we need to get the JSON for the
             * transaction based on its hash.
             */
            URL transactionRequestUrl = NetworkUtils.getUrl(currentHash);

            /* Use the URL to retrieve the JSON */
            String jsonTransactionResponse = NetworkUtils.getResponseFromHttpUrl(transactionRequestUrl);

            Log.d("Transaction Response: ", jsonTransactionResponse);
            /* Parse the JSON into a list of transaction values */
            ContentValues transactionValues = TransactionJsonUtils
                    .getTransactionValuesFromJson(jsonTransactionResponse);

            /*
             * The getUrl method will return the URL that we need to get the location JSON for the
             * transaction, based on its Relayed By IP address
            */
            URL latLongRequestUrl = NetworkUtils.getUrl(transactionValues.getAsString(RELAYED_BY));

            /* Use the URL to retrieve the JSON */
            String jsonLatLongResponse = NetworkUtils.getResponseFromHttpUrl(latLongRequestUrl);

            Log.d("Lat/Long Response: ", jsonLatLongResponse);
            ContentValues latLongValues = TransactionJsonUtils.getLatLongValuesFromJson(jsonLatLongResponse);
            /* Put the lat/long values into the Content Values for insertino in the database */
            transactionValues.put(BlockContract.BlockEntry.COLUMN_LATITUDE, latLongValues.getAsString(LATITUDE));
            transactionValues.put(BlockContract.BlockEntry.COLUMN_LONGITUDE, latLongValues.getAsString(LONGITUDE));

            /*
             * In cases where our JSON contained an error code, gettransactionContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if (transactionValues != null) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver transactionContentResolver = getContext().getContentResolver();

                /* Delete old transaction data because we don't need to keep multiple data */
                transactionContentResolver.delete(
                        BlockContract.BlockEntry.CONTENT_URI,
                        null,
                        null);

                /* Insert our new transaction data into Blockwatch's ContentProvider */
                transactionContentResolver.insert(
                        BlockContract.BlockEntry.CONTENT_URI,
                        transactionValues);
            }
        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
        return;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        BlockwatchSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}

