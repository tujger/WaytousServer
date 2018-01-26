package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Transaction;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class StatisticsAccounts implements NamedCall {

    private DatabaseReference firebaseStat;
    private DatabaseReference firebaseAccounts;
    private String accountAction;
    private String firebaseAccessToken;
    private String accountId;
    private String errorMessage;
    private Boolean persistent;
    private StatisticsMessage statisticsMessage;
    private String key;
    private Object value;
    private Transaction.Handler incrementValue;

    @Override
    public String getName() {
        return "statistics/account";
    }

    @Override
    public void call(JSONObject json, Object request) {

        DatabaseReference referenceTotal;
        DatabaseReference referenceToday;
        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));

        referenceTotal = getFirebaseStat().child(Firebase.STAT_TOTAL);
        referenceToday = getFirebaseStat().child(Firebase.STAT_BY_DATE).child(today);

        switch(getAccountAction()) {
            case Firebase.STAT_ACCOUNTS_CREATED:
                referenceTotal = referenceTotal.child(Firebase.STAT_ACCOUNTS_CREATED);
                referenceToday = referenceToday.child(Firebase.STAT_ACCOUNTS_CREATED);
                referenceToday.runTransaction(incrementValue);
                referenceTotal.runTransaction(incrementValue);
                break;
            case Firebase.STAT_ACCOUNTS_DELETED:
                referenceTotal = referenceTotal.child(Firebase.STAT_ACCOUNTS_DELETED);
                referenceToday = referenceToday.child(Firebase.STAT_ACCOUNTS_DELETED);
                referenceToday.runTransaction(incrementValue);
                referenceTotal.runTransaction(incrementValue);
                break;
        }

        if(getErrorMessage() != null && getErrorMessage().length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("account", getAccountId());
            map.put("action", getAccountAction());
            getStatisticsMessage().setMessage(getErrorMessage()).call(null, map);
        }

        if(getKey() != null && accountId != null && accountId.length() > 0) {
            new TaskSingleValueEventFor<JSONObject>(getFirebaseAccounts().child(accountId)).setFirebaseRest(getFirebaseAccessToken()).addOnCompleteListener(new Runnable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    if(json.has(Firebase.PRIVATE) && json.getBoolean(Firebase.PRIVATE)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put(Firebase.TIMESTAMP, new Date().getTime());
                        map.put(Firebase.KEYS, getKey());
                        if (getAccountAction() != null) map.put(Firebase.MODE, getAccountAction());

                        if (getValue() instanceof Boolean) {
                            map.put(Firebase.VALUE, getValue());
                        } else if (getValue() instanceof Number) {
                            map.put(Firebase.VALUE, getValue());
                        } else if (getValue() instanceof String) {
                            if (((String) getValue()).length() < 50) {
                                map.put(Firebase.VALUE, getValue());
                            } else {
                                map.put(Firebase.VALUE, ((String) getValue()).substring(0, 40) + "...");
                            }
                        } else if (getValue() instanceof ArrayList) {
                            map.put(Firebase.VALUE, "Array(" + ((ArrayList) getValue()).size() + ")");
                        } else if (getValue() != null) {
                            map.put(Firebase.VALUE, "[" + getValue().getClass().getSimpleName() + "]");
                        }
                        getFirebaseAccounts().child(accountId).child(Firebase.PRIVATE).child(Firebase.HISTORY).push().setValue(map);
                        Misc.log("StatisticsAccounts", "putStaticticsAccount:", accountId, "action:", getAccountAction());
                    }
                }
            }).start();

        }
//        json.put(STATUS, STATUS_SUCCESS);
    }

    public DatabaseReference getFirebaseStat() {
        return firebaseStat;
    }

    public StatisticsAccounts setFirebaseStat(DatabaseReference firebaseStat) {
        this.firebaseStat = firebaseStat;
        return this;
    }

    public String getFirebaseAccessToken() {
        return firebaseAccessToken;
    }

    public StatisticsAccounts setFirebaseAccessToken(String firebaseAccessToken) {
        this.firebaseAccessToken = firebaseAccessToken;
        return this;
    }

    public String getAccountAction() {
        return accountAction;
    }

    public String getAccountId() {
        return accountId;
    }

    public StatisticsAccounts setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public StatisticsAccounts setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    public StatisticsAccounts setPersistent(Boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public StatisticsMessage getStatisticsMessage() {
        return statisticsMessage;
    }

    public StatisticsAccounts setStatisticsMessage(StatisticsMessage statisticsMessage) {
        this.statisticsMessage = statisticsMessage;
        return this;
    }

    public DatabaseReference getFirebaseAccounts() {
        return firebaseAccounts;
    }

    public StatisticsAccounts setFirebaseAccounts(DatabaseReference firebaseAccounts) {
        this.firebaseAccounts = firebaseAccounts;
        return this;
    }

    public StatisticsAccounts setAccountAction(String accountAction) {
        this.accountAction = accountAction;
        return this;
    }

    public String getKey() {
        return key;
    }

    public StatisticsAccounts setKey(String key) {
        this.key = key;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public StatisticsAccounts setValue(Object value) {
        this.value = value;
        return this;
    }

    public Transaction.Handler getIncrementValue() {
        return incrementValue;
    }

    public StatisticsAccounts setIncrementValue(Transaction.Handler incrementValue) {
        this.incrementValue = incrementValue;
        return this;
    }
}
