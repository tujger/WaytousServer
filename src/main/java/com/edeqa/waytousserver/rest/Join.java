package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.HttpDPConnection;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class Join extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/rest/join";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean call(JSONObject json, RequestWrapper request) {
        String body = request.getBody();
        JSONObject options = new JSONObject(body);

        Misc.log("Join", body);
        try {
            Common.getInstance().getDataProcessor("v1").onMessage(new HttpDPConnection(request), body);
            json.put(STATUS, STATUS_SUCCESS);
            json.put(CODE, CODE_DELAYED);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
