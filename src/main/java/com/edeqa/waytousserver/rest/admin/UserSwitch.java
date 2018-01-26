package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class UserSwitch implements NamedCall<RequestWrapper> {

    @Override
    public String getName() {
        return "user/switch";
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) {
        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_DELAYED);
        request.processBody(new Runnable1<StringBuilder>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(StringBuilder buf) {
                String options = buf.toString();

                //noinspection HardCodedStringLiteral
                Misc.log(UserSwitch.this.getClass().getSimpleName(), options);

                JSONObject json = new JSONObject(options);
                String groupId = json.getString(Rest.GROUP_ID);
                Long userNumber = Long.parseLong(json.getString(Rest.USER_NUMBER));
                String property = json.getString(Rest.PROPERTY);
                Boolean value = json.getBoolean(Rest.VALUE);

                Common.getInstance().getDataProcessor("v1").switchPropertyForUser(groupId,userNumber,property,value,new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        request.sendResult(json);
                    }
                }, new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        request.sendError(500, json);
                    }
                });
            }
        }, new Runnable1<Exception>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(Exception e) {
                Misc.err(UserSwitch.this.getClass().getSimpleName(), e);
                JSONObject json = new JSONObject();
                json.put(STATUS, STATUS_ERROR);
                json.put(MESSAGE, "Incorrect request.");
                json.put(EXTRA, e.getMessage());
                request.sendError(400, json);
            }
        });
    }
}
