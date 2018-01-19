package com.edeqa.waytousserver.interfaces;


import com.edeqa.edequate.helpers.RequestWrapper;

/**
 * Created 1/23/2017.
 */

public interface PageHolder {

    String getType();

    boolean perform(RequestWrapper requestWrapper);
}
