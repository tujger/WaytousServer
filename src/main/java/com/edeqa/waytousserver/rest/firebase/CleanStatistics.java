package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class CleanStatistics extends AbstractAction<CleanStatistics, Object> {

    private Runnable1<JSONObject> onSuccess;
    private Runnable1<JSONObject> onError;

    @Override
    public String getName() {
        return "firebase/clean/statistics";
    }

    @Override
    public void call(JSONObject json, Object object) {
        final JSONObject res = new JSONObject();

        getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_MESSAGES).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                res.put(STATUS, STATUS_SUCCESS);
                Misc.log("CleanStatistics", "done");
                getOnSuccess().call(res);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.put(STATUS, STATUS_ERROR);
                res.put(MESSAGE, e.getMessage());
                Misc.err("CleanStatistics", "failed:", e.getMessage());
                getOnError().call(res);
            }
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
