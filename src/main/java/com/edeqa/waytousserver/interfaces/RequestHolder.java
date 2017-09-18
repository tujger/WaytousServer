package com.edeqa.waytousserver.interfaces;

import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;

import org.json.JSONObject;

/**
 * Created 1/16/17.
 */

public interface RequestHolder {
    String getType();

    boolean perform(MyGroup token, MyUser user, JSONObject request, JSONObject result);

    boolean isSaveable();

    boolean isPrivate();
}
