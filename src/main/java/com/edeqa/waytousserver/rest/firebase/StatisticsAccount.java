package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class StatisticsAccount extends AbstractFirebaseAction<StatisticsAccount, String> {

    public static final String TYPE = "/rest/firebase/statistics/account";

    private AbstractDataProcessor.Action action;
    private String message;
    private String key;
    private Object value;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final String accountId) {

        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));

        final DatabaseReference refAccounts = getFirebaseReference().child(Firebase.SECTION_USERS);

        DatabaseReference refTotal = getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_TOTAL);
        DatabaseReference refToday = getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_BY_DATE).child(today);

        switch(getAction()) {
            case ACCOUNT_CREATED:
                refTotal = refTotal.child(Firebase.STAT_ACCOUNTS_CREATED);
                refToday = refToday.child(Firebase.STAT_ACCOUNTS_CREATED);
                refToday.runTransaction(incrementValue);
                refTotal.runTransaction(incrementValue);
                break;
            case ACCOUNT_DELETED:
                refTotal = refTotal.child(Firebase.STAT_ACCOUNTS_DELETED);
                refToday = refToday.child(Firebase.STAT_ACCOUNTS_DELETED);
                refToday.runTransaction(incrementValue);
                refTotal.runTransaction(incrementValue);
                break;
        }

        if(getMessage() != null && getMessage().length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("account", accountId);
            map.put("action", getAction().name());
            ((StatisticsMessage) getFireBus().getHolder(StatisticsMessage.TYPE))
                    .setMessage(getMessage())
                    .call(null, map);
        }

        if(getKey() != null && accountId != null && accountId.length() > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put(Firebase.TIMESTAMP, new Date().getTime());
            map.put(Firebase.KEYS, getKey());
            if (getAction() != null) map.put(Firebase.MODE, getAction());

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
            refAccounts.child(accountId).child(Firebase.PRIVATE).child(Firebase.HISTORY).push().setValueAsync(map);
//            refAccounts.child(accountId).child(Firebase.PRIVATE).child(Firebase.HISTORY).push().setValue(map);//FIXME
            Misc.log("StatisticsAccount", "register", accountId, "with action", getAction());
        } else {
            clear();
        }
    }

    public StatisticsAccount clear() {
        setAction(null);
        setMessage(null);
        setKey(null);
        setValue(null);
        return this;
    }

    public AbstractDataProcessor.Action getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public StatisticsAccount setMessage(String message) {
        this.message = message;
        return this;
    }

    public StatisticsAccount setAction(AbstractDataProcessor.Action action) {
        this.action = action;
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
}
