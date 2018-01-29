package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.HttpDPConnection;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.REQUEST_TOKEN;

@SuppressWarnings("unused")
public class Join implements NamedCall<RequestWrapper> {

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
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
    }
}
