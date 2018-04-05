package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.FirebaseGroup;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.helpers.UserRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.edeqa.waytous.Constants.REQUEST_JOIN_GROUP;
import static com.edeqa.waytous.Constants.RESPONSE_CONTROL;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_CHECK;

@SuppressWarnings("unused")
public class JoinGroup extends AbstractFirebaseAction<JoinGroup, UserRequest> {

    public static final String TYPE = "/rest/firebase/group/join";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(final JSONObject json, final UserRequest userRequest) {
        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        if (userRequest != null) {
            final DatabaseReference refGroup = refGroups.child(userRequest.getGroupId());
            final TaskSingleValueEventFor[] requestDataPrivateTask = new TaskSingleValueEventFor[1];
            final GroupRequest groupRequest = new GroupRequest();
            requestDataPrivateTask[0] = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(dataSnapshot -> {

                int count = 1;
                boolean found = false;
                Object value = dataSnapshot.getValue();

                if (value == null) {
                    dataSnapshot.getRef().push().setValue(userRequest.getUid());
                    requestDataPrivateTask[0].setRef(refGroup.child(Firebase.USERS).child(Firebase.QUEUE)).start();
                    return;
                }

                //noinspection unchecked
                TreeMap<String, String> map = new TreeMap<>((HashMap<String, String>) dataSnapshot.getValue());

                for (Map.Entry<String, String> x : map.entrySet()) {
                    if (userRequest.getUid().equals(x.getValue())) {
                        found = true;
                        break;
                    }
                    ++count;
                }
                if (found) {
                    userRequest.setNumber(count);
                    final MyUser user = userRequest.fetchUser();
                    ((CreateAccount) getFireBus().getHolder(CreateAccount.TYPE)).setOnSuccess(() -> ((RegisterUser) getFireBus().getHolder(RegisterUser.TYPE))
                            .setGroupId(userRequest.getGroupId())
                            .setAction(REQUEST_JOIN_GROUP)
                            .call(null, user)).setOnError(error -> ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(userRequest)
                            .call(json,"Cannot create group (code 17).")).call(null, user);
                } else {
                    if(groupRequest.getLimitUsers() > 0 && count > groupRequest.getLimitUsers()) {
                        ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                .setUserRequest(userRequest)
                                .call(json,"Sorry, group is full.");
                    } else {
                        DatabaseReference nodeNumber = refGroups.child(userRequest.getGroupId()).child(Firebase.USERS).child(Firebase.QUEUE).push();
                        nodeNumber.setValue(userRequest.getUid());
                        requestDataPrivateTask[0].setRef(refGroup.child(Firebase.USERS).child(Firebase.QUEUE)).start();
                    }
                }
            });

            final TaskSingleValueEventFor numberForKeyTask = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(dataSnapshot -> {
                if (dataSnapshot.getValue() != null) { //join as existing member, go to check
                    userRequest.setNumber(Integer.parseInt(dataSnapshot.getValue().toString()));

                    Misc.log("JoinGroup", "checks request:", userRequest);

                    json.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                    json.put(RESPONSE_CONTROL, userRequest.getControl());
                    try {
                        userRequest.send(json.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else { // join as new member
                    requestDataPrivateTask[0].setRef(refGroup.child(Firebase.USERS).child(Firebase.QUEUE)).start();
                }
            });

            TaskSingleValueEventFor groupOptionsTask = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(dataSnapshot -> {
                FirebaseGroup group = dataSnapshot.getValue(FirebaseGroup.class);
                if (dataSnapshot.getValue() != null) {
                    try {
                        groupRequest.setLimitUsers(group.limitUsers);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    numberForKeyTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.KEYS).child(userRequest.getUid())).start();
                } else {
                    ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(userRequest)
                            .call(json,"This group is expired. (001)");
                }
            });

            if (userRequest.getUid() != null) {
                Misc.log("JoinGroup", "joining:", userRequest);
                groupOptionsTask.setRef(refGroup.child(Firebase.OPTIONS)).start();
            } else {
                json.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                json.put(RESPONSE_CONTROL, userRequest.getControl());
                Misc.log("JoinGroup", "reconnecting:", userRequest);
                userRequest.send(json.toString());
            }
        } else {
            Misc.err("JoinGroup", "failed: userRequest is null");
        }
    }
}
