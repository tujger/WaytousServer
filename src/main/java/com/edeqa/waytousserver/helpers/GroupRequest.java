package com.edeqa.waytousserver.helpers;

import com.edeqa.helpers.Misc;

public class GroupRequest {

    private String id;
    private boolean requiresPassword;
    private String password;
    private String welcomeMessage;
    private boolean persistent;
    private long timeToLiveIfEmpty;
    private boolean dismissInactive;
    private long delayToDismiss;
    private MyGroup group;

    public GroupRequest() {
        this(null);
        fetchNewId();
    }

    public GroupRequest(String id) {
        this.id = id;
        setDelayToDismiss(3600);
        setDismissInactive(false);
        setPersistent(false);
        setRequiresPassword(false);
        setTimeToLiveIfEmpty(24 * 60);
    }

    public void fetchNewId() {
        id = Misc.getUnique();
    }

    public MyGroup fetchGroup() {
        if(group != null) return group;
        group = new MyGroup();
        group.setId(getId());
        group.setRequiresPassword(isRequiresPassword());
        group.setPassword(getPassword());
        group.setPersistent(isPersistent());
        group.setTimeToLiveIfEmpty((int) getTimeToLiveIfEmpty());
        group.setDismissInactive(isDismissInactive());
        group.setDelayToDismiss((int) getDelayToDismiss());
        group.setWelcomeMessage(getWelcomeMessage());
        return group;
    }

    public String getId() {
        return id;
    }

    public GroupRequest setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isRequiresPassword() {
        return requiresPassword;
    }

    public GroupRequest setRequiresPassword(boolean requiresPassword) {
        this.requiresPassword = requiresPassword;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public GroupRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public GroupRequest setPersistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public long getTimeToLiveIfEmpty() {
        return timeToLiveIfEmpty;
    }

    public GroupRequest setTimeToLiveIfEmpty(long timeToLiveIfEmpty) {
        this.timeToLiveIfEmpty = timeToLiveIfEmpty;
        return this;
    }

    public boolean isDismissInactive() {
        return dismissInactive;
    }

    public GroupRequest setDismissInactive(boolean dismissInactive) {
        this.dismissInactive = dismissInactive;
        return this;
    }

    public long getDelayToDismiss() {
        return delayToDismiss;
    }

    public GroupRequest setDelayToDismiss(long delayToDismiss) {
        this.delayToDismiss = delayToDismiss;
        return this;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public GroupRequest setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
        return this;
    }

    @Override
    public String toString() {
        return "GroupRequest{" +
                "id='" + id + '\'' +
                ", requiresPassword=" + requiresPassword +
                (requiresPassword ? ", password='" + password + '\'' : "") +
                (welcomeMessage != null ? ", welcomeMessage='" + welcomeMessage + '\'' : "") +
                ", persistent=" + persistent +
                (persistent ? ", timeToLiveIfEmpty=" + timeToLiveIfEmpty : "") +
                ", dismissInactive=" + dismissInactive +
                (dismissInactive ? ", delayToDismiss=" + delayToDismiss : "") +
                '}';
    }
}
