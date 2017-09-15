package com.edeqa.waytousserver.helpers;

import com.edeqa.waytousserver.interfaces.DataProcessorConnection;

import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;

/**
 * Created 5/24/2017.
 */
public class WebsocketDPConnection implements DataProcessorConnection {

    private final WebSocket conn;

    public WebsocketDPConnection(WebSocket conn) {
        this.conn = conn;
    }

    @Override
    public boolean isOpen() {
        return conn.isOpen();
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return conn.getRemoteSocketAddress();
    }

    @Override
    public void send(String string) {
        try {
            if(conn.isOpen()) {
                conn.send(string);
            } else {
                Common.err(WebsocketDPConnection.this, "send:failed", "connectionState:" + conn.getReadyState());
            }
        } catch(Exception e) {
            Common.err(WebsocketDPConnection.this, "send:", string, "error:", e.getMessage());
        }
    }

    @Override
    public void close() {
        conn.close();
    }
}
