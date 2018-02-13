package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("unused")
public class GroupProperty extends AbstractFirebaseAction<GroupProperty, Object> {

    public static final String TYPE = "/rest/firebase/group/property";

    private String groupId;
    private String key;
    private Serializable value;
    private Runnable1<JSONObject> onSuccess;
    private Runnable1<JSONObject> onError;
    private boolean switchBoolean;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, Object request) {

        final JSONObject res = new JSONObject();
        res.put(Rest.PROPERTY, getKey());
        res.put(Rest.VALUE, getValue());

        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.put(STATUS, STATUS_ERROR);
                res.put(MESSAGE, e.getMessage());
                Misc.log("GroupProperty", "'" + getKey() + "'", "for group", getGroupId(), "not modified, error:", e.getMessage());
                getOnError().call(res);
            }
        };

        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(getGroupId()).child(Firebase.OPTIONS).child(getKey()))
                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Serializable oldValue = (Serializable) dataSnapshot.getValue();
                        if ((oldValue != null && getValue() != null)
                                || isSwitchBoolean()) {
                            res.put(Rest.OLD_VALUE, oldValue);

                            if(isSwitchBoolean()) {
                                if(oldValue == null) oldValue = false;
                                setValue(!(Boolean)oldValue);
                            }

                            ApiFuture<Void> task = refGroups.child(getGroupId()).child(Firebase.OPTIONS).child(getKey()).setValueAsync(getValue());
                            try {
                                task.get();
                                res.put(STATUS, STATUS_SUCCESS);
                                getOnSuccess().call(res);
                            } catch (Exception e) {
                                onFailureListener.onFailure(e);
                            }
                        } else {
                            onFailureListener.onFailure(new Exception("value not defined"));
                        }
                    }
                }).start();


    }

    public String getGroupId() {
        return groupId;
    }

    public GroupProperty setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public Runnable1<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public GroupProperty setOnSuccess(Runnable1<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Runnable1<JSONObject> getOnError() {
        return onError;
    }

    public GroupProperty setOnError(Runnable1<JSONObject> onError) {
        this.onError = onError;
        return this;
    }

    public String getKey() {
        return key;
    }

    public GroupProperty setKey(String key) {
        this.key = key;
        return this;
    }

    public Serializable getValue() {
        return value;
    }

    public GroupProperty setValue(Serializable value) {
        this.value = value;
        return this;
    }

    public boolean isSwitchBoolean() {
        return switchBoolean;
    }

    public GroupProperty performSwitchBoolean() {
        this.switchBoolean = true;
        return this;
    }

}
