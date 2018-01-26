package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.helpers.Misc;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

@SuppressWarnings("unused")
/*
 * This method requests and returns accessToken for Firebase. Depending on current installation type
 * it defines the properly request and performs it. Installation type can be defined in gradle.build.
 */
public class AccessToken implements NamedCall {

    private String firebasePrivateKeyFile;

    @Override
    public String getName() {
        return "firebase/accesstoken";
    }

    @Override
    public void call(JSONObject json, Object request) throws Exception {
        String token = "";
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

            json.put(STATUS, STATUS_SUCCESS);
            json.put(MESSAGE, token);
    }

    public String fetchToken() {
        JSONObject json = new JSONObject();
        Misc.log("AccessToken", "performing");
        try {
            call(json, null);
            Misc.log("AccessToken", "->", json.getString(MESSAGE));
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
