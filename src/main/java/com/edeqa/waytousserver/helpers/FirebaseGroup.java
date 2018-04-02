package com.edeqa.waytousserver.helpers;

public class FirebaseGroup {

    public Long ch;
    public Long cr;
    public Integer timeToLiveIfEmpty;
    public Integer delayToDismiss;
    public Integer limitUsers;
    public Boolean requiresPassword;
    public Boolean persistent;
    public Boolean dismissInactive;

    public FirebaseGroup() {
    }

    @Override
    public String toString() {
        return "FirebaseGroup{" +
                       "ch=" + ch +
                       ", cr=" + cr +
                       ", timeToLiveIfEmpty=" + timeToLiveIfEmpty +
                       ", delayToDismiss=" + delayToDismiss +
                       ", limitUsers=" + limitUsers +
                       ", requiresPassword=" + requiresPassword +
                       ", persistent=" + persistent +
                       ", dismissInactive=" + dismissInactive +
                       '}';
    }
}
