package com.edeqa.waytousserver.servers;

import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.UserRequests;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;
import com.edeqa.waytousserver.interfaces.FlagHolder;
import com.edeqa.waytousserver.interfaces.RequestHolder;

import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created 10/5/16.
 */

@SuppressWarnings("HardCodedStringLiteral")
abstract public class AbstractDataProcessor {

    final ConcurrentHashMap<String, MyGroup> groups;
    ConcurrentHashMap<String, MyGroup> ipToToken;
    ConcurrentHashMap<String, MyUser> ipToUser;
//    ConcurrentHashMap<String, CheckReq> ipToCheck;
    final UserRequests userRequests;
    final HashMap<String, RequestHolder> requestHolders;
    protected final HashMap<String, FlagHolder> flagHolders;
    private boolean serverMode = false;

    abstract public void validateGroups();

    abstract public void validateUsers();

    public abstract void validateAccounts();

    public enum GroupAction {
        GROUP_CREATED_PERSISTENT(Firebase.STAT_GROUPS_CREATED_PERSISTENT), GROUP_CREATED_TEMPORARY(Firebase.STAT_GROUPS_CREATED_TEMPORARY), GROUP_DELETED(Firebase.STAT_GROUPS_DELETED), GROUP_REJECTED(Firebase.STAT_GROUPS_REJECTED);
        private String id;
        GroupAction(String id) {
            this.id = id;
        }
        public String toString() {
            return this.id;
        }
    }

    public enum UserAction {
        USER_JOINED(Firebase.STAT_USERS_JOINED), USER_RECONNECTED(Firebase.STAT_USERS_RECONNECTED), USER_REJECTED(Firebase.STAT_USERS_REJECTED), USER_REMOVED(Firebase.STAT_USERS_REMOVED);
        private String id;
        UserAction(String id) {
            this.id = id;
        }
        public String toString() {
            return this.id;
        }
    }

    public enum AccountAction {
        ACCOUNT_CREATED(Firebase.STAT_ACCOUNTS_CREATED), ACCOUNT_DELETED(Firebase.STAT_ACCOUNTS_DELETED);
        private String id;
        AccountAction(String id) {
            this.id = id;
        }
        public String toString() {
            return this.id;
        }
    }

    public AbstractDataProcessor() {
        groups = new ConcurrentHashMap<>();
//        ipToToken = new ConcurrentHashMap<>();
//        ipToUser = new ConcurrentHashMap<>();
//        ipToCheck = new ConcurrentHashMap<>();
        userRequests = new UserRequests();

        requestHolders = new LinkedHashMap<>();

        LinkedList<String> classes = getRequestHoldersList();

        if (classes != null) {
            for (String s : classes) {
                try {
                    Class<RequestHolder> _tempClass = (Class<RequestHolder>) Class.forName("com.edeqa.waytousserver.holders.request." + s);
                    Constructor<RequestHolder> ctor = _tempClass.getDeclaredConstructor(AbstractDataProcessor.class);
                    registerRequestHolder(ctor.newInstance(this));
                } catch (Exception e) {
                    System.err.println("Trying to instantiate " + s);
                    e.printStackTrace();
                }
            }
        }

        flagHolders = new LinkedHashMap<>();
        classes = getFlagsHoldersList();
        if (classes != null) {
            for (String s : classes) {
                try {
                    Class<FlagHolder> _tempClass = (Class<FlagHolder>) Class.forName("com.edeqa.waytousserver.holders.flag." + s);
                    Constructor<FlagHolder> ctor = _tempClass.getDeclaredConstructor(AbstractDataProcessor.class);
                    registerFlagHolder(ctor.newInstance(this));
                } catch (Exception e) {
                    System.out.println("Trying to instantiate " + s);
                    e.printStackTrace();
                }
            }
        }
    }

    abstract public LinkedList<String> getRequestHoldersList();

    abstract public LinkedList<String> getFlagsHoldersList();

    public void registerRequestHolder(RequestHolder holder) {
        if (holder.getType() == null) return;
        requestHolders.put(holder.getType(), holder);
    }

    public void registerFlagHolder(FlagHolder holder) {
        if (holder.getType() == null) return;
        flagHolders.put(holder.getType(), holder);
    }

    final public void onOpen(DataProcessorConnection conn, ClientHandshake handshake) {
        try {
//            conn.send("{\"" + RESPONSE_STATUS + "\":\""+RESPONSE_STATUS_CONNECTED+"\",\"version\":" + SERVER_BUILD + "}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClose(DataProcessorConnection conn, int code, String reason, boolean remote) {
//        System.out.println("WSS:on close:" + conn.getRemoteSocketAddress() + " disconnected:by client:"+remote+":"+code+":"+reason);
//        this.sendToAll( conn + " has left the room!" );
//        String ip = conn.getRemoteSocketAddress().toString();
        getUserRequests().remove(conn);
//        if (ipToCheck.containsKey(ip)) ipToCheck.remove(ip);

    }

    abstract public void onMessage(final DataProcessorConnection conn, String message);

    final public void onError(DataProcessorConnection conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null && conn.getRemoteSocketAddress() != null) {
            getUserRequests().remove(conn);
//            String ip = conn.getRemoteSocketAddress().toString();
//            if (ipToToken.containsKey(ip)) ipToToken.remove(ip);
//            if (ipToUser.containsKey(ip)) ipToUser.remove(ip);
//            if (ipToCheck.containsKey(ip)) ipToCheck.remove(ip);
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    public void onWebSocketPing(DataProcessorConnection conn, Framedata f) {
        try {
//            UserRequest userRequest = getUserRequests().findByConnection(conn);
//            if(userRequest != null) userRequest.setChanged();
//            String ip = conn.getRemoteSocketAddress().toString();
//            if (ipToUser.containsKey(ip)) {
//                ipToUser.get(ip).setChanged();
//            }
//            System.out.println("PING:" + conn.getRemoteSocketAddress() + ":" + f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    abstract public void createGroup(GroupRequest groupRequest, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    abstract public void deleteGroup(String groupId, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    abstract public void switchPropertyInGroup(String groupId, String property, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    abstract public void modifyPropertyInGroup(String groupId, String property, Serializable value, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    public abstract void registerUser(String groupId, MyUser user, String action, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    abstract public void removeUserFromGroup(String groupId, Long userNumber, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    public abstract void deleteAccount(String accountId, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    abstract public void switchPropertyForUser(String groupId, Long userNumber, String property, Boolean value, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror);

    public ConcurrentHashMap<String, MyGroup> getGroups() {
        return groups;
    }

//    public ConcurrentHashMap<String, MyGroup> getIpToToken() {
//        return ipToToken;
//    }
//
//    public ConcurrentHashMap<String, MyUser> getIpToUser() {
//        return ipToUser;
//    }
//
//    public ConcurrentHashMap<String, CheckReq> getIpToCheck() {
//        return ipToCheck;
//    }

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
