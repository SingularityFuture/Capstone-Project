package utilities;

/**
 * Created by test on 2/26/2017.
 */

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
        import android.content.ContentResolver;
        import android.content.ContentValues;
        import android.content.Context;

        import java.net.URL;

        import data.BlockContract;

public class BlockchainSyncTask {

    /**
     * Performs the network request for updated transaction, parses the JSON from that request, and
     * inserts the new transaction information into our ContentProvider. Will notify the user that new
     * transaction has been loaded if the user hasn't been notified of the transaction within the last day
     * AND they haven't disabled notifications in the preferences screen.
     *
     * @param context Used to access utility methods and the ContentResolver
     */

    private static final String MAX_TEMP = "com.example.android.Blockwatch.key.max_temp";
    private static final String MIN_TEMP = "com.example.android.Blockwatch.key.min_temp";
    private static final String CURRENT_TIME= "com.example.android.Blockwatch.key.time";
    private static final String TAG = "Sync Task";
    private static final String transaction_ID = "com.example.android.Blockwatch.key.transaction_id";
    private static boolean mResolvingError = false;
    private static Context mContext;

    synchronized static public void syncTransaction(Context context) {
        mContext=context;

        try {
            /*
             * The getUrl method will return the URL that we need to get the forecast JSON for the
             * transaction. It will decide whether to create a URL based off of the latitude and
             * longitude or off of a simple location as a String.
             */
            URL transactionRequestUrl = NetworkUtils.getUrl(context);

            /* Use the URL to retrieve the JSON */
            String jsonTransactionResponse = NetworkUtils.getResponseFromHttpUrl(transactionRequestUrl);

            /* Parse the JSON into a list of transaction values */
            ContentValues[] transactionValues = TransactionJsonUtils
                    .getTransactionValuesFromJson(context, jsonTransactionResponse);

            double hash = transactionValues[0].getAsInteger(BlockContract.BlockEntry.COLUMN_HASH);
            double blockHeight = transactionValues[0].getAsInteger(BlockContract.BlockEntry.COLUMN_BLOCK_HEIGHT);
            int lockTime = transactionValues[0].getAsInteger(BlockContract.BlockEntry.COLUMN_LOCK_TIME);

            /*
             * In cases where our JSON contained an error code, gettransactionContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if (transactionValues != null && transactionValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver transactionContentResolver = context.getContentResolver();

                /* Delete old transaction data because we don't need to keep multiple days' data */
                transactionContentResolver.delete(
                        BlockContract.BlockEntry.CONTENT_URI,
                        null,
                        null);

                /* Insert our new transaction data into Blockwatch's ContentProvider */
                transactionContentResolver.bulkInsert(
                        BlockContract.BlockEntry.CONTENT_URI,
                        transactionValues);
            }
        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
    }
}