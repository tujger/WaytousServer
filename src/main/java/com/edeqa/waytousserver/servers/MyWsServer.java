package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.WebsocketDPConnection;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.edeqa.waytous.Constants.LIFETIME_INACTIVE_USER;
import static com.edeqa.waytous.Constants.OPTIONS;

/**
 * Created 10/5/16.
 */

public class MyWsServer extends WebSocketServer {

    private static boolean validationStarted = false;

    public MyWsServer(int port) {
        super(new InetSocketAddress(port));

        if(!MyWsServer.isValidationStarted() && !OPTIONS.isDebugMode()) {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Common.getInstance().getDataProcessor("v1").validateGroups();
                }
            }, 0, LIFETIME_INACTIVE_USER, TimeUnit.SECONDS);
            MyWsServer.setValidationStarted(true);
        }
    }

    /*   @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        System.out.println("HANDSHAKE:"+conn+":"+draft+":"+request);

        return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
    }
*/
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Common.log("WS","onOpen:"+conn.getRemoteSocketAddress(),handshake.getResourceDescriptor() );
        Common.getInstance().getDataProcessor("v1").onOpen(new WebsocketDPConnection(conn), handshake);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Common.log("WS","onClose:"+conn.getRemoteSocketAddress(),"code:"+code, "reason:"+reason);
        Common.getInstance().getDataProcessor("v1").onClose(new WebsocketDPConnection(conn), code, reason, remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Common.log("WS","onMessage:"+conn.getRemoteSocketAddress(), message.length() > 200 ? "("+message.length() + " byte(s))" : message );
        Common.getInstance().getDataProcessor("v1").onMessage(new WebsocketDPConnection(conn), message);
    }

//    @Override
//    public void onWebsocketPong(WebSocket conn, Framedata f) {
//        super.onWebsocketPong(conn, f);
//        System.out.println("PONG:"+conn.getRemoteSocketAddress()+":"+f);
//    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Common.log("WS","onError:"+conn.getRemoteSocketAddress(),"exception:"+ex.getMessage());
        Common.getInstance().getDataProcessor("v1").onError(new WebsocketDPConnection(conn), ex);
    }

    @Override
    public void onStart() {
        Common.log("WS","onStart/"+this.getClass().getSimpleName()+":"+this.getPort());
//        Common.log("WS","onStart/"+Common.getInstance().getDataProcessor("v1").getClass().getSimpleName()+":"+this.getPort());
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        super.onWebsocketPing(conn, f);
        Common.getInstance().getDataProcessor("v1").onWebSocketPing(new WebsocketDPConnection(conn), f);
    }

    public boolean parse(BufferedReader sysin) throws IOException, InterruptedException {
        String in = sysin.readLine();
        System.out.println("READ:" + in);
//                        s.sendToAll(in);
        if (in.equals("exit")) {
            stop();
            return false;
        } else if (in.equals("restart")) {
            stop();
            start();
            return false;
        }
        return true;
    }

    public static void setValidationStarted(boolean validationStarted) {
        MyWsServer.validationStarted = validationStarted;
    }

    public static boolean isValidationStarted() {
        return validationStarted;
    }

}
