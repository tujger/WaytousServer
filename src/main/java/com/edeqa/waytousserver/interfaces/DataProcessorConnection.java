package com.edeqa.waytousserver.interfaces;

import java.net.InetSocketAddress;

/**
 * Created 5/24/2017.
 */
public interface DataProcessorConnection {
    boolean isOpen();

    InetSocketAddress getRemoteSocketAddress();

    void send(String string);

    void close();
}
