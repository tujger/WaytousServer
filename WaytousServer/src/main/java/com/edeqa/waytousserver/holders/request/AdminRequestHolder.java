package com.edeqa.waytousserver.holders.request;

import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import java.util.HashMap;

import static com.edeqa.waytous.Constants.REQUEST_LEAVE;


/**
 * Created 1/16/17.
 */

public class AdminRequestHolder implements RequestHolder {

    public static final String TYPE = REQUEST_LEAVE;

    private final HashMap<String,MyUser> admins;


    public AdminRequestHolder(AbstractDataProcessor context) {
        admins = new HashMap<>();
    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyGroup token, MyUser user, JSONObject request, JSONObject result) {

        admins.put(user.connection.getRemoteSocketAddress().toString(), user);

        System.out.println("ADMIN:"+user);


        return false;
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
