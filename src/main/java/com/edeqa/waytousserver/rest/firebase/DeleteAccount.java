package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class DeleteAccount extends AbstractFirebaseAction<DeleteAccount, String> {

    public static final String TYPE = "/rest/firebase/delete/account";

    private Consumer<JSONObject> onSuccess;
    private Consumer<JSONObject> onError;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final String accountId) {
        json = new JSONObject();
        json.put(Rest.UID, accountId);

        JSONObject finalJson = json;
        getFirebaseReference().child(Firebase.SECTION_USERS).child(accountId).removeValue((error, ref) -> {
            if(error == null) {
                finalJson.put(STATUS, STATUS_SUCCESS);
                Misc.log("DeleteAccount", accountId, "deleted");
                getOnSuccess().accept(finalJson);

                ((StatisticsAccount) getFireBus().getHolder(StatisticsAccount.TYPE))
                        .setAction(AbstractDataProcessor.Action.ACCOUNT_DELETED)
                        .setMessage(accountId + " deleted.")
                        .call(null, accountId);
            } else {
                error.toException().printStackTrace();
                finalJson.put(STATUS, STATUS_ERROR);
                finalJson.put(MESSAGE, error.toException().getMessage());
                Misc.err("DeleteAccount", accountId, "not deleted, error:" + error.toException().getMessage());
                getOnError().accept(finalJson);
            }
        });
    }

    public Consumer<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public DeleteAccount setOnSuccess(Consumer<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Consumer<JSONObject> getOnError() {
        return onError;
    }

    public DeleteAccount setOnError(Consumer<JSONObject> onError) {
        this.onError = onError;
        return this;
    }

}
