package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.edequate.interfaces.NamedCall;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

@SuppressWarnings("unused")
abstract public class AbstractAction<U extends NamedCall, T> implements NamedCall<T> {

    private DatabaseReference firebaseReference;

    @Override
    abstract public String getName();

    @Override
    abstract public void call(JSONObject json, T object) throws Exception;


    public DatabaseReference getFirebaseReference() {
        return firebaseReference;
    }

    public U setFirebaseReference(DatabaseReference firebaseReference) {
        this.firebaseReference = firebaseReference;
        return (U) this;
    }
}
