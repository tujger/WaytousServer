package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates group based on given {@link GroupRequest}
 */
@SuppressWarnings("unused")
public class CreateGroup extends AbstractFirebaseAction<CreateGroup, GroupRequest> {

    public static final String TYPE = "/rest/firebase/create/group";

    private Runnable1<JSONObject> onSuccess;
    private Runnable1<JSONObject> onError;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(final JSONObject json, final GroupRequest group) {
        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        Misc.log("CreateGroup", "creating:", group.getId());

        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(group.getId())).addOnCompleteListener(dataSnapshot -> {
            if (dataSnapshot.getValue() == null) {
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(Firebase.OPTIONS + "/" + Firebase.WELCOME_MESSAGE, group.getWelcomeMessage());
                childUpdates.put(Firebase.OPTIONS + "/" + Firebase.REQUIRES_PASSWORD, group.isRequiresPassword());
                childUpdates.put(Firebase.OPTIONS + "/" + Firebase.TIME_TO_LIVE_IF_EMPTY, group.getTimeToLiveIfEmpty());
                childUpdates.put(Firebase.OPTIONS + "/" + Firebase.PERSISTENT, group.isPersistent());
                childUpdates.put(Firebase.OPTIONS + "/" + Firebase.DISMISS_INACTIVE, group.isDismissInactive());
                childUpdates.put(Firebase.OPTIONS + "/" + Firebase.DELAY_TO_DISMISS, group.getDelayToDismiss());
                childUpdates.put(Firebase.OPTIONS + "/" + Firebase.LIMIT_USERS, group.getLimitUsers());
                childUpdates.put(Firebase.OPTIONS + "/" + Firebase.CREATED, ServerValue.TIMESTAMP);
                childUpdates.put(Firebase.OPTIONS + "/" + Firebase.CHANGED, ServerValue.TIMESTAMP);

                ApiFuture<Void> task = refGroups.child(group.getId()).updateChildrenAsync(childUpdates);
                ApiFutures.addCallback(task, new ApiFutureCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        json.put(STATUS, STATUS_ERROR);
                        json.put(Rest.GROUP_ID, group.getId());
                        json.put(MESSAGE, "Group " + group.getId() + " already exists.");
                        Misc.err("CreateGroup", group.getId(), t.getMessage());
                        if (getOnError() != null) getOnError().call(json);
                        ((StatisticsGroup) getFireBus().getHolder(StatisticsGroup.TYPE))
                                .setAction(AbstractDataProcessor.Action.GROUP_REJECTED)
                                .setMessage(t.getMessage())
                                .call(null, group);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        json.put(STATUS, STATUS_SUCCESS);
                        json.put(Rest.GROUP_ID, group.getId());
                        getOnSuccess().call(json);
                        ((StatisticsGroup) getFireBus().getHolder(StatisticsGroup.TYPE))
                                .setAction(group.isPersistent() ? AbstractDataProcessor.Action.GROUP_CREATED_PERSISTENT : AbstractDataProcessor.Action.GROUP_CREATED_TEMPORARY)
                                .call(null, group);
                    }
                });

                /*refGroups.child(group.getId()).updateChildren(childUpdates);

                json.put(STATUS, STATUS_SUCCESS);
                json.put(Rest.GROUP_ID, group.getId());

                getOnSuccess().call(json);

                ((StatisticsGroup) getFireBus().getHolder(StatisticsGroup.TYPE))
                        .setAction(group.isPersistent() ? AbstractDataProcessor.GroupAction.GROUP_CREATED_PERSISTENT : AbstractDataProcessor.GroupAction.GROUP_CREATED_TEMPORARY)
                        .call(null, group);*/
            } else {
                json.put(STATUS, STATUS_ERROR);
                json.put(Rest.GROUP_ID, group.getId());
                json.put(MESSAGE, "Group " + group.getId() + " already exists.");
                Misc.err("CreateGroup", group.getId(), "not created, already exists");
                if (getOnError() != null) getOnError().call(json);
                ((StatisticsGroup) getFireBus().getHolder(StatisticsGroup.TYPE))
                        .setAction(AbstractDataProcessor.Action.GROUP_REJECTED)
                        .setMessage("already exists")
                        .call(null, group);
            }
        }).start();
    }

    public Runnable1<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public CreateGroup setOnSuccess(Runnable1<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Runnable1<JSONObject> getOnError() {
        return onError;
    }

    public CreateGroup setOnError(Runnable1<JSONObject> onError) {
        this.onError = onError;
        return this;
    }
}
