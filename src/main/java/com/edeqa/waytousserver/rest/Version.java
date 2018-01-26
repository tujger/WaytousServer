package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Version implements NamedCall<RequestWrapper> {

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, 1);
        json.put(MESSAGE, Common.SERVER_BUILD);
    }
}
