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
            Misc.log("CheckUser", "onMessage:checkFound:", userRequest.toString());

            final DatabaseReference refGroup = refGroups.child(userRequest.getGroupId());

            final TaskSingleValueEventFor userCheckTask = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(new Runnable1<DataSnapshot>() {
                @Override
                public void call(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) { //join as existing member
                        try {
                            if (userRequest.checkControl((String) ((HashMap) dataSnapshot.getValue()).get(REQUEST_UID), getHash())) {
                                Misc.log("CheckUser", "onMessage:joinAsExisting:", userRequest.toString());

                                try {
                                    final String customToken = ((CustomToken) getFireBus().getHolder(CustomToken.TYPE)).fetchToken(userRequest.getUid());

                                    final Map<String, Object> update = new HashMap<>();
                                    update.put(Firebase.ACTIVE, true);
                                    update.put(Firebase.COLOR, Utils.selectColor((int) userRequest.getNumber()));
                                    update.put(Firebase.CHANGED, new Date().getTime());
                                    if (userRequest.getName() != null && userRequest.getName().length() > 0) {
                                        update.put(USER_NAME, userRequest.getName());
                                    }

                                    ((CreateAccount) getFireBus().getHolder(CreateAccount.TYPE))
                                            .setOnSuccess(new Runnable() {
                                        @Override
                                        public void run() {
                                            Task<Void> updateUserTask = refGroup.child(Firebase.USERS).child(Firebase.PUBLIC).child("" + userRequest.getNumber()).updateChildren(update);
                                            try {
                                                Tasks.await(updateUserTask);
                                                json.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                                json.put(RESPONSE_NUMBER, userRequest.getNumber());
                                                json.put(RESPONSE_SIGN, customToken);

                                                userRequest.send(json.toString());

                                                Misc.log("CheckUser", "onMessage:joined:" + userRequest.getAddress(), "signToken: [provided]"/*+customToken*/);

                                                ((StatisticsUser) getFireBus().getHolder(StatisticsUser.TYPE))
                                                        .setGroupId(userRequest.getGroupId())
                                                        .setAction(AbstractDataProcessor.UserAction.USER_RECONNECTED)
                                                        .call(null, userRequest.getUid());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).setOnError(new Runnable1<Throwable>() {
                                        @Override
                                        public void call(Throwable error) {
                                            Misc.err("CheckUser", "onMessage:joinNotAuthenticated:", userRequest.toString(), error);
                                            ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                                    .setUserRequest(userRequest)
                                                    .call(json, "Cannot join to group (code 19).");
                                        }
                                    }).call(null, userRequest.fetchUser());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Misc.err("CheckUser", "onMessage:joinNotAuthenticated:", userRequest.toString(), "hash not equals");
                                ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                        .setUserRequest(userRequest)
                                        .call(json, "Cannot join to group (user not authenticated).");
                            }

                        } catch (Exception e) {
                            Misc.err("CheckUser", "onMessage:joinHashFailed:", userRequest.toString());
                            ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                    .setUserRequest(userRequest)
                                    .call(json, "Cannot join to group (user not authenticated).");
                            e.printStackTrace();
                        }

                    } else { // join as new member
                        ((CreateAccount) getFireBus().getHolder(CreateAccount.TYPE))
                        .setOnSuccess(new Runnable() {
                            @Override
                            public void run() {
                                ((RegisterUser) getFireBus().getHolder(RegisterUser.TYPE))
                                        .setGroupId(userRequest.getGroupId())
                                        .setAction(REQUEST_CHECK_USER)
                                        .call(null, userRequest.fetchUser());
                                Misc.log("CheckUser", "onMessage:joinAsNew:" + userRequest.getAddress());
                            }
                        }).setOnError(new Runnable1<Throwable>() {
                            @Override
                            public void call(Throwable error) {
                                Misc.err("CheckUser", "onMessage:joinAsNew:", userRequest.toString(), error);
                                ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                        .setUserRequest(userRequest)
                                        .call(json, "Cannot join to group (code 18).");
                            }
                        }).call(null, userRequest.fetchUser());
                    }
                }
            });

            final TaskSingleValueEventFor userGetNumberTask = new TaskSingleValueEventFor<DataSnapshot>()
                    .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                        @Override
                        public void call(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                Misc.log("CheckUser", "onMessage:joinNumberFound:" + userRequest.getAddress(), "number:", dataSnapshot.getValue().toString());
//                                            check.setNumber(Long.parseLong(dataSnapshot.getValue().toString()));
                                userRequest.setNumber(Integer.parseInt(dataSnapshot.getValue().toString()));
                                userCheckTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE).child(dataSnapshot.getValue().toString())).start();

                            } else {
                                Misc.err("CheckUser", "onMessage:joinNumberNotFound:" + userRequest.getAddress());
                                ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                        .setUserRequest(userRequest)
                                        .call(json, "This group is expired. (005)");
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
                                        if (userRequest.checkControl(user.get(REQUEST_UID).toString(), hash)) {
                                            userGetNumberTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.KEYS).child(userRequest.getUid())).start();
                                            return;
                                        }
                                    }
                                }
                                Misc.err("CheckUser", "onMessage:joinUserNotFound:", userRequest.getAddress());
                                ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                        .setUserRequest(userRequest)
                                        .call(json, "This group is expired. (004)");
                            } else {
                                Misc.err("CheckUser", "onMessage:joinEmptyGroup:", userRequest.getAddress());
                                ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                        .setUserRequest(userRequest)
                                        .call(json, "This group is expired. (003)");
                            }
                        }
                    });

            TaskSingleValueEventFor groupOptionsTask = new TaskSingleValueEventFor<DataSnapshot>()
                    .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                        @Override
                        public void call(DataSnapshot dataSnapshot) {
                            if (userRequest.getUid() != null) {
                                if (dataSnapshot.getValue() != null) {
                                    userCheckTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE).child("" + userRequest.getNumber())).start();
                                } else {
                                    Misc.err("CheckUser", "onMessage:joinUserNotExists:" + userRequest.getAddress());
                                    ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                            .setUserRequest(userRequest)
                                            .call(json, "This group is expired. (002)");
                                }
                            } else {
                                userSearchTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE)).start();
                            }
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