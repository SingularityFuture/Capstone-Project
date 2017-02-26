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

    /* Transaction information */
    private static final String VER = "ver";
    private static final String LOCK_TIME= "lock_time";
    private static final String RELAYED_BY = "relayed_by";

    //private static final String MESSAGE_CODE = "cod";

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the transaction.
     */
    
    public static ContentValues getTransactionValuesFromJson(Context context, String transactionJsonStr)
            throws JSONException {

        JSONObject transactionJson = new JSONObject(transactionJsonStr);

        /* Is there an error? */
/*        if (transactionJson.has(MESSAGE_CODE)) {
            int errorCode = transactionJson.getInt(MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    *//* Location invalid *//*
                    return null;
                default:
                    *//* Server probably down *//*
                    return null;
            }
        }*/
        int version = transactionJson.getInt(VER);
        double lockTime = transactionJson.getDouble(LOCK_TIME);
        double relayedBy= transactionJson.getDouble(RELAYED_BY);

        ContentValues transactionContentValues = new ContentValues();
        transactionContentValues.put(BlockContract.BlockEntry.COLUMN_VER, version);
        transactionContentValues.put(BlockContract.BlockEntry.COLUMN_LOCK_TIME, lockTime);
        transactionContentValues.put(BlockContract.BlockEntry.COLUMN_RELAYED_BY, relayedBy);

        return transactionContentValues;
    }
}