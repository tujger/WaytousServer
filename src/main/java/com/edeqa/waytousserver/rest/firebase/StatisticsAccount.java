package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Transaction;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class StatisticsAccount extends AbstractAction<StatisticsAccount, Object> {

    private String accountAction;
    private AccessToken firebaseAccessToken;
    private String accountId;
    private String message;
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

        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));

        final DatabaseReference refAccounts = getFirebaseReference().child(Firebase.SECTION_USERS);

        DatabaseReference refTotal = getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_TOTAL);
        DatabaseReference refToday = getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_BY_DATE).child(today);

        switch(getAccountAction()) {
            case Firebase.STAT_ACCOUNTS_CREATED:
                refTotal = refTotal.child(Firebase.STAT_ACCOUNTS_CREATED);
                refToday = refToday.child(Firebase.STAT_ACCOUNTS_CREATED);
                refToday.runTransaction(getIncrementValue());
                refTotal.runTransaction(getIncrementValue());
                break;
            case Firebase.STAT_ACCOUNTS_DELETED:
                refTotal = refTotal.child(Firebase.STAT_ACCOUNTS_DELETED);
                refToday = refToday.child(Firebase.STAT_ACCOUNTS_DELETED);
                refToday.runTransaction(getIncrementValue());
                refTotal.runTransaction(getIncrementValue());
                break;
        }

        if(getMessage() != null && getMessage().length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("account", getAccountId());
            map.put("action", getAccountAction());
            getStatisticsMessage().setMessage(getMessage()).call(null, map);
        }

        if(getKey() != null && getAccountId() != null && getAccountId().length() > 0) {
            new TaskSingleValueEventFor<JSONObject>(refAccounts.child(getAccountId())).setFirebaseRest(getFirebaseAccessToken().fetchToken()).addOnCompleteListener(new Runnable1<JSONObject>() {
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
                        refAccounts.child(getAccountId()).child(Firebase.PRIVATE).child(Firebase.HISTORY).push().setValue(map);
                        Misc.log("StatisticsAccount", getAccountId(), "action:", getAccountAction());
                    }
                    clear();
                }
            }).start();

        } else {
            clear();
        }
//        json.put(STATUS, STATUS_SUCCESS);
    }

    public StatisticsAccount clear() {
        setAction(null);
        setAccountId(null);
        setMessage(null);
        setKey(null);
        setValue(null);
        setPersistent(null);
        return this;
    }

    public AccessToken getFirebaseAccessToken() {
        return firebaseAccessToken;
    }

    public StatisticsAccount setFirebaseAccessToken(AccessToken firebaseAccessToken) {
        this.firebaseAccessToken = firebaseAccessToken;
        return this;
    }

    public String getAccountAction() {
        return accountAction;
    }

    public String getAccountId() {
        return accountId;
    }

    public StatisticsAccount setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public StatisticsAccount setMessage(String message) {
        this.message = message;
        return this;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    public StatisticsAccount setPersistent(Boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public StatisticsMessage getStatisticsMessage() {
        return statisticsMessage;
    }

    public StatisticsAccount setStatisticsMessage(StatisticsMessage statisticsMessage) {
        this.statisticsMessage = statisticsMessage;
        return this;
    }

    public StatisticsAccount setAction(String accountAction) {
        this.accountAction = accountAction;
        return this;
    }

    public String getKey() {
        return key;
    }

    public StatisticsAccount setKey(String key) {
        this.key = key;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public StatisticsAccount setValue(Object value) {
        this.value = value;
        return this;
    }

    public Transaction.Handler getIncrementValue() {
        return incrementValue;
    }

    public StatisticsAccount setIncrementValue(Transaction.Handler incrementValue) {
        this.incrementValue = incrementValue;
        return this;
    }

}
