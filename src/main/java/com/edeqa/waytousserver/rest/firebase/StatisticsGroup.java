package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class StatisticsGroup extends AbstractFirebaseAction<StatisticsGroup, GroupRequest> {

    public static final String TYPE = "/rest/firebase/statistics/group";

    private AbstractDataProcessor.Action action;
    private String message;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, GroupRequest groupRequest) {

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

        refToday.runTransaction(incrementValue);
        refTotal.runTransaction(incrementValue);

        Map<String, String> map = new HashMap<>();
        map.put("group", groupRequest.getId());
        map.put("action", getAction().toString());
        ((StatisticsMessage) getFireBus().getHolder(StatisticsMessage.TYPE))
                .setMessage(getMessage())
                .call(null, map);
        clear();
    }

    public StatisticsGroup clear() {
        setAction(null);
        setMessage(null);
        return this;
    }

    public AbstractDataProcessor.Action getAction() {
        return action;
    }

    public StatisticsGroup setAction(AbstractDataProcessor.Action action) {
        this.action = action;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public StatisticsGroup setMessage(String message) {
        this.message = message;
        return this;
    }

}
