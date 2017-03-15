package sync;

/**
 * Created by Michael on 3/11/2017.
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.blockwatch.R;

import java.net.URL;

import data.BlockContract;
import data.BlockExplorerClass;
import utilities.NetworkUtils;
import utilities.TransactionJsonUtils;

public class BlockwatchSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = BlockwatchSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the blockchain transaction, in seconds.
    // 5 seconds
    public static final long SYNC_INTERVAL = 5/12;
    public static final long SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE= "longitude";
    private static final String RELAYED_BY = "relayed_by";
    private String currentHash;

    public static final String ACTION_DATA_UPDATED =
            "com.example.blockwatch.ACTION_DATA_UPDATED";

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
            /* Put the lat/long values into the Content Values for insertion in the database */
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

                // Call the service to update the widgets
                updateWidgets();
            }
        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
        return;
    }

    // Update the widget information
    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, long syncInterval, long flexTime) {
        Account account = getSyncAccount(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, BlockContract.CONTENT_AUTHORITY).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    BlockContract.CONTENT_AUTHORITY, new Bundle(), syncInterval);
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
        ContentResolver.setSyncAutomatically(newAccount, BlockContract.CONTENT_AUTHORITY, true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    // This is called in the onCreate method of Main Activity to start off the sync
    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}

