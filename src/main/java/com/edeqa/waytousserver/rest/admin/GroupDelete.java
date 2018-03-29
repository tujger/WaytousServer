package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class GroupDelete extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/group/delete";

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
            Misc.log(GroupDelete.this.getClass().getSimpleName(), options);

            String groupId = new JSONObject(options).getString(Rest.GROUP_ID);

            Common.getInstance().getDataProcessor().deleteGroup(groupId, request::sendResult, jsonError -> request.sendError(500, jsonError));
        }, exception -> {
            Misc.err(GroupDelete.this.getClass().getSimpleName(), exception);
            JSONObject jsonResult = new JSONObject();
            jsonResult.put(STATUS, STATUS_ERROR);
            jsonResult.put(MESSAGE, "Incorrect request.");
            jsonResult.put(EXTRA, exception.getMessage());
            request.sendError(400, jsonResult);
        });
    }
}
