package com.edeqa.waytousserver.holders.flag;

import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.FlagHolder;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.REQUEST_PUSH;


/**
 * Created 1/19/17.
 */

public class PushFlagHolder implements FlagHolder {

    public static final String TYPE = REQUEST_PUSH;

    public PushFlagHolder(AbstractDataProcessor context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyGroup token, MyUser user, JSONObject request, JSONObject result) {

        System.out.println("PUSHFLAGHOLDER:" + request);

        if (request.has(REQUEST_PUSH) && request.getBoolean(REQUEST_PUSH)){
            result.put(REQUEST_PUSH, true);
        }
        return true;
    }


}
