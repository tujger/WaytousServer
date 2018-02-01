package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class CreateGroup extends AbstractAction<CreateGroup, MyGroup> {

    private Runnable1<JSONObject> onSuccess;
    private Runnable1<JSONObject> onError;
    private StatisticsGroup statisticsGroup;

    @Override
    public String getName() {
        return "firebase/create/group";
    }

    @Override
    public void call(final JSONObject json, final MyGroup group) {
        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        Misc.log("CreateGroup", "creating:", group.getId());

        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(group.getId()))
                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.WELCOME_MESSAGE, group.getWelcomeMessage());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.REQUIRES_PASSWORD, group.isRequirePassword());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.TIME_TO_LIVE_IF_EMPTY, group.getTimeToLiveIfEmpty());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.PERSISTENT, group.isPersistent());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.DISMISS_INACTIVE, group.isDismissInactive());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.DELAY_TO_DISMISS, group.getDelayToDismiss());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.CREATED, ServerValue.TIMESTAMP);
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.CHANGED, ServerValue.TIMESTAMP);
                            refGroups.child(group.getId()).updateChildren(childUpdates);
//                            refGroups.child(Firebase.SECTION_GROUPS).child(group.getId()).setValue(0);

                            json.put(STATUS, STATUS_SUCCESS);
                            json.put(Rest.GROUP_ID, group.getId());

                            Misc.log("CreateGroup", group.getId(), "created");

                            getOnSuccess().call(json);

                            getStatisticsGroup().setGroupId(group.getId()).setPersistentGroup(group.isPersistent()).setAction(group.isPersistent() ? AbstractDataProcessor.GroupAction.GROUP_CREATED_PERSISTENT : AbstractDataProcessor.GroupAction.GROUP_CREATED_TEMPORARY).call(null, null);
                        } else {
                            json.put(STATUS, STATUS_ERROR);
                            json.put(Rest.GROUP_ID, group.getId());
                            json.put(MESSAGE, "Group " + group.getId() + " already exists.");
                            Misc.err("CreateGroup", group.getId(), "not created, already exists");
                            if (getOnError() != null) getOnError().call(json);
                            getStatisticsGroup().setGroupId(group.getId()).setPersistentGroup(group.isPersistent()).setAction(AbstractDataProcessor.GroupAction.GROUP_REJECTED).setMessage("already exists").call(null, null);
                        }
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

    public StatisticsGroup getStatisticsGroup() {
        return statisticsGroup;
    }

    public CreateGroup setStatisticsGroup(StatisticsGroup statisticsGroup) {
        this.statisticsGroup = statisticsGroup;
        return this;
    }
}
