package utilities;

/**
 * Created by Michael on 2/26/2017.
 */
import android.content.Context;
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

    public static URL getUrl(Context context, String transactionHash) {
        return buildTransactionUrl(transactionHash);
    }

    /**
     * Builds the URL used to talk to the blockchain.info server using the selected transaction hash
     *
     * @param transactionHash  The hash of the selected transaction
     * @return The Url to use to query the blockchain server.
     */
    private static URL buildTransactionUrl(String transactionHash) {
        Uri transactionQueryUri = Uri.parse(BLOCKCHAIN_URL).buildUpon()
                .appendPath(transactionHash)
                .build();

        try {
            URL transactionQueryUrl = new URL(transactionQueryUri.toString());
            Log.v(TAG, "URL: " + transactionQueryUri);
            return transactionQueryUrl;
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
