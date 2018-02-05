package com.edeqa.waytousserver.rest.tracking;

import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.edeqa.waytousserver.rest.tracking.AbstractTrackingAction;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.REQUEST_WELCOME_MESSAGE;


/**
 * Created 1/16/17.
 */

public class WelcomeMessage extends AbstractTrackingAction {

    public static final String TYPE = REQUEST_WELCOME_MESSAGE;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyGroup token, MyUser user, JSONObject request, JSONObject result) {

        String text = request.getString(REQUEST_WELCOME_MESSAGE);
        if (user.getNumber() == 0 && text != null) {
            token.setWelcomeMessage(text);
        }
        System.out.println("UPDATE:welcome message:" + request.getString(REQUEST_WELCOME_MESSAGE));

        return false;
    }
}
