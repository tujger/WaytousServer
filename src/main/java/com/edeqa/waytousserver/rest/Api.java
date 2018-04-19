package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class Api extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/rest/api";

    private static final String VERSION = "version";
    private static final String VERSION_NAME = "version_name";
    private static final String VERSION_CODE = "version_code";
    private JSONObject options;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {

        setOptions(request.fetchOptions());
//        JSONObject resultObject = new JSONObject();
        JSONArray resultArray = new JSONArray();

        boolean success = false;
        if(getOptions().has(VERSION)) {
            JSONObject resultObject = new JSONObject();
            resultObject.put(VERSION_NAME, Common.SERVER_VERSION + "." + Common.SERVER_BUILD);
            resultObject.put(VERSION_CODE, Common.SERVER_BUILD);
            json.put(VERSION, resultObject);
            success = true;
        }

        if(success) {
            json.put(STATUS, STATUS_SUCCESS);
            json.put(CODE, CODE_JSON);
        } else {
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_METHOD_NOT_ALLOWED);
            json.put(MESSAGE, "Not enough arguments");
        }
    }

    public JSONObject getOptions() {
        return options;
    }

    public void setOptions(JSONObject options) {
        this.options = options;
    }
}
