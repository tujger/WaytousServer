package com.edeqa.waytousserver.rest.firebase;

import com.google.firebase.database.Transaction;

import org.json.JSONObject;

@SuppressWarnings("unused")
public class RequestNewGroup extends AbstractFirebaseAction<RequestNewGroup, Object> {

    public static final String TYPE = "/rest/firebase/join/new";

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
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean call(JSONObject json, Object request) {

//        if (uid != null) {
//            final MyGroup group = new MyGroup();
//            final MyUser user = new MyUser(conn, request);
//            Misc.log(LOG, "onMessage:requestNew:" + conn.getRemoteSocketAddress(), "{ uid:" + uid + " }");
//
//            createOrUpdateUserAccount(user, new Runnable() {
//                @Override
//                public void run() {
//                    //noinspection unchecked
//                    final Runnable1<JSONObject>[] onresult = new Runnable1[2];
//                    onresult[0] = new Runnable1<JSONObject>() {
//                        @Override
//                        public void onEvent(JSONObject json) {
//                            registerUser(group.getId(), user, REQUEST_NEW_GROUP, new Runnable1<JSONObject>() {
//                                @Override
//                                public void onEvent(JSONObject json) {
//                                    user.connection.send(json.toString());
//                                    user.connection.close();
//                                }
//                            }, new Runnable1<JSONObject>() {
//                                @Override
//                                public void onEvent(JSONObject json) {
//                                    user.connection.send(json.toString());
//                                    user.connection.close();
//                                }
//                            });
//                        }
//                    };
//                    onresult[1] = new Runnable1<JSONObject>() {
//                        @Override
//                        public void onEvent(JSONObject json) {
//                            group.fetchNewId();
//                            createGroup(group, onresult[0], onresult[1]);
//                            putStaticticsAccount(user.getUid(), AbstractDataProcessor.GroupAction.GROUP_CREATED_TEMPORARY.toString(), "group", group.getId(), null);
//
//                        }
//                    };
//                    createGroup(group, onresult[0], onresult[1]);
//                    putStaticticsAccount(user.getUid(), AbstractDataProcessor.GroupAction.GROUP_CREATED_TEMPORARY.toString(), "group", group.getId(), null);
//                }
//            }, new Runnable1<Throwable>() {
//                @Override
//                public void onEvent(Throwable error) {
//                    Misc.err(LOG, "onMessage:newGroup:",user, error);
//                    rejectUser(response, user.connection, null, null, "Cannot create group (code 16).");
//                }
//            });
//        } else {
//            rejectUser(response, conn, null, null, "Cannot create group (code 15).");
//            Misc.err(LOG, "onMessage:newGroup:", response);
//        }

        return true;
    }

    public AccessToken getFirebaseAccessToken() {
        return firebaseAccessToken;
    }

    public RequestNewGroup setFirebaseAccessToken(AccessToken firebaseAccessToken) {
        this.firebaseAccessToken = firebaseAccessToken;
        return this;
    }

    public String getAccountAction() {
        return accountAction;
    }

    public String getAccountId() {
        return accountId;
    }

    public RequestNewGroup setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public RequestNewGroup setMessage(String message) {
        this.message = message;
        return this;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    public RequestNewGroup setPersistent(Boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public StatisticsMessage getStatisticsMessage() {
        return statisticsMessage;
    }

    public RequestNewGroup setStatisticsMessage(StatisticsMessage statisticsMessage) {
        this.statisticsMessage = statisticsMessage;
        return this;
    }

    public RequestNewGroup setAction(String accountAction) {
        this.accountAction = accountAction;
        return this;
    }

    public String getKey() {
        return key;
    }

    public RequestNewGroup setKey(String key) {
        this.key = key;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public RequestNewGroup setValue(Object value) {
        this.value = value;
        return this;
    }

    public Transaction.Handler getIncrementValue() {
        return incrementValue;
    }

    public RequestNewGroup setIncrementValue(Transaction.Handler incrementValue) {
        this.incrementValue = incrementValue;
        return this;
    }

}
