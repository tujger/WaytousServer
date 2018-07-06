package com.edeqa.waytousserver.helpers;

import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytousserver.rest.firebase.AbstractFirebaseAction;
import com.edeqa.waytousserver.rest.firebase.AdminToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

/**
 * Created 6/13/2017.
 */

public class TaskSingleValueEventFor<T> {
    private static final String LOG = "TSVEF";
    private DatabaseReference ref;
    private boolean firebaseRest = false;
    private boolean checkExisting;

    private Consumer<T> onCompleteListener;
    private Consumer<T> onSuccessListener;
    //noinspection HardCodedStringLiteral
    private Consumer<Throwable> onFailureListener = Throwable::printStackTrace;

    public TaskSingleValueEventFor() {
    }

    public TaskSingleValueEventFor(DatabaseReference ref) {
        this.ref = ref;
    }

    public TaskSingleValueEventFor<T> setRef(DatabaseReference ref) {
        this.ref = ref;
        return this;
    }

    public TaskSingleValueEventFor<T> addOnCompleteListener(Consumer<T> listener) {
        onCompleteListener = listener;
        return this;
    }

    public TaskSingleValueEventFor<T> addOnSuccessListener(Consumer<T> listener) {
        onSuccessListener = listener;
        return this;
    }

    public TaskSingleValueEventFor<T> addOnFailureListener(Consumer<Throwable> listener) {
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
        if(isFirebaseRest() || checkExisting) {
            restRequest();
/*
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
*/
        } else {
//            final TaskCompletionSource<DataSnapshot> tcs = new TaskCompletionSource<>();
//            ref.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    tcs.setResult(dataSnapshot);
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    tcs.setException(databaseError.toException());
//                    databaseError.toException().printStackTrace();
//                }
//            });
//            Task<DataSnapshot> task = tcs.getTask();
//            try {
//                Tasks.await(task);
//                DataSnapshot dataSnapshot = task.getResult();
//                if(onSuccessListener != null) //noinspection unchecked
//                    onSuccessListener.accept((T) dataSnapshot);
//                if(onCompleteListener != null) //noinspection unchecked
//                    onCompleteListener.accept((T) dataSnapshot);
//            } catch (ExecutionException | InterruptedException e) {
//                e.printStackTrace();
//                if(onFailureListener != null) onFailureListener.accept(e);
//                if(onCompleteListener != null) onCompleteListener.accept(null);
//            }

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (onSuccessListener != null) //noinspection unchecked
                            onSuccessListener.accept((T) dataSnapshot);
                        if (onCompleteListener != null) //noinspection unchecked
                            onCompleteListener.accept((T) dataSnapshot);
                    } catch (Exception e) {
                        Misc.err(LOG, "failed:", e);
                        if (onFailureListener != null) onFailureListener.accept(e);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                    if (onFailureListener != null) onFailureListener.accept(databaseError.toException());
                    if (onCompleteListener != null) onCompleteListener.accept(null);
                }
            });

        }
    }

    private void restRequest() {
        try {
            String customToken = ((AdminToken) EventBus.getOrCreate(AbstractFirebaseAction.EVENTBUS).getHolder(AdminToken.TYPE)).fetchToken();
            String url = "" + ref.getDatabase().getReference() + ref.getPath() + ".json?shallow=true&access_token=" + customToken;
            Misc.log(LOG, "restRequest:" + url);
            String res = Misc.getUrl(url, "UTF-8");
//            Misc.log(LOG, "restResult:" + res);
            JSONObject json = null;
            if(res.length() == 0 || res.startsWith("null")) {
//                json = new JSONObject();
            } else {
                json = new JSONObject(res);
            }
//            Common.log(LOG, res);

            if(onSuccessListener != null) //noinspection unchecked
                onSuccessListener.accept((T) json);
            if(onCompleteListener != null) //noinspection unchecked
                onCompleteListener.accept((T) json);

        } catch(Exception e) {
            Misc.err(LOG, "restRequest:error:"+ref.getDatabase().getReference() + ref.getPath(), e.getMessage());
            if(onFailureListener != null) onFailureListener.accept(e);
            if(onCompleteListener != null) onCompleteListener.accept(null);
        }
    }

    public boolean isFirebaseRest() {
        return firebaseRest;
    }

    public TaskSingleValueEventFor<T> setFirebaseRest() {
        this.firebaseRest = true;
        return this;
    }

    public TaskSingleValueEventFor<T> ifExists() {
        checkExisting = true;
        return this;
    }
}
