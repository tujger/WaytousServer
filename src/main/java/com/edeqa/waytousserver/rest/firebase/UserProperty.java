package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.Serializable;

@SuppressWarnings("unused")
public class UserProperty extends AbstractFirebaseAction<UserProperty, Object> {

    public static final String TYPE = "/rest/firebase/user/property";

    private String groupId;
    private Long userNumber;
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

        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        final JSONObject res = new JSONObject();
        res.put(Rest.PROPERTY, getKey());

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.put(STATUS, STATUS_ERROR);
                res.put(MESSAGE, e.getMessage());
                Misc.log("UserProperty", "'" + getKey() + "'", "for user", getUserNumber(), "in group", getGroupId(), "not modified, error:", e.getMessage());
                getOnError().call(res);
            }
        };

        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(getGroupId()).child(Firebase.USERS).child(Firebase.PUBLIC).child(String.valueOf(getUserNumber())).child(getKey()))
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

                            refGroups.child(getGroupId()).child(Firebase.USERS).child(Firebase.PUBLIC).child(String.valueOf(getUserNumber())).child(getKey()).setValue(getValue()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    res.put(STATUS, STATUS_SUCCESS);
                                    getOnSuccess().call(res);
                                }
                            }).addOnFailureListener(onFailureListener);
                        } else {
                            onFailureListener.onFailure(new Exception("Invalid property."));
                        }
                    }
                }).start();
    }

    public String getGroupId() {
        return groupId;
    }

    public UserProperty setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public Runnable1<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public UserProperty setOnSuccess(Runnable1<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Runnable1<JSONObject> getOnError() {
        return onError;
    }

    public UserProperty setOnError(Runnable1<JSONObject> onError) {
        this.onError = onError;
        return this;
    }

    public String getKey() {
        return key;
    }

    public UserProperty setKey(String key) {
        this.key = key;
        return this;
    }

    public Serializable getValue() {
        return value;
    }

    public UserProperty setValue(Serializable value) {
        this.value = value;
        return this;
    }

    public boolean isSwitchBoolean() {
        return switchBoolean;
    }

    public UserProperty performSwitchBoolean() {
        this.switchBoolean = true;
        return this;
    }

    public Long getUserNumber() {
        return userNumber;
    }

    public UserProperty setUserNumber(Long userNumber) {
        this.userNumber = userNumber;
        return this;
    }
}
