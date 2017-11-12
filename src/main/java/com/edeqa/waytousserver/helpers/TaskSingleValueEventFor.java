package com.edeqa.waytousserver.helpers;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.TaskCompletionSource;
import com.google.firebase.tasks.Tasks;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static com.edeqa.waytous.Constants.OPTIONS;

/**
 * Created 6/13/2017.
 */

public class TaskSingleValueEventFor<T> {
    private static final String LOG = "TSVEF";
    private DatabaseReference ref;
    private boolean firebaseRest = false;
    private String customToken;

    private Runnable1<T> onCompleteListener;
    private Runnable1<T> onSuccessListener;
    private Runnable1<Throwable> onFailureListener = new Runnable1<Throwable>() {
        @Override
        public void call(Throwable arg) {
            //noinspection HardCodedStringLiteral
            arg.printStackTrace();
        }
    };

    public TaskSingleValueEventFor() {
    }

    public TaskSingleValueEventFor(DatabaseReference ref) {
        this.ref = ref;
    }

    public TaskSingleValueEventFor<T> setRef(DatabaseReference ref) {
        this.ref = ref;
        return this;
    }

    public TaskSingleValueEventFor<T> addOnCompleteListener(Runnable1<T> listener) {
        onCompleteListener = listener;
        return this;
    }

    public TaskSingleValueEventFor<T> addOnSuccessListener(Runnable1<T> listener) {
        onSuccessListener = listener;
        return this;
    }

    public TaskSingleValueEventFor<T> addOnFailureListener(Runnable1<Throwable> listener) {
        onFailureListener = listener;
        return this;
    }

    public void start() {
        if(ref == null) {
            //noinspection HardCodedStringLiteral
            System.err.println("TaskSingleValueEventFor: ref is not defined.");
            Thread.dumpStack();
            return;
        }
        if(isFirebaseRest()) {
            if(customToken == null) {
                HashMap<String, Object> additionalClaims = new HashMap<String, Object>();
                additionalClaims.put("Administrator", true);
                FirebaseAuth.getInstance().createCustomToken("Administrator", additionalClaims)
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String token) {
                                customToken = token;
                                // Send token back to client
                                restRequestWithTokenUpdate();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                            }
                        });
            } else {
                restRequest();
            }
        } else {
            final TaskCompletionSource<DataSnapshot> tcs = new TaskCompletionSource<>();
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tcs.setResult(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    tcs.setException(databaseError.toException());
                    databaseError.toException().printStackTrace();
                }
            });
            Task<DataSnapshot> task = tcs.getTask();
            try {
                Tasks.await(task);
                DataSnapshot dataSnapshot = task.getResult();
                if(onSuccessListener != null) onSuccessListener.call((T) dataSnapshot);
                if(onCompleteListener != null) onCompleteListener.call((T) dataSnapshot);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                if(onFailureListener != null) onFailureListener.call(e);
                if(onCompleteListener != null) onCompleteListener.call(null);
            }
        }
    }

    private void restRequestWithTokenUpdate() {

        try {
            GoogleCredential googleCred = GoogleCredential.fromStream(new FileInputStream(OPTIONS.getFirebasePrivateKeyFile()));
            GoogleCredential scoped = googleCred.createScoped(
                    Arrays.asList(
                            "https://www.googleapis.com/auth/firebase.database",
                            "https://www.googleapis.com/auth/userinfo.email"
                    )
            );
            scoped.refreshToken();
            customToken = scoped.getAccessToken();
            restRequest();
        } catch(Exception e) {
            e.printStackTrace();
        }
        /*HashMap<String, Object> additionalClaims = new HashMap<String, Object>();
        additionalClaims.put("Administrator", true);
        FirebaseAuth.getInstance().createCustomToken("Administrator", additionalClaims)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String customToken) {
                        // Send token back to client
                        Common.log(LOG, "--debug-- init003:"+customToken);
                        restRequest();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Common.err(LOG, "--debug-- init004:"+e);
                        onCompleteListener.call(null);
                        e.printStackTrace();
                    }
                });*/
    }

    private void restRequest() {
        try {
            String url = "" + ref.getDatabase().getReference() + ref.getPath() + ".json?shallow=true&print=pretty&access_token=" + customToken;
            Common.log(LOG, "restRequest:"+url);
            String res = Misc.getUrl(url, "UTF-8");
            if(res == null || res.length() == 0 || res.startsWith("null")) {
                return;
            }
//            Common.log(LOG, res);

            JSONObject json = new JSONObject(res);
            if(onSuccessListener != null) onSuccessListener.call((T) json);
            if(onCompleteListener != null) onCompleteListener.call((T) json);

        } catch(Exception e) {
            Common.err(LOG, "restRequest:error:"+ref.getDatabase().getReference() + ref.getPath(), e.getMessage());
            if(onFailureListener != null) onFailureListener.call(e);
            if(onCompleteListener != null) onCompleteListener.call(null);
        }
    }


    public boolean isFirebaseRest() {
        return firebaseRest;
    }

    public TaskSingleValueEventFor<T> setFirebaseRest(boolean firebaseRest) {
        this.firebaseRest = firebaseRest;
        return this;
    }
}
