package utilities;

/**
 * Created by Michael on 2/26/2017.
 */

import android.content.ContentValues;
import android.content.Context;

import data.BlockContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Utility functions to handle Blockchain.info JSON data.
 */
public final class TransactionJsonUtils {

    /* Location information */
    private static final String OWM_CITY = "city";

    private static final String OWM_WEATHER_ID = "id";

    private static final String OWM_MESSAGE_CODE = "cod";

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the transaction.
     */
    
    public static ContentValues[] getTransactionValuesFromJson(Context context, String forecastJsonStr)
            throws JSONException {

        JSONObject transactionJson = new JSONObject(forecastJsonStr);

        /* Is there an error? */
        if (transactionJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = transactionJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray jsonBlockArray = transactionJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = transactionJson.getJSONObject(OWM_CITY);

        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
        double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

        ContentValues[] weatherContentValues = new ContentValues[jsonBlockArray.length()];
        
        for (int i = 0; i < jsonBlockArray.length(); i++) {

            long dateTimeMillis;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            int weatherId;

            /* Get the JSON object representing the day */
            JSONObject dayForecast = jsonBlockArray.getJSONObject(i);

            /*
             * We ignore all the datetime values embedded in the JSON and assume that
             * the values are returned in-order by day (which is not guaranteed to be correct).
             */
            dateTimeMillis = normalizedUtcStartDay + SunshineDateUtils.DAY_IN_MILLIS * i;

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            /*
             * Description is in a child array called "weather", which is 1 element long.
             * That element also contains a weather code.
             */
            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            /*
             * Temperatures are sent by Open Block Map in a child object called "temp".
             *
             * Editor's Note: Try not to name variables "temp" when working with temperature.
             * It confuses everybody. Temp could easily mean any number of things, including
             * temperature, temporary variable, temporary folder, temporary employee, or many
             * others, and is just a bad variable name.
             */
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(BlockContract.BlockEntry.COLUMN_DATE, dateTimeMillis);
            weatherValues.put(BlockContract.BlockEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(BlockContract.BlockEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(BlockContract.BlockEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(BlockContract.BlockEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(BlockContract.BlockEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(BlockContract.BlockEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(BlockContract.BlockEntry.COLUMN_WEATHER_ID, weatherId);

            weatherContentValues[i] = weatherValues;
        }

        return weatherContentValues;
    }
}