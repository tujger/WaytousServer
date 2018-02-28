package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class GroupSwitch extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/group/switch";

    @Override
    public String getType() {
        return TYPE;
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
                Misc.log(GroupSwitch.this.getClass().getSimpleName(), options);

                JSONObject json = new JSONObject(options);
                String groupId = json.getString(Rest.GROUP_ID);
                String property = json.getString(Rest.PROPERTY);

                Common.getInstance().getDataProcessor().switchPropertyInGroup(groupId,property,new Runnable1<JSONObject>() {
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
                Misc.err(GroupSwitch.this.getClass().getSimpleName(), e);
                JSONObject json = new JSONObject();
                json.put(STATUS, STATUS_ERROR);
                json.put(MESSAGE, "Incorrect request.");
                json.put(EXTRA, e.getMessage());
                request.sendError(400, json);
            }
        });
    }
}
