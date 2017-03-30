package utilities;

/**
 * Created by Michael on 2/26/2017.
 */

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the blockchain.info servers.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String BLOCKCHAIN_URL =
            "https://blockchain.info/rawtx/"; // URL to get transaction information

    private static final String LATLONG_URL =
            "http://freegeoip.net/json/"; // URL to convert Relayed By IP address of transaction to geolocation

    private static final String HISTORICAL_PRICES_URL =
            "https://api.blockchain.info/charts/"; // URL to get JSON of historical prices

    private static final String HISTORICAL_PRICES = "market-price"; // Create a tag that marks you want the historical prices URL

    // This was probably here in case you wanted to filter the input here
    public static URL getUrl(String URLInput) {
        return buildUrl(URLInput);
    }

    /**
     * Builds the URL used to talk to the blockchain.info server using the selected transaction hash
     *
     * @param UrlAppend The hash of the selected transaction  //  Also, the IP address to query latitude and longitude
     * @return The Url to use to query the blockchain server.
     */
    private static URL buildUrl(String UrlAppend) {
        Uri queryUri;

        if (UrlAppend.indexOf('.') < 0 && !UrlAppend.equals(HISTORICAL_PRICES)) { // A dot indicates that the input is a latitude/longitude format
            queryUri = Uri.parse(BLOCKCHAIN_URL).buildUpon() // If you don't find it, build the transaction URL
                    .appendPath(UrlAppend)
                    .build();
        } else if (UrlAppend.indexOf('.') != 0 && !UrlAppend.equals(HISTORICAL_PRICES)) { // If there is a dot, build the latitude/longitude URL
            queryUri = Uri.parse(LATLONG_URL).buildUpon()
                    .appendPath(UrlAppend)
                    .build();
        } else if (UrlAppend.equals(HISTORICAL_PRICES)){ // If it equals the tag we specified for historical prices,
            queryUri = Uri.parse(HISTORICAL_PRICES_URL).buildUpon() // Then build the historical prices URL
                    .appendPath(UrlAppend)
                    .appendQueryParameter("format","json")
                    .build();
        } else {
            return null; // Return null if you can't match a URL request
        }

        try {
            URL queryUrl = new URL(queryUri.toString());
            Log.v(TAG, "URL: " + queryUri);
            return queryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response, null if no response
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }
}
