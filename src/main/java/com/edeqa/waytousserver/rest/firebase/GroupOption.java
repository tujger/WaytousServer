package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.io.Serializable;

@SuppressWarnings("unused")
public class GroupOption extends AbstractFirebaseAction<GroupOption, Object> {

    public static final String TYPE = "/rest/firebase/group/option";

    private String groupId;
    private String key;
    private Serializable value;
    private Consumer<JSONObject> onSuccess;
    private Consumer<JSONObject> onError;
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

        final Consumer<Throwable> onFailureListener = error -> {
            res.put(STATUS, STATUS_ERROR);
            res.put(MESSAGE, error.getMessage());
            Misc.log("GroupOption", "'" + getKey() + "'", "for group", getGroupId(), "not modified, error:", error.getMessage());
            getOnError().accept(res);
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
                        refGroups.child(getGroupId()).child(Firebase.OPTIONS).child(getKey()).setValue(getValue(), (error, ref) -> {
                            if(error == null) {
                                res.put(STATUS, STATUS_SUCCESS);
                                getOnSuccess().accept(res);
                            } else {
                                onFailureListener.accept(error.toException());
                            }
                        });

//                            } else {
//                                onFailureListener.onFailure(new Exception("value not defined"));
//                            }
                    }).start();
        } else {
            onFailureListener.accept(new Exception("Incorrect option name."));
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public GroupOption setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public Consumer<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public GroupOption setOnSuccess(Consumer<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Consumer<JSONObject> getOnError() {
        return onError;
    }

    public GroupOption setOnError(Consumer<JSONObject> onError) {
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
