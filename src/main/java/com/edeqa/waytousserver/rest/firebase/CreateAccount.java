package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.edeqa.waytous.Constants.REQUEST_MODEL;
import static com.edeqa.waytous.Constants.REQUEST_OS;
import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;
import static com.edeqa.waytousserver.servers.AbstractDataProcessor.AccountAction.ACCOUNT_CREATED;

@SuppressWarnings("unused")
public class CreateAccount extends AbstractFirebaseAction<CreateAccount, MyUser> {

    public static final String TYPE = "/rest/firebase/create/account";

    private Runnable onSuccess;
    private Runnable1<Throwable> onError;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(final JSONObject json, final MyUser user) {
        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        if(!user.isAccountAllowed()) {
            Misc.log("CreateAccount", "skipped for uid:", user.getUid(), "[" + user.getSignProvider() +"]");
            getOnSuccess().run();
            return;
        }

        final DatabaseReference refAccounts = getFirebaseReference().child(Firebase.SECTION_USERS);

        TaskSingleValueEventFor createAccountTask = new TaskSingleValueEventFor<DataSnapshot>()
                .setRef(refAccounts.child(user.getUid()))
                .addOnSuccessListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Map<String, Object> accountPrivateData = new HashMap<>();

                        if (user.getName() != null && user.getName().length() > 0) {
                            accountPrivateData.put(Firebase.NAME, user.getName());
                        }
                        if (user.getSignProvider() != null) {
                            accountPrivateData.put(REQUEST_SIGN_PROVIDER, user.getSignProvider().toString());
                        }
                        accountPrivateData.put(Firebase.CHANGED, ServerValue.TIMESTAMP);

                        if (dataSnapshot.getValue() == null) {
                            accountPrivateData.put(REQUEST_MODEL, user.getModel());
                            accountPrivateData.put(REQUEST_OS, user.getOs());
                            accountPrivateData.put(Firebase.CREATED, ServerValue.TIMESTAMP);
                            Misc.log("CreateAccount", "created for uid:", user.getUid(), accountPrivateData);

                            ((StatisticsAccount) getFireBus().getHolder(StatisticsAccount.TYPE))
                                    .setAction(ACCOUNT_CREATED.toString())
                                    .call(null, user.getUid());
                        } else {
                            Misc.log("CreateAccount", "updated for uid:", user.getUid(), accountPrivateData);
                        }
                        final Task<Void> updateAccountTask = refAccounts.child(user.getUid()).child(Firebase.PRIVATE).updateChildren(accountPrivateData);

                        try {
                            Tasks.await(updateAccountTask);
//                            Misc.log(LOG, "createOrUpdateAccount:accountDone:" + user.getUid());
                            getOnSuccess().run();
                        } catch (Exception e) {
                            Misc.err("CreateAccount", "failed for uid:", user.getUid(), e);
                            getOnError().call(e);
                        }
                    }
                })
                .addOnFailureListener(new Runnable1<Throwable>() {
                    @Override
                    public void call(Throwable error) {
                        Misc.err("CreateAccount", "failed for uid:", user.getUid(), error);
                        getOnError().call(error);
                    }
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

    public Runnable1<Throwable> getOnError() {
        return onError;
    }

    public CreateAccount setOnError(Runnable1<Throwable> onError) {
        this.onError = onError;
        return this;
    }

}