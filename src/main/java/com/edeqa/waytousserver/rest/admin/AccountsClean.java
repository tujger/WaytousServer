package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class AccountsClean implements NamedCall<RequestWrapper> {

    @Override
    public String getName() {
        return "accounts/clean";
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) {
        //noinspection HardCodedStringLiteral
        Common.getInstance().getDataProcessor("v1").validateAccounts();

        json.put(STATUS, STATUS_SUCCESS);
        json.put(MESSAGE, "Clean started.");
    }
}
