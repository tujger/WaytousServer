package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.google.api.core.ApiFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.tasks.OnFailureListener;

import org.json.JSONObject;

import java.io.Serializable;

@SuppressWarnings("unused")
public class GroupOption extends AbstractFirebaseAction<GroupOption, Object> {

    public static final String TYPE = "/rest/firebase/group/option";

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

        final OnFailureListener onFailureListener = error -> {
            res.put(STATUS, STATUS_ERROR);
            res.put(MESSAGE, error.getMessage());
            Misc.log("GroupOption", "'" + getKey() + "'", "for group", getGroupId(), "not modified, error:", error.getMessage());
            getOnError().call(res);
        };

        if(Firebase.DELAY_TO_DISMISS.equals(getKey())
            || Firebase.DISMISS_INACTIVE.equals(getKey())
            || Firebase.LIMIT_USERS.equals(getKey())
            || Firebase.PERSISTENT.equals(getKey())
            || Firebase.REQUIRES_PASSWORD.equals(getKey())
            || Firebase.TIME_TO_LIVE_IF_EMPTY.equals(getKey())
            || Firebase.WELCOME_MESSAGE.equals(getKey())) {
            new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(getGroupId()).child(Firebase.OPTIONS).child(getKey()))
                    .addOnCompleteListener(dataSnapshot -> {
                        Serializable oldValue = (Serializable) dataSnapshot.getValue();

//                            if ((oldValue != null && getValue() != null)
//                                    || isSwitchBoolean()) {
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
//                            } else {
//                                onFailureListener.onFailure(new Exception("value not defined"));
//                            }
                    }).start();
        } else {
            onFailureListener.onFailure(new Exception("Incorrect option name."));
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public GroupOption setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public Runnable1<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public GroupOption setOnSuccess(Runnable1<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Runnable1<JSONObject> getOnError() {
        return onError;
    }

    public GroupOption setOnError(Runnable1<JSONObject> onError) {
        this.onError = onError;
        return this;
    }

    public String getKey() {
        return key;
    }

    public GroupOption setKey(String key) {
        this.key = key;
        return this;
    }

    public Serializable getValue() {
        return value;
    }

    public GroupOption setValue(Serializable value) {
        this.value = value;
        return this;
    }

    public boolean isSwitchBoolean() {
        return switchBoolean;
    }

    public GroupOption switchBoolean() {
        this.switchBoolean = true;
        return this;
    }

}
