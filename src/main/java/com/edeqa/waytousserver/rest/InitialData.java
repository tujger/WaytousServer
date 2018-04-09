package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.eventbus.EventBus;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.rest.firebase.AbstractFirebaseAction;
import com.edeqa.waytousserver.rest.firebase.AdminToken;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;

@SuppressWarnings("unused")
public class InitialData extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/rest/initial";

    private boolean admin;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper requestWrapper) {
        json.put("version", SERVER_BUILD);
        json.put("HTTP_PORT", OPTIONS.getHttpPortMasked());
        json.put("HTTPS_PORT", OPTIONS.getHttpsPortMasked());
        json.put("WS_FB_PORT", OPTIONS.getWsPortFirebase());
        json.put("WSS_FB_PORT", OPTIONS.getWssPortFirebase());
        json.put("WS_PORT", OPTIONS.getWsPortDedicated());
        json.put("WSS_PORT", OPTIONS.getWssPortDedicated());
        json.put("firebase_config", OPTIONS.getFirebaseConfig());


        if(isAdmin()) {
            json.put("sign", Common.getInstance().getDataProcessor().createCustomToken("Viewer"));
            json.put("access", ((AdminToken) EventBus.getOrCreate(AbstractFirebaseAction.EVENTBUS).getHolder(AdminToken.TYPE)).fetchToken());
            json.put("user", requestWrapper.getUserName());
        } else {
            json.put("is_stand_alone", Common.getInstance().getDataProcessor().isServerMode());
            if(OPTIONS.isDebugMode()) json.put("is_debug_mode", true);
            json.put("google_analytics_tracking_id", OPTIONS.getGoogleAnalyticsTrackingId());
        }
        json.put(STATUS, STATUS_SUCCESS);
    }

    public boolean isAdmin() {
        return admin;
    }

    public InitialData setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }
}
