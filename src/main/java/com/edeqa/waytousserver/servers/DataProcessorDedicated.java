package com.edeqa.waytousserver.servers;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytousserver.helpers.CheckReq;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.edeqa.waytous.Constants.LIFETIME_INACTIVE_GROUP;
import static com.edeqa.waytous.Constants.LIFETIME_INACTIVE_USER;
import static com.edeqa.waytous.Constants.REQUEST;
import static com.edeqa.waytous.Constants.REQUEST_CHECK_USER;
import static com.edeqa.waytous.Constants.REQUEST_HASH;
import static com.edeqa.waytous.Constants.REQUEST_JOIN_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_MANUFACTURER;
import static com.edeqa.waytous.Constants.REQUEST_MODEL;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_OS;
import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.REQUEST_TOKEN;
import static com.edeqa.waytous.Constants.REQUEST_UID;
import static com.edeqa.waytous.Constants.RESPONSE_CONTROL;
import static com.edeqa.waytous.Constants.RESPONSE_MESSAGE;
import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.Constants.RESPONSE_PRIVATE;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_CHECK;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ERROR;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_UPDATED;
import static com.edeqa.waytous.Constants.RESPONSE_TOKEN;
import static com.edeqa.waytous.Constants.USER_COLOR;
import static com.edeqa.waytous.Constants.USER_DISMISSED;
import static com.edeqa.waytous.Constants.USER_JOINED;
import static com.edeqa.waytous.Constants.USER_NAME;

