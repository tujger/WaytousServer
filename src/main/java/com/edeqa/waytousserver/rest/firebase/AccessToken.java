package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Calendar;

@SuppressWarnings("unused")
/*
 * This method requests and returns accessToken for Firebase. Depending on current installation type
 * it defines the properly request and performs it. Installation type can be defined in gradle.build.
 */
public class AccessToken extends AbstractAction<AccessToken, Object> {

    private String firebasePrivateKeyFile;
    private Long timeCreated = 0L;
    private String token;

    @Override
    public String getName() {
        return "firebase/token/access";
    }

    @Override
    public void call(JSONObject json, Object request) throws Exception {
        Misc.log("AccessToken", "is performing");

        Calendar cal = Calendar.getInstance();
        Long now = cal.getTime().getTime();

        if(token == null || timeCreated < now - 30*60*1000) {
            FileInputStream serviceAccount = new FileInputStream(getFirebasePrivateKeyFile());
            GoogleCredential googleCred = GoogleCredential.fromStream(serviceAccount);
            GoogleCredential scoped = googleCred.createScoped(
                    Arrays.asList(
                            "https://www.googleapis.com/auth/firebase.database",
                            "https://www.googleapis.com/auth/userinfo.email"
                    )
            );
            scoped.refreshToken();
            token = scoped.getAccessToken();
            Misc.log("AccessToken", "generated", token);
            timeCreated = now;
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

    public String getFirebasePrivateKeyFile() {
        return firebasePrivateKeyFile;
    }

    public AccessToken setFirebasePrivateKeyFile(String firebasePrivateKeyFile) {
        this.firebasePrivateKeyFile = firebasePrivateKeyFile;
        return this;
    }
}
