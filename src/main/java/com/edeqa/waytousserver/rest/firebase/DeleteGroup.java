package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

@SuppressWarnings("unused")
public class DeleteGroup extends AbstractFirebaseAction<DeleteGroup, String> {

    public static final String TYPE = "/rest/firebase/delete/group";

    private Runnable1<JSONObject> onSuccess;
    private Runnable1<JSONObject> onError;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final String groupId) {
        json = new JSONObject();

        json.put(Rest.GROUP_ID, groupId);

        Task<Void> deleteGroupTask = getFirebaseReference().child(Firebase.SECTION_GROUPS).child(groupId).removeValue();
        try {
            Tasks.await(deleteGroupTask);
            json.put(STATUS, STATUS_SUCCESS);
            Misc.log("DeleteGroup", groupId);
            getOnSuccess().call(json);

            ((StatisticsGroup) EventBus.getOrCreateEventBus().getHolder(StatisticsGroup.TYPE))
                    .setAction(AbstractDataProcessor.GroupAction.GROUP_DELETED)
                    .call(null, new GroupRequest(groupId));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            json.put(STATUS, STATUS_ERROR);
            json.put(MESSAGE, e.getMessage());
            Misc.err("DeleteGroup", groupId, "not deleted, error:" + e.getMessage());
            getOnError().call(json);
        }
    }

    public Runnable1<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public DeleteGroup setOnSuccess(Runnable1<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Runnable1<JSONObject> getOnError() {
        return onError;
    }

    public DeleteGroup setOnError(Runnable1<JSONObject> onError) {
        this.onError = onError;
        return this;
    }

}
