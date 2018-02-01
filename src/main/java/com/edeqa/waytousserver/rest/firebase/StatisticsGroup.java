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
public class StatisticsGroup extends AbstractAction<StatisticsGroup, Object> {

    private AbstractDataProcessor.GroupAction action;
    private String groupId;
    private String message;
    private Boolean persistent;
    private StatisticsMessage statisticsMessage;
    private Transaction.Handler incrementValue;

    @Override
    public String getName() {
        return "firebase/statistics/group";
    }

    @Override
    public void call(JSONObject json, Object request) {

        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));

        DatabaseReference refTotal = getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_TOTAL);
        DatabaseReference refToday = getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_BY_DATE).child(today);

        switch(getAction()) {
            case GROUP_CREATED_PERSISTENT:
                refTotal = refTotal.child(Firebase.STAT_GROUPS_CREATED_PERSISTENT);
                refToday = refToday.child(Firebase.STAT_GROUPS_CREATED_PERSISTENT);
                break;
            case GROUP_CREATED_TEMPORARY:
                refTotal = refTotal.child(Firebase.STAT_GROUPS_CREATED_TEMPORARY);
                refToday = refToday.child(Firebase.STAT_GROUPS_CREATED_TEMPORARY);
                break;
            case GROUP_DELETED:
                refTotal = refTotal.child(Firebase.STAT_GROUPS_DELETED);
                refToday = refToday.child(Firebase.STAT_GROUPS_DELETED);
                break;
            case GROUP_REJECTED:
                refTotal = refTotal.child(Firebase.STAT_GROUPS_REJECTED);
                refToday = refToday.child(Firebase.STAT_GROUPS_REJECTED);
                break;
            default:
                return;
        }

        refToday.runTransaction(getIncrementValue());
        refTotal.runTransaction(getIncrementValue());

//        if(getMessage() != null && getMessage().length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("group", getGroupId());
            map.put("action", getAction().toString());
            getStatisticsMessage().setMessage(getMessage()).call(null, map);
//        }
        clear();

//        json.put(STATUS, STATUS_SUCCESS);
    }

    public StatisticsGroup clear() {
        setAction(null);
        setGroupId(null);
        setMessage(null);
        setPersistentGroup(null);
        return this;
    }

    public AbstractDataProcessor.GroupAction getAction() {
        return action;
    }

    public StatisticsGroup setAction(AbstractDataProcessor.GroupAction action) {
        this.action = action;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public StatisticsGroup setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public StatisticsGroup setMessage(String message) {
        this.message = message;
        return this;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    public StatisticsGroup setPersistentGroup(Boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public StatisticsMessage getStatisticsMessage() {
        return statisticsMessage;
    }

    public StatisticsGroup setStatisticsMessage(StatisticsMessage statisticsMessage) {
        this.statisticsMessage = statisticsMessage;
        return this;
    }

    public Transaction.Handler getIncrementValue() {
        return incrementValue;
    }

    public StatisticsGroup setIncrementValue(Transaction.Handler incrementValue) {
        this.incrementValue = incrementValue;
        return this;
    }

}
