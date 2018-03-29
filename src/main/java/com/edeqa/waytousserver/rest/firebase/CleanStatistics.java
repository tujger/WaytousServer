package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class CleanStatistics extends AbstractFirebaseAction<CleanStatistics, Object> {

    public static final String TYPE = "/rest/firebase/clean/statistics";

    private Runnable1<JSONObject> onSuccess;
    private Runnable1<JSONObject> onError;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, Object object) {
        final JSONObject res = new JSONObject();

        getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_MESSAGES).setValue(null).addOnSuccessListener(result -> {
            res.put(STATUS, STATUS_SUCCESS);
            Misc.log("CleanStatistics", "done");
            getOnSuccess().call(res);
        }).addOnFailureListener(error -> {
            res.put(STATUS, STATUS_ERROR);
            res.put(MESSAGE, error.getMessage());
            Misc.err("CleanStatistics", "failed:", error.getMessage());
            getOnError().call(res);
        });
    }

    public Runnable1<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public CleanStatistics setOnSuccess(Runnable1<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Runnable1<JSONObject> getOnError() {
        return onError;
    }

    public CleanStatistics setOnError(Runnable1<JSONObject> onError) {
        this.onError = onError;
        return this;
    }
}
