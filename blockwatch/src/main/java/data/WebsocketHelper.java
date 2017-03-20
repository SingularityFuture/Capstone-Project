package data;

import android.content.Context;
import android.util.Log;

import com.neovisionaries.ws.client.OpeningHandshakeException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketOpcode;
import com.neovisionaries.ws.client.WebSocketState;

/**
 * Created by retrieveUnconfirmedTransactions on 2/20/2017.
 */
// Implements websocket library's connection
public class WebsocketHelper {

    private WebSocketFactory factory = new WebSocketFactory(); // Create a WebSocketFactory instance.
    private WebSocket ws; // Create the socket
    public WebSocketAdapter adapter; // Create the adapter
    public Context context;

    public WebSocket createSocket(Context context) {
        this.context = context;
        String blockchainInfoURL = "wss://echo.websocket.org";
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
                    .addListener(new WebSocketAdapter() {
                        // A frame arrived from the server.
                        public void onFrame(WebSocket websocket, WebSocketFrame frame) {
                            System.out.println(frame.toString());
                            // Received a text message.
                            //Log.d("onFrame: ", frame.toString());
                            if (frame.hasPayload())
                                Log.d("onFrame text: ", frame.getPayloadText());
                        }
                    })
                    .addListener(new WebSocketAdapter() {
                        // A frame arrived from the server.
                        public void onTextFrame(WebSocket websocket, WebSocketFrame frame) {
                            System.out.println(frame.toString());
                            // Received a text message.
                            Log.d("onTextFrame: ", frame.toString());
                        }
                    })
                    .addListener(new WebSocketAdapter() {
                        // A frame arrived from the server.
                        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) {
                            System.out.println(frame.toString());
                            // Received a text message.
                            Log.d("onPongFrame: ", frame.toString());
                        }
                    })
                    .addListener(new WebSocketAdapter() {
                        // A frame arrived from the server.
                        public void onStateChanged(WebSocket websocket, WebSocketState state) {
                            System.out.println(state.toString());
                            // Received a text message.
                            Log.d("onStateChanged: ", state.toString());
                        }
                    })
                    .connect();
            // Try this "connectAsynchronously()" without using strict thread policy mode in MainActivity

            WebSocketState state = ws.getState();
            Log.d("WebSocket State: ", state.toString());
        } catch (java.io.IOException exception) {
            Log.d("IO Exception: ", exception.getMessage());
        } catch (OpeningHandshakeException e) {
            Log.d("Opening Handshake Exc: ", e.getMessage());
        } catch (WebSocketException socketException) {
            Log.d("WebSocket Exception: ", socketException.getMessage());
        }

        WebSocketFrame test = new WebSocketFrame();
        test.setOpcode(WebSocketOpcode.PING);  // Testing opcodes
        test.setPayload("{op:ping_tx}");
        test.setFin(true);
        ws.sendFrame(test);
/*        retrieveUnconfirmedTransactions.setOpcode(13);  // Testing opcodes
        ws.sendFrame(retrieveUnconfirmedTransactions);
        retrieveUnconfirmedTransactions.setOpcode(14);  // Testing opcodes
        ws.sendFrame(retrieveUnconfirmedTransactions);
        retrieveUnconfirmedTransactions.setOpcode(15);  // Testing opcodes
        ws.sendFrame(retrieveUnconfirmedTransactions);*/

        //ws.sendText("helloWorld");

        WebSocketState state = ws.getState();
        Log.d("WebSocket State: ", state.toString());

        return ws;
    }
}
