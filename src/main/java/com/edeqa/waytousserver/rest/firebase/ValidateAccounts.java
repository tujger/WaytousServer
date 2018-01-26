package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;

@SuppressWarnings("unused")
public class ValidateAccounts implements NamedCall {

    private DatabaseReference firebaseStat;
    private DatabaseReference firebaseAccounts;
    private String firebaseAccessToken;
    private StatisticsAccounts statisticsAccounts;

    @Override
    public String getName() {
        return "validate/accounts";
    }

    @Override
    public void call(JSONObject json, Object request) {

        getFirebaseStat().child(Firebase.STAT_MISC).child(Firebase.STAT_MISC_ACCOUNTS_CLEANED).setValue(ServerValue.TIMESTAMP);

        Misc.log("ValidateAccounts", "Accounts validation is performing, checking online users");

        new TaskSingleValueEventFor<JSONObject>(getFirebaseAccounts()).setFirebaseRest(getFirebaseAccessToken()).addOnCompleteListener(new Runnable1<JSONObject>() {
            @Override
            public void call(JSONObject accounts) {
                try {
                    Iterator<String> iter = accounts.keys();
                    while (iter.hasNext()) {
                        final String uid = iter.next();

                        new TaskSingleValueEventFor<DataSnapshot>(getFirebaseAccounts().child(uid).child(Firebase.PRIVATE))
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        try {
                                            Map value = (Map) dataSnapshot.getValue();
                                            boolean expired = false;
                                            boolean trusted = false;
                                            if (value.containsKey(REQUEST_SIGN_PROVIDER) && !"anonymous".equals(value.get(REQUEST_SIGN_PROVIDER))) {
                                                trusted = true;
                                            }

                                            if (value.containsKey(Firebase.CHANGED)) {
                                                if ((new Date().getTime() - (long) value.get(Firebase.CHANGED)) > 30 * 24 * 60 * 60 * 1000L) {
                                                    expired = true;
                                                }
                                            } else {
                                                expired = true;
                                            }

                                            if (!trusted && expired) {
                                                String message = Misc.durationToString(new Date().getTime() - (long) value.get(Firebase.CHANGED));
                                                Misc.log("ValidateAccounts", "--- removing account: " + uid, "expired for: " +message);

                                                getFirebaseAccounts().child(uid).setValue(null);
                                                getStatisticsAccounts().setAccountId(uid).setAccountAction(AbstractDataProcessor.AccountAction.ACCOUNT_DELETED.toString()).setKey(null).setValue(null).setErrorMessage("Expired for " + message).call(null, null);
                                            }
                                        } catch(Exception e) {
                                            Misc.err("ValidateAccounts", "validateAccounts:failed:", uid, e.getMessage());
                                        }
                                    }
                                }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
//        json.put(STATUS, STATUS_SUCCESS);
    }

    public DatabaseReference getFirebaseStat() {
        return firebaseStat;
    }

    public ValidateAccounts setFirebaseStat(DatabaseReference firebaseStat) {
        this.firebaseStat = firebaseStat;
        return this;
    }

    public String getFirebaseAccessToken() {
        return firebaseAccessToken;
    }

    public ValidateAccounts setFirebaseAccessToken(String firebaseAccessToken) {
        this.firebaseAccessToken = firebaseAccessToken;
        return this;
    }

    public StatisticsAccounts getStatisticsAccounts() {
        return statisticsAccounts;
    }

    public ValidateAccounts setStatisticsAccounts(StatisticsAccounts statisticsAccounts) {
        this.statisticsAccounts = statisticsAccounts;
        return this;
    }

    public DatabaseReference getFirebaseAccounts() {
        return firebaseAccounts;
    }

    public ValidateAccounts setFirebaseAccounts(DatabaseReference firebaseAccounts) {
        this.firebaseAccounts = firebaseAccounts;
        return this;
    }
}
