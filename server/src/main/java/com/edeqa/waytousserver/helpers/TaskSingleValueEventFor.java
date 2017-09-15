package com.edeqa.waytousserver.helpers;

import com.edeqa.waytous.interfaces.Runnable1;
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

import static com.edeqa.waytous.Constants.SENSITIVE;

/**
 * Created 6/13/2017.
 */

public class TaskSingleValueEventFor<T> {
    private static final String LOG = "TSVEF";
    private DatabaseReference ref;
    private boolean firebaseRest = false;
    private String customToken;

    private Runnable1<T> onCompleteListener = new Runnable1<T>() {
        @Override
        public void call(T arg) {
            //noinspection HardCodedStringLiteral
            System.out.println("onCompleteListener:"+arg.toString());
        }
    };

    public TaskSingleValueEventFor() {
    }

    public TaskSingleValueEventFor(DatabaseReference ref) {
        this.ref = ref;
    }

    public TaskSingleValueEventFor setRef(DatabaseReference ref) {
        this.ref = ref;
        return this;
    }

    public TaskSingleValueEventFor addOnCompleteListener(Runnable1<T> listener) {
        onCompleteListener = listener;
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
                onCompleteListener.call((T) dataSnapshot);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                onCompleteListener.call(null);
            }
        }
    }

    private void restRequestWithTokenUpdate() {

        try {
            GoogleCredential googleCred = GoogleCredential.fromStream(new FileInputStream(SENSITIVE.getFirebasePrivateKeyFile()));
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
            String res = Utils.getUrl(url, "UTF-8");
            if(res == null || res.length() == 0 || res.startsWith("null")) {
                return;
            }
            Common.log(LOG, res);

            JSONObject json = new JSONObject(res);
            onCompleteListener.call((T) json);

        } catch(Exception e) {
            Common.err(LOG, "restRequest:error:"+ref.getDatabase().getReference() + ref.getPath(), e.getMessage());
            e.printStackTrace();
        }
    }


    public boolean isFirebaseRest() {
        return firebaseRest;
    }

    public TaskSingleValueEventFor setFirebaseRest(boolean firebaseRest) {
        this.firebaseRest = firebaseRest;
        return this;
    }
}
