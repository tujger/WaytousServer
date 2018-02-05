package com.edeqa.waytousserver.rest.tracking;

import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.edeqa.waytousserver.rest.tracking.AbstractTrackingAction;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import java.util.HashMap;

import static com.edeqa.waytous.Constants.REQUEST_LEAVE;


/**
 * Created 1/16/17.
 */

public class Admin extends AbstractTrackingAction {

    public static final String TYPE = REQUEST_LEAVE;

    private final HashMap<String,MyUser> admins;


    public Admin() {
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

}
