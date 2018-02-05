package com.edeqa.waytousserver.rest.tracking;

import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.edeqa.waytousserver.rest.tracking.AbstractTrackingAction;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.REQUEST_MESSAGE;
import static com.edeqa.waytous.Constants.USER_MESSAGE;


/**
 * Created 1/16/17.
 */

public class Message extends AbstractTrackingAction {

    public static final String TYPE = REQUEST_MESSAGE;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyGroup token, MyUser user, JSONObject request, JSONObject result) {

        result.put(USER_MESSAGE, request.getString(USER_MESSAGE));

        return true;
    }

    @Override
    public boolean isSaveable() {
        return true;
    }

}
