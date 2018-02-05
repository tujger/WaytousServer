package com.edeqa.waytousserver.servers;

import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.UserRequest;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;
import com.edeqa.waytousserver.rest.firebase.AbstractFirebaseAction;
import com.edeqa.waytousserver.rest.firebase.AdminToken;
import com.edeqa.waytousserver.rest.firebase.CheckUser;
import com.edeqa.waytousserver.rest.firebase.CleanStatistics;
import com.edeqa.waytousserver.rest.firebase.CreateAccount;
import com.edeqa.waytousserver.rest.firebase.CreateGroup;
import com.edeqa.waytousserver.rest.firebase.CustomToken;
import com.edeqa.waytousserver.rest.firebase.DeleteAccount;
import com.edeqa.waytousserver.rest.firebase.DeleteGroup;
import com.edeqa.waytousserver.rest.firebase.GroupProperty;
import com.edeqa.waytousserver.rest.firebase.JoinGroup;
import com.edeqa.waytousserver.rest.firebase.RegisterUser;
import com.edeqa.waytousserver.rest.firebase.RejectUser;
import com.edeqa.waytousserver.rest.firebase.RemoveUser;
import com.edeqa.waytousserver.rest.firebase.NewGroup;
import com.edeqa.waytousserver.rest.firebase.StatisticsAccount;
import com.edeqa.waytousserver.rest.firebase.StatisticsGroup;
import com.edeqa.waytousserver.rest.firebase.StatisticsMessage;
import com.edeqa.waytousserver.rest.firebase.StatisticsUser;
import com.edeqa.waytousserver.rest.firebase.UserProperty;
import com.edeqa.waytousserver.rest.firebase.ValidateAccounts;
import com.edeqa.waytousserver.rest.firebase.ValidateGroups;
import com.edeqa.waytousserver.rest.tracking.AbstractTrackingAction;
import com.edeqa.waytousserver.rest.tracking.ChangeName;
import com.edeqa.waytousserver.rest.tracking.Leave;
import com.edeqa.waytousserver.rest.tracking.Message;
import com.edeqa.waytousserver.rest.tracking.SavedLocation;
import com.edeqa.waytousserver.rest.tracking.Tracking;
import com.edeqa.waytousserver.rest.tracking.WelcomeMessage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.REQUEST;
import static com.edeqa.waytous.Constants.REQUEST_CHECK_USER;
import static com.edeqa.waytous.Constants.REQUEST_HASH;
import static com.edeqa.waytous.Constants.REQUEST_JOIN_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.REQUEST_TOKEN;
import static com.edeqa.waytous.Constants.REQUEST_UID;
import static com.edeqa.waytous.Constants.RESPONSE_MESSAGE;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_UPDATED;

