package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.waytous.Firebase;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class StatisticsMessage extends AbstractFirebaseAction<StatisticsMessage, Map<String,String>> {

    public static final String TYPE = "/rest/firebase/statistics/message";

    private DatabaseReference firebaseReference;
    private String message;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean onEvent(JSONObject json, Map<String,String> payload) {
        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d %02d-%02d-%02d-%03d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));

        if(payload == null) {
            payload = new HashMap<>();
        }
        payload.put(MESSAGE, getMessage());
        getFirebaseReference().child(Firebase.SECTION_STAT).child(Firebase.STAT_MESSAGES).child(today).setValue(payload);
        clear();
        return true;
    }

    public StatisticsMessage clear() {
        setMessage(null);
        return this;
    }

    public String getMessage() {
        return message;
    }

    public StatisticsMessage setMessage(String message) {
        this.message = message;
        return this;
    }

}
