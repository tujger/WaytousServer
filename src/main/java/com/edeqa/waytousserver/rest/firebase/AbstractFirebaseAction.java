package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.rest.tracking.AbstractTrackingAction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

@SuppressWarnings("unused")
abstract public class AbstractFirebaseAction<U extends AbstractAction, T> extends AbstractAction<T> {

    public static final String EVENTBUS = "firebase";


    private static DatabaseReference firebaseReference;

    public static DatabaseReference getFirebaseReference() {
        return firebaseReference;
    }

    public static void setFirebaseReference(DatabaseReference firebaseReference) {
        AbstractFirebaseAction.firebaseReference = firebaseReference;
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

    protected EventBus<AbstractFirebaseAction> getFireBus() {
        //noinspection unchecked
        return (EventBus<AbstractFirebaseAction>) EventBus.getOrCreate(AbstractFirebaseAction.EVENTBUS);
    }

    @SuppressWarnings("WeakerAccess")
    protected EventBus<AbstractTrackingAction> getTrackingBus() {
        //noinspection unchecked
        return (EventBus<AbstractTrackingAction>) EventBus.getOrCreate(AbstractTrackingAction.EVENTBUS);
    }
}
