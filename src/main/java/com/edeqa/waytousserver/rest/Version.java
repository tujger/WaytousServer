package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Version implements RestAction {

    @Override
    public String getActionName() {
        return "version";
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        try {
            json.put(STATUS, STATUS_SUCCESS);
            json.put(CODE, 1);
            json.put(MESSAGE, Common.SERVER_BUILD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
