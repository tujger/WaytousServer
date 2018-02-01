package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.helpers.UserRequest;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Transaction;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.edeqa.waytous.Constants.REQUEST_CHECK_USER;
import static com.edeqa.waytous.Constants.REQUEST_UID;
import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.Constants.RESPONSE_SIGN;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytous.Constants.USER_NAME;

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
    private String hash;
    private CustomToken customToken;
    private CreateAccount createAccount;
    private StatisticsUser statisticsUser;
    private RejectUser rejectUser;
    private RegisterUser registerUser;
    private UserRequest userRequest;

    @Override
    public String getName() {
        return "firebase/check/user";
    }

    @Override
    public void call(final JSONObject json, Object request) {
        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        if (getUserRequest() != null) {
            Misc.log("RequestCheckUser", "onMessage:checkFound:", getUserRequest().toString());

            final DatabaseReference refGroup = refGroups.child(getUserRequest().getGroupId());

            final TaskSingleValueEventFor userCheckTask = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(new Runnable1<DataSnapshot>() {
                @Override
                public void call(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) { //join as existing member
                        try {
                            if (getUserRequest().checkControl((String) ((HashMap) dataSnapshot.getValue()).get(REQUEST_UID), getHash())) {
                                Misc.log("RequestCheckUser", "onMessage:joinAsExisting:", getUserRequest().toString());

                                try {
                                    final String customToken = getCustomToken().fetchToken(getUserRequest().getUid());

                                    final Map<String, Object> update = new HashMap<>();
                                    update.put(Firebase.ACTIVE, true);
                                    update.put(Firebase.COLOR, Utils.selectColor((int) getUserRequest().getNumber()));
                                    update.put(Firebase.CHANGED, new Date().getTime());
                                    if (getUserRequest().getName() != null && getUserRequest().getName().length() > 0) {
                                        update.put(USER_NAME, getUserRequest().getName());
                                    }

                                    getCreateAccount().setOnSuccess(new Runnable() {
                                        @Override
                                        public void run() {
                                            Task<Void> updateUserTask = refGroup.child(Firebase.USERS).child(Firebase.PUBLIC).child("" + getUserRequest().getNumber()).updateChildren(update);
                                            try {
                                                Tasks.await(updateUserTask);
                                                json.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                                json.put(RESPONSE_NUMBER, getUserRequest().getNumber());
                                                json.put(RESPONSE_SIGN, customToken);

                                                getUserRequest().send(json.toString());

                                                Misc.log("RequestCheckUser", "onMessage:joined:" + getUserRequest().getAddress(), "signToken: [provided]"/*+customToken*/);

                                                getStatisticsUser().setGroupId(getUserRequest().getGroupId()).setUserId(getUserRequest().getUid()).setAction(AbstractDataProcessor.UserAction.USER_RECONNECTED).call(null, null);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).setOnError(new Runnable1<Throwable>() {
                                        @Override
                                        public void call(Throwable error) {
                                            Misc.err("RequestCheckUser", "onMessage:joinNotAuthenticated:", getUserRequest().toString(), error);
                                            getRejectUser().setUserRequest(getUserRequest()).call(json, "Cannot join to group (code 19).");
                                        }
                                    }).call(null, getUserRequest().fetchUser());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Misc.err("RequestCheckUser", "onMessage:joinNotAuthenticated:", getUserRequest().toString(), "hash not equals");
                                rejectUser.setUserRequest(getUserRequest()).call(json, "Cannot join to group (user not authenticated).");
                            }

                        } catch (Exception e) {
                            Misc.err("RequestCheckUser", "onMessage:joinHashFailed:", getUserRequest().toString());
                            rejectUser.setUserRequest(getUserRequest()).call(json, "Cannot join to group (user not authenticated).");
                            e.printStackTrace();
                        }

                    } else { // join as new member
                        createAccount.setOnSuccess(new Runnable() {
                            @Override
                            public void run() {
                                getRegisterUser()
                                        .setGroupId(getUserRequest().getGroupId())
                                        .setUser(getUserRequest().fetchUser())
                                        .setAction(REQUEST_CHECK_USER)
                                        .call(null, null);
                                Misc.log("RequestCheckUser", "onMessage:joinAsNew:" + getUserRequest().getAddress());
                            }
                        }).setOnError(new Runnable1<Throwable>() {
                            @Override
                            public void call(Throwable error) {
                                Misc.err("RequestCheckUser", "onMessage:joinAsNew:", getUserRequest().toString(), error);
                                getRejectUser().setUserRequest(getUserRequest()).call(json, "Cannot join to group (code 18).");
                            }
                        }).call(null, getUserRequest().fetchUser());
                    }
                }
            });

            final TaskSingleValueEventFor userGetNumberTask = new TaskSingleValueEventFor<DataSnapshot>()
                    .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                        @Override
                        public void call(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                Misc.log("RequestCheckUser", "onMessage:joinNumberFound:" + getUserRequest().getAddress(), "number:", dataSnapshot.getValue().toString());
//                                            check.setNumber(Long.parseLong(dataSnapshot.getValue().toString()));
                                getUserRequest().setNumber(Integer.parseInt(dataSnapshot.getValue().toString()));
                                userCheckTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE).child(dataSnapshot.getValue().toString())).start();

                            } else {
                                Misc.err("RequestCheckUser", "onMessage:joinNumberNotFound:" + getUserRequest().getAddress());
                                getRejectUser().setUserRequest(getUserRequest()).call(json, "This group is expired. (005)");
                            }
                        }
                    });

            final TaskSingleValueEventFor userSearchTask = new TaskSingleValueEventFor<DataSnapshot>()
                    .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                        @Override
                        public void call(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                ArrayList<HashMap<String, Object>> users = (ArrayList<HashMap<String, Object>>) dataSnapshot.getValue();
                                for (HashMap<String, Object> user : users) {
                                    if (user != null && user.containsKey(REQUEST_UID)) {
                                        if (getUserRequest().checkControl(user.get(REQUEST_UID).toString(), hash)) {
                                            userGetNumberTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.KEYS).child(getUserRequest().getUid())).start();
                                            return;
                                        }
                                    }
                                }
                                Misc.err("RequestCheckUser", "onMessage:joinUserNotFound:", getUserRequest().getAddress());
                                getRejectUser().setUserRequest(getUserRequest()).call(json, "This group is expired. (004)");
                            } else {
                                Misc.err("RequestCheckUser", "onMessage:joinEmptyGroup:", getUserRequest().getAddress());
                                getRejectUser().setUserRequest(getUserRequest()).call(json, "This group is expired. (003)");
                            }
                        }
                    });

            TaskSingleValueEventFor groupOptionsTask = new TaskSingleValueEventFor<DataSnapshot>()
                    .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                        @Override
                        public void call(DataSnapshot dataSnapshot) {
                            if (getUserRequest().getUid() != null) {
                                if (dataSnapshot.getValue() != null) {
                                    userCheckTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE).child("" + getUserRequest().getNumber())).start();
                                } else {
                                    Misc.err("RequestCheckUser", "onMessage:joinUserNotExists:" + getUserRequest().getAddress());
                                    getRejectUser().setUserRequest(getUserRequest()).call(json, "This group is expired. (002)");
                                }
                            } else {
                                userSearchTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE)).start();
                            }
                        }
                    });

            groupOptionsTask.setRef(refGroup.child(Firebase.OPTIONS)).start();
        }
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

    public UserRequest getUserRequest() {
        return userRequest;
    }

    public String getHash() {
        return hash;
    }

    public CustomToken getCustomToken() {
        return customToken;
    }

    public CreateAccount getCreateAccount() {
        return createAccount;
    }

    public StatisticsUser getStatisticsUser() {
        return statisticsUser;
    }

    public RejectUser getRejectUser() {
        return rejectUser;
    }

    public RegisterUser getRegisterUser() {
        return registerUser;
    }
}
