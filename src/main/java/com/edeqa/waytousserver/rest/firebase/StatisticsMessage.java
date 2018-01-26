package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class StatisticsMessage implements NamedCall<Map<String,String>> {

    private DatabaseReference firebaseStat;
    private String message;

    @Override
    public String getName() {
        return "statistics/message";
    }

    @Override
    public void call(JSONObject json, Map<String,String> payload) {

        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d %02d-%02d-%02d-%03d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));

        if(payload == null) {
            payload = new HashMap<>();
        }
        payload.put(MESSAGE, message);
        getFirebaseStat().child(Firebase.STAT_MESSAGES).child(today).setValue(payload);

//        json.put(STATUS, STATUS_SUCCESS);
    }

    public DatabaseReference getFirebaseStat() {
        return firebaseStat;
    }

    public StatisticsMessage setFirebaseStat(DatabaseReference firebaseStat) {
        this.firebaseStat = firebaseStat;
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
