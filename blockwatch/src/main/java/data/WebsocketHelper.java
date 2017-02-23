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
 * Created by test on 2/20/2017.
 */
// Implements websocket library's connection
public class WebsocketHelper{

    public WebSocketFactory factory = new WebSocketFactory(); // Create a WebSocketFactory instance.
    public WebSocket ws; // Create the socket
    public WebSocketAdapter adapter; // Create the adapter
    public final String blockchainInfoURL = "wss://echo.websocket.org"; //"wss://ws.blockchain.info/inv"; //"wss://echo.websocket.org"
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
            .addListener(new WebSocketAdapter() {
                        // A frame arrived from the server.
                        public void onFrame(WebSocket websocket, WebSocketFrame frame) {
                            System.out.println(frame.toString());
                            // Received a text message.
                            //Log.d("onFrame: ", frame.toString());
                            if(frame.hasPayload())
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
            Log.d("WebSocket Exception: ", exception.getMessage());
        } catch (OpeningHandshakeException e)
        {
            Log.d("WebSocket Exception: ", e.getMessage());
        }
        catch (WebSocketException socketException){
            Log.d("WebSocket Exception: ", socketException.getMessage());
        }

        WebSocketFrame test = new WebSocketFrame();
        test.setOpcode(WebSocketOpcode.PING);  // Testing opcodes
        test.setPayload("{op:ping_tx}");
        ws.sendFrame(test);
/*        test.setOpcode(13);  // Testing opcodes
        ws.sendFrame(test);
        test.setOpcode(14);  // Testing opcodes
        ws.sendFrame(test);
        test.setOpcode(15);  // Testing opcodes
        ws.sendFrame(test);*/

        ws.sendText("helloWorld");

        WebSocketState state = ws.getState();
        Log.d("WebSocket State: ", state.toString());

        return ws;
    }
}
