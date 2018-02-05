package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class AccountsClean extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/account/clean";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) {
        //noinspection HardCodedStringLiteral
        Common.getInstance().getDataProcessor("v1").validateAccounts();

        json.put(STATUS, STATUS_SUCCESS);
        json.put(MESSAGE, "Clean started.");
    }
}
