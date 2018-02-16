package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.eventbus.EventBus;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.rest.firebase.AbstractFirebaseAction;
import com.edeqa.waytousserver.rest.firebase.AdminToken;
import com.edeqa.waytousserver.servers.DataProcessorFirebaseV1;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;

@SuppressWarnings("unused")
public class InitialData extends com.edeqa.waytousserver.rest.InitialData {

    public static final String TYPE = "/admin/rest/initial";

    private boolean admin;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper requestWrapper) {
        super.setAdmin(true);
        super.call(json, requestWrapper);
    }

}
