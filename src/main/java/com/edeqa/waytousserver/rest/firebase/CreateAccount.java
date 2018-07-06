package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.edeqa.waytous.Constants.REQUEST_MODEL;
import static com.edeqa.waytous.Constants.REQUEST_OS;
import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;
import static com.edeqa.waytousserver.servers.AbstractDataProcessor.Action.ACCOUNT_CREATED;

@SuppressWarnings("unused")
public class CreateAccount extends AbstractFirebaseAction<CreateAccount, MyUser> {

    public static final String TYPE = "/rest/firebase/create/account";

    private Runnable onSuccess;
    private Consumer<Throwable> onError;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(final JSONObject json, final MyUser user) {
        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        getOnSuccess().run();

        if(!user.isAccountAllowed()) {
            Misc.log("CreateAccount", "skipped for uid:", user.getUid(), "[" + user.getSignProvider() +"]");
//            getOnSuccess().run();
            return;
        }

        final DatabaseReference refAccounts = getFirebaseReference().child(Firebase.SECTION_USERS);
        TaskSingleValueEventFor createAccountTask = new TaskSingleValueEventFor<JSONObject>()
                .setRef(refAccounts.child(user.getUid()))
                .ifExists()
                .addOnSuccessListener(dataSnapshot -> {

                    Map<String, Object> accountPrivateData = new HashMap<>();

                    if (user.getName() != null && user.getName().length() > 0) {
                        accountPrivateData.put(Firebase.NAME, user.getName());
                    }
                    if (user.getSignProvider() != null) {
                        accountPrivateData.put(REQUEST_SIGN_PROVIDER, user.getSignProvider().toString());
                    }
                    accountPrivateData.put(Firebase.CHANGED, ServerValue.TIMESTAMP);

                    if (dataSnapshot == null) {
                        accountPrivateData.put(REQUEST_MODEL, user.getModel());
                        accountPrivateData.put(REQUEST_OS, user.getOs());
                        accountPrivateData.put(Firebase.CREATED, ServerValue.TIMESTAMP);
                        Misc.log("CreateAccount", "created for uid:", user.getUid(), accountPrivateData);

                        ((StatisticsAccount) getFireBus().getHolder(StatisticsAccount.TYPE))
                                .setAction(ACCOUNT_CREATED)
                                .call(null, user.getUid());
                    } else {
                        Misc.log("CreateAccount", "updating for uid:", user.getUid(), accountPrivateData);
                    }
                    refAccounts.child(user.getUid()).child(Firebase.PRIVATE).updateChildren(accountPrivateData, (error, ref) -> {
                        if(error == null) {

                        } else {
                            Misc.err("CreateAccount", "failed for uid:", user.getUid(), error.toException());
                            getOnError().accept(error.toException());
                        }
                    });
                })
                .addOnFailureListener(error -> {
                    Misc.err("CreateAccount", "failed for uid:", user.getUid(), error);
                    getOnError().accept(error);
                });
        createAccountTask.start();
    }

    public void clear() {
        setOnSuccess(null);

    }

    public Runnable getOnSuccess() {
        return onSuccess;
    }

    public CreateAccount setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Consumer<Throwable> getOnError() {
        return onError;
    }

    public CreateAccount setOnError(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

}
