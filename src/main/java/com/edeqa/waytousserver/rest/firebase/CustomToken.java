package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
/*
 * This method requests and returns accessToken for Firebase. Depending on current installation type
 * it defines the properly request and performs it. Installation type can be defined in gradle.build.
 */
public class CustomToken extends AbstractAction<CustomToken, String> {

    private final int MAXIMUM_CACHED_TOKENS = 1000;

    private Long timeCreated = 0L;
    private Map<String,String> tokens = new HashMap<>();

    @Override
    public String getName() {
        return "firebase/token/custom";
    }

    @Override
    public void call(JSONObject json, String uid) throws Exception {
        Misc.log("CustomToken", "is performing for uid:", uid);

        if(tokens.size() > MAXIMUM_CACHED_TOKENS) {
            Misc.log("CustomToken", "clears cache due to overflow");
            tokens.clear();
        }

        Calendar cal = Calendar.getInstance();
        Long now = cal.getTime().getTime();
        if(!tokens.containsKey(uid) || tokens.get(uid) == null || timeCreated < now - 30*60*1000) {
            if (Common.getInstance().getDataProcessor("v1").isServerMode()) {
                try {
                    Class tempClass = Class.forName("com.google.firebase.auth.FirebaseAuth");
                    //noinspection unchecked
                    Method method = tempClass.getDeclaredMethod("createCustomToken", String.class);
                    //noinspection unchecked
                    Task<String> taskCreateToken = (Task<String>) method.invoke(FirebaseAuth.getInstance(), uid);
                    Tasks.await(taskCreateToken);
                    tokens.put(uid, taskCreateToken.getResult());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                tokens.put(uid, String.valueOf(FirebaseAuth.getInstance().createCustomToken(uid)));
            }
            Misc.log("CustomToken", "generated", "[" + tokens.get(uid).length() + " byte(s)]");
            timeCreated = now;
        }
        json.put(STATUS, STATUS_SUCCESS);
        json.put(MESSAGE, tokens.get(uid));
    }

    public String fetchToken(String uid) {
        JSONObject json = new JSONObject();
        try {
            call(json, uid);
            return json.getString(MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