/**
 * Created 10/5/16.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class DataProcessorFirebaseV1 extends AbstractDataProcessor {

    public static final String VERSION = "v1";
    private static String LOG = "DPF1";
    private DatabaseReference refRoot;

    public DataProcessorFirebaseV1() throws IOException {
        super();

        if(OPTIONS.isDebugMode()) {
            try {
                Misc.log(LOG, "Data Processor Firebase " + VERSION + ", config file: " + new File(OPTIONS.getFirebasePrivateKeyFile()).getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FirebaseOptions options = createFirebaseOptions();
        FirebaseApp defaultApp = FirebaseApp.initializeApp(options);

        /*try {
            if(FirebaseApp.getApps().size() < 1) {
                FirebaseApp.initializeApp(options);
//            FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
//            Common.log("already exists...");
//            e.printStackTrace();
        }*/

        /*try {
            FirebaseDatabase.getInstance(defaultApp);
//            FirebaseApp.getInstance();
        } catch (Exception e) {
//            Common.log("doesn't exist...");
//            e.printStackTrace();
        }*/

        try {
            refRoot = FirebaseDatabase.getInstance(defaultApp).getReference();
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventBus<AbstractFirebaseAction> fireBus = getFireBus();
        fireBus.register(new AdminToken().setFirebasePrivateKeyFile(OPTIONS.getFirebasePrivateKeyFile()));
        fireBus.register(new CreateAccount().setFirebaseReference(refRoot));
        fireBus.register(new CreateGroup().setFirebaseReference(refRoot));
        fireBus.register(new CustomToken());
        fireBus.register(new RegisterUser().setFirebaseReference(refRoot));
        fireBus.register(new RejectUser());
        fireBus.register(new JoinGroup().setFirebaseReference(refRoot));
        fireBus.register(new CheckUser().setFirebaseReference(refRoot));
        fireBus.register(new NewGroup().setFirebaseReference(refRoot));
        fireBus.register(new StatisticsAccount().setFirebaseReference(refRoot));
        fireBus.register(new StatisticsGroup().setFirebaseReference(refRoot));
        fireBus.register(new StatisticsMessage().setFirebaseReference(refRoot));
        fireBus.register(new StatisticsUser().setFirebaseReference(refRoot));

        EventBus<AbstractTrackingAction> trackingBus = getTrackingBus();
//        trackingBus.register(new Admin());
        trackingBus.register(new ChangeName());
        trackingBus.register(new Leave());
        trackingBus.register(new Message());
        trackingBus.register(new SavedLocation());
        trackingBus.register(new Tracking());
        trackingBus.register(new WelcomeMessage());
    }

    /**
     * This method creates the options for Firebase connecting. Depending on current installation type
     * it defines the properly request and performs it. Installation type can be defined in gradle.build.
     * <p>
     * Current installation type is recognizing by presense of method:
     * - "setCredential" in stand-alone server mode,
     * - "setServiceAccount" in Google AppEngine mode.
     * <p>
     * Stand-alone server mode extends com.sun.net.httpserver.HttpServer.
     */
    private FirebaseOptions createFirebaseOptions() throws FileNotFoundException {

        FirebaseOptions.Builder builder = new FirebaseOptions.Builder();

        Class<? extends FirebaseOptions.Builder> builderClass = builder.getClass();
        Method[] methods = builderClass.getDeclaredMethods();
        Method method = null;
        for (Method m : methods) {
            if ("setCredential".equals(m.getName())) {
                method = m;
                setServerMode(true);
                break;
            } else if ("setServiceAccount".equals(m.getName())) {
                method = m;
//                break;
            }
        }
        if (isServerMode()) {
            try {
                Class tempClass = Class.forName("com.google.firebase.auth.FirebaseCredentials");
                Method fromCertificate = tempClass.getDeclaredMethod("fromCertificate", InputStream.class);

                assert method != null;
                builder = (FirebaseOptions.Builder) method.invoke(builder, fromCertificate.invoke(null, new FileInputStream(OPTIONS.getFirebasePrivateKeyFile())));
//                builder = (FirebaseOptions.Builder) method.invoke(builder, FirebaseCredentials.fromCertificate(new FileInputStream(OPTIONS.getFirebasePrivateKeyFile())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                assert method != null;
                builder = (FirebaseOptions.Builder) method.invoke(builder, new FileInputStream(OPTIONS.getFirebasePrivateKeyFile()));
            } catch (IllegalAccessException | InvocationTargetException | IOException e) {
                e.printStackTrace();
            }
        }

//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredential(com.google.firebase.auth.FirebaseCredentials.fromCertificate(new FileInputStream(OPTIONS.getFirebasePrivateKeyFile())))
//                .setDatabaseUrl(OPTIONS.getFirebaseDatabaseUrl())
//                .build();

//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setServiceAccount(new FileInputStream(OPTIONS.getFirebasePrivateKeyFile()))
//                .setDatabaseUrl(OPTIONS.getFirebaseDatabaseUrl())
//                .build();

        return builder.setDatabaseUrl(OPTIONS.getFirebaseDatabaseUrl()).build();
    }

    @Override
    public LinkedList<String> getFlagsHoldersList() {
        return null;
    }

    /*   @Override
        public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
            Common.log("Main","HANDSHAKE:"+conn+":"+draft+":"+request);

            return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
        }
    */

    @Override
    public void onMessage(final DataProcessorConnection conn, String message) {
        try {
            final JSONObject request, response = new JSONObject();
            try {
                request = new JSONObject(message);
            } catch (JSONException e) {
                Misc.err(LOG, "onMessage:request" + e.getMessage());
                ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                        .setUserRequest(new UserRequest(conn))
                        .call(response,"Wrong request");
                return;
            }
            if (!request.has(REQUEST) || !request.has(REQUEST_TIMESTAMP)) {
                ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                        .setUserRequest(new UserRequest(conn))
                        .call(response,"Wrong request");
                return;
            }
            final String uid;
            if(request.has(REQUEST_UID)) {
                uid = request.getString(REQUEST_UID);
                if(uid.startsWith("Administrator") || uid.startsWith("Viewer")) {
                    ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(new UserRequest(conn))
                            .call(response,"Wrong UID");
                    return;
                }
            } else {
                uid = null;
            }

            String req = request.getString(REQUEST);
            if ("test".equals(req)) {
                Misc.log(LOG, "onMessage:testMessage:" + conn.getRemoteSocketAddress(), message);
                response.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                response.put(RESPONSE_MESSAGE, "OK");
                conn.send(response.toString());
                conn.close();
            } else if (REQUEST_NEW_GROUP.equals(req)) {
                if (uid != null) {
                    final GroupRequest groupRequest = new GroupRequest();
                    final UserRequest userRequest = new UserRequest(conn);
                    userRequest.parse(request);
                    ((NewGroup) getFireBus().getHolder(NewGroup.TYPE))
                            .setUserRequest(userRequest)
                            .call(response, groupRequest);
                } else {
                    ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(new UserRequest(conn))
                            .call(response,"Cannot create group (code 15).");
                }
            } else if (REQUEST_JOIN_GROUP.equals(req)) {
                if (request.has(REQUEST_TOKEN)) {
                    final String groupId = request.getString(REQUEST_TOKEN);
                    final UserRequest userRequest = new UserRequest(conn)
                            .setGroupId(groupId)
                            .setUid(uid);
                    userRequest.parse(request);
                    getUserRequests().add(userRequest);

                    ((JoinGroup) getFireBus().getHolder(JoinGroup.TYPE))
                            .call(response, userRequest);
                } else {
                    ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(new UserRequest(conn))
                            .call(response,"Wrong request (group not defined).");
                }
            } else if (REQUEST_CHECK_USER.equals(req)) {
                if (request.has(REQUEST_HASH)) {
                    final String hash = request.getString((REQUEST_HASH));

                    Misc.log(LOG, "onMessage:checkResponse:" + conn.getRemoteSocketAddress(), "hash:" + hash);
                    final UserRequest userRequest = getUserRequests().findByConnection(conn);
                    if (userRequest != null) {
                        userRequest.setDataProcessorConnection(conn);
                        ((CheckUser) getFireBus().getHolder(CheckUser.TYPE))
                                .setHash(hash)
                                .call(response, userRequest);
                    } else {
                        ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                .setUserRequest(new UserRequest(conn))
                                .call(response,"Cannot join to group (user not authorized).");
                    }
                } else {
                    ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(new UserRequest(conn))
                            .call(response,"Cannot join to group (hash not defined).");
                }
            }
        } catch (Exception e) {
            Misc.err(LOG, "onMessage:error:" + e.getMessage(), "req:" + message);
            e.printStackTrace();
            conn.send("{\"status\":\"Request failed\"}");
        }
    }

    @Override
    public void createGroup(final GroupRequest groupRequest, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        ((CreateGroup) getFireBus().getHolder(CreateGroup.TYPE))
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .call(new JSONObject(), groupRequest);
    }

    @Override
    public void deleteGroup(final String groupId, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        new DeleteGroup()
                .setFirebaseReference(refRoot)
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .call(null, groupId);
    }

    @Override
    public void switchPropertyInGroup(final String groupId, final String property, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        new GroupProperty()
                .setFirebaseReference(refRoot)
                .setGroupId(groupId)
                .setKey(property)
                .performSwitchBoolean()
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .call(null, null);
    }

    @Override
    public void modifyPropertyInGroup(final String groupId, final String property, final Serializable value, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        new GroupProperty()
                .setFirebaseReference(refRoot)
                .setGroupId(groupId)
                .setKey(property)
                .setValue(value)
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .call(null, null);
    }

    @Override
    public void registerUser(String groupId, MyUser user, String action, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror) {
        ((RegisterUser) getFireBus().getHolder(RegisterUser.TYPE))
                .setGroupId(groupId)
                .setAction(action)
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .call(null, user);
    }

