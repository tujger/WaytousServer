package com.edeqa.waytousserver.helpers;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Mime;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;

import java.net.InetSocketAddress;

/**
 * Created 5/24/2017.
 */
public class HttpDPConnection implements DataProcessorConnection {

    private final RequestWrapper requestWrapper;

    public HttpDPConnection(RequestWrapper requestWrapper) {
        this.requestWrapper = requestWrapper;
    }

    @Override
    public boolean isOpen() {
        return true;//conn.isOpen();
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return requestWrapper.getRemoteAddress();
    }

    @Override
    public void send(String string) {
        requestWrapper.sendResult(200, Mime.APPLICATION_JSON, string.getBytes());
    }

    @Override
    public void close() {
//            conn.close();
    }
}
