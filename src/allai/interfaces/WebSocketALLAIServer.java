/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.interfaces;

import allai.main.ALLAI;
import static allai.utils.ALLAILogger.logError;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import static allai.utils.ALLAILogger.logInfo;

/**
 * @author Daniel Alejandro Hurtado Simoes Universidad de Málaga TFG - Grado en
 * Ingeniería Telemática
 */
public class WebSocketALLAIServer extends WebSocketServer {

    public WebSocketALLAIServer(int puerto) throws UnknownHostException {
        super(new InetSocketAddress(puerto));
        logInfo("WebSocketServer: Awaiting connections on port " + puerto);
    }

    @Override
    public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) {
        logInfo("WebSocketServer: The connection has been closed");
    }

    @Override
    public void onError(WebSocket arg0, Exception e) {
        logError("WebSocketServer: Error occurred during connection: " + e.getMessage());
    }

    @Override
    public void onMessage(WebSocket webSocket, String mensaje) {
        logInfo("WebSocketServer: Received message: " + mensaje);
        RequestWorker thread = new RequestWorker(webSocket, mensaje);
        thread.start();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake arg1) {
        logInfo("WebSocketServer: A new connection has been opened");
    }

    public class RequestWorker extends Thread {

        private WebSocket webSocket;
        private String mensaje;

        private RequestWorker(WebSocket webSocket, String mensaje) {
            this.webSocket = webSocket;
            this.mensaje = mensaje;
        }

        @Override
        public void run() {
            ALLAI allai = new ALLAI();
            String response = allai.getResponse(mensaje) + '\n';
            logInfo("WebSocketServer: Sending response: " + response);
            webSocket.send(response);
        }

    }
}
