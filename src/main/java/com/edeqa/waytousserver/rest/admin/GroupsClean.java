package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class GroupsClean implements NamedCall<RequestWrapper> {

    @Override
    public String getName() {
        return "groups/clean";
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) {
        //noinspection HardCodedStringLiteral
        Common.getInstance().getDataProcessor("v1").validateGroups();

        json.put(STATUS, STATUS_SUCCESS);
        json.put(MESSAGE, "Clean started.");
    }
}
