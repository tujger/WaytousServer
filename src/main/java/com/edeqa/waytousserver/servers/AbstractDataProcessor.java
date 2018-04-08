package com.edeqa.waytousserver.servers;

import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.UserRequests;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;

import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created 10/5/16.
 */

@SuppressWarnings("HardCodedStringLiteral")
abstract public class AbstractDataProcessor {

    final ConcurrentHashMap<String, MyGroup> groups;
    private final UserRequests userRequests;
    private boolean serverMode = false;

    abstract public void validateGroups();

    abstract public void validateUsers();

    public abstract void validateAccounts();

    public enum Action {
        GROUP_CREATED_PERSISTENT(Firebase.STAT_GROUPS_CREATED_PERSISTENT), GROUP_CREATED_TEMPORARY(Firebase.STAT_GROUPS_CREATED_TEMPORARY), GROUP_DELETED(Firebase.STAT_GROUPS_DELETED), GROUP_REJECTED(Firebase.STAT_GROUPS_REJECTED),
        USER_JOINED(Firebase.STAT_USERS_JOINED), USER_RECONNECTED(Firebase.STAT_USERS_RECONNECTED), USER_REJECTED(Firebase.STAT_USERS_REJECTED), USER_REMOVED(Firebase.STAT_USERS_REMOVED),
        ACCOUNT_CREATED(Firebase.STAT_ACCOUNTS_CREATED), ACCOUNT_DELETED(Firebase.STAT_ACCOUNTS_DELETED);
        private String id;
        Action(String id) {
            this.id = id;
        }
        public String toString() {
            return this.id;
        }
    }

    public AbstractDataProcessor() {
        groups = new ConcurrentHashMap<>();
        userRequests = new UserRequests();
    }

    final public void onOpen(DataProcessorConnection conn, ClientHandshake handshake) {
        try {
//            conn.send("{\"" + RESPONSE_STATUS + "\":\""+RESPONSE_STATUS_CONNECTED+"\",\"version\":" + SERVER_BUILD + "}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClose(DataProcessorConnection conn, int code, String reason, boolean remote) {
        getUserRequests().remove(conn);
    }

    abstract public void onMessage(final DataProcessorConnection conn, String message);

    final public void onError(DataProcessorConnection conn, Exception ex) {
        ex.printStackTrace();
        getUserRequests().remove(conn);
    }

    public void onWebSocketPing(DataProcessorConnection conn, Framedata f) {
        try {
//            UserRequest userRequest = getUserRequests().findByConnection(conn);
//            if(userRequest != null) userRequest.setDefined();
//            String ip = conn.getRemoteSocketAddress().toString();
//            if (ipToUser.containsKey(ip)) {
//                ipToUser.get(ip).setDefined();
//            }
//            System.out.println("PING:" + conn.getRemoteSocketAddress() + ":" + f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    abstract public void createGroup(GroupRequest groupRequest, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    abstract public void deleteGroup(String groupId, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    abstract public void switchOptionInGroup(String groupId, String option, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    abstract public void modifyOptionInGroup(String groupId, String option, Serializable value, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    public abstract void registerUser(String groupId, MyUser user, String action, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    abstract public void removeUserFromGroup(String groupId, Long userNumber, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    public abstract void deleteAccount(String accountId, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    abstract public void switchPropertyForUser(String groupId, Long userNumber, String property, Boolean value, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    public ConcurrentHashMap<String, MyGroup> getGroups() {
        return groups;
    }

    public boolean isServerMode() {
        return serverMode;
    }

    public void setServerMode(boolean serverMode) {
        this.serverMode = serverMode;
    }

    public abstract String createCustomToken(String id);

    public abstract void cleanStatisticsMessages(Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    public UserRequests getUserRequests() {
        return userRequests;
    }
}
