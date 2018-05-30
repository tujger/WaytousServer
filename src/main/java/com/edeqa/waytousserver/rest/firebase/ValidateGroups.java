package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class ValidateGroups extends AbstractFirebaseAction<ValidateGroups, Object> {

    public static final String TYPE = "/rest/firebase/validate/groups";

    private Runnable onFinish;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, Object request) {

        final DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);

        getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_MISC).child(Firebase.STAT_MISC_GROUPS_CLEANED).setValueAsync(ServerValue.TIMESTAMP);

        Map<String, String> map = new HashMap<>();
//        map.put("group", groupRequest.getId());
//        map.put("user", userId);
        map.put("action", Firebase.STAT_MISC_GROUPS_CLEANED);
        ((StatisticsMessage) getFireBus().getHolder(StatisticsMessage.TYPE))
//                .setMessage(getMessage())
                .call(null, map);

        Misc.log("ValidateGroups", "is performing");
        new TaskSingleValueEventFor<JSONObject>(refGroups.child("/")).setFirebaseRest().addOnCompleteListener(jsonGroups -> {
            try {
                Misc.log("ValidateGroups", "found groups:", jsonGroups.length() + ", checking online users");

                Iterator<String> iter = jsonGroups.keys();
                AtomicInteger count = new AtomicInteger(0);
                while (iter.hasNext()) {
                    final String group = iter.next();
                    if (group.startsWith("_") || "overview".equals(group)) {
                        Misc.log("ValidateGroups", "skips:", group);
                        continue;
                    }

                    count.getAndIncrement();

                    new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(group).child(Firebase.OPTIONS)).addOnCompleteListener(dataSnapshot -> {
                        Map value = (Map) dataSnapshot.getValue();

                        GroupRequest groupRequest = new GroupRequest(group);

                        if (value == null) {
                            Misc.log("ValidateGroups", "removes lost group");
                            refGroups.child(group).removeValueAsync();
                            ((StatisticsGroup) getFireBus().getHolder(StatisticsGroup.TYPE))
                                    .setAction(AbstractDataProcessor.Action.GROUP_DELETED)
                                    .setMessage("lost group removing: " + group)
                                    .call(null, groupRequest);
                        } else {
                            Object object = value.get(Firebase.REQUIRES_PASSWORD);
                            groupRequest.setRequiresPassword(object != null && (boolean) object);

                            object = value.get(Firebase.DISMISS_INACTIVE);
                            groupRequest.setDismissInactive(object != null && (boolean) object);

                            object = value.get(Firebase.PERSISTENT);
                            groupRequest.setPersistent(object != null && (boolean) object);

                            object = value.get(Firebase.DELAY_TO_DISMISS);
                            groupRequest.setDelayToDismiss(object != null ? Long.parseLong("0" + object.toString()) : 0);

                            object = value.get(Firebase.TIME_TO_LIVE_IF_EMPTY);
                            groupRequest.setTimeToLiveIfEmpty(object != null ? Long.parseLong("0" + object.toString()) : 0);

                            new ValidateUsers()
                                    .call(null, groupRequest);
                        }
                        count.decrementAndGet();
                        if(count.get() == 0 && getOnFinish() != null) {
                            getOnFinish().run();
                        }
                    }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

               /*try {
            System.out.println("https://waytous-beta.firebaseio.com/.json?shallow=true&print=pretty&auth="+OPTIONS.getFirebaseApiKey());

            String res = Utils.getUrl("https://waytous-beta.firebaseio.com/.json?shallow=true&print=pretty&auth="+OPTIONS.getFirebaseApiKey(),"UTF-8");

            JSONObject groups = new JSONObject(res);

            Iterator<String> iter = groups.keys();
            while(iter.hasNext()) {
                final String group = iter.next();
                if(Constants.DATABASE.SECTION_GROUPS.equals(group) || "overview".equals(group)) continue;

                new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(group).child(Constants.DATABASE.OPTIONS))
                        .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                            @Override
                            public void onEvent(DataSnapshot dataSnapshot) {
                                Map value = (Map) dataSnapshot.getValue();

                                Common.log(LOG, "Group found:", group*//* + ", leader id:", leader, dataSnapshot.getValue()*//*);

                                if (value == null) {
                                    Common.log(LOG, "--- corrupted group detected, removing ----- 1"); //TODO
                                    refGroups.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                    refGroups.child(group).removeValue();
                                    return;
                                }

                                final boolean requiresPassword;
                                final boolean dismissInactive;
                                final boolean persistent;
                                final long delayToDismiss;
                                final long timeToLiveIfEmpty;


                                Object object = value.get(Constants.DATABASE.REQUIRES_PASSWORD);
                                requiresPassword = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.DISMISS_INACTIVE);
                                dismissInactive = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.PERSISTENT);
                                persistent = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.DELAY_TO_DISMISS);
                                if (object != null)
                                    delayToDismiss = Long.parseLong("0" + object.toString());
                                else delayToDismiss = 0;

                                object = value.get(Constants.DATABASE.TIME_TO_LIVE_IF_EMPTY);
                                if (object != null)
                                    timeToLiveIfEmpty = Long.parseLong("0" + object.toString());
                                else timeToLiveIfEmpty = 0;

                                new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(group).child(Constants.DATABASE.SECTION_USERS_DATA))
                                        .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                            @Override
                                            public void onEvent(DataSnapshot dataSnapshot) {
                                                Common.log(LOG, "Users validation for group:", group);

                                                ArrayList<Map<String, Serializable>> users = null;
                                                try {
                                                    users = (ArrayList<Map<String, Serializable>>) dataSnapshot.getValue();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                if (users == null) {
                                                    Common.log(LOG, "--- corrupted group detected, removing: ----- 2"); //TODO
                                                    refGroups.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                                    refGroups.child(group).removeValue();
                                                    return;
                                                }
                                                long groupChanged = 0;

                                                for (int i = 0; i < users.size(); i++) {
                                                    Map<String, Serializable> user = users.get(i);
                                                    if (user == null) continue;

                                                    String name = (String) user.get(Constants.DATABASE.NAME);
                                                    Long changed = (Long) user.get(Constants.DATABASE.CHANGED);
                                                    if (changed != null && changed > groupChanged)
                                                        groupChanged = changed;
                                                    boolean active = false;
                                                    Object object = user.get(Constants.DATABASE.ACTIVE);
                                                    if (object != null) {
                                                        active = (Boolean) object;
                                                    }

                                                    if (!active) continue;

                                                    if (dismissInactive) {
                                                        Long current = new Date().getTime();
                                                        if (changed == null) {
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is NULL");
                                                            dataSnapshot.getRef().child("" + i).child(Constants.DATABASE.ACTIVE).setValue(false);
                                                        } else if (current - delayToDismiss * 1000 > changed) {
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is EXPIRED for", ((current - delayToDismiss * 1000 - changed) / 1000), "seconds");
                                                            dataSnapshot.getRef().child("" + i).child(Constants.DATABASE.ACTIVE).setValue(false);
                                                        } else {
                                                            dataSnapshot.getRef().getParent().getParent().child(Constants.DATABASE.OPTIONS).child(Constants.DATABASE.CHANGED).setValue(changed);
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is OK");
                                                        }
                                                    }
                                                }

                                                if (!persistent && timeToLiveIfEmpty > 0 && new Date().getTime() - groupChanged > timeToLiveIfEmpty * 60 * 1000) {
                                                    Common.log(LOG, "--- removing group " + group + " expired for", (new Date().getTime() - groupChanged - timeToLiveIfEmpty * 60 * 1000) / 1000 / 60, "minutes");
                                                    refGroups.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                                    refGroups.child(group).removeValue();
                                                }
                                            }
                                        }).start();
                            }
                        }).start();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    public Runnable getOnFinish() {
        return onFinish;
    }

    public ValidateGroups setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
        return this;
    }
}
