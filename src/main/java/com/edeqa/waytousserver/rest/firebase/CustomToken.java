package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.google.api.core.ApiFuture;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
/*
 * This class requests and returns custom token for Firebase. Depending on current installation type
 * it defines the properly request and performs it. Installation type can be defined in gradle.build.
 */
public class CustomToken extends AbstractFirebaseAction<CustomToken, String> {

    public static final String TYPE = "/rest/firebase/token/custom";

    private static final int MAXIMUM_CACHED_TOKENS = 1000;

    private Long timeCreated = 0L;
    private Map<String,Map<String,Serializable>> tokens = new HashMap<>();

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, String uid) {
        Misc.log("CustomToken", "is performing for uid:", uid);

        if(tokens.size() > MAXIMUM_CACHED_TOKENS) {
            Misc.log("CustomToken", "clears cache due to overflow");
            tokens.clear();
        }

        Calendar cal = Calendar.getInstance();
        Long now = cal.getTime().getTime();
        if(!tokens.containsKey(uid) || tokens.get(uid) == null || ((long)tokens.get(uid).get("timestamp")) < now - 10*60*1000) {
            if (Common.getInstance().getDataProcessor().isServerMode()) {
                try {
                    Class tempClass = Class.forName("com.google.firebase.auth.FirebaseAuth");
                    //noinspection unchecked
//                    Method method = tempClass.getDeclaredMethod("createCustomToken", String.class);
                    //noinspection unchecked
//                    Task<String> taskCreateToken = (Task<String>) method.invoke(FirebaseAuth.getInstance(), uid);
//                    Tasks.await(taskCreateToken);

                    //noinspection unchecked
                    Method method2 = tempClass.getDeclaredMethod("createCustomTokenAsync", String.class);
                    //noinspection unchecked
                    ApiFuture<String> api = (ApiFuture<String>) method2.invoke(FirebaseAuth.getInstance(), uid);
                    HashMap<String, Serializable> token = new HashMap<>();
                    token.put("token", api.get() /*taskCreateToken.getResult()*/);
                    token.put("timestamp", now);
                    tokens.put(uid, token);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                HashMap<String, Serializable> token = new HashMap<>();
                try {
                    token.put("token", String.valueOf(FirebaseAuth.getInstance().createCustomToken(uid)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                token.put("timestamp", now);
                tokens.put(uid, token);
            }
            Misc.log("CustomToken", "generated", "[" + ((String)tokens.get(uid).get("token")).length() + " byte(s)]");
        }
        json.put(STATUS, STATUS_SUCCESS);
        json.put(MESSAGE, tokens.get(uid).get("token"));
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
