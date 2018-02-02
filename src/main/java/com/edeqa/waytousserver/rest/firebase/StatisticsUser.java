package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.eventbus.EventBus;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.UserRequest;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class StatisticsUser extends AbstractFirebaseAction<StatisticsUser, String> {

    public static final String TYPE = "/rest/firebase/statistics/user";

    private AbstractDataProcessor.UserAction action;
    private String groupId;
    private String message;
    private UserRequest userRequest;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean onEvent(JSONObject json, String userId) {
        ((StatisticsAccount) EventBus.getOrCreateEventBus().getHolder(StatisticsAccount.TYPE))
                .setKey("group")
                .setAction(getAction().toString())
                .setValue(getGroupId())
                .setMessage(getMessage())
                .onEvent(null, userId);

        if(getUserRequest() != null) {
            setGroupId(getUserRequest().getGroupId());
            userId = getUserRequest().getUid();
        }

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

        refToday.runTransaction(incrementValue);
        refTotal.runTransaction(incrementValue);

        if(getMessage() != null && getMessage().length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("group", getGroupId());
            map.put("user", userId);
            map.put("action", getAction().toString());
            ((StatisticsMessage) EventBus.getOrCreateEventBus().getHolder(StatisticsMessage.TYPE))
                    .setMessage(getMessage())
                    .onEvent(null, map);
        }

        clear();
        return true;
    }

    public StatisticsUser clear() {
        setAction(null);
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

    public UserRequest getUserRequest() {
        return userRequest;
    }

    public StatisticsUser setUserRequest(UserRequest userRequest) {
        this.userRequest = userRequest;
        return this;
    }
}
