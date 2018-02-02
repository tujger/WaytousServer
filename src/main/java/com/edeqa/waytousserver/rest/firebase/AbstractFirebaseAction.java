package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.helpers.Misc;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.json.JSONObject;

@SuppressWarnings("unused")
abstract public class AbstractFirebaseAction<U extends AbstractAction, T> extends AbstractAction<T> {

    private DatabaseReference firebaseReference;

    public DatabaseReference getFirebaseReference() {
        return firebaseReference;
    }

    public U setFirebaseReference(DatabaseReference firebaseReference) {
        this.firebaseReference = firebaseReference;
        return (U) this;
    }

    protected Transaction.Handler incrementValue = new Transaction.Handler() {
        @Override
        public Transaction.Result doTransaction(MutableData mutableData) {
            Integer value = mutableData.getValue(Integer.class);
            if (value == null) {
                value = 0;
            }
            value++;
            mutableData.setValue(value);
            return Transaction.success(mutableData);
        }

        @Override
        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
            // Transaction completed
            if (databaseError != null) {
                Misc.err("AbstractFirebaseAction", "increment error:", databaseError);
            }
        }
    };

}
