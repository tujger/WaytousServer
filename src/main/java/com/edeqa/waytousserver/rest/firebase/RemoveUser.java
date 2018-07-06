package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.rest.tracking.AbstractTrackingAction;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.edeqa.waytous.Constants.REQUEST_UID;

@SuppressWarnings("unused")
public class RemoveUser extends AbstractFirebaseAction<RemoveUser, Object> {

    public static final String TYPE = "/rest/firebase/remove/user";

    private Consumer<JSONObject> onSuccess;
    private Consumer<JSONObject> onError;
    private String groupId;
    private Long userNumber;
    private String action;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(final JSONObject json, Object object) {

        final JSONObject res = new JSONObject();

        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        res.put(Rest.GROUP_ID, getGroupId());
        res.put(Rest.USER_NUMBER, getUserNumber());

        final Consumer<Throwable> onFailureListener = error -> {
            res.put(STATUS, STATUS_ERROR);
            res.put(MESSAGE, error.getMessage());
            Misc.log("RemoveUser", getUserNumber(), "from group", getGroupId(), "failed:", error.getMessage());
            getOnError().accept(res);
        };

        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(getGroupId()).child(Firebase.USERS).child(Firebase.PRIVATE).child(String.valueOf(getUserNumber())).child(REQUEST_UID))
                .addOnCompleteListener(dataSnapshot -> {

                    final Object value = dataSnapshot.getValue();

                    if (value != null && value instanceof String) {
                        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(getGroupId()).child(Firebase.USERS).child(Firebase.QUEUE))
                                .addOnCompleteListener(dataQueue -> {
                                    for(DataSnapshot x: dataQueue.getChildren()) {
                                        if(x.getValue().equals(value)) {
                                            Map<String,Object> updates = new HashMap<>();
                                            updates.put(Firebase.USERS + "/" + Firebase.PUBLIC + "/" + getUserNumber(), null);
                                            updates.put(Firebase.USERS + "/" + Firebase.QUEUE + "/" + x.getKey(), "removed:" + x.getValue());
                                            updates.put(Firebase.USERS + "/" + Firebase.PRIVATE + "/" + getUserNumber(), null);
                                            updates.put(Firebase.USERS + "/" + Firebase.KEYS + "/" + value.toString(), null);

                                            for (Map.Entry<String,AbstractTrackingAction> entry : getTrackingBus().getHolders().entrySet()) {
                                                if (entry.getValue().isSaveable()) {
                                                    updates.put(Firebase.PUBLIC + "/" + entry.getKey() + "/" + getUserNumber(), null);
                                                }
                                            }
                                            refGroups.child(getGroupId()).updateChildren(updates, (error, ref) -> {
                                                if(error == null) {
                                                    res.put(STATUS, STATUS_SUCCESS);
                                                    Misc.log("RemoveUser", getUserNumber(), "[" + value.toString() + "]", "removed from group", getGroupId());
                                                    getOnSuccess().accept(res);
                                                    ((StatisticsUser) getFireBus().getHolder(StatisticsUser.TYPE))
                                                            .setGroupId(getGroupId())
                                                            .setAction(AbstractDataProcessor.Action.USER_REMOVED)
                                                            .call(null, value.toString());
                                                } else {
                                                    onFailureListener.accept(error.toException());
                                                }
                                            });
                                        }
                                    }
                                }).start();
                    } else {
                        onFailureListener.accept(new Exception("User not found."));
                    }
                }).start();
    }

    public Consumer<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public RemoveUser setOnSuccess(Consumer<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Consumer<JSONObject> getOnError() {
        return onError;
    }

    public RemoveUser setOnError(Consumer<JSONObject> onError) {
        this.onError = onError;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public RemoveUser setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public Long getUserNumber() {
        return userNumber;
    }

    public RemoveUser setUserNumber(Long userNumber) {
        this.userNumber = userNumber;
        return this;
    }

    public String getAction() {
        return action;
    }

    public RemoveUser setAction(String action) {
        this.action = action;
        return this;
    }
}
