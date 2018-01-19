package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;
import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.HttpDPConnection;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@SuppressWarnings("unused")
public class Join implements RestAction {

    @Override
    public String getActionName() {
        return "join";
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {
        try {
            String body = request.getBody();
            JSONObject options = new JSONObject(request.getBody());

            Misc.log("Join", request.getRemoteAddress(), "joinV1:", body);
            Common.getInstance().getDataProcessor(request.getRequestURI().getPath().split("/")[3]).onMessage(new HttpDPConnection(request), body);

            json.put(STATUS, STATUS_SUCCESS);

//            System.out.println(json.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
            json.put(Rest.STATUS, "error");
            json.put(Rest.REASON, "Action failed");
            json.put(Rest.MESSAGE, e.getMessage());
        }
    }
}
