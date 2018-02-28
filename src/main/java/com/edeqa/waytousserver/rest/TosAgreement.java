package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.HttpDPConnection;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class TosAgreement extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/rest/tosAgreement";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        String body = request.getBody();
        Misc.log("TosAgreement", request.getRemoteAddress(), "tosAgreement:", body);

        Common.getInstance().getDataProcessor().onMessage(new HttpDPConnection(request), body);

        json.put(STATUS, STATUS_SUCCESS);
    }
}
