package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.helpers.UserRequest;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.api.core.ApiFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

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
public class CheckUser extends AbstractFirebaseAction<CheckUser, UserRequest> {

    public static final String TYPE = "/rest/firebase/check/user";

    private String hash;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(final JSONObject json, final UserRequest userRequest) {
        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        if (userRequest != null) {
            Misc.log("CheckUser", "has found:", userRequest);

            final DatabaseReference refGroup = refGroups.child(userRequest.getGroupId());

            final TaskSingleValueEventFor userCheckTask = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(dataSnapshot -> {
                if (dataSnapshot.getValue() != null) { //join as existing member
                    try {
                        if (userRequest.checkControl((String) ((HashMap) dataSnapshot.getValue()).get(REQUEST_UID), getHash())) {
                            Misc.log("CheckUser", "as existing:", userRequest);

                            try {
                                final String customToken = ((CustomToken) getFireBus().getHolder(CustomToken.TYPE)).fetchToken(userRequest.getUid());

                                final Map<String, Object> update = new HashMap<>();
                                update.put(Firebase.ACTIVE, true);
                                update.put(Firebase.COLOR, MyUser.selectColor(userRequest.getNumber()));
                                update.put(Firebase.CHANGED, new Date().getTime());
                                if (userRequest.getName() != null && userRequest.getName().length() > 0) {
                                    update.put(USER_NAME, userRequest.getName());
                                }

                                ((CreateAccount) getFireBus().getHolder(CreateAccount.TYPE)).setOnSuccess(() -> {
                                    ApiFuture<Void> updateUserTask = refGroup.child(Firebase.USERS).child(Firebase.PUBLIC).child("" + userRequest.getNumber()).updateChildrenAsync(update);
                                    try {
                                        updateUserTask.get();
                                        json.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                        json.put(RESPONSE_NUMBER, userRequest.getNumber());
                                        json.put(RESPONSE_SIGN, customToken);

                                        userRequest.send(json.toString());

                                        Misc.log("CheckUser", "joined:", userRequest, "signToken: [provided]"/*+customToken*/);

                                        ((StatisticsUser) getFireBus().getHolder(StatisticsUser.TYPE))
                                                .setGroupId(userRequest.getGroupId())
                                                .setAction(AbstractDataProcessor.Action.USER_RECONNECTED)
                                                .call(null, userRequest.getUid());
                                    } catch (Exception e) {
                                        Misc.err("CheckUser", "failed joining:", e);
                                    }
                                }).setOnError(error -> {
                                    Misc.err("CheckUser", "failed:", userRequest, "[" + error + "]");
                                    ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                            .setUserRequest(userRequest)
                                            .call(json, "Cannot join group (code 19).");
                                }).call(null, userRequest.fetchUser());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Misc.err("CheckUser", "hashes not equal:", userRequest);
                            ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                    .setUserRequest(userRequest)
                                    .call(json, "Cannot join group (user not authenticated).");
                        }
                    } catch (Exception e) {
                        Misc.err("CheckUser", "hash failed:", userRequest);
                        ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                .setUserRequest(userRequest)
                                .call(json, "Cannot join group (user not authenticated).");
                        e.printStackTrace();
                    }
                } else { // join as new member
                    ((CreateAccount) getFireBus().getHolder(CreateAccount.TYPE)).setOnSuccess(() -> {
                        Misc.log("CheckUser", "as new:", userRequest);
                        ((RegisterUser) getFireBus().getHolder(RegisterUser.TYPE))
                                .setGroupId(userRequest.getGroupId())
                                .setAction(REQUEST_CHECK_USER)
                                .call(null, userRequest.fetchUser());
                    }).setOnError(error -> {
                        Misc.err("CheckUser", "failed:", userRequest, "[" + error + "]");
                        ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                .setUserRequest(userRequest)
                                .call(json, "Cannot join group (code 18).");
                    }).call(null, userRequest.fetchUser());
                }
            });

            final TaskSingleValueEventFor userGetNumberTask = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(dataSnapshot -> {
                if (dataSnapshot.getValue() != null) {
                    Misc.log("CheckUser", "found number:", userRequest, "number:", dataSnapshot.getValue().toString());
                    userRequest.setNumber(Integer.parseInt(dataSnapshot.getValue().toString()));
                    userCheckTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE).child(dataSnapshot.getValue().toString())).start();

                } else {
                    Misc.err("CheckUser", "number not found:", userRequest);
                    ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(userRequest)
                            .call(json, "This group is expired. (005)");
                }
            });

            final TaskSingleValueEventFor userSearchTask = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(dataSnapshot -> {
                if (dataSnapshot.getValue() != null) {
                    ArrayList<HashMap<String, Object>> users = (ArrayList<HashMap<String, Object>>) dataSnapshot.getValue();
                    for (HashMap<String, Object> user : users) {
                        if (user != null && user.containsKey(REQUEST_UID)) {
                            if (userRequest.checkControl(user.get(REQUEST_UID).toString(), hash)) {
                                userGetNumberTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.KEYS).child(userRequest.getUid())).start();
                                return;
                            }
                        }
                    }
                    Misc.err("CheckUser", "user not found:", userRequest);
                    ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(userRequest)
                            .call(json, "This group is expired. (004)");
                } else {
                    Misc.err("CheckUser", "empty group requested:", userRequest);
                    ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(userRequest)
                            .call(json, "This group is expired. (003)");
                }
            });

            TaskSingleValueEventFor groupOptionsTask = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(dataSnapshot -> {
                if (userRequest.getUid() != null) {
                    if (dataSnapshot.getValue() != null) {
                        userCheckTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE).child("" + userRequest.getNumber())).start();
                    } else {
                        Misc.err("CheckUser", "user not exists:", userRequest);
                        ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                .setUserRequest(userRequest)
                                .call(json, "This group is expired. (002)");
                    }
                } else {
                    userSearchTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE)).start();
                }
            });

            groupOptionsTask.setRef(refGroup.child(Firebase.OPTIONS)).start();
        }
    }

    public String getHash() {
        return hash;
    }

    public CheckUser setHash(String hash) {
        this.hash = hash;
        return this;
    }
}
