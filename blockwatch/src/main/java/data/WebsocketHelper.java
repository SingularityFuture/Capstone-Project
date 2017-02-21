package data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

/**
 * Created by test on 2/20/2017.
 */


// Implements websocket library's connection
public class WebsocketHelper{

    public WebSocketFactory factory = new WebSocketFactory(); // Create a WebSocketFactory instance.
    public WebSocket ws; // Create the socket
    public WebSocketAdapter adapter; // Create the adapter
    public final String blockchainInfoURL = "wss://ws.blockchain.info/inv";
    public Context context;

    public WebSocketFactory createSocket(Context context) {
        this.context = context;
        // Create a WebSocket. The scheme part can be one of the following:
        // 'ws', 'wss', 'http' and 'https' (case-insensitive). The user info
        // part, if any, is interpreted as expected. If a raw socket failed
        // to be created, an IOException is thrown.
        try {
            ws = factory.createSocket(blockchainInfoURL);
        } catch (java.io.IOException exception) {
            Log.d("WebSocket Exception: ", exception.getMessage());
        }

        adapter = new WebSocketAdapter();
        // Register a listener to receive WebSocket events.
        ws.addListener(adapter);
        try {
            adapter.onTextMessage(ws, "hello");
        }
        catch(Exception e){
            Log.d("onTextMessage error: ",  e.getMessage());
        }
        return factory;
    }


    public void onTextMessage(WebSocket websocket, String message) throws Exception {
        // Received a text message.
        Log.d("onTextMessage: ", message);
        Toast.makeText(context, message, Toast.LENGTH_LONG);
    }
}
