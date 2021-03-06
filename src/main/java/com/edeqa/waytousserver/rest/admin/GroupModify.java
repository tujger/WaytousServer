package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class GroupModify extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/group/modify";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) {
        json.put(STATUS, STATUS_DELAYED);
        request.processBody(buf -> {
            String options = buf.toString();

            //noinspection HardCodedStringLiteral
            Misc.log(GroupModify.this.getClass().getSimpleName(), options);

            JSONObject jsonOptions = new JSONObject(options);
            String groupId = jsonOptions.getString(Rest.GROUP_ID);
            String property = jsonOptions.getString(Rest.PROPERTY);
            String value = jsonOptions.getString(Rest.VALUE);

            Common.getInstance().getDataProcessor().modifyOptionInGroup(groupId, property, value, request::sendResult, jsonError -> request.sendError(500, jsonError));
        }, exception -> {
            Misc.err(GroupModify.this.getClass().getSimpleName(), exception);
            JSONObject jsonResult = new JSONObject();
            jsonResult.put(STATUS, STATUS_ERROR);
            jsonResult.put(MESSAGE, "Incorrect request.");
            jsonResult.put(EXTRA, exception.getMessage());
            request.sendError(400, jsonResult);
        });
    }
}
