package com.edeqa.waytousserver.helpers;

import com.edeqa.helpers.Misc;
import com.edeqa.waytous.SignProvider;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.util.Date;

import static com.edeqa.waytous.Constants.REQUEST_MANUFACTURER;
import static com.edeqa.waytous.Constants.REQUEST_MODEL;
import static com.edeqa.waytous.Constants.REQUEST_OS;
import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;
import static com.edeqa.waytous.Constants.USER_NAME;

/**
 * Created 1/28/18.
 */
public class UserRequest {

    private final DataProcessorConnection dataProcessorConnection;
    private long timestamp;
    private String control;
    private String name;
    private String uid;
    private int number;
    private String groupId;
    private String manufacturer;
    private String model;
    private String os;
    private SignProvider signProvider;
    private DatabaseReference groupReference;
    private MyUser user;


    public UserRequest(DataProcessorConnection dataProcessorConnection) {
        this.dataProcessorConnection = dataProcessorConnection;
        timestamp = new Date().getTime();
        setControl(Misc.getUnique());
    }

    public void send(String string) {
        getDataProcessorConnection().send(string);
        getDataProcessorConnection().close();
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
        if(user != null) user.setNumber(number);
    }

    public DataProcessorConnection getDataProcessorConnection() {
        return dataProcessorConnection;
    }

    public String getAddress() {
        return getDataProcessorConnection().getRemoteSocketAddress().toString();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void parse(JSONObject json) {

        if (json.has(REQUEST_MANUFACTURER)) setManufacturer(json.getString(REQUEST_MANUFACTURER));
        if (json.has(REQUEST_MODEL)) setModel(json.getString(REQUEST_MODEL));
        if (json.has(REQUEST_OS)) setOs(json.getString(REQUEST_OS));
        if (json.has(REQUEST_SIGN_PROVIDER)) setSignProvider(SignProvider.parse(json.getString(REQUEST_SIGN_PROVIDER)));
        if (json.has(USER_NAME)) setName(json.getString(USER_NAME));

    }

    public MyUser fetchUser() {
        if(user != null) return user;

        user = new MyUser(getDataProcessorConnection(), getUid());
        user.setManufacturer(getManufacturer());
        user.setModel(getModel());
        user.setOs(getOs());
        user.setName(getName());
        user.setSignProvider(getSignProvider());

        user.setNumber(getNumber());

        return user;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOs() {
        return os;
    }

    public void setSignProvider(SignProvider signProvider) {
        this.signProvider = signProvider;
    }

    public SignProvider getSignProvider() {
        return signProvider;
    }

    public void setGroupReference(DatabaseReference groupReference) {
        this.groupReference = groupReference;
    }

    public DatabaseReference getGroupReference() {
        return groupReference;
    }

    public boolean checkControl(String uid, String compareHash) {
        String calculatedHash = Misc.getEncryptedHash(getControl() + ":" + uid);
        return calculatedHash.equals(compareHash);
    }

    @Override
    public String toString() {
        return "UserRequest {" +
                "address=" + getAddress() +
                ", timestamp=" + timestamp +
                ", control='" + control + '\'' +
                (name != null ? ", name='" + name + '\'' : "") +
                ", uid='" + uid + '\'' +
                ", number=" + number +
                ", groupId='" + groupId + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", model='" + model + '\'' +
                ", os='" + os + '\'' +
                ", signProvider=" + signProvider +
                '}';
    }
}
