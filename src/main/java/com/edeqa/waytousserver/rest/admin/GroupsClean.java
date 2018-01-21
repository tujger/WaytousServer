package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytous.SignProvider;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;

@SuppressWarnings("unused")
public class GroupsClean implements RestAction {

    @Override
    public String getActionName() {
        return "groups/clean";
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
                Misc.log(GroupsClean.this.getClass().getSimpleName());

                Common.getInstance().getDataProcessor("v1").validateGroups();

                JSONObject json = new JSONObject();
                json.put(Rest.STATUS, Rest.SUCCESS);
                json.put(Rest.MESSAGE, "Clean started.");
                request.sendResult(json);

            }
        }, new Runnable1<Exception>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(Exception e) {
                Misc.err(GroupsClean.this.getClass().getSimpleName(), e);
                JSONObject json = new JSONObject();
                json.put(STATUS, STATUS_ERROR);
                json.put(MESSAGE, "Incorrect request.");
                json.put(Rest.REASON, e.getMessage());
                request.sendError(400, json);
            }
        });
    }
}
