package com.edeqa.waytousserver.rest.tracking;

import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.edeqa.waytousserver.rest.tracking.AbstractTrackingAction;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import java.util.Iterator;

import static com.edeqa.waytous.Constants.REQUEST_TRACKING;


/**
 * Created 1/16/17.
 */

public class Tracking extends AbstractTrackingAction {

    public static final String TYPE = REQUEST_TRACKING;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyGroup token, MyUser user, JSONObject request, JSONObject result) {

        user.addPosition(request);
        JSONObject o = user.getPosition().toJSON();
        Iterator iter = o.keys();
        while(iter.hasNext()) {
            String key = (String) iter.next();
            result.put(key, o.get(key));
        }
        return true;
    }

    @Override
    public boolean isSaveable() {
        return true;
    }

}
