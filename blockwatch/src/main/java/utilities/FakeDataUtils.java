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

    private static int [] weatherIDs = {200,300,500,711,900,962};

    /**
     * Creates a single ContentValues object with random weather data for the provided date
     * @param date a normalized date
     * @return ContentValues object filled with random weather data
     */
    private static ContentValues createTestWeatherContentValues(long date) {
        ContentValues testWeatherValues = new ContentValues();
        testWeatherValues.put(BlockContract.BlockEntry.COLUMN_DATE, date);
        testWeatherValues.put(BlockContract.BlockEntry.COLUMN_DEGREES, Math.random()*2);
        testWeatherValues.put(BlockContract.BlockEntry.COLUMN_HUMIDITY, Math.random()*100);
        testWeatherValues.put(BlockContract.BlockEntry.COLUMN_PRESSURE, 870 + Math.random()*100);
        int maxTemp = (int)(Math.random()*100);
        testWeatherValues.put(BlockContract.BlockEntry.COLUMN_MAX_TEMP, maxTemp);
        testWeatherValues.put(BlockContract.BlockEntry.COLUMN_MIN_TEMP, maxTemp - (int) (Math.random()*10));
        testWeatherValues.put(BlockContract.BlockEntry.COLUMN_WIND_SPEED, Math.random()*10);
        testWeatherValues.put(BlockContract.BlockEntry.COLUMN_WEATHER_ID, weatherIDs[(int)(Math.random()*10)%5]);
        return testWeatherValues;
    }

    /**
     * Creates random weather data for 7 days starting today
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
        // Bulk Insert our new weather data into Sunshine's Database
        context.getContentResolver().bulkInsert(
                BlockContract.BlockEntry.CONTENT_URI,
                fakeValues.toArray(new ContentValues[7]));
    }
}
