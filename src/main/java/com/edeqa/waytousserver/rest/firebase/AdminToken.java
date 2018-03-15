package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Calendar;

@SuppressWarnings("unused")
/*
 * This class requests and returns accessToken for Firebase. Depending on current installation type
 * it defines the properly request and performs it. Installation type can be defined in gradle.build.
 */
public class AdminToken extends AbstractFirebaseAction<AdminToken, Object> {

    public static final String TYPE = "/rest/firebase/token/access";

    private String firebasePrivateKeyFile;
    private Long timeCreated = 0L;
    private String token;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, Object request) throws Exception {
        Misc.log("AdminToken", "is performing");

        Calendar cal = Calendar.getInstance();
        Long now = cal.getTime().getTime();

        if(token == null || timeCreated < now - 10*60*1000) {
            try (FileInputStream serviceAccount = new FileInputStream(getFirebasePrivateKeyFile()) ) {
                GoogleCredential googleCred = GoogleCredential.fromStream(serviceAccount);
                GoogleCredential scoped = googleCred.createScoped(
                        Arrays.asList(
                                "https://www.googleapis.com/auth/firebase.database",
                                "https://www.googleapis.com/auth/userinfo.email"
                        )
                );
                scoped.refreshToken();
                token = scoped.getAccessToken();
                Misc.log("AdminToken", "generated", "[" + token + "]");
                timeCreated = now;
            }
        }

        json.put(STATUS, STATUS_SUCCESS);
        json.put(MESSAGE, token);
    }

    public String fetchToken() {
        JSONObject json = new JSONObject();
        try {
            call(json, null);
            return json.getString(MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getFirebasePrivateKeyFile() {
        return firebasePrivateKeyFile;
    }

    public AdminToken setFirebasePrivateKeyFile(String firebasePrivateKeyFile) {
        this.firebasePrivateKeyFile = firebasePrivateKeyFile;
        return this;
    }
}
