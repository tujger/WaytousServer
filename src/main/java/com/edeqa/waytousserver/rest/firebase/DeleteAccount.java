package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

@SuppressWarnings("unused")
public class DeleteAccount extends AbstractAction<DeleteAccount, String> {

    private Runnable1<JSONObject> onSuccess;
    private Runnable1<JSONObject> onError;
    private StatisticsAccount statisticsAccount;

    @Override
    public String getName() {
        return "firebase/delete/account";
    }

    @Override
    public void call(JSONObject json, final String accountId) {
        json = new JSONObject();
        json.put(Rest.UID, accountId);

        Task<Void> deleteAccountTask = getFirebaseReference().child(Firebase.SECTION_USERS).child(accountId).removeValue();
        try {
            Tasks.await(deleteAccountTask);

            json.put(STATUS, STATUS_SUCCESS);
            Misc.log("DeleteAccount", accountId, "deleted");
            getOnSuccess().call(json);

            getStatisticsAccount()
                    .setAccountId(accountId)
                    .setAction(AbstractDataProcessor.AccountAction.ACCOUNT_DELETED.toString())
                    .setMessage(accountId + " deleted.")
                    .call(null, null);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            json.put(STATUS, STATUS_ERROR);
            json.put(MESSAGE, e.getMessage());
            Misc.err("DeleteAccount", accountId, "not deleted, error:" + e.getMessage());
            getOnError().call(json);
        }
    }

    public Runnable1<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public DeleteAccount setOnSuccess(Runnable1<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Runnable1<JSONObject> getOnError() {
        return onError;
    }

    public DeleteAccount setOnError(Runnable1<JSONObject> onError) {
        this.onError = onError;
        return this;
    }

    public StatisticsAccount getStatisticsAccount() {
        return statisticsAccount;
    }

    public DeleteAccount setStatisticsAccount(StatisticsAccount statisticsAccount) {
        this.statisticsAccount = statisticsAccount;
        return this;
    }
}
