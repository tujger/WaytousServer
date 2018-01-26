package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.HttpDPConnection;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class TosAgreement implements NamedCall<RequestWrapper> {

    @Override
    public String getName() {
        return "tosAgreement";
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        String body = request.getBody();
        Misc.log("TosAgreement", request.getRemoteAddress(), "tosAgreement:", body);

        Common.getInstance().getDataProcessor(request.getRequestURI().getPath().split("/")[3]).onMessage(new HttpDPConnection(request), body);

        json.put(STATUS, STATUS_SUCCESS);
    }
}
