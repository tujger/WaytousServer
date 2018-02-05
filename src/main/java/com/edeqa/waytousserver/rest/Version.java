package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Version extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/rest/version";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean call(JSONObject json, RequestWrapper request) {
        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, 1);
        json.put(MESSAGE, Common.SERVER_BUILD);
        return true;
    }
}