//    @Override
//    public void onWebsocketPong(WebSocket conn, Framedata f) {
//        super.onWebsocketPong(conn, f);
//        System.out.println("PONG:"+conn.getRemoteSocketAddress()+":"+f);
//    }

//    @Override
//    public void onWebsocketPing(WebSocket conn, Framedata f) {
//        super.onWebsocketPing(conn, f);
//
//        try {
//            String ip = conn.getRemoteSocketAddress().toString();
//            if (ipToUser.containsKey(ip)) {
//                ipToUser.get(ip).updateChanged();
//            }
////            System.out.println("PING:" + conn.getRemoteSocketAddress() + ":" + f);
//        } catch ( Exception e) {
//            e.printStackTrace();
//        }
//    }


    @Override
    public void removeUserFromGroup(final String groupId, final Long userNumber, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        new RemoveUser()
                .setFirebaseReference(refRoot)
                .setGroupId(groupId)
                .setUserNumber(userNumber)
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .call(null, null);
    }

    @Override
    public void deleteAccount(final String accountId, Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        new DeleteAccount()
                .setFirebaseReference(refRoot)
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .call(null, accountId);
    }

    @Override
    public void switchPropertyForUser(final String groupId, final Long userNumber, final String property, final Boolean value, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {

        new UserProperty()
                .setFirebaseReference(refRoot)
                .setGroupId(groupId)
                .setUserNumber(userNumber)
                .setKey(property)
                .performSwitchBoolean()
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .call(null, null);
    }

    public void validateGroups() {
        new ValidateGroups()
                .setFirebaseReference(refRoot)
                .call(null, null);
    }

    @Override
    public void validateUsers() {

    }

    @Override
    public void validateAccounts() {
        new ValidateAccounts()
                .setFirebaseReference(refRoot)
                .call(null,null);
    }

    @Override
    public String createCustomToken(String uid) {
        return ((CustomToken) getFireBus().getHolder(CustomToken.TYPE)).fetchToken(uid);
    }

    @Override
    public void cleanStatisticsMessages(final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        new CleanStatistics()
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .setFirebaseReference(refRoot)
                .call(null, null);
    }

    private EventBus<AbstractFirebaseAction> getFireBus() {
        return (EventBus<AbstractFirebaseAction>) EventBus.getOrCreate(AbstractFirebaseAction.EVENTBUS);
    }

    private EventBus<AbstractTrackingAction> getTrackingBus() {
        return (EventBus<AbstractTrackingAction>) EventBus.getOrCreate(AbstractTrackingAction.EVENTBUS);
    }
}
