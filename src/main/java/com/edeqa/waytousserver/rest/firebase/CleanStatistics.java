package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.Firebase;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class CleanStatistics extends AbstractFirebaseAction<CleanStatistics, Object> {

    public static final String TYPE = "/rest/firebase/clean/statistics";

    private Consumer<JSONObject> onSuccess;
    private Consumer<JSONObject> onError;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, Object object) {
        final JSONObject res = new JSONObject();

        getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_MESSAGES).setValue(null, (error, ref) -> {
            if(error != null) {
                res.put(STATUS, STATUS_ERROR);
                res.put(MESSAGE, error.getMessage());
                Misc.err("CleanStatistics", "failed:", error.getMessage());
                getOnError().accept(res);
            } else {
                res.put(STATUS, STATUS_SUCCESS);
                Misc.log("CleanStatistics", "done");
                getOnSuccess().accept(res);
            }
        });
    }

    public Consumer<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public CleanStatistics setOnSuccess(Consumer<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Consumer<JSONObject> getOnError() {
        return onError;
    }

    public CleanStatistics setOnError(Consumer<JSONObject> onError) {
        this.onError = onError;
        return this;
    }
}
