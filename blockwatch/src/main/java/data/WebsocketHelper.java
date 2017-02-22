package data;

import android.content.Context;
import android.util.Log;

import com.neovisionaries.ws.client.OpeningHandshakeException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

/**
 * Created by test on 2/20/2017.
 */
// Implements websocket library's connection
public class WebsocketHelper{

    public WebSocketFactory factory = new WebSocketFactory(); // Create a WebSocketFactory instance.
    public WebSocket ws; // Create the socket
    public WebSocketAdapter adapter; // Create the adapter
    public final String blockchainInfoURL = "ws://echo.websocket.org"; // "wss://ws.blockchain.info/inv"
    public Context context;

    public WebSocket createSocket(Context context) {
        this.context = context;
        // Create a WebSocket. The scheme part can be one of the following:
        // 'ws', 'wss', 'http' and 'https' (case-insensitive). The user info
        // part, if any, is interpreted as expected. If a raw socket failed
        // to be created, an IOException is thrown.
        try {
            ws = factory.createSocket(blockchainInfoURL).addListener(new WebSocketAdapter() {
                // A text message arrived from the server.
                public void onTextMessage(WebSocket websocket, String message) {
                    System.out.println(message);
                    // Received a text message.
                    Log.d("onTextMessage: ", message);
                }
            })
            .connect();
            // Try this "connectAsynchronously()" without using strict thread policy mode in MainActivity

            ws.sendText("test123");
            ws.sendText("op: ping");

        } catch (java.io.IOException exception) {
            Log.d("WebSocket Exception: ", exception.getMessage());
        } catch (OpeningHandshakeException e)
        {
/*            // Get the status code.
            int statusCode = e.getStatusLine().getStatusCode();
            // If the status code is in the range of 300 to 399.
            if (300 <= statusCode && statusCode <= 399)
            {   // Location header should hold the redirection URL.
                String location = e.getHeaders().get("Location").get(0);
            }*/
        }
        catch (WebSocketException socketException){
            Log.d("WebSocket Exception: ", socketException.getMessage());
        }

        return ws;
    }
}
