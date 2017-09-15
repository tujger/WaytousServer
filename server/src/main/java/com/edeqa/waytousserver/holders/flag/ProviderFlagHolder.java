package com.edeqa.waytousserver.holders.flag;

import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.FlagHolder;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;


/**
 * Created 1/19/17.
 */

public class ProviderFlagHolder implements FlagHolder {

    public static final String TYPE = "provider";

    public ProviderFlagHolder(AbstractDataProcessor context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyGroup token, MyUser user, JSONObject request, JSONObject result) {

        System.out.println("PROVIDERFLAGHOLDER:" + request);
        return true;
    }


}
