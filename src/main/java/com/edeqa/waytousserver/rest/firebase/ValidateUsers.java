package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@SuppressWarnings("unused")
public class ValidateUsers extends AbstractFirebaseAction<ValidateUsers, GroupRequest> {

    public static final String TYPE = "/rest/firebase/validate/users";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final GroupRequest groupRequest) {

        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(groupRequest.getId()).child(Firebase.USERS).child(Firebase.PUBLIC)).addOnCompleteListener(dataSnapshot -> {

            ArrayList<Map<String, Serializable>> users = null;
            try {
                //noinspection unchecked
                users = (ArrayList<Map<String, Serializable>>) dataSnapshot.getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (users == null) {
                Misc.log("ValidateUsers", "corrupted group found, removing:", groupRequest.getId());
                refGroups.child(groupRequest.getId()).removeValueAsync();
//                refGroups.child(groupRequest.getId()).removeValue();//FIXME
                ((StatisticsGroup) getFireBus().getHolder(StatisticsGroup.TYPE))
                        .setAction(AbstractDataProcessor.Action.GROUP_DELETED)
                        .setMessage("corrupted group removing: " + groupRequest.getId())
                        .call(null, groupRequest);
                return;
            }
            Misc.log("ValidateUsers", "is checking for group", groupRequest.getId(), "[" + users.size() + "]");
            long groupChanged = 0;

            for (int i = 0; i < users.size(); i++) {
                Map<String, Serializable> user = users.get(i);
                if (user == null) continue;

                String name = (String) user.get(Firebase.NAME);
                Long changed = (Long) user.get(Firebase.CHANGED);
                if (changed != null && changed > groupChanged)
                    groupChanged = changed;
                boolean active = false;
                Object object = user.get(Firebase.ACTIVE);
                if (object != null) {
                    active = (Boolean) object;
                }

                if (!active) continue;

                if (groupRequest.isDismissInactive()) {
                    Long current = new Date().getTime();
                    if (changed == null) {
                        Misc.log("ValidateUsers", "--- user", i, "is NULL", (name != null ? "[" + name + "]" : ""));
                        dataSnapshot.getRef().child("" + i).child(Firebase.ACTIVE).setValueAsync(false);
//                        dataSnapshot.getRef().child("" + i).child(Firebase.ACTIVE).setValue(false);//FIXME
                    } else if (current - groupRequest.getDelayToDismiss() * 1000 > changed) {
                        Misc.log("ValidateUsers", "--- user", i, "is EXPIRED for", ((current - groupRequest.getDelayToDismiss() * 1000 - changed) / 1000), "seconds", (name != null ? "[" + name + "]" : ""));
                        dataSnapshot.getRef().child("" + i).child(Firebase.ACTIVE).setValueAsync(false);
//                        dataSnapshot.getRef().child("" + i).child(Firebase.ACTIVE).setValue(false);//FIXME
                    } else {
                        dataSnapshot.getRef().getParent().getParent().child(Firebase.OPTIONS).child(Firebase.CHANGED).setValueAsync(changed);
//                        dataSnapshot.getRef().getParent().getParent().child(Firebase.OPTIONS).child(Firebase.CHANGED).setValue(changed);//FIXME
                        Misc.log("ValidateUsers", "--- user", i, "is OK", (name != null ? "[" + name + "]" : ""));
                    }
                }
            }

            if (!groupRequest.isPersistent() && groupRequest.getTimeToLiveIfEmpty() > 0 && new Date().getTime() - groupChanged > groupRequest.getTimeToLiveIfEmpty() * 60 * 1000) {
                String info = groupRequest.getId() + " expired for " + ((new Date().getTime() - groupChanged - groupRequest.getTimeToLiveIfEmpty() * 60 * 1000) / 1000 / 60) + " minutes";
                Misc.log("ValidateUsers", "removes group", info);
                refGroups.child(groupRequest.getId()).removeValueAsync();
//                refGroups.child(groupRequest.getId()).removeValue();//FIXME
                ((StatisticsGroup) getFireBus().getHolder(StatisticsGroup.TYPE))
                        .setAction(AbstractDataProcessor.Action.GROUP_DELETED)
                        .setMessage(info)
                        .call(null, groupRequest);
            }
        }).start();
    }
}
