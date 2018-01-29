package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Transaction;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class StatisticsUser extends AbstractAction<StatisticsUser, Object> {

    private AbstractDataProcessor.UserAction action;
    private String groupId;
    private String userId;
    private String message;
    private StatisticsMessage statisticsMessage;
    private StatisticsAccount statisticsAccount;
    private Transaction.Handler incrementValue;

    @Override
    public String getName() {
        return "statistics/user";
    }

    @Override
    public void call(JSONObject json, Object request) {
        getStatisticsAccount().clear()
                .setAccountId(getUserId())
                .setKey("group")
                .setAction(getAction().toString())
                .setValue(getGroupId())
                .setMessage(getMessage())
                .call(null, null);

        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));

        DatabaseReference refTotal = getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_TOTAL);
        DatabaseReference refToday = getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_BY_DATE).child(today);
        switch(getAction()) {
            case USER_JOINED:
                refTotal = refTotal.child(Firebase.STAT_USERS_JOINED);
                refToday = refToday.child(Firebase.STAT_USERS_JOINED);
                break;
            case USER_RECONNECTED:
                refTotal = refTotal.child(Firebase.STAT_USERS_RECONNECTED);
                refToday = refToday.child(Firebase.STAT_USERS_RECONNECTED);
                break;
            case USER_REJECTED:
                refTotal = refTotal.child(Firebase.STAT_USERS_REJECTED);
                refToday = refToday.child(Firebase.STAT_USERS_REJECTED);
                break;
            case USER_REMOVED:
                refTotal = refTotal.child(Firebase.STAT_USERS_REMOVED);
                refToday = refToday.child(Firebase.STAT_USERS_REMOVED);
                break;
            default:
                break;
        }

        refToday.runTransaction(getIncrementValue());
        refTotal.runTransaction(getIncrementValue());

        if(getMessage() != null && getMessage().length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("group", getGroupId());
            map.put("user", getUserId());
            map.put("action", getAction().toString());
            getStatisticsMessage().setMessage(getMessage()).call(null, map);
        }

        clear();
    }

    public StatisticsUser clear() {
        setAction(null);
        setUserId(null);
        setGroupId(null);
        setMessage(null);
        return this;
    }

    public AbstractDataProcessor.UserAction getAction() {
        return action;
    }

    public StatisticsUser setAction(AbstractDataProcessor.UserAction action) {
        this.action = action;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public StatisticsUser setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public StatisticsUser setMessage(String message) {
        this.message = message;
        return this;
    }

    public StatisticsMessage getStatisticsMessage() {
        return statisticsMessage;
    }

    public StatisticsUser setStatisticsMessage(StatisticsMessage statisticsMessage) {
        this.statisticsMessage = statisticsMessage;
        return this;
    }

    public Transaction.Handler getIncrementValue() {
        return incrementValue;
    }

    public StatisticsUser setIncrementValue(Transaction.Handler incrementValue) {
        this.incrementValue = incrementValue;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public StatisticsUser setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public StatisticsAccount getStatisticsAccount() {
        return statisticsAccount;
    }

    public StatisticsUser setStatisticsAccount(StatisticsAccount statisticsAccount) {
        this.statisticsAccount = statisticsAccount;
        return this;
    }
}
