package com.edeqa.waytousserver.holders.request;

import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.REQUEST_CHANGE_NAME;
import static com.edeqa.waytous.Constants.USER_NAME;


/**
 * Created 1/16/17.
 */

public class ChangeNameRequestHolder implements RequestHolder {

    public static final String TYPE = REQUEST_CHANGE_NAME;

    public ChangeNameRequestHolder(AbstractDataProcessor context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyGroup token, MyUser user, JSONObject request, JSONObject result) {

        if (request.has(USER_NAME)) {
            result.put(USER_NAME, request.getString(USER_NAME));
            user.setName(request.getString(USER_NAME));
        }

        return true;
    }

    @Override
    public boolean isSaveable() {
        return false;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }


}
