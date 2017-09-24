package com.edeqa.waytousserver.helpers;

import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;
import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONObject;

import java.beans.Transient;
import java.util.Date;

import static com.edeqa.waytous.Constants.REQUEST_DEVICE_ID;
import static com.edeqa.waytous.Constants.REQUEST_MANUFACTURER;
import static com.edeqa.waytous.Constants.REQUEST_MODEL;
import static com.edeqa.waytous.Constants.REQUEST_OS;
import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;
import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.USER_ACCURACY;
import static com.edeqa.waytous.Constants.USER_ALTITUDE;
import static com.edeqa.waytous.Constants.USER_BEARING;
import static com.edeqa.waytous.Constants.USER_LATITUDE;
import static com.edeqa.waytous.Constants.USER_LONGITUDE;
import static com.edeqa.waytous.Constants.USER_NAME;
import static com.edeqa.waytous.Constants.USER_PROVIDER;
import static com.edeqa.waytous.Constants.USER_SPEED;


/**
 * Created 10/9/16.
 */

@IgnoreExtraProperties
public class MyUser {
    transient public DataProcessorConnection connection;
    public String name;
    public long created;
    public long changed;
    public int color;
    public int number;
//    private ArrayList<MyPosition> positions;
    transient private MyPosition position;
    private String deviceId;
//    private String userId;
    private String control;
    private String model;
    private String manufacturer;
    private String signProvider;
    private String os;


    public MyUser(DataProcessorConnection connection, String deviceId) {
        this.connection = connection;
        this.deviceId = deviceId;
        created = new Date().getTime();
        setChanged();
//        positions = new ArrayList<MyPosition>();

        newControl();
//        System.out.println("USER CONTROL:" + control);
        calculateHash();
    }

    public MyUser(DataProcessorConnection conn, JSONObject request) {
        this(conn, request.getString(REQUEST_DEVICE_ID));

        if (request.has(REQUEST_MANUFACTURER)) setManufacturer(request.getString(REQUEST_MANUFACTURER));
        if (request.has(REQUEST_MODEL)) setModel(request.getString(REQUEST_MODEL));
        if (request.has(REQUEST_OS)) setOs(request.getString(REQUEST_OS));
        if (request.has(REQUEST_SIGN_PROVIDER)) setSignProvider(request.getString(REQUEST_SIGN_PROVIDER));
        if (request.has(USER_NAME)) setName(request.getString(USER_NAME));

    }

    public String calculateHash() {
        return calculateHash(control);
    }

    public String calculateHash(String control) {
        return Misc.getEncryptedHash(control + ":" + deviceId);
    }

    public String getControl() {
        return control;
    }

    public String newControl() {
        control = Misc.getUnique();
        return control;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUid() {
        return deviceId/*Misc.getEncryptedHash(deviceId)*/;
    }

    public String getAddress() {
        if (connection == null) return null;
        if (connection.getRemoteSocketAddress() == null) return null;
        return connection.getRemoteSocketAddress().toString();
    }

//    @Transient
//    public WebSocket getConnection() {
//        return connection;
//    }

    public void setConnection(DataProcessorConnection connection) {
        this.connection = connection;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    @Override
    public String toString() {
        String res = "";
        res += "number:" + number;
        res += ", deviceId:" + deviceId;
//        if (userId != null) res += ", userId:" + userId;
        res += ", address:" + connection.getRemoteSocketAddress();
        res += ", created:" + getCreated() + "/" + new Date(getCreated()).toString();
        res += ", changed:" + getChanged() + "/" + new Date(getChanged()).toString();
        res += ", control:" + getControl();
        if (hasName()) res += ", name:" + name;
        if (model != null) res += ", model:" + model;
        if (manufacturer != null) res += ", manufacturer:" + manufacturer;
        if (os != null) res += ", os:" + os;
//        res += ", positions:" + positions.size();

        return res;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasName() {
        return name != null;
    }

    public void send(JSONObject o) {
        send(o.toString());
    }

    public void send(String text) {
        connection.send(text);
    }

    public void disconnect() {
        connection.close();
    }

    public void addPosition(JSONObject message) {
        if (message.has(REQUEST_TIMESTAMP)) {
            MyPosition pos = new MyPosition();

            long timestamp = message.getLong(REQUEST_TIMESTAMP);

            if (message.has(USER_LATITUDE)) pos.latitude = message.getDouble(USER_LATITUDE);
            if (message.has(USER_LONGITUDE)) pos.longitude = message.getDouble(USER_LONGITUDE);
            if (message.has(USER_ALTITUDE)) pos.altitude = message.getDouble(USER_ALTITUDE);
            if (message.has(USER_ACCURACY)) pos.accuracy = message.getDouble(USER_ACCURACY);
            if (message.has(USER_BEARING)) pos.bearing = message.getDouble(USER_BEARING);
            if (message.has(USER_SPEED)) pos.speed = message.getDouble(USER_SPEED);
            if (message.has(USER_PROVIDER)) pos.provider = message.getString(USER_PROVIDER);
            pos.timestamp = timestamp;

//            positions.add(pos);
            setPosition(pos);
            setChanged();
        }
    }

    public long getCreated() {
        return created;
    }

    @Transient
    public MyPosition getPosition() {
        return position;
/*
        if (positions.size() > 0) {
            return positions.get(positions.size() - 1);
        }
        return new MyPosition();
*/
    }

    public void setPosition(MyPosition position) {
        this.position = position;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Long getChanged() {
        return changed;
    }

    public void setChanged(Long changed) {
        this.changed = changed;
    }

    public void setChanged() {
        changed = new Date().getTime();
    }

//    public String getUserId() {
//        return userId;
//    }
//
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }

    public String getSignProvider() {
        return signProvider;
    }

    public void setSignProvider(String signProvider) {
        this.signProvider = signProvider;
    }

    public class MyPosition {
        public double latitude;
        public double longitude;
        public double altitude;
        public double accuracy;
        public double bearing;
        public double speed;
        public long timestamp;
        public String provider;

        public String toString() {
            return "Position [latitude:" + latitude + ", longitude:" + longitude
                    + ", altitude:" + altitude + ", accuracy:" + accuracy + ", bearing:" + bearing
                    + ", provider:" + provider + ", speed:" + speed + ", timestamp:" + timestamp + "]";
        }

        public JSONObject toJSON() {
            JSONObject o = new JSONObject();
            o.put(USER_LATITUDE, latitude);
            o.put(USER_LONGITUDE, longitude);
            o.put(USER_ALTITUDE, altitude);
            o.put(USER_ACCURACY, accuracy);
            o.put(USER_BEARING, bearing);
            o.put(USER_SPEED, speed);
            o.put(USER_PROVIDER, provider);
            o.put(REQUEST_TIMESTAMP, timestamp);
            return o;
        }

    }

/*
    public ArrayList<MyPosition> getPositions() {
        return positions;
    }
*/

}
