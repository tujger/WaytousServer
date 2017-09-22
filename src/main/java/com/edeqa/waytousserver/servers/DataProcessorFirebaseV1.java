package com.edeqa.waytousserver.servers;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.CheckReq;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;

import static com.edeqa.waytous.Constants.REQUEST;
import static com.edeqa.waytous.Constants.REQUEST_CHECK_USER;
import static com.edeqa.waytous.Constants.REQUEST_DEVICE_ID;
import static com.edeqa.waytous.Constants.REQUEST_HASH;
import static com.edeqa.waytous.Constants.REQUEST_JOIN_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_KEY;
import static com.edeqa.waytous.Constants.REQUEST_MODEL;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_OS;
import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;
import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.REQUEST_TOKEN;
import static com.edeqa.waytous.Constants.REQUEST_USER_ID;
import static com.edeqa.waytous.Constants.RESPONSE_CONTROL;
import static com.edeqa.waytous.Constants.RESPONSE_MESSAGE;
import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.Constants.RESPONSE_SIGN;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_CHECK;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ERROR;
import static com.edeqa.waytous.Constants.RESPONSE_TOKEN;
import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.USER_NAME;


/**
 * Created 10/5/16.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class DataProcessorFirebaseV1 extends AbstractDataProcessor {

    public static final String VERSION = "v1";
    private static String LOG = "DPF1";
    private DatabaseReference ref;

    public DataProcessorFirebaseV1() throws ServletException, IOException {
        super();

        try {
            Common.log(LOG, "Data Processor Firebase " + VERSION + ", config file: " + new File(OPTIONS.getFirebasePrivateKeyFile()).getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        FirebaseOptions options = createFirebaseOptions();

        FirebaseApp defaultApp = FirebaseApp.initializeApp(options);
//        System.out.println(defaultApp.getName());  // "[DEFAULT]"
//        System.out.println(FirebaseDatabase.getInstance(defaultApp));


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
            ref = FirebaseDatabase.getInstance(defaultApp).getReference();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        throw new ServletException("OPTIONS:"+FirebaseDatabase.getInstance());

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
            final String ip = conn.getRemoteSocketAddress().toString();
            final JSONObject request, response = new JSONObject();

            try {
                request = new JSONObject(message);
            } catch (JSONException e) {
                Common.err(LOG, "onMessage:request" + e.getMessage());
                return;
            }
            if (!request.has(REQUEST) || !request.has(REQUEST_TIMESTAMP)) return;

            String req = request.getString(REQUEST);
            /*if (REQUEST_TRACKING.equals(req)) {
                response.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                conn.send(response.toString());
                conn.close();
                Common.log(LOG,"onMessage:updateCoords:fake",response);
            } else*/
            if (REQUEST_NEW_GROUP.equals(req)) {
                if (request.has(REQUEST_DEVICE_ID)) {
                    final MyGroup group = new MyGroup();
                    final MyUser user = new MyUser(conn, request);
                    Common.log(LOG, "onMessage:requestNew:" + conn.getRemoteSocketAddress(), "{ deviceId:" + request.getString(REQUEST_DEVICE_ID) + " }");
                    //noinspection unchecked
                    final Runnable1<JSONObject>[] onresult = new Runnable1[2];
                    onresult[0] = new Runnable1<JSONObject>() {
                        @Override
                        public void call(JSONObject json) {

//                            ref.child(Constants.DATABASE.SECTION_GROUPS).child(groupId).setValue(user.getUid());
//                            DatabaseReference nodeNumber = ref.child(groupId).child(Constants.DATABASE.SECTION_USERS_ORDER).push();
//                            nodeNumber.setValue(user.getUid());

                            registerUser(group.getId(), user, REQUEST_NEW_GROUP, new Runnable1<JSONObject>() {
                                @Override
                                public void call(JSONObject json) {
                                    user.connection.send(json.toString());
                                    user.connection.close();
                                }
                            }, new Runnable1<JSONObject>() {
                                @Override
                                public void call(JSONObject json) {
                                    user.connection.send(json.toString());
                                    user.connection.close();
                                }
                            });
                        }
                    };
                    onresult[1] = new Runnable1<JSONObject>() {
                        @Override
                        public void call(JSONObject json) {
                            group.fetchNewId();
                            createGroup(group, onresult[0], onresult[1]);
                        }
                    };
                    createGroup(group, onresult[0], onresult[1]);
                } else {
                    rejectUser(response, conn, null, null, "Cannot create group (code 15).");
                    Common.err(LOG, "onMessage:newGroup:", response);
                }
            } else if (REQUEST_JOIN_GROUP.equals(req)) {
                if (request.has(REQUEST_TOKEN)) {

                    final String groupId = request.getString(REQUEST_TOKEN);
                    final DatabaseReference refGroup = ref.child(groupId);

                    final TaskSingleValueEventFor[] requestDataPrivateTask = new TaskSingleValueEventFor[1];
                    requestDataPrivateTask[0] = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(new Runnable1<DataSnapshot>() {
                        @Override
                        public void call(DataSnapshot dataSnapshot) {
//                            final MyUser user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));
                            final MyUser user = new MyUser(conn, request);

                            int count = 1;
                            boolean found = false;
                            Object value = dataSnapshot.getValue();

                            if (value == null) {
                                dataSnapshot.getRef().push().setValue(user.getUid());
                                requestDataPrivateTask[0].setRef(refGroup.child(Firebase.SECTION_USERS_ORDER)).start();
                                return;
                            }

                            TreeMap<String, String> map = new TreeMap<>();
                            //noinspection unchecked
                            map.putAll((HashMap<String, String>) dataSnapshot.getValue());

                            for (Map.Entry<String, String> x : map.entrySet()) {
                                if (user.getUid().equals(x.getValue())) {
                                    found = true;
                                    break;
                                }
                                ++count;
                            }
                            if (found) {
//                                Common.log(LOG, "onMessage:newGroup:", "user found:", user.getUid());
                                user.number = count;
                                registerUser(groupId, user, REQUEST_JOIN_GROUP, null, null);
                            } else {
//                                Common.log(LOG, "onMessage:newGroup:", "user not found adding:", user.getUid());
                                ref.child(Firebase.SECTION_GROUPS).child(groupId).setValue(user.getUid());
                                DatabaseReference nodeNumber = ref.child(groupId).child(Firebase.SECTION_USERS_ORDER).push();
                                nodeNumber.setValue(user.getUid());
                                requestDataPrivateTask[0].setRef(refGroup.child(Firebase.SECTION_USERS_ORDER)).start();
                            }
                        }
                    });

                    final TaskSingleValueEventFor numberForKeyTask = new TaskSingleValueEventFor<DataSnapshot>()
                            .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                @Override
                                public void call(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) { //join as existing member, go to check
                                        CheckReq check = new CheckReq();
                                        check.setControl(Misc.getUnique());
                                        check.setGroupId(groupId);
                                        check.setUid(dataSnapshot.getKey());
                                        check.setNumber((long) dataSnapshot.getValue());
                                        check.setUser(conn, request);

                                        Common.log(LOG, "onMessage:checkRequest:" + conn.getRemoteSocketAddress(), "{ number:" + dataSnapshot.getValue(), "key:" + dataSnapshot.getKey(), "control:" + check.getControl() + " }");

                                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                                        response.put(RESPONSE_CONTROL, check.getControl());
                                        ipToCheck.put(ip, check);
                                        try {
                                            conn.send(response.toString());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else { // join as new member
                                        requestDataPrivateTask[0].setRef(refGroup.child(Firebase.SECTION_USERS_ORDER)).start();
                                    }
                                }
                            });

                    TaskSingleValueEventFor groupOptionsTask = new TaskSingleValueEventFor<DataSnapshot>()
                            .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                @Override
                                public void call(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) {
                                        String deviceId = request.getString(REQUEST_DEVICE_ID);
                                        final String uid = Misc.getEncryptedHash(deviceId);

                                        numberForKeyTask.setRef(refGroup.child(Firebase.SECTION_USERS_KEYS).child(uid)).start();
                                    } else {
                                        rejectUser(response, conn, groupId, null, "This group is expired. (001)");
                                    }
                                }
                            });

                    if (request.has(REQUEST_DEVICE_ID)) {
                        Common.log(LOG, "onMessage:requestJoin:" + conn.getRemoteSocketAddress(), "{ groupId:" + groupId, "deviceId:" + request.getString(REQUEST_DEVICE_ID) + " }");
                        groupOptionsTask.setRef(refGroup.child(Firebase.SECTION_OPTIONS)).start();
                    } else {
                        CheckReq check = new CheckReq();
                        check.setControl(Misc.getUnique());
                        check.setGroupId(groupId);

                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                        response.put(RESPONSE_CONTROL, check.getControl());
                        ipToCheck.put(ip, check);
                        Common.log(LOG, "onMessage:requestReconnect:" + conn.getRemoteSocketAddress(), "{ groupId:" + groupId, "} control:", check.getControl());
                        conn.send(response.toString());
                    }
                    return;
                } else {
                    rejectUser(response, conn, null, null, "Wrong request (group not defined).");
                    System.out.println("JOIN:response:" + response);
                }
            } else if (REQUEST_CHECK_USER.equals(req)) {
                if (request.has(REQUEST_HASH)) {
                    final String hash = request.getString((REQUEST_HASH));
                    Common.log(LOG, "onMessage:checkResponse:" + conn.getRemoteSocketAddress(), "hash:" + hash);
                    if (ipToCheck.containsKey(ip)) {
                        final CheckReq check = ipToCheck.get(ip);
                        ipToCheck.remove(ip);

                        Common.log(LOG, "onMessage:checkFound:" + conn.getRemoteSocketAddress(), "{ name:" + check.getName(), "group:" + check.getGroupId(), "control:" + check.getControl() + " }");

                        final DatabaseReference refGroup = ref.child(check.getGroupId());

                        final TaskSingleValueEventFor userCheckTask = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(new Runnable1<DataSnapshot>() {
                            @Override
                            public void call(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) { //join as existing member
                                    try {
                                        String calculatedHash = Misc.getEncryptedHash(check.getControl() + ":" + ((HashMap) dataSnapshot.getValue()).get("device_id"));

                                        if (calculatedHash.equals(hash)) {
                                            Common.log(LOG, "onMessage:joinAsExisting:" + conn.getRemoteSocketAddress(), "group:" + check.getGroupId(), "user:{ number:" + dataSnapshot.getKey(), "properties:" + dataSnapshot.getValue(), " }");

                                            try {
                                                String customToken = createCustomToken(check.getUid());

                                                Map<String, Object> update = new HashMap<>();
                                                update.put(Firebase.USER_ACTIVE, true);
                                                update.put(Firebase.USER_COLOR, Utils.selectColor((int) check.getNumber()));
                                                update.put(Firebase.USER_CHANGED, new Date().getTime());
                                                System.out.println("SIGNPROVIDER2:"+check.getUser().getSignProvider());
                                                if (check.getName() != null && check.getName().length() > 0) {
                                                    update.put(USER_NAME, check.getName());
                                                }

                                                Task<Void> updateUserTask = refGroup.child(Firebase.SECTION_USERS_DATA).child("" + check.getNumber()).updateChildren(update);
                                                try {
                                                    Tasks.await(updateUserTask);
                                                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                                    response.put(RESPONSE_NUMBER, check.getNumber());
                                                    response.put(RESPONSE_SIGN, customToken);
                                                    conn.send(response.toString());
                                                    Common.log(LOG, "onMessage:joined:" + conn.getRemoteSocketAddress(), "signToken: [provided]"/*+customToken*/);
                                                    conn.close();
                                                    putStaticticsUser(check.getGroupId(), check.getName(), UserAction.USER_RECONNECTED, null);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Common.err(LOG, "onMessage:joinNotAuthenticated:" + conn.getRemoteSocketAddress(), "group:" + check.getGroupId(), "{ number:" + dataSnapshot.getKey(), "properties:" + dataSnapshot.getValue(), "} got:", hash, " waited:", calculatedHash);
                                            rejectUser(response, conn, check.getGroupId(), check.getName(), "Cannot join to group (user not authenticated).");
                                        }

                                    } catch (Exception e) {
                                        Common.err(LOG, "onMessage:joinHashFailed:" + conn.getRemoteSocketAddress(), "group:" + check.getGroupId(), "{ number:" + dataSnapshot.getKey(), "properties:" + dataSnapshot.getValue(), "}");
                                        rejectUser(response, conn, check.getGroupId(), check.getName(), "Cannot join to group (user not authenticated).");
                                        e.printStackTrace();
                                    }

                                } else { // join as new member

                                    check.getUser().setNumber((int) check.getNumber());
                                    registerUser(check.getGroupId(), check.getUser(), REQUEST_CHECK_USER, null, null);
                                    Common.log(LOG, "onMessage:joinAsNew:" + check.getUser().connection.getRemoteSocketAddress());
                                }
                            }
                        });

                        final TaskSingleValueEventFor userGetNumberTask = new TaskSingleValueEventFor<DataSnapshot>()
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null) {
                                            Common.log(LOG, "onMessage:joinNumberFound:" + conn.getRemoteSocketAddress(), "number:", dataSnapshot.getValue().toString());
                                            check.setNumber(Long.parseLong(dataSnapshot.getValue().toString()));
                                            userCheckTask.setRef(refGroup.child(Firebase.SECTION_USERS_DATA_PRIVATE).child(dataSnapshot.getValue().toString())).start();

                                        } else {
                                            Common.err(LOG, "onMessage:joinNumberNotFound:" + conn.getRemoteSocketAddress());
                                            rejectUser(response, conn, check.getGroupId(), null, "This group is expired. (005)");
                                        }
                                    }
                                });

                        final TaskSingleValueEventFor userSearchTask = new TaskSingleValueEventFor<DataSnapshot>()
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null) {
                                            ArrayList<HashMap<String, Object>> users = (ArrayList<HashMap<String, Object>>) dataSnapshot.getValue();
                                            for (HashMap<String, Object> user : users) {
                                                if (user != null && user.containsKey(REQUEST_DEVICE_ID) && user.containsKey(REQUEST_KEY)) {
                                                    String calculatedHash = Misc.getEncryptedHash(check.getControl() + ":" + user.get(REQUEST_DEVICE_ID).toString());
                                                    if (calculatedHash.equals(hash)) {
                                                        check.setUid(user.get(REQUEST_KEY).toString());
                                                        userGetNumberTask.setRef(refGroup.child(Firebase.SECTION_USERS_KEYS).child(check.getUid())).start();
                                                        return;
                                                    }
                                                }
                                            }
                                            Common.err(LOG, "onMessage:joinUserNotFound:" + conn.getRemoteSocketAddress());
                                            rejectUser(response, conn, check.getGroupId(), null, "This group is expired. (004)");
                                        } else {
                                            Common.err(LOG, "onMessage:joinEmptyGroup:" + conn.getRemoteSocketAddress());
                                            rejectUser(response, conn, check.getGroupId(), null, "This group is expired. (003)");
                                        }
                                    }
                                });

                        TaskSingleValueEventFor groupOptionsTask = new TaskSingleValueEventFor<DataSnapshot>()
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        if (check.getUid() != null) {
                                            if (dataSnapshot.getValue() != null) {
                                                userCheckTask.setRef(refGroup.child(Firebase.SECTION_USERS_DATA_PRIVATE).child("" + check.getNumber())).start();
                                            } else {
                                                Common.err(LOG, "onMessage:joinUserNotExists:" + conn.getRemoteSocketAddress());
                                                rejectUser(response, conn, check.getGroupId(), null, "This group is expired. (002)");
                                            }
                                        } else {
                                            userSearchTask.setRef(refGroup.child(Firebase.SECTION_USERS_DATA_PRIVATE)).start();
                                        }
                                    }
                                });


                        groupOptionsTask.setRef(refGroup.child(Firebase.SECTION_OPTIONS)).start();
                    } else {
                        Common.err(LOG, "onMessage:joinNotAuthorized:" + conn.getRemoteSocketAddress());
                        rejectUser(response, conn, null, null, "Cannot join to group (user not authorized).");
                    }
                } else {
                    Common.err(LOG, "onMessage:joinNotDefined:" + conn.getRemoteSocketAddress());
                    rejectUser(response, conn, null, null, "Cannot join to group (hash not defined).");
                }
            }
        } catch (Exception e) {
            Common.err(LOG, "onMessage:error:" + e.getMessage(), "req:" + message);
            e.printStackTrace();
            conn.send("{\"status\":\"Request failed\"}");
        }
    }

    private void rejectUser(JSONObject response, DataProcessorConnection conn, String groupId, String userId, String message) {
        Common.err(LOG, "rejectUser:" + userId, "groupId:" + groupId, "reason:" + message, "response:" + response);
        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
        response.put(RESPONSE_MESSAGE, message);
        conn.send(response.toString());
        conn.close();
        putStaticticsUser(groupId, null, UserAction.USER_REJECTED, message);
    }

    @Override
    public void createGroup(final MyGroup group, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {

        final JSONObject json = new JSONObject();

        Common.log(LOG, "New group ID:", group.getId());

        new TaskSingleValueEventFor<DataSnapshot>(ref.child(Firebase.SECTION_GROUPS).child(group.getId()))
                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put(Firebase.SECTION_OPTIONS + "/"
                                    + Firebase.OPTION_WELCOME_MESSAGE, group.getWelcomeMessage());
                            childUpdates.put(Firebase.SECTION_OPTIONS + "/"
                                    + Firebase.OPTION_REQUIRES_PASSWORD, group.isRequirePassword());
                            childUpdates.put(Firebase.SECTION_OPTIONS + "/"
                                    + Firebase.OPTION_TIME_TO_LIVE_IF_EMPTY, group.getTimeToLiveIfEmpty());
                            childUpdates.put(Firebase.SECTION_OPTIONS + "/"
                                    + Firebase.OPTION_PERSISTENT, group.isPersistent());
                            childUpdates.put(Firebase.SECTION_OPTIONS + "/"
                                    + Firebase.OPTION_DISMISS_INACTIVE, group.isDismissInactive());
                            childUpdates.put(Firebase.SECTION_OPTIONS + "/"
                                    + Firebase.OPTION_DELAY_TO_DISMISS, group.getDelayToDismiss());
                            childUpdates.put(Firebase.SECTION_OPTIONS + "/"
                                    + Firebase.OPTION_DATE_CREATED, ServerValue.TIMESTAMP);
                            childUpdates.put(Firebase.SECTION_OPTIONS + "/"
                                    + Firebase.OPTION_DATE_CHANGED, ServerValue.TIMESTAMP);
                            ref.child(group.getId()).updateChildren(childUpdates);
                            ref.child(Firebase.SECTION_GROUPS).child(group.getId()).setValue(0);

                            json.put(Rest.STATUS, Rest.SUCCESS);
                            json.put(Rest.GROUP_ID, group.getId());

                            Common.log(LOG, "createGroup:created:" + group.getId());

                            onsuccess.call(json);

                            putStaticticsGroup(group.getId(), group.isPersistent(), GroupAction.GROUP_CREATED, null);
                        } else {
                            json.put(Rest.STATUS, Rest.ERROR);
                            json.put(Rest.GROUP_ID, group.getId());
                            json.put(Rest.MESSAGE, "Group " + group.getId() + " already exists.");
                            Common.err(LOG, "createGroup:alreadyExists:" + group.getId());
                            if (onerror != null) onerror.call(json);
                            putStaticticsGroup(group.getId(), group.isPersistent(), GroupAction.GROUP_REJECTED, "already exists");
                        }
                    }
                }).start();

    }

    @Override
    public void deleteGroup(final String groupId, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        final JSONObject json = new JSONObject();

        json.put(Rest.GROUP_ID, groupId);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                json.put(Rest.STATUS, Rest.ERROR);
                json.put(Rest.MESSAGE, e.getMessage());
                Common.err(LOG, "deleteGroup:" + groupId, "error:" + e.getMessage());
                onerror.call(json);
            }
        };

        Task<Void> deleteGroupTask = ref.child(Firebase.SECTION_GROUPS).child(groupId).removeValue();
        try {
            Tasks.await(deleteGroupTask);

            Task<Void> deleteGroupIdTask = ref.child(groupId).removeValue();
            Tasks.await(deleteGroupIdTask);
            json.put(Rest.STATUS, Rest.SUCCESS);
            Common.log(LOG, "deleteGroup:" + groupId);
            onsuccess.call(json);

            putStaticticsGroup(groupId, false, GroupAction.GROUP_DELETED, null);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            onFailureListener.onFailure(e);
        }
    }

    @Override
    public void switchPropertyInGroup(final String groupId, final String property, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {

        final JSONObject res = new JSONObject();
        res.put(Rest.PROPERTY, property);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.put(Rest.STATUS, Rest.ERROR);
                res.put(Rest.MESSAGE, e.getMessage());
                Common.log(LOG, "switchPropertyInGroup:", property, e.getMessage());
                onerror.call(res);
            }
        };

        new TaskSingleValueEventFor<DataSnapshot>(ref.child(groupId).child(Firebase.SECTION_OPTIONS).child(property))
                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Boolean value = (Boolean) dataSnapshot.getValue();
                        if (value != null) {
                            res.put(Rest.OLD_VALUE, value);
                            value = !value;
                            ref.child(groupId).child(Firebase.SECTION_OPTIONS).child(property).setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    res.put(Rest.STATUS, Rest.SUCCESS);
                                    onsuccess.call(res);
                                }
                            }).addOnFailureListener(onFailureListener);
                        } else {
                            onFailureListener.onFailure(new Exception("Null value."));
                        }
                    }
                }).start();

    }

    @Override
    public void modifyPropertyInGroup(final String groupId, final String property, final Serializable value, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {

        final JSONObject res = new JSONObject();
        res.put(Rest.PROPERTY, property);
        res.put(Rest.VALUE, value);

        new TaskSingleValueEventFor<DataSnapshot>(ref.child(groupId).child(Firebase.SECTION_OPTIONS).child(property))
                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Serializable oldValue = (Serializable) dataSnapshot.getValue();
                        if (oldValue != null && value != null) {
                            res.put(Rest.OLD_VALUE, oldValue);
                            ref.child(groupId).child(Firebase.SECTION_OPTIONS).child(property).setValue(value);
                            res.put(Rest.STATUS, Rest.SUCCESS);
                            onsuccess.call(res);
                        } else {
                            Common.err(LOG, "modifyPropertyInGroup:nullValue:", property);
                            res.put(Rest.STATUS, Rest.ERROR);
                            onerror.call(res);
                        }
                    }
                }).start();

    }

    @Override
    public void registerUser(final String groupId, final MyUser user, String action, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        final JSONObject response = new JSONObject();

        if(REQUEST_NEW_GROUP.equals(action)) {
            ref.child(Firebase.SECTION_GROUPS).child(groupId).setValue(user.getUid());
            DatabaseReference nodeNumber = ref.child(groupId).child(Firebase.SECTION_USERS_ORDER).push();
            nodeNumber.setValue(user.getUid());
        }

        user.setColor(Utils.selectColor(user.getNumber()));

        final Map<String, Object> childUpdates = new HashMap<>();

        Map<String, Object> o = new HashMap<>();
        o.put(Firebase.USER_COLOR, user.getColor());
        o.put(Firebase.USER_NAME, user.getName());
        if(!user.getDeviceId().startsWith("server:")) {
            o.put(Firebase.USER_ACTIVE, true);
        }
        o.put(Firebase.USER_CREATED, user.getCreated());
        o.put(Firebase.USER_CHANGED, ServerValue.TIMESTAMP);
        childUpdates.put(Firebase.SECTION_USERS_DATA + "/" + user.getNumber(), o);

        o = new HashMap<>();

//                    o.put(RESPONSE_CONTROL,user.getControl());
        o.put(REQUEST_MODEL, user.getModel());
        o.put(REQUEST_DEVICE_ID, user.getDeviceId());
        o.put(REQUEST_OS, user.getOs());
        if(user.getSignProvider() != null) o.put(REQUEST_SIGN_PROVIDER, user.getSignProvider());

        System.out.println("SIGNPROVIDER:"+user.getSignProvider());
        final String uid;
        if(user.getUserId() != null && user.getUserId().length() > 0) {
            uid = user.getUserId();
        } else {
            uid = Misc.getEncryptedHash(user.getDeviceId());
        }
        o.put(REQUEST_KEY, uid);
        childUpdates.put(Firebase.SECTION_USERS_DATA_PRIVATE + "/" + user.getNumber(), o);

        for (Map.Entry<String, RequestHolder> entry : requestHolders.entrySet()) {
            if (entry.getValue().isSaveable()) {
                childUpdates.put(Firebase.SECTION_PUBLIC + "/" + entry.getKey() + "/" + user.getNumber(), "{}");
            }
        }

        childUpdates.put(Firebase.SECTION_USERS_KEYS + "/" + uid, user.getNumber());

        final Task<Void> updateUserTask = ref.child(groupId).updateChildren(childUpdates);
        try {
            Tasks.await(updateUserTask);

            Common.log(LOG, "registerUser:" + user.getNumber(), "uid:" + uid, "group:" + groupId);

            if(action != null) {
                String customToken = createCustomToken(uid);

                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                if (!REQUEST_JOIN_GROUP.equals(action) && !REQUEST_CHECK_USER.equals(action)) {
                    response.put(RESPONSE_TOKEN, groupId);
                }
                response.put(RESPONSE_NUMBER, user.getNumber());
                response.put(RESPONSE_SIGN, customToken);

            }
            if(onsuccess != null) {
                onsuccess.call(response);
            } else {
                user.connection.send(response.toString());
                user.connection.close();
            }
            putStaticticsUser(groupId, user.getName(), UserAction.USER_JOINED, null);
        } catch (Exception e) {
            e.printStackTrace();
            if(onerror != null) onerror.call(response);

            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
            response.put(RESPONSE_MESSAGE, "Cannot register (code 18).");
            Common.err(LOG, "registerUser:error:",user, "groupId:", groupId, e);
            if (onerror != null) {
                onerror.call(response);
            } else {
                user.connection.send(response.toString());
                user.connection.close();
            }
            putStaticticsUser(groupId, user.getName(), UserAction.USER_REJECTED, e.getMessage());
        }
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
    public void removeUser(final String groupId, final Long userNumber, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {

        final JSONObject json = new JSONObject();
//        final String user = String.valueOf(userNumber);

        json.put(Rest.GROUP_ID, groupId);
        json.put(Rest.USER_NUMBER, userNumber);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                json.put(Rest.STATUS, Rest.ERROR);
                json.put(Rest.MESSAGE, e.getMessage());
                Common.err(LOG, "removeUser:" + userNumber, "group:" + groupId, "error:" + e.getMessage());
                onerror.call(json);
            }
        };
        onFailureListener.onFailure(new Exception("Not implemented yet."));

        /*ref.child(groupId).child(DATABASE.SECTION_USERS_DATA_PRIVATE).child(user).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(user).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        new TaskSingleValueEventFor(ref.child(groupId).child(DATABASE.SECTION_USERS_KEYS))
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        HashMap<String,Serializable> val = (HashMap<String, Serializable>) dataSnapshot.getValue();
                                        for(Map.Entry<String,Serializable> x:val.entrySet()) {
                                            System.out.println(userNumber +":"+x.getKey() + ":" + x.getValue() + ":"+x.getValue().getClass()+":"+(x.getValue() == userNumber));
                                            if(x.getValue() == userNumber) {
                                                ref.child(groupId).child(DATABASE.SECTION_USERS_KEYS).child(x.getKey()).removeValue().addOnCompleteListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        json.put(Constants.REST.STATUS, Constants.REST.SUCCESS);
                                                        Common.log(LOG, "removeUser:" + userNumber, "group:"+groupId);
                                                        onsuccess.call(json);
                                                    }
                                                });
                                                return;
                                            }
                                        }
                                        onFailureListener.onFailure(new Exception("User not found."));
                                    }
                                }).start();
                    }
                }).addOnFailureListener(onFailureListener);
            }
        }).addOnFailureListener(onFailureListener);*/
    }

    @Override
    public void switchPropertyForUser(final String groupId, final Long userNumber, final String property, final Boolean value, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {

        final JSONObject res = new JSONObject();
        res.put(Rest.PROPERTY, property);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.put(Rest.STATUS, Rest.ERROR);
                res.put(Rest.MESSAGE, e.getMessage());
                Common.log(LOG, "switchPropertyForUser:", property, e.getMessage());
                onerror.call(res);
            }
        };

        new TaskSingleValueEventFor<DataSnapshot>(ref.child(groupId).child(Firebase.SECTION_USERS_DATA).child(String.valueOf(userNumber)).child(property))
                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Boolean oldValue = (Boolean) dataSnapshot.getValue();
                        if (oldValue != null) {
                            res.put(Rest.OLD_VALUE, oldValue);
                            Boolean newValue = !oldValue;
                            if (value != null) newValue = value;
                            ref.child(groupId).child(Firebase.SECTION_USERS_DATA).child(String.valueOf(userNumber)).child(property).setValue(newValue).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    res.put(Rest.STATUS, Rest.SUCCESS);
                                    onsuccess.call(res);
                                }
                            }).addOnFailureListener(onFailureListener);
                        } else {
                            onFailureListener.onFailure(new Exception("Invalid property."));
                        }
                    }
                }).start();
    }

    public void validateGroups() {

        Common.log(LOG, "Groups validation is performing, checking online users");
        new TaskSingleValueEventFor<JSONObject>(ref.child("/")).setFirebaseRest(true).addOnCompleteListener(new Runnable1<JSONObject>() {
            @Override
            public void call(JSONObject groups) {
                try {
                    Iterator<String> iter = groups.keys();
                    while (iter.hasNext()) {
                        final String group = iter.next();
                        if (group.startsWith("_") || "overview".equals(group)) {
                            Common.log(LOG, "Key skipped: " + group);
                            continue;
                        }

                        new TaskSingleValueEventFor<DataSnapshot>(ref.child(group).child(Firebase.SECTION_OPTIONS))
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        Map value = (Map) dataSnapshot.getValue();

                                        Common.log(LOG, "Group found:", group/* + ", leader id:", leader, dataSnapshot.getValue()*/);

                                        if (value == null) {
                                            Common.log(LOG, "--- corrupted group detected, removing ----- 1"); //TODO
                                            ref.child(Firebase.SECTION_GROUPS).child(group).removeValue();
                                            ref.child(group).removeValue();
                                            putStaticticsGroup(group, false, GroupAction.GROUP_DELETED, "corrupted group detected, removing ----- 1");
                                            return;
                                        }

                                        final boolean requiresPassword;
                                        final boolean dismissInactive;
                                        final boolean persistent;
                                        final long delayToDismiss;
                                        final long timeToLiveIfEmpty;


                                        Object object = value.get(Firebase.OPTION_REQUIRES_PASSWORD);
                                        requiresPassword = object != null && (boolean) object;

                                        object = value.get(Firebase.OPTION_DISMISS_INACTIVE);
                                        dismissInactive = object != null && (boolean) object;

                                        object = value.get(Firebase.OPTION_PERSISTENT);
                                        persistent = object != null && (boolean) object;

                                        object = value.get(Firebase.OPTION_DELAY_TO_DISMISS);
                                        if (object != null)
                                            delayToDismiss = Long.parseLong("0" + object.toString());
                                        else delayToDismiss = 0;

                                        object = value.get(Firebase.OPTION_TIME_TO_LIVE_IF_EMPTY);
                                        if (object != null)
                                            timeToLiveIfEmpty = Long.parseLong("0" + object.toString());
                                        else timeToLiveIfEmpty = 0;

                                        new TaskSingleValueEventFor<DataSnapshot>(ref.child(group).child(Firebase.SECTION_USERS_DATA))
                                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                                    @Override
                                                    public void call(DataSnapshot dataSnapshot) {
                                                        Common.log(LOG, "Users validation for group:", group);

                                                        ArrayList<Map<String, Serializable>> users = null;
                                                        try {
                                                            //noinspection unchecked
                                                            users = (ArrayList<Map<String, Serializable>>) dataSnapshot.getValue();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        if (users == null) {
                                                            Common.log(LOG, "--- corrupted group detected, removing: ----- 2"); //TODO
                                                            ref.child(Firebase.SECTION_GROUPS).child(group).removeValue();
                                                            ref.child(group).removeValue();
                                                            putStaticticsGroup(group, false, GroupAction.GROUP_DELETED, "corrupted group detected, removing: ----- 2");
                                                            return;
                                                        }
                                                        long groupChanged = 0;

                                                        for (int i = 0; i < users.size(); i++) {
                                                            Map<String, Serializable> user = users.get(i);
                                                            if (user == null) continue;

                                                            String name = (String) user.get(Firebase.USER_NAME);
                                                            Long changed = (Long) user.get(Firebase.USER_CHANGED);
                                                            if (changed != null && changed > groupChanged)
                                                                groupChanged = changed;
                                                            boolean active = false;
                                                            Object object = user.get(Firebase.USER_ACTIVE);
                                                            if (object != null) {
                                                                active = (Boolean) object;
                                                            }

                                                            if (!active) continue;

                                                            if (dismissInactive) {
                                                                Long current = new Date().getTime();
                                                                if (changed == null) {
                                                                    Common.log(LOG, "--- user:", i, "name:", name, "is NULL");
                                                                    dataSnapshot.getRef().child("" + i).child(Firebase.USER_ACTIVE).setValue(false);
                                                                } else if (current - delayToDismiss * 1000 > changed) {
                                                                    Common.log(LOG, "--- user:", i, "name:", name, "is EXPIRED for", ((current - delayToDismiss * 1000 - changed) / 1000), "seconds");
                                                                    dataSnapshot.getRef().child("" + i).child(Firebase.USER_ACTIVE).setValue(false);
                                                                } else {
                                                                    dataSnapshot.getRef().getParent().getParent().child(Firebase.SECTION_OPTIONS).child(Firebase.OPTION_DATE_CHANGED).setValue(changed);
                                                                    Common.log(LOG, "--- user:", i, "name:", name, "is OK");
                                                                }
                                                            }
                                                        }

                                                        if (!persistent && timeToLiveIfEmpty > 0 && new Date().getTime() - groupChanged > timeToLiveIfEmpty * 60 * 1000) {
                                                            String info = group + " expired for " + ((new Date().getTime() - groupChanged - timeToLiveIfEmpty * 60 * 1000) / 1000 / 60) + " minutes";
                                                            Common.log(LOG, "--- removing group " + info);
                                                            ref.child(Firebase.SECTION_GROUPS).child(group).removeValue();
                                                            ref.child(group).removeValue();
                                                            putStaticticsGroup(group, false, GroupAction.GROUP_DELETED, info);
                                                        }
                                                    }
                                                }).start();
                                    }
                                }).start();


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

        /*try {
            System.out.println("https://waytous-beta.firebaseio.com/.json?shallow=true&print=pretty&auth="+OPTIONS.getFirebaseApiKey());

            String res = Utils.getUrl("https://waytous-beta.firebaseio.com/.json?shallow=true&print=pretty&auth="+OPTIONS.getFirebaseApiKey(),"UTF-8");

            JSONObject groups = new JSONObject(res);

            Iterator<String> iter = groups.keys();
            while(iter.hasNext()) {
                final String group = iter.next();
                if(Constants.DATABASE.SECTION_GROUPS.equals(group) || "overview".equals(group)) continue;

                new TaskSingleValueEventFor<DataSnapshot>(ref.child(group).child(Constants.DATABASE.SECTION_OPTIONS))
                        .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                            @Override
                            public void call(DataSnapshot dataSnapshot) {
                                Map value = (Map) dataSnapshot.getValue();

                                Common.log(LOG, "Group found:", group*//* + ", leader id:", leader, dataSnapshot.getValue()*//*);

                                if (value == null) {
                                    Common.log(LOG, "--- corrupted group detected, removing ----- 1"); //TODO
                                    ref.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                    ref.child(group).removeValue();
                                    return;
                                }

                                final boolean requiresPassword;
                                final boolean dismissInactive;
                                final boolean persistent;
                                final long delayToDismiss;
                                final long timeToLiveIfEmpty;


                                Object object = value.get(Constants.DATABASE.OPTION_REQUIRES_PASSWORD);
                                requiresPassword = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.OPTION_DISMISS_INACTIVE);
                                dismissInactive = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.OPTION_PERSISTENT);
                                persistent = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.OPTION_DELAY_TO_DISMISS);
                                if (object != null)
                                    delayToDismiss = Long.parseLong("0" + object.toString());
                                else delayToDismiss = 0;

                                object = value.get(Constants.DATABASE.OPTION_TIME_TO_LIVE_IF_EMPTY);
                                if (object != null)
                                    timeToLiveIfEmpty = Long.parseLong("0" + object.toString());
                                else timeToLiveIfEmpty = 0;

                                new TaskSingleValueEventFor<DataSnapshot>(ref.child(group).child(Constants.DATABASE.SECTION_USERS_DATA))
                                        .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                            @Override
                                            public void call(DataSnapshot dataSnapshot) {
                                                Common.log(LOG, "Users validation for group:", group);

                                                ArrayList<Map<String, Serializable>> users = null;
                                                try {
                                                    users = (ArrayList<Map<String, Serializable>>) dataSnapshot.getValue();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                if (users == null) {
                                                    Common.log(LOG, "--- corrupted group detected, removing: ----- 2"); //TODO
                                                    ref.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                                    ref.child(group).removeValue();
                                                    return;
                                                }
                                                long groupChanged = 0;

                                                for (int i = 0; i < users.size(); i++) {
                                                    Map<String, Serializable> user = users.get(i);
                                                    if (user == null) continue;

                                                    String name = (String) user.get(Constants.DATABASE.USER_NAME);
                                                    Long changed = (Long) user.get(Constants.DATABASE.USER_CHANGED);
                                                    if (changed != null && changed > groupChanged)
                                                        groupChanged = changed;
                                                    boolean active = false;
                                                    Object object = user.get(Constants.DATABASE.USER_ACTIVE);
                                                    if (object != null) {
                                                        active = (Boolean) object;
                                                    }

                                                    if (!active) continue;

                                                    if (dismissInactive) {
                                                        Long current = new Date().getTime();
                                                        if (changed == null) {
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is NULL");
                                                            dataSnapshot.getRef().child("" + i).child(Constants.DATABASE.USER_ACTIVE).setValue(false);
                                                        } else if (current - delayToDismiss * 1000 > changed) {
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is EXPIRED for", ((current - delayToDismiss * 1000 - changed) / 1000), "seconds");
                                                            dataSnapshot.getRef().child("" + i).child(Constants.DATABASE.USER_ACTIVE).setValue(false);
                                                        } else {
                                                            dataSnapshot.getRef().getParent().getParent().child(Constants.DATABASE.SECTION_OPTIONS).child(Constants.DATABASE.OPTION_DATE_CHANGED).setValue(changed);
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is OK");
                                                        }
                                                    }
                                                }

                                                if (!persistent && timeToLiveIfEmpty > 0 && new Date().getTime() - groupChanged > timeToLiveIfEmpty * 60 * 1000) {
                                                    Common.log(LOG, "--- removing group " + group + " expired for", (new Date().getTime() - groupChanged - timeToLiveIfEmpty * 60 * 1000) / 1000 / 60, "minutes");
                                                    ref.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                                    ref.child(group).removeValue();
                                                }
                                            }
                                        }).start();
                            }
                        }).start();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    public void validateUsers() {

    }

    /**
     * This method requests and returns customToken from Firebase. Depending on current installation type
     * it defines the properly request and performs it. Installation type can be defined in gradle.build.
     */
    @Override
    public String createCustomToken(String uid) {
        String customToken = null;
        if (Common.getInstance().getDataProcessor("v1").isServerMode()) {
            try {
                Class tempClass = Class.forName("com.google.firebase.auth.FirebaseAuth");
                //noinspection unchecked
                Method method = tempClass.getDeclaredMethod("createCustomToken", String.class);
                //noinspection unchecked
                Task<String> taskCreateToken = (Task<String>) method.invoke(FirebaseAuth.getInstance(), uid);
                Tasks.await(taskCreateToken);
                customToken = taskCreateToken.getResult();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            customToken = String.valueOf(FirebaseAuth.getInstance().createCustomToken(uid));
        }
        return customToken;
    }

    @Override
    public void putStaticticsGroup(String groupId, boolean isPersistent, GroupAction action, String errorMessage) {
        DatabaseReference referenceTotal;
        DatabaseReference referenceToday;
        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));

        referenceTotal = ref.child(Firebase.SECTION_STAT).child(Firebase.STAT_TOTAL);
        referenceToday = ref.child(Firebase.SECTION_STAT).child(Firebase.STAT_BY_DATE).child(today);

        switch(action) {
            case GROUP_CREATED:
                if(isPersistent) {
                    referenceTotal = referenceTotal.child(Firebase.STAT_GROUPS_CREATED_PERSISTENT);
                    referenceToday = referenceToday.child(Firebase.STAT_GROUPS_CREATED_PERSISTENT);
                } else {
                    referenceTotal = referenceTotal.child(Firebase.STAT_GROUPS_CREATED_TEMPORARY);
                    referenceToday = referenceToday.child(Firebase.STAT_GROUPS_CREATED_TEMPORARY);
                }
                break;
            case GROUP_DELETED:
                referenceTotal = referenceTotal.child(Firebase.STAT_GROUPS_DELETED);
                referenceToday = referenceToday.child(Firebase.STAT_GROUPS_DELETED);
                break;
            case GROUP_REJECTED:
                referenceTotal = referenceTotal.child(Firebase.STAT_GROUPS_REJECTED);
                referenceToday = referenceToday.child(Firebase.STAT_GROUPS_REJECTED);
                break;
        }

        referenceToday.runTransaction(incrementValue);
        referenceTotal.runTransaction(incrementValue);

        if(errorMessage != null && errorMessage.length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("group", groupId);
            map.put("action", action.toString());
            putStaticticsMessage(errorMessage, map);
        }
    }

    @Override
    public void putStaticticsUser(String groupId, String userId, UserAction action, String errorMessage) {
        DatabaseReference referenceTotal;
        DatabaseReference referenceToday;
        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));

        referenceTotal = ref.child(Firebase.SECTION_STAT).child(Firebase.STAT_TOTAL);
        referenceToday = ref.child(Firebase.SECTION_STAT).child(Firebase.STAT_BY_DATE).child(today);
        switch(action) {
            case USER_JOINED:
                referenceTotal = referenceTotal.child(Firebase.STAT_USERS_JOINED);
                referenceToday = referenceToday.child(Firebase.STAT_USERS_JOINED);
                break;
            case USER_RECONNECTED:
                referenceTotal = referenceTotal.child(Firebase.STAT_USERS_RECONNECTED);
                referenceToday = referenceToday.child(Firebase.STAT_USERS_RECONNECTED);
                break;
            case USER_REJECTED:
                referenceTotal = referenceTotal.child(Firebase.STAT_USERS_REJECTED);
                referenceToday = referenceToday.child(Firebase.STAT_USERS_REJECTED);
                break;
        }

        referenceToday.runTransaction(incrementValue);
        referenceTotal.runTransaction(incrementValue);

        if(errorMessage != null && errorMessage.length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("group", groupId);
            map.put("user", userId);
            map.put("action", action.toString());
            putStaticticsMessage(errorMessage, map);
        }
    }

    @Override
    public void putStaticticsMessage(String message, Map<String, String> map) {
        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d %02d-%02d-%02d-%03d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));

        if(map == null) {
            map = new HashMap<>();
        }
        map.put("message", message);
        ref.child(Firebase.SECTION_STAT).child(Firebase.STAT_MESSAGES).child(today).setValue(map);
    }

    @Override
    public void cleanStatisticsMessages(final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {

        final JSONObject res = new JSONObject();

        ref.child(Firebase.SECTION_STAT).child(Firebase.STAT_MESSAGES).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                res.put(Rest.STATUS, Rest.SUCCESS);
                Common.log(LOG, "cleanStatisticsMessages:done");
                onsuccess.call(res);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.put(Rest.STATUS, Rest.ERROR);
                res.put(Rest.MESSAGE, e.getMessage());
                Common.err(LOG, "cleanStatisticsMessages:failed", e.getMessage());
                onerror.call(res);
            }
        });
    }

    private Transaction.Handler incrementValue = new Transaction.Handler() {
        @Override
        public Transaction.Result doTransaction(MutableData mutableData) {
            Integer value = mutableData.getValue(Integer.class);
            if (value == null) {
                value = 0;
            }
            value++;
            mutableData.setValue(value);
            return Transaction.success(mutableData);
        }
        @Override
        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
            // Transaction completed
            Common.log(LOG, "incrementValue:onComplete:" + databaseError);
        }
    };

}
