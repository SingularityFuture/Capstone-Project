package utilities;

/**
 * Created by Michael on 2/26/2017.
 */

import android.content.ContentValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import data.BlockContract;

/**
 * Utility functions to handle Blockchain.info JSON data.
 */
public final class TransactionJsonUtils {

    /* JSON Transaction labels */
    private static final String HASH = "hash";
    private static final String VER = "ver";
    private static final String LOCK_TIME = "lock_time";
    private static final String RELAYED_BY = "relayed_by";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String PRICES = "values";

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the transaction.
     */

    public static ContentValues getTransactionValuesFromJson(String transactionJsonStr)
            throws JSONException {

        JSONObject transactionJson = new JSONObject(transactionJsonStr);
        String hash = "";  // Make sure you initialize the values in case one is null
        int version = 0;
        int lockTime = 0;
        String relayedBy = "";

        if (!transactionJson.isNull(HASH)) {
            hash = transactionJson.getString(HASH);  // Get each element from the JSON object
        }
        if (!transactionJson.isNull(VER)) {
            version = transactionJson.getInt(VER);
        }
        if (!transactionJson.isNull(LOCK_TIME)) {
            lockTime = transactionJson.getInt(LOCK_TIME);
        }
        if (!transactionJson.isNull(RELAYED_BY)) {
            relayedBy = transactionJson.getString(RELAYED_BY);
        }
        ContentValues transactionContentValues = new ContentValues(); // Put each element in a set of Content Values
        transactionContentValues.put(BlockContract.BlockEntry.COLUMN_HASH, hash);
        transactionContentValues.put(BlockContract.BlockEntry.COLUMN_VER, version);
        transactionContentValues.put(BlockContract.BlockEntry.COLUMN_LOCK_TIME, lockTime);
        transactionContentValues.put(BlockContract.BlockEntry.COLUMN_RELAYED_BY, relayedBy);

        return transactionContentValues; // Return the Content Values so you can insert them into the database using the database helper
    }

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the latitude and longitude of the 'Relayed By' field of the current transaction.
     */
    public static ContentValues getLatLongValuesFromJson(String latLongJsonStr)
            throws JSONException {

        JSONObject latLongJson = new JSONObject(latLongJsonStr);
        double latitude = 0;
        double longitude = 0;

        if (!latLongJson.isNull(LATITUDE)) {
            latitude = Double.valueOf(latLongJson.getString(LATITUDE));  // Get each element from the JSON object
        }
        if (!latLongJson.isNull(LONGITUDE)) {
            longitude = Double.valueOf(latLongJson.getString(LONGITUDE));  // Get each element from the JSON object
        }

        ContentValues latLongValues = new ContentValues(); // Put each element in a set of Content Values
        latLongValues.put(BlockContract.BlockEntry.COLUMN_LATITUDE, latitude);
        latLongValues.put(BlockContract.BlockEntry.COLUMN_LONGITUDE, longitude);

        return latLongValues; // Return the Content Values so you can insert them into the database using the database helper
    }

    /**
     * This method parses JSON from a web response and returns an 2-dimensional array of integers
     * representing the timestamp and price in USD of Bitcoin starting from yesterday
     */
    public static double[][] getHistoricalPricesFromJson(String historicalPricesJsonStr)
        throws JSONException {
        JSONObject historicalPricesJson = new JSONObject(historicalPricesJsonStr);
        JSONArray priceJSONArray;
        double[][] price_array = new double[365][365];

        if (!historicalPricesJson.isNull(PRICES)) {
            priceJSONArray = historicalPricesJson.getJSONArray(PRICES);  // Get the value element from the JSON object
            for(int i=0; i<priceJSONArray.length(); i++){
                price_array[i][0] = priceJSONArray.getJSONObject(i).getInt("x"); // Get each Unix timestamp
                price_array[i][1] = priceJSONArray.getJSONObject(i).getInt("y"); // Get each price
            }
        }
        return price_array;
    }
}