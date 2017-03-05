package utilities;

/**
 * Created by Michael on 2/26/2017.
 */

import android.content.ContentValues;
import android.content.Context;

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
    private static final String LOCK_TIME= "lock_time";
    private static final String RELAYED_BY = "relayed_by";
    //private static final String BLOCK_HEIGHT = "block_height";

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the transaction.
     */
    
    public static ContentValues getTransactionValuesFromJson(Context context, String transactionJsonStr)
            throws JSONException {

        JSONObject transactionJson = new JSONObject(transactionJsonStr);

        String hash = transactionJson.getString(HASH);  // Get each element from the JSON object
        int version = transactionJson.getInt(VER);
        int lockTime = transactionJson.getInt(LOCK_TIME);
        String relayedBy = transactionJson.getString(RELAYED_BY);
        //int blockHeight = transactionJson.getInt(BLOCK_HEIGHT);  // Doesn't seem to be in the current API

        ContentValues transactionContentValues = new ContentValues(); // Put each element in a set of Content Values
        transactionContentValues.put(BlockContract.BlockEntry.COLUMN_HASH, hash);
        transactionContentValues.put(BlockContract.BlockEntry.COLUMN_VER, version);
        transactionContentValues.put(BlockContract.BlockEntry.COLUMN_LOCK_TIME, lockTime);
        transactionContentValues.put(BlockContract.BlockEntry.COLUMN_RELAYED_BY, relayedBy);
        //transactionContentValues.put(BlockContract.BlockEntry.COLUMN_BLOCK_HEIGHT, blockHeight);  // Not inside unconfirmed transactions


        return transactionContentValues; // Return the Content Values so you can insert them into the database using the database helper
    }
}