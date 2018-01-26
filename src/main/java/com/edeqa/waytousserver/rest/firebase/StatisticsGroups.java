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
import com.google.firebase.database.Transaction;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.edeqa.waytousserver.servers.AbstractDataProcessor.GroupAction.GROUP_CREATED_PERSISTENT;
import static com.edeqa.waytousserver.servers.AbstractDataProcessor.GroupAction.GROUP_CREATED_TEMPORARY;
import static com.edeqa.waytousserver.servers.AbstractDataProcessor.GroupAction.GROUP_DELETED;
import static com.edeqa.waytousserver.servers.AbstractDataProcessor.GroupAction.GROUP_REJECTED;

@SuppressWarnings("unused")
public class StatisticsGroups implements NamedCall {

    private DatabaseReference firebaseStat;
    private DatabaseReference firebaseGroups;
    private AbstractDataProcessor.GroupAction groupAction;
    private String firebaseAccessToken;
    private String groupId;
    private String errorMessage;
    private Boolean persistent;
    private StatisticsMessage statisticsMessage;
    private Transaction.Handler incrementValue;

    @Override
    public String getName() {
        return "statistics/group";
    }

    @Override
    public void call(JSONObject json, Object request) {

        DatabaseReference referenceTotal;
        DatabaseReference referenceToday;
        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));

        referenceTotal = getFirebaseStat().child(Firebase.STAT_TOTAL);
        referenceToday = getFirebaseStat().child(Firebase.STAT_BY_DATE).child(today);

        switch(getGroupAction()) {
            case GROUP_CREATED_PERSISTENT:
                referenceTotal = referenceTotal.child(Firebase.STAT_GROUPS_CREATED_PERSISTENT);
                referenceToday = referenceToday.child(Firebase.STAT_GROUPS_CREATED_PERSISTENT);
                break;
            case GROUP_CREATED_TEMPORARY:
                referenceTotal = referenceTotal.child(Firebase.STAT_GROUPS_CREATED_TEMPORARY);
                referenceToday = referenceToday.child(Firebase.STAT_GROUPS_CREATED_TEMPORARY);
                break;
            case GROUP_DELETED:
                referenceTotal = referenceTotal.child(Firebase.STAT_GROUPS_DELETED);
                referenceToday = referenceToday.child(Firebase.STAT_GROUPS_DELETED);
                break;
            case GROUP_REJECTED:
                referenceTotal = referenceTotal.child(Firebase.STAT_GROUPS_REJECTED);
                referenceToday = referenceToday.child(Firebase.STAT_GROUPS_REJECTED);
                break;
        }

        referenceToday.runTransaction(getIncrementValue());
        referenceTotal.runTransaction(getIncrementValue());

        if(errorMessage != null && errorMessage.length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("group", groupId);
            map.put("action", getGroupAction().toString());
            getStatisticsMessage().setFirebaseStat(getFirebaseStat()).setMessage(errorMessage).call(null, map);
        }

//        json.put(STATUS, STATUS_SUCCESS);
    }

    public DatabaseReference getFirebaseStat() {
        return firebaseStat;
    }

    public StatisticsGroups setFirebaseStat(DatabaseReference firebaseStat) {
        this.firebaseStat = firebaseStat;
        return this;
    }

    public DatabaseReference getFirebaseGroups() {
        return firebaseGroups;
    }

    public StatisticsGroups setFirebaseGroups(DatabaseReference firebaseGroups) {
        this.firebaseGroups = firebaseGroups;
        return this;
    }

    public String getFirebaseAccessToken() {
        return firebaseAccessToken;
    }

    public StatisticsGroups setFirebaseAccessToken(String firebaseAccessToken) {
        this.firebaseAccessToken = firebaseAccessToken;
        return this;
    }

    public AbstractDataProcessor.GroupAction getGroupAction() {
        return groupAction;
    }

    public StatisticsGroups setGroupAction(AbstractDataProcessor.GroupAction groupAction) {
        this.groupAction = groupAction;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public StatisticsGroups setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public StatisticsGroups setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    public StatisticsGroups setPersistent(Boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public StatisticsMessage getStatisticsMessage() {
        return statisticsMessage;
    }

    public StatisticsGroups setStatisticsMessage(StatisticsMessage statisticsMessage) {
        this.statisticsMessage = statisticsMessage;
        return this;
    }

    public Transaction.Handler getIncrementValue() {
        return incrementValue;
    }

    public StatisticsGroups setIncrementValue(Transaction.Handler incrementValue) {
        this.incrementValue = incrementValue;
        return this;
    }
}
