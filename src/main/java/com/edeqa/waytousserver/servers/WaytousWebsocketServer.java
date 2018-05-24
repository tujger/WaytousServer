package com.edeqa.waytousserver.servers;

import com.edeqa.helpers.Misc;
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

import static com.edeqa.waytous.Constants.LIFETIME_INACTIVE_GROUP;
import static com.edeqa.waytous.Constants.OPTIONS;

/**
 * Created 10/5/16.
 */

public class WaytousWebsocketServer extends WebSocketServer {

    private static final String LOG = "WWS";

    private static boolean validationStarted = false;

    public WaytousWebsocketServer(int port) {
        super(new InetSocketAddress(port));

        if(!WaytousWebsocketServer.isValidationStarted() && !OPTIONS.isDebugMode()) {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> Common.getInstance().getDataProcessor().validateGroups(), 0, LIFETIME_INACTIVE_GROUP, TimeUnit.SECONDS);
            WaytousWebsocketServer.setValidationStarted(true);
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
        Misc.log(LOG, "[" + conn.getRemoteSocketAddress() + "]","onOpen:", handshake.getResourceDescriptor() );
        Common.getInstance().getDataProcessor().onOpen(new WebsocketDPConnection(conn), handshake);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Misc.log(LOG, "[" + conn.getRemoteSocketAddress() + "]", "onClose: code:"+code, "reason:"+reason);
        Common.getInstance().getDataProcessor().onClose(new WebsocketDPConnection(conn), code, reason, remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Misc.log(LOG, "[" + conn.getRemoteSocketAddress() + "]", "onMessage:", message.length() > 200 ? "("+message.length() + " byte(s))" : message );
        Common.getInstance().getDataProcessor().onMessage(new WebsocketDPConnection(conn), message);
    }

//    @Override
//    public void onWebsocketPong(WebSocket conn, Framedata f) {
//        super.onWebsocketPong(conn, f);
//        System.out.println("PONG:"+conn.getRemoteSocketAddress()+":"+f);
//    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Misc.err(LOG, "[" + conn.getRemoteSocketAddress() + "]", "onError:", ex.getMessage());
        System.out.println(Misc.toStringDeep(conn));
        Common.getInstance().getDataProcessor().onError(new WebsocketDPConnection(conn), ex);
    }

    @Override
    public void onStart() {
        Misc.log(LOG, "onStart/"+this.getClass().getSimpleName()+":"+this.getPort());
//        Common.log(LOG,"onStart/"+Common.getInstance().getDataProcessor().getClass().getSimpleName()+":"+this.getPort());
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        super.onWebsocketPing(conn, f);
        Common.getInstance().getDataProcessor().onWebSocketPing(new WebsocketDPConnection(conn), f);
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
        WaytousWebsocketServer.validationStarted = validationStarted;
    }

    public static boolean isValidationStarted() {
        return validationStarted;
    }

}
