package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
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
    public boolean onEvent(JSONObject json, final RequestWrapper request) {
        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_DELAYED);

        //noinspection HardCodedStringLiteral
        Misc.log(StatClean.this.getClass().getSimpleName(), "started");
        Common.getInstance().getDataProcessor("v1").cleanStatisticsMessages(new Runnable1<JSONObject>() {
            @Override
            public void call(JSONObject json) {
                Misc.log(StatClean.this.getClass().getSimpleName(), "done");
                request.sendResult(json);
            }
        }, new Runnable1<JSONObject>() {
            @Override
            public void call(JSONObject json) {
                Misc.err(StatClean.this.getClass().getSimpleName(), "failed");
                json.put(STATUS, STATUS_ERROR);
                json.put(MESSAGE, "Messages cleaning failed.");
                request.sendError(500, json);
            }
        });
        return true;
    }
}
