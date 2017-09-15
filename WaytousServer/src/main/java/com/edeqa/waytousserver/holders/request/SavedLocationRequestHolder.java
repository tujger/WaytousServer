package com.edeqa.waytousserver.holders.request;

import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.REQUEST_SAVED_LOCATION;
import static com.edeqa.waytous.Constants.USER_ADDRESS;
import static com.edeqa.waytous.Constants.USER_DESCRIPTION;
import static com.edeqa.waytous.Constants.USER_LATITUDE;
import static com.edeqa.waytous.Constants.USER_LONGITUDE;
import static com.edeqa.waytous.Constants.USER_NAME;


/**
 * Created 1/16/17.
 */

public class SavedLocationRequestHolder implements RequestHolder {

    public static final String TYPE = REQUEST_SAVED_LOCATION;

    public SavedLocationRequestHolder(AbstractDataProcessor context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyGroup token, MyUser user, JSONObject request, JSONObject result) {

        System.out.println("SAVEDLOCATION:");

        result.put(USER_LATITUDE, request.getDouble(USER_LATITUDE));
        result.put(USER_LONGITUDE, request.getDouble(USER_LONGITUDE));
        if(request.has(USER_ADDRESS)) {
            result.put(USER_ADDRESS, request.getString(USER_ADDRESS));
        }
        if(request.has(USER_NAME)) {
            result.put(USER_NAME, request.getString(USER_NAME));
        }
        if(request.has(USER_DESCRIPTION)) {
            result.put(USER_DESCRIPTION, request.getString(USER_DESCRIPTION));
        }

        return true;
    }

    @Override
    public boolean isSaveable() {
        return true;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }


}
