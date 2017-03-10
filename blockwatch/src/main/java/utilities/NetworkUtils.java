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
            "https://blockchain.info/rawtx/";

    private static final String LATLONG_URL =
            "http://freegeoip.net/json/";

    public static URL getUrl(String transactionHash) {
        return buildTransactionUrl(transactionHash);
    }

    /**
     * Builds the URL used to talk to the blockchain.info server using the selected transaction hash
     *
     * @param UrlAppend  The hash of the selected transaction  //  Also, the IP address to query latitude and longitude
     * @return The Url to use to query the blockchain server.
     */
    private static URL buildTransactionUrl(String UrlAppend) {
        Uri queryUri;

        if(UrlAppend.indexOf('.')<0){
            queryUri = Uri.parse(BLOCKCHAIN_URL).buildUpon()
                    .appendPath(UrlAppend)
                    .build();
        }
        else{
            queryUri = Uri.parse(LATLONG_URL).buildUpon()
                    .appendPath(UrlAppend)
                    .build();
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
