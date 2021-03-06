package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;

@SuppressWarnings("unused")
public class ValidateAccounts extends AbstractFirebaseAction<ValidateAccounts, Object> {

    public static final String TYPE = "/rest/firebase/validate/accounts";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, Object request) {

        final DatabaseReference refAccounts = getFirebaseReference().child(Firebase.SECTION_USERS);

        getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_MISC).child(Firebase.STAT_MISC_ACCOUNTS_CLEANED).setValueAsync(ServerValue.TIMESTAMP);
//        getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_MISC).child(Firebase.STAT_MISC_ACCOUNTS_CLEANED).setValue(ServerValue.TIMESTAMP);//FIXME

        Misc.log("ValidateAccounts", "is performing, checking online users");

        new TaskSingleValueEventFor<JSONObject>(refAccounts).setFirebaseRest().addOnCompleteListener(jsonAccounts -> {
            try {
                Iterator<String> iter = jsonAccounts.keys();
                while (iter.hasNext()) {
                    final String uid = iter.next();

                    new TaskSingleValueEventFor<DataSnapshot>(refAccounts.child(uid).child(Firebase.PRIVATE)).addOnCompleteListener(dataSnapshot -> {
                        try {
                            Map value = (Map) dataSnapshot.getValue();
                            boolean expired = false;
                            boolean trusted = false;
                            if (value.containsKey(REQUEST_SIGN_PROVIDER) && !"anonymous".equals(value.get(REQUEST_SIGN_PROVIDER))) {
                                trusted = true;
                            }

                            if (value.containsKey(Firebase.CHANGED)) {
                                if ((new Date().getTime() - (long) value.get(Firebase.CHANGED)) > 30 * 24 * 60 * 60 * 1000L) {
                                    expired = true;
                                }
                            } else {
                                expired = true;
                            }

                            if (!trusted && expired) {
                                String message = Misc.durationToString(new Date().getTime() - (long) value.get(Firebase.CHANGED));
                                Misc.log("ValidateAccounts", "removes:", uid, "expired for:", message);

                                refAccounts.child(uid).setValueAsync(null);
//                                refAccounts.child(uid).setValue(null);//FIXME
                                ((StatisticsAccount) getFireBus().getHolder(StatisticsAccount.TYPE))
                                        .setAction(AbstractDataProcessor.Action.ACCOUNT_DELETED)
                                        .setKey(null)
                                        .setValue(null)
                                        .setMessage("Expired for " + message)
                                        .call(null, uid);
                            }
                        } catch(Exception e) {
                            Misc.err("ValidateAccounts", "failed:", uid, "error:", e.getMessage());
                        }
                    }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
