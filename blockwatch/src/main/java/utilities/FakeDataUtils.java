package utilities;

/**
 * Created by Michael on 2/20/2017.
 */


import android.content.ContentValues;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import data.BlockContract;

public class FakeDataUtils {
    

    /**
     * Creates a single ContentValues object with random block data for the provided date
     * @param date a normalized date
     * @return ContentValues object filled with random block data
     */
    private static ContentValues createTestWeatherContentValues(long date) {
        ContentValues testWeatherValues = new ContentValues();
        testWeatherValues.put(BlockContract.BlockEntry.COLUMN_DATE, date);
        return testWeatherValues;
    }

    /**
     * Creates random block data for 7 days starting today
     * @param context
     */
    public static void insertFakeData(Context context) {
        //Get today's normalized date
        //long today = SunshineDateUtils.normalizeDate(System.currentTimeMillis());
        long today=1234;
        List<ContentValues> fakeValues = new ArrayList<ContentValues>();
        //loop over 7 days starting today onwards
        for(int i=0; i<7; i++) {
            fakeValues.add(FakeDataUtils.createTestWeatherContentValues(today + TimeUnit.DAYS.toMillis(i)));
        }
        // Bulk Insert our new block data into Sunshine's Database
        context.getContentResolver().bulkInsert(
                BlockContract.BlockEntry.CONTENT_URI,
                fakeValues.toArray(new ContentValues[7]));
    }
}
