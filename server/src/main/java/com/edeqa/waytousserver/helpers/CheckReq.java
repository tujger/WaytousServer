package com.edeqa.waytousserver.helpers;

import com.edeqa.waytousserver.interfaces.DataProcessorConnection;

import org.json.JSONObject;

import java.util.Date;

import static com.edeqa.waytous.Constants.REQUEST_DEVICE_ID;
import static com.edeqa.waytous.Constants.REQUEST_MANUFACTURER;
import static com.edeqa.waytous.Constants.REQUEST_MODEL;
import static com.edeqa.waytous.Constants.REQUEST_OS;
import static com.edeqa.waytous.Constants.USER_NAME;

/**
 * Created 2/2/17.
 */
public class CheckReq {

    private long timestamp;
    private MyGroup token;
    private String tokenId;
    private String control;
    private String name;
    private String uid;
    private long number;
    private MyUser user;

    public CheckReq() {
        timestamp = new Date().getTime();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyGroup getToken() {
        return token;
    }

    public void setToken(MyGroup token) {
        this.token = token;
    }

    public String getGroupId() {
        return tokenId;
    }

    public void setGroupId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    public void setUser(DataProcessorConnection connection, JSONObject request) {
        user = new MyUser(connection, request.getString(REQUEST_DEVICE_ID));

        user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
        user.setModel(request.getString(REQUEST_MODEL));
        user.setOs(request.getString(REQUEST_OS));
        if (request.has(USER_NAME)) user.setName(request.getString(USER_NAME));

    }
}
