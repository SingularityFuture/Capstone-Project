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

import com.google.android.gms.common.api.GoogleApiClient;
import com.singularityfuture.blockwatch.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


import org.json.JSONException;

import java.net.URL;

import data.BlockContract;
import data.BlockExplorerClass;
import utilities.NetworkUtils;
import utilities.TransactionJsonUtils;

import static data.BlockExplorerClass.retrieveCurrentPrice;

public class BlockwatchSyncAdapter extends AbstractThreadedSyncAdapter implements DataApi.DataListener {
    // Interval at which to sync with the blockchain transaction, in seconds
    public static final long SYNC_INTERVAL = 5;
    public static final long SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final String ACTION_DATA_UPDATED =
            "com.example.com.ACTION_DATA_UPDATED";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String RELAYED_BY = "relayed_by";
    public final String LOG_TAG = BlockwatchSyncAdapter.class.getSimpleName();
    private String currentHash;
    private double currentPrice = 1.0;
    private static final String HISTORICAL_PRICES = "market-price";

    private static final String TAG = "BlockWatch Face Canvas";
    private static final String BITCOIN_PRICE = "com.singularityfuture.blockwatchface.key.bitcoinprice";
    private static boolean mResolvingError = false;
    private static GoogleApiClient mGoogleApiClient;
    private static Context mContext;
    double[][] price_array = new double[365][365]; // Declare the array of historical prices

    public BlockwatchSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
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
     *
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
        if (accountManager.getPassword(newAccount) == null) {

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
        BlockwatchSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME); // Since we've created an account
        ContentResolver.setSyncAutomatically(newAccount, BlockContract.CONTENT_AUTHORITY, true); // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        syncImmediately(context); // Finally, let's do a sync to get things started
    }

    // This is called in the onCreate method of Main Activity to start off the sync
    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        try {
            currentHash = BlockExplorerClass.retrieveUnconfirmedTransactions(); // Update the watch at the beginning with a fresh hash
        } catch (Exception e) {
            Log.d("Explorer error: ", e.getMessage());
        }
        // Get the current price in USD as well
        try {
            currentPrice = retrieveCurrentPrice().doubleValue();
        } catch (Exception e) {
            Log.d("Explorer error: ", e.getMessage());
        }

        try {
             /*
             * Transaction information
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
             * Latitude/Longitude
             * The getUrl method will return the URL that we need to get the location JSON based on its Relayed By IP address
            */
            URL latLongRequestUrl = NetworkUtils.getUrl(transactionValues.getAsString(RELAYED_BY));
            /* Use the URL to retrieve the JSON */
            String jsonLatLongResponse = NetworkUtils.getResponseFromHttpUrl(latLongRequestUrl);
            Log.d("Lat/Long Response: ", jsonLatLongResponse);
            ContentValues latLongValues = TransactionJsonUtils.getLatLongValuesFromJson(jsonLatLongResponse);

            /*
             * Historical Prices
             * The getUrl method will return the URL that we need to get the location JSON based on its Relayed By IP address
            */
            URL historicalPricesRequestUrl = NetworkUtils.getUrl(HISTORICAL_PRICES);
            /* Use the URL to retrieve the JSON */
            String jsonHistoricalPricesResponse = NetworkUtils.getResponseFromHttpUrl(historicalPricesRequestUrl);
            Log.d("Historical Response: ", jsonHistoricalPricesResponse); // Log what you got
            // Since we are storing the JSON response here as a string, we don't need to convert it
            // Later, when we query for actual prices, we will use TransactionJsonUtilsl.getHistoricalPricesFromJson

            /*
             * In cases where our JSON contained an error code, getTransactionValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if (transactionValues != null) {
                if (latLongValues != null){
                    /* Put the lat/long values into the Content Values for insertion in the database */
                    transactionValues.put(BlockContract.BlockEntry.COLUMN_LATITUDE, latLongValues.getAsString(LATITUDE));
                    transactionValues.put(BlockContract.BlockEntry.COLUMN_LONGITUDE, latLongValues.getAsString(LONGITUDE));
                }
                if (currentPrice != 0) { // Put the current price in USD into the database, will be 0 if there was no result
                    transactionValues.put(BlockContract.BlockEntry.COLUMN_PRICE, currentPrice);
                }
                if (jsonHistoricalPricesResponse != null){
                    // If you got the JSON response, put the whole response as a string in this column
                    // Later, in BlockwatchFragment and elsewhere, we use TransactionJsonUtilsl.getHistoricalPricesFromJson to parse the JSON
                    // and get prices on certain dates
                    // This is easier than creating another table or a column for each price
                    transactionValues.put(BlockContract.BlockEntry.COLUMN_PRICE_HISTORY, jsonHistoricalPricesResponse);
                }
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

                final BlockwatchSyncAdapter sync_instance = new BlockwatchSyncAdapter(mContext,true);
                /* If the code reaches this point, we have successfully performed our sync */
                mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                        .addApi(Wearable.API)
                        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected (Bundle connectionHint){
                                Log.d(TAG, "onConnected: " + connectionHint);
                                mResolvingError = false;
                                // Now you can use the Data Layer API
                                Wearable.DataApi.addListener(mGoogleApiClient,sync_instance);
                            }
                            @Override
                            public void onConnectionSuspended ( int cause){
                                Log.d(TAG, "onConnectionSuspended: " + cause);
                            }
                        })
                        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener(){
                            @Override
                            public void onConnectionFailed(ConnectionResult connectionResult){
                                Log.d(TAG, "onConnectionFailed");
                                if (!mResolvingError) {
                                    if (connectionResult.hasResolution()) {
            /*                try {
                                mResolvingError = true;
                                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                            } catch (IntentSender.SendIntentException e) {
                                // There was an error with the resolution intent. Try again.
                                mGoogleApiClient.connect();
                            }*/
                                    } else {
                                        Log.e(TAG, "Connection to Google API client has failed");
                                        mResolvingError = false;
                                        //SunshineSyncTask sync_instance = new SunshineSyncTask();
                                        Wearable.DataApi.removeListener(mGoogleApiClient,sync_instance);
                                        //sync_instance.removeListener(mGoogleApiClient);
                                    }
                                }

                            }
                        })
                        .build();
                if (!mResolvingError) {
                    mGoogleApiClient.connect();
                }

                PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/price_info");
                putDataMapReq.getDataMap().putDouble(BITCOIN_PRICE, currentPrice);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                putDataReq.setUrgent();
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq)
                        .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(DataApi.DataItemResult dataItemResult) {
                                Log.d(TAG, "Sending data was successful: " + dataItemResult.getStatus()
                                        .isSuccess());
                            }
                        });

            }

        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
        return;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "DataItem changed: " + event.getDataItem().getUri());
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/blockwatchface_installed") == 0) {
                    syncImmediately(mContext);
                }
            }
        }
    }
    // Update the widget information
    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}

