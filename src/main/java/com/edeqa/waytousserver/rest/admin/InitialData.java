package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.helpers.RequestWrapper;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class InitialData extends com.edeqa.waytousserver.rest.InitialData {

    public static final String TYPE = "/admin/rest/initial";

    private boolean admin;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper requestWrapper) {
        super.setAdmin(true);
        super.call(json, requestWrapper);
    }

}
