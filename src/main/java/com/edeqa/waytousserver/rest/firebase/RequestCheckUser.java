package com.edeqa.waytousserver.rest.firebase;

import com.google.firebase.database.Transaction;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class RequestCheckUser extends AbstractAction<RequestCheckUser, Object> {

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
        return "check/user";
    }

    @Override
    public void call(JSONObject json, Object request) {


    }

    public AccessToken getFirebaseAccessToken() {
        return firebaseAccessToken;
    }

    public RequestCheckUser setFirebaseAccessToken(AccessToken firebaseAccessToken) {
        this.firebaseAccessToken = firebaseAccessToken;
        return this;
    }

    public String getAccountAction() {
        return accountAction;
    }

    public String getAccountId() {
        return accountId;
    }

    public RequestCheckUser setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public RequestCheckUser setMessage(String message) {
        this.message = message;
        return this;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    public RequestCheckUser setPersistent(Boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public StatisticsMessage getStatisticsMessage() {
        return statisticsMessage;
    }

    public RequestCheckUser setStatisticsMessage(StatisticsMessage statisticsMessage) {
        this.statisticsMessage = statisticsMessage;
        return this;
    }

    public RequestCheckUser setAction(String accountAction) {
        this.accountAction = accountAction;
        return this;
    }

    public String getKey() {
        return key;
    }

    public RequestCheckUser setKey(String key) {
        this.key = key;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public RequestCheckUser setValue(Object value) {
        this.value = value;
        return this;
    }

    public Transaction.Handler getIncrementValue() {
        return incrementValue;
    }

    public RequestCheckUser setIncrementValue(Transaction.Handler incrementValue) {
        this.incrementValue = incrementValue;
        return this;
    }

}
