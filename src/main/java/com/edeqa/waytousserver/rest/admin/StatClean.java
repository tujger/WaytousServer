package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class StatClean extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/stat/clean";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) {
        json.put(STATUS, STATUS_DELAYED);

        //noinspection HardCodedStringLiteral
        Misc.log(StatClean.this.getClass().getSimpleName(), "started");
        Common.getInstance().getDataProcessor().cleanStatisticsMessages(jsonSuccess -> {
            Misc.log(StatClean.this.getClass().getSimpleName(), "done");
            request.sendResult(jsonSuccess);
        }, jsonError -> {
            Misc.err(StatClean.this.getClass().getSimpleName(), "failed");
            jsonError.put(STATUS, STATUS_ERROR);
            jsonError.put(MESSAGE, "Messages cleaning failed.");
            request.sendError(500, jsonError);
        });
    }
}