/**
 * Created 10/5/16.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class DataProcessorDedicated extends AbstractDataProcessor {

    public static final String VERSION = "v1";
//    private HashMap<String,FlagHolder> flagHolders;

    public DataProcessorDedicated() {
        super();
        Misc.log("DPD","Data Processor Dedicated "+VERSION+".");
    }

    @Override
    public void validateGroups() {
        //noinspection InfiniteLoopStatement
        while(true) {
            try {
                Thread.sleep(LIFETIME_INACTIVE_USER * 1000);

                MyUser user;
                long currentDate = new Date().getTime();
                for (Map.Entry<String, MyUser> entry : ipToUser.entrySet()) {

                    user = entry.getValue();
                    if (user != null) {

                        System.out.println("INACTIVITY: " + user.getName() + ":" + (currentDate - user.getChanged()));

                        if (currentDate - user.getChanged() > LIFETIME_INACTIVE_USER * 1000) {
                            // dismiss user
                            JSONObject o = new JSONObject();
                            if(ipToToken.containsKey(entry.getKey())) {
                                MyGroup token = ipToToken.get(entry.getKey());
                                o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                                o.put(USER_DISMISSED, user.getNumber());
                                token.sendToAllFrom(o, user);
                            }

                            if(ipToUser.containsKey(entry.getKey())) ipToUser.remove(entry.getKey());
                            if(ipToToken.containsKey(entry.getKey())) ipToToken.remove(entry.getKey());
//                            if(ipToCheck.containsKey(entry.getKey())) ipToCheck.remove(entry.getKey());
                            user.connection.close();
                        }

                    } else {
                        if (ipToToken.containsKey(entry.getKey())) ipToToken.remove(entry.getKey());
//                        if (ipToCheck.containsKey(entry.getKey())) ipToCheck.remove(entry.getKey());
                        ipToUser.remove(entry.getKey());
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void validateUsers() {

    }

    @Override
    public void validateAccounts() {

    }

    @Override
    public LinkedList<String> getRequestHoldersList() {
        LinkedList<String> classes = new LinkedList<>();
        classes.add("TrackingRequestHolder");
        classes.add("MessageRequestHolder");
        classes.add("ChangeNameRequestHolder");
        classes.add("WelcomeMessageRequestHolder");
        classes.add("LeaveRequestHolder");
        classes.add("SavedLocationRequestHolder");
        return classes;
    }

    @Override
    public LinkedList<String> getFlagsHoldersList() {
        LinkedList<String> classes = new LinkedList<>();
        classes.add("PushFlagHolder");
        classes.add("DeliveryFlagHolder");
        classes.add("ProviderFlagHolder");
        return classes;
    }

    @Override
    public void onClose(DataProcessorConnection conn, int code, String reason, boolean remote) {
        System.out.println("WSS:on close:" + conn.getRemoteSocketAddress() + " disconnected:by client:"+remote+":"+code+":"+reason);
//        this.sendToAll( conn + " has left the room!" );
        String ip = conn.getRemoteSocketAddress().toString();
        if(ipToToken.containsKey(ip)){
            final MyGroup token = ipToToken.get(ip);
            ipToToken.remove(ip);

            try {
                MyUser user = ipToUser.get(ip);
                JSONObject o = new JSONObject();
                o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                o.put(USER_DISMISSED, user.getNumber());
                token.sendToAllFrom(o, user);
            } catch (Exception e){
                e.printStackTrace();
            }

            final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    if(token.isEmpty() && new Date().getTime() - token.getChanged() >= LIFETIME_INACTIVE_GROUP) {
                        groups.remove(token.getId());
                    }
                }
            }, LIFETIME_INACTIVE_GROUP +10, TimeUnit.SECONDS);

        }
        if(ipToUser.containsKey(ip)) ipToUser.remove(ip);
//        if(ipToCheck.containsKey(ip)) ipToCheck.remove(ip);

    }

    @Override
    public void onMessage(DataProcessorConnection conn, String message) {
        boolean disconnect = false;
        try {
            System.out.println("WSS:on message:" + conn.getRemoteSocketAddress() + ": " + message);

            String ip = conn.getRemoteSocketAddress().toString();
            JSONObject request, response = new JSONObject();

            try {
                request = new JSONObject(message);
            } catch(JSONException e) {
                System.err.println("WSS:error in request message: "+e.getMessage());
                return;
            }
            if (!request.has(REQUEST_TIMESTAMP)) return;
//            long timestamp = request.getLong(REQUEST_TIMESTAMP);
        /*if(new Date().getTime() - timestamp > LIFETIME_REQUEST_TIMEOUT*1000) {
            System.out.println("WSS:ignore request because of timeout");
//            conn.close(CloseFrame.GOING_AWAY, "Request timeout");
            return;
        }*/

            if (!request.has(REQUEST)) return;

            String req = request.getString(REQUEST);
            if (REQUEST_NEW_GROUP.equals(req)) {
                if (request.has(REQUEST_UID)) {
                    MyGroup token = new MyGroup();
                    MyUser user = new MyUser(conn, request.getString(REQUEST_UID));
                    user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
                    user.setModel(request.getString(REQUEST_MODEL));
                    user.setOs(request.getString(REQUEST_OS));
                    if (request.has(USER_NAME)) user.setName(request.getString(USER_NAME));

                    token.addUser(user);
                    groups.put(token.getId(), token);

//                    if(request.has(REQUEST_MODEL)){
//                        user.setModel(request.getString(REQUEST_MODEL));
//                    }
//                    if(request.has(REQUEST_MANUFACTURER)){
//                        user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
//                    }
//                    if(request.has(REQUEST_OS)){
//                        user.setOs(request.getString(REQUEST_OS));
//                    }
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                    response.put(RESPONSE_TOKEN, token.getId());
                    response.put(RESPONSE_NUMBER, user.getNumber());

                    ipToToken.put(ip, token);
                    ipToUser.put(ip, user);
                    System.out.println("NEW:token created and accepted:" + token);

                } else {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Your device id is not defined");
                    disconnect = true;
                }

                System.out.println("NEW:response:" + response);

                Misc.pause(2);//FIXME remove pause

                conn.send(response.toString());
            } else if (REQUEST_JOIN_GROUP.equals(req)) {
                if (request.has(REQUEST_TOKEN)) {
                    String tokenId = request.getString(REQUEST_TOKEN);
                    if (groups.containsKey(tokenId)) {
                        MyGroup token = groups.get(tokenId);

                        if (request.has(REQUEST_UID)) {
                            String uid = request.getString(REQUEST_UID);
                            MyUser user = null;
                            for (Map.Entry<String, MyUser> x : token.users.entrySet()) {
                                if (uid.equals(x.getValue().getUid())) {
                                    user = x.getValue();
                                    break;
                                }
                            }
                            if (user != null) {
                                user.setChanged();
                                CheckReq check = new CheckReq();
                                check.setControl(Misc.getUnique());
                                check.setToken(token);
                                if (request.has(USER_NAME))
                                    check.setName(request.getString(USER_NAME));

                                response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                                response.put(RESPONSE_CONTROL, check.getControl());
//                                ipToCheck.put(ip, check);
                            } else {

                                user = new MyUser(conn, request.getString(REQUEST_UID));
                                user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
                                user.setModel(request.getString(REQUEST_MODEL));
                                user.setOs(request.getString(REQUEST_OS));
                                if (request.has(USER_NAME) && request.getString(USER_NAME) != null && request.getString(USER_NAME).length() > 0) {
                                    user.setName(request.getString(USER_NAME));
                                }
                                token.addUser(user);

                                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                response.put(RESPONSE_NUMBER, user.getNumber());

                                ipToToken.put(ip, token);
                                ipToUser.put(ip, user);

                                token.sendInitialTo(response, user);

                                JSONObject o = new JSONObject();
                                o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                                o.put(USER_COLOR, user.getColor());
                                o.put(USER_JOINED, user.getNumber());
                                if (user.getName() != null && user.getName().length() > 0) {
                                    o.put(USER_NAME, user.getName());
                                }
                                token.sendToAllFrom(o, user);
                                return;
                            }

                        } else {
                            CheckReq check = new CheckReq();
                            check.setControl(Misc.getUnique());
                            check.setToken(token);

                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                            response.put(RESPONSE_CONTROL, check.getControl());
//                            ipToCheck.put(ip, check);
                        }
                    } else {
                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                        response.put(RESPONSE_MESSAGE, "This group is expired.");
                        disconnect = true;
                    }
                } else {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Wrong request (token not defined).");
                    disconnect = true;
                }
                Misc.pause(2);//FIXME remove pause
                conn.send(response.toString());
                System.out.println("JOIN:response:" + response);
            } else if (REQUEST_CHECK_USER.equals(req)) {
                if (request.has(REQUEST_HASH)) {
                    String hash = request.getString((REQUEST_HASH));
                    System.out.println(("CHECK:requested device: [hash=" + hash + "]"));
/*                    if (ipToCheck.containsKey(ip)) {
                        CheckReq check = ipToCheck.get(ip);

                        System.out.println("CHECK:found token: [name=" + check.getName() + ", token=" + check.getToken().getId() + ", control=" + check.getControl() + "]");

                        MyUser user = null;
                        for (Map.Entry<String, MyUser> x : check.getToken().users.entrySet()) {
                            System.out.println("CHECK:looking for device: [control=" + check.getControl() + ", uid=" + x.getValue().getUid().substring(0, 20) + "..., calculatedHash=" + x.getValue().calculateHash(check.getControl()) + "]");
                            if (hash.equals(x.getValue().calculateHash(check.getControl()))) {
                                user = x.getValue();
                                break;
                            }
                        }
                        if (user != null) {
                            System.out.println("CHECK:user accepted:" + user);
                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                            response.put(RESPONSE_NUMBER, user.getNumber());
                            user.setConnection(conn);
                            user.setChanged();
                            if (check.getName() != null && check.getName().length() > 0) {
                                user.setName(check.getName());
                            }

                            ipToToken.put(ip, check.getToken());
                            ipToUser.put(ip, user);
                            ipToCheck.remove(ip);

                            check.getToken().sendInitialTo(response, user);

                            JSONObject o = new JSONObject();//user.getPosition().toJSON();
                            o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                            o.put(USER_COLOR, user.getColor());
                            o.put(USER_JOINED, user.getNumber());
                            if (check.getName() != null && check.getName().length() > 0) {
                                o.put(USER_NAME, check.getName());
                            }
                            check.getToken().sendToAllFrom(o, user);
                            return;
//                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
//                            response.put(RESPONSE_MESSAGE, "User not granted.");
                        } else {
                            System.out.println("CHECK:user not accepted for hash:" + hash);
                            if (ipToToken.containsKey(ip)) ipToToken.remove(ip);
                            if (ipToUser.containsKey(ip)) ipToUser.remove(ip);

                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                            response.put(RESPONSE_MESSAGE, "Cannot join to group (user not accepted).");
                            disconnect = true;
                        }

                        ipToCheck.remove(ip);
                    } else {
                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                        response.put(RESPONSE_MESSAGE, "Cannot join to group (user not authorized).");
                        disconnect = true;
                    }*/
                } else {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Cannot join to group (hash not defined).");
                    disconnect = true;
                }

                System.out.println("CHECK:response:" + response);
                Misc.pause(2);//FIXME remove pause
                conn.send(response.toString());
            } else {
                if (ipToToken.containsKey(ip)) {
                    MyGroup token = ipToToken.get(ip);
                    MyUser user = ipToUser.get(ip);
                    user.setChanged();

                    if (requestHolders.containsKey(req)) {
                        JSONObject o = new JSONObject();
                        o.put(RESPONSE_STATUS, req);

                        if(requestHolders.get(req).perform(token, user, request, o)) {
                            token.updateChanged();

                            Set<String> keys = new LinkedHashSet<>(request.keySet());
                            keys.retainAll(flagHolders.keySet());
                            for(String flag: keys){
                                flagHolders.get(flag).perform(token, user, request, o);
                            }


                            if (request.has(RESPONSE_PRIVATE)) {
                                o.put(RESPONSE_PRIVATE, request.getInt(RESPONSE_PRIVATE));
                                token.sendToFrom(o, request.getInt(RESPONSE_PRIVATE), user);
                            } else {
                                token.sendToAllFrom(o, user);
                            }
                        }

                    }

                } else {
                    System.out.println("UPDATE:token not found");
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Group not exists.");
                    conn.send(response.toString());
                    conn.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(disconnect) {
            conn.close();
        }
    }

    @Override
    public void createGroup(GroupRequest groupRequest, Runnable1 onsuccess, Runnable1 onerror) {
        // TODO
    }

    @Override
    public void deleteGroup(String groupId, Runnable1 onsuccess, Runnable1 onerror) {
        // TODO
    }

    @Override
    public void switchPropertyInGroup(String groupId, String property, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror) {
        // TODO
    }

    @Override
    public void modifyPropertyInGroup(String groupId, String property, Serializable value, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror) {
        // TODO
    }

    @Override
    public void registerUser(String groupId, MyUser user, String action, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror) {

    }

//    @Override
//    public void onWebsocketPong(WebSocket conn, Framedata f) {
//        super.onWebsocketPong(conn, f);
//        System.out.println("PONG:"+conn.getRemoteSocketAddress()+":"+f);
//    }

    /*public void sendToAll(String text, WebSocket insteadConnection) {
        Collection<WebSocket> con = connections();
//        synchronized (con) {
            for (WebSocket c : con) {
                if (insteadConnection != null && c == insteadConnection) continue;
                System.out.println("WSS:to:" + c.getRemoteSocketAddress() + ":" + text);
                c.send(text);
            }
//        }
    }*/

    /*@Override
    public void removeUserFromGroup(String tokenId,String id){
        if(tokenId != null && id != null && groups.containsKey(tokenId)){
            MyGroup t = groups.get(tokenId);
            MyUser user = t.users.get(id);
            if(user != null){
                JSONObject response = new JSONObject();
                response.put(RESPONSE_STATUS, USER_DISMISSED);
                response.put(RESPONSE_MESSAGE,"You have been dismissed.");
                user.send(response);
                user.disconnect();
                t.removeUserFromGroup(id);
            }
        }
    }*/

    @Override
    public void removeUserFromGroup(String groupId, Long userNumber, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror) {
        // TODO
    }

    @Override
    public void deleteAccount(String accountId, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror) {

    }

    @Override
    public void switchPropertyForUser(String groupId, Long userNumber, String property, Boolean value, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror) {
        // TODO
    }

    @Override
    public String createCustomToken(String id) {
        return null;
    }

    @Override
    public void cleanStatisticsMessages(Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror) {

    }

}
