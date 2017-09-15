package com.edeqa.waytousserver.holders.flag;

import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.FlagHolder;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.REQUEST;
import static com.edeqa.waytous.Constants.REQUEST_DELIVERY_CONFIRMATION;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;


/**
 * Created 1/19/17.
 */

public class DeliveryFlagHolder implements FlagHolder {

    public static final String TYPE = REQUEST_DELIVERY_CONFIRMATION;

    public DeliveryFlagHolder(AbstractDataProcessor context) {

    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyGroup token, MyUser user, JSONObject request, JSONObject result) {

        String id = request.getString(REQUEST_DELIVERY_CONFIRMATION);
        JSONObject o = new JSONObject();
        try {
            o.put(RESPONSE_STATUS, request.get(REQUEST));
            o.put(REQUEST_DELIVERY_CONFIRMATION, id);
            user.send(o);
        } catch(Exception e){
            e.printStackTrace();
        }

        return true;
    }


}
