package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("unused")
public class ValidateGroups implements NamedCall {

    private DatabaseReference firebaseStat;
    private DatabaseReference firebaseGroups;
    private String firebaseAccessToken;
    private StatisticsGroups statisticsGroups;

    @Override
    public String getName() {
        return "validate/groups";
    }

    @Override
    public void call(JSONObject json, Object request) {

        getFirebaseStat().child(Firebase.STAT_MISC).child(Firebase.STAT_MISC_GROUPS_CLEANED).setValue(ServerValue.TIMESTAMP);

        Misc.log("ValidateGroups", "Groups validation is performing, checking online users");
        new TaskSingleValueEventFor<JSONObject>(getFirebaseGroups().child("/")).setFirebaseRest(getFirebaseAccessToken()).addOnCompleteListener(new Runnable1<JSONObject>() {
            @Override
            public void call(JSONObject groups) {
                try {
                    Iterator<String> iter = groups.keys();
                    while (iter.hasNext()) {
                        final String group = iter.next();
                        if (group.startsWith("_") || "overview".equals(group)) {
                            Misc.log("ValidateGroups", "Key skipped: " + group);
                            continue;
                        }

                        new TaskSingleValueEventFor<DataSnapshot>(getFirebaseGroups().child(group).child(Firebase.OPTIONS))
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        Map value = (Map) dataSnapshot.getValue();

                                        Misc.log("ValidateGroups", "Group found:", group/* + ", leader id:", leader, dataSnapshot.getValue()*/);

                                        if (value == null) {
                                            Misc.log("ValidateGroups", "--- corrupted group detected, removing ----- 1"); //TODO
                                            getFirebaseGroups().child(group).removeValue();
                                            getStatisticsGroups().setGroupId(group).setPersistent(false).setGroupAction(AbstractDataProcessor.GroupAction.GROUP_DELETED).setErrorMessage("corrupted group detected, removing: ----- 1").call(null, null);
                                            return;
                                        }

                                        final boolean requiresPassword;
                                        final boolean dismissInactive;
                                        final boolean persistent;
                                        final long delayToDismiss;
                                        final long timeToLiveIfEmpty;


                                        Object object = value.get(Firebase.REQUIRES_PASSWORD);
                                        requiresPassword = object != null && (boolean) object;

                                        object = value.get(Firebase.DISMISS_INACTIVE);
                                        dismissInactive = object != null && (boolean) object;

                                        object = value.get(Firebase.PERSISTENT);
                                        persistent = object != null && (boolean) object;

                                        object = value.get(Firebase.DELAY_TO_DISMISS);
                                        if (object != null)
                                            delayToDismiss = Long.parseLong("0" + object.toString());
                                        else delayToDismiss = 0;

                                        object = value.get(Firebase.TIME_TO_LIVE_IF_EMPTY);
                                        if (object != null)
                                            timeToLiveIfEmpty = Long.parseLong("0" + object.toString());
                                        else timeToLiveIfEmpty = 0;

                                        new TaskSingleValueEventFor<DataSnapshot>(getFirebaseGroups().child(group).child(Firebase.USERS).child(Firebase.PUBLIC))
                                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                                    @Override
                                                    public void call(DataSnapshot dataSnapshot) {
                                                        Misc.log("ValidateGroups", "Users validation for group:", group);

                                                        ArrayList<Map<String, Serializable>> users = null;
                                                        try {
                                                            //noinspection unchecked
                                                            users = (ArrayList<Map<String, Serializable>>) dataSnapshot.getValue();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        if (users == null) {
                                                            Misc.log("ValidateGroups", "--- corrupted group detected, removing: ----- 2"); //TODO
                                                            getFirebaseGroups().child(group).removeValue();
                                                            getStatisticsGroups().setGroupId(group).setPersistent(false).setGroupAction(AbstractDataProcessor.GroupAction.GROUP_DELETED).setErrorMessage("corrupted group detected, removing: ----- 2").call(null, null);
                                                            return;
                                                        }
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

                                                            if (dismissInactive) {
                                                                Long current = new Date().getTime();
                                                                if (changed == null) {
                                                                    Misc.log("ValidateGroups", "--- user:", i, "name:", name, "is NULL");
                                                                    dataSnapshot.getRef().child("" + i).child(Firebase.ACTIVE).setValue(false);
                                                                } else if (current - delayToDismiss * 1000 > changed) {
                                                                    Misc.log("ValidateGroups", "--- user:", i, "name:", name, "is EXPIRED for", ((current - delayToDismiss * 1000 - changed) / 1000), "seconds");
                                                                    dataSnapshot.getRef().child("" + i).child(Firebase.ACTIVE).setValue(false);
                                                                } else {
                                                                    dataSnapshot.getRef().getParent().getParent().child(Firebase.OPTIONS).child(Firebase.CHANGED).setValue(changed);
                                                                    Misc.log("ValidateGroups", "--- user:", i, "name:", name, "is OK");
                                                                }
                                                            }
                                                        }

                                                        if (!persistent && timeToLiveIfEmpty > 0 && new Date().getTime() - groupChanged > timeToLiveIfEmpty * 60 * 1000) {
                                                            String info = group + " expired for " + ((new Date().getTime() - groupChanged - timeToLiveIfEmpty * 60 * 1000) / 1000 / 60) + " minutes";
                                                            Misc.log("ValidateGroups", "--- removing group " + info);
                                                            getFirebaseGroups().child(group).removeValue();
                                                            getStatisticsGroups().setGroupId(group).setPersistent(false).setGroupAction(AbstractDataProcessor.GroupAction.GROUP_DELETED).setErrorMessage(info).call(null, null);
                                                        }
                                                    }
                                                }).start();
                                    }
                                }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                            public void call(DataSnapshot dataSnapshot) {
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
                                            public void call(DataSnapshot dataSnapshot) {
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

//        json.put(STATUS, STATUS_SUCCESS);
    }

    public DatabaseReference getFirebaseStat() {
        return firebaseStat;
    }

    public ValidateGroups setFirebaseStat(DatabaseReference firebaseStat) {
        this.firebaseStat = firebaseStat;
        return this;
    }

    public DatabaseReference getFirebaseGroups() {
        return firebaseGroups;
    }

    public ValidateGroups setFirebaseGroups(DatabaseReference firebaseGroups) {
        this.firebaseGroups = firebaseGroups;
        return this;
    }

    public String getFirebaseAccessToken() {
        return firebaseAccessToken;
    }

    public ValidateGroups setFirebaseAccessToken(String firebaseAccessToken) {
        this.firebaseAccessToken = firebaseAccessToken;
        return this;
    }

    public StatisticsGroups getStatisticsGroups() {
        return statisticsGroups;
    }

    public ValidateGroups setStatisticsGroups(StatisticsGroups statisticsGroups) {
        this.statisticsGroups = statisticsGroups;
        return this;
    }
}
