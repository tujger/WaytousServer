package com.edeqa.waytousserver.rest.tracking;

import com.edeqa.eventbus.AbstractEntityHolder;
import com.edeqa.waytousserver.interfaces.RequestHolder;

import java.util.List;

public abstract class AbstractTrackingAction extends AbstractEntityHolder implements RequestHolder {

    public static final String EVENTBUS = "tracking";


    @Override
    public boolean isSaveable() {
        return false;
    }

    @Override
    public List<String> events() {
        return null;
    }

    @Override
    public void start() {
    }

    @Override
    public void finish() {
    }

    @Override
    public boolean onEvent(String eventName, Object eventObject) {
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "type=" + getType() +
                ", saveable=" + isSaveable() +
                '}';
    }
}
