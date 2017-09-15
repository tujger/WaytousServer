package com.edeqa.waytousserver.interfaces;

import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.sun.net.httpserver.HttpExchange;

/**
 * Created 1/23/2017.
 */

public interface PageHolder {

    String getType();

    boolean perform(RequestWrapper requestWrapper);
}
