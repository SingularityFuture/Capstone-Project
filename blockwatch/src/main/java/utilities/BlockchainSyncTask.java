package utilities;

/**
 * Created by Michael on 2/26/2017.
 */

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import java.net.URL;

import data.BlockContract;

public class BlockchainSyncTask {

    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE= "longitude";
    private static final String RELAYED_BY = "relayed_by";

    /**
     * Performs the network request for updated transaction, parses the JSON from that request, and
     * inserts the new transaction information into our ContentProvider. Will notify the user that new
     * transaction has been loaded if the user hasn't been notified of the transaction within the last day
     * AND they haven't disabled notifications in the preferences screen.
     *
     * @param context Used to access utility methods and the ContentResolver
     */

    synchronized static public void syncTransaction(Context context, String currentHash) {
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
                ContentResolver transactionContentResolver = context.getContentResolver();

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
    }
}