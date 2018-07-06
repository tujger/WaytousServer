package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class DeleteGroup extends AbstractFirebaseAction<DeleteGroup, String> {

    public static final String TYPE = "/rest/firebase/delete/group";

    private Consumer<JSONObject> onSuccess;
    private Consumer<JSONObject> onError;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final String groupId) {
        json = new JSONObject();

        json.put(Rest.GROUP_ID, groupId);
        JSONObject finalJson = json;
        getFirebaseReference().child(Firebase.SECTION_GROUPS).child(groupId).removeValue((error, ref) -> {
            if(error == null) {
                finalJson.put(STATUS, STATUS_SUCCESS);
                Misc.log("DeleteGroup", groupId);
                getOnSuccess().accept(finalJson);

                ((StatisticsGroup) getFireBus().getHolder(StatisticsGroup.TYPE))
                        .setAction(AbstractDataProcessor.Action.GROUP_DELETED)
                        .call(null, new GroupRequest(groupId));

            } else {
                error.toException().printStackTrace();
                finalJson.put(STATUS, STATUS_ERROR);
                finalJson.put(MESSAGE, error.toException().getMessage());
                Misc.err("DeleteGroup", groupId, "not deleted, error:" + error.toException().getMessage());
                getOnError().accept(finalJson);

            }
        });
    }

    public Consumer<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public DeleteGroup setOnSuccess(Consumer<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Consumer<JSONObject> getOnError() {
        return onError;
    }

    public DeleteGroup setOnError(Consumer<JSONObject> onError) {
        this.onError = onError;
        return this;
    }

}
