package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class StatClean implements RestAction {

    @Override
    public String getActionName() {
        return "stat/clean";
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) {
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
    }
}