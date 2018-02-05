package com.edeqa.waytousserver.servers;

import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.CheckReq;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.edeqa.waytousserver.rest.tracking.AbstractTrackingAction;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;

import static com.edeqa.edequate.abstracts.AbstractAction.MESSAGE;
import static com.edeqa.edequate.abstracts.AbstractAction.STATUS;
import static com.edeqa.edequate.abstracts.AbstractAction.STATUS_ERROR;
import static com.edeqa.edequate.abstracts.AbstractAction.STATUS_SUCCESS;
import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.REQUEST;
import static com.edeqa.waytous.Constants.REQUEST_CHECK_USER;
import static com.edeqa.waytous.Constants.REQUEST_HASH;
import static com.edeqa.waytous.Constants.REQUEST_JOIN_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_MODEL;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_OS;
import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;
import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.REQUEST_TOKEN;
import static com.edeqa.waytous.Constants.REQUEST_UID;
import static com.edeqa.waytous.Constants.RESPONSE_CONTROL;
import static com.edeqa.waytous.Constants.RESPONSE_MESSAGE;
import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.Constants.RESPONSE_SIGN;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_CHECK;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ERROR;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_UPDATED;
import static com.edeqa.waytous.Constants.RESPONSE_TOKEN;
import static com.edeqa.waytousserver.servers.AbstractDataProcessor.AccountAction.ACCOUNT_CREATED;

/**
 * Created 10/5/16.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class DataProcessorFirebaseOld extends AbstractDataProcessor {

    public static final String VERSION = "v1";
    private static String LOG = "DPF1";
    private DatabaseReference refAccounts;
    private DatabaseReference refGroups;
    private DatabaseReference refStat;
    private DatabaseReference refRoot;

    public DataProcessorFirebaseOld() throws ServletException, IOException {
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
//        System.out.println(defaultApp.getType());  // "[DEFAULT]"
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
            refRoot = FirebaseDatabase.getInstance(defaultApp).getReference();
            refGroups = refRoot.child(Firebase.SECTION_GROUPS);
            refStat = refRoot.child(Firebase.SECTION_STAT);
            refAccounts = refRoot.child(Firebase.SECTION_USERS);
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
                Misc.err(LOG, "onMessage:request" + e.getMessage());
                return;
            }
            if (!request.has(REQUEST) || !request.has(REQUEST_TIMESTAMP)) {
                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                response.put(RESPONSE_MESSAGE, "Wrong request");
                conn.send(response.toString());
                conn.close();
                return;
            }
            final String uid;
            if(request.has(REQUEST_UID)) {
                uid = request.getString(REQUEST_UID);
                if(uid.startsWith("Administrator") || uid.startsWith("Viewer")) {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Wrong UID");
                    conn.send(response.toString());
                    conn.close();
                    putStaticticsUser(null, uid, UserAction.USER_REJECTED, message);
                    return;
                }
            } else {
                uid = null;
            }

            String req = request.getString(REQUEST);
            /*if (REQUEST_TRACKING.equals(req)) {
                response.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                conn.send(response.toString());
                conn.close();
                Common.log(LOG,"onMessage:updateCoords:fake",response);
            } else*/
            if ("test".equals(req)) {
                Misc.log(LOG, "onMessage:testMessage:" + conn.getRemoteSocketAddress(), message);
                response.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                response.put(RESPONSE_MESSAGE, "OK");
                conn.send(response.toString());
                conn.close();
            } else if (REQUEST_NEW_GROUP.equals(req)) {
                if (uid != null) {
                    final MyGroup group = new MyGroup();
                    final MyUser user = new MyUser(conn, request);
                    Misc.log(LOG, "onMessage:requestNew:" + conn.getRemoteSocketAddress(), "{ uid:" + uid + " }");

                    /*createOrUpdateUserAccount(user, new Runnable() {
                        @Override
                        public void run() {
                            //noinspection unchecked
                            final Runnable1<JSONObject>[] onresult = new Runnable1[2];
                            onresult[0] = new Runnable1<JSONObject>() {
                                @Override
                                public void call(JSONObject json) {
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
//                                    group.fetchNewId();
                                    createGroup(group, onresult[0], onresult[1]);
                                    putStaticticsAccount(user.getUid(), GroupAction.GROUP_CREATED_TEMPORARY.toString(), "group", group.getId(), null);

                                }
                            };
                            createGroup(group, onresult[0], onresult[1]);
                            putStaticticsAccount(user.getUid(), GroupAction.GROUP_CREATED_TEMPORARY.toString(), "group", group.getId(), null);
                        }
                    }, new Runnable1<Throwable>() {
                        @Override
                        public void call(Throwable error) {
                            Misc.err(LOG, "onMessage:newGroup:",user, error);
                            rejectUser(response, user.connection, null, null, "Cannot create group (code 16).");
                        }
                    });*/
                } else {
                    rejectUser(response, conn, null, null, "Cannot create group (code 15).");
                    Misc.err(LOG, "onMessage:newGroup:", response);
                }
            } else if (REQUEST_JOIN_GROUP.equals(req)) {
                if (request.has(REQUEST_TOKEN)) {

                    final String groupId = request.getString(REQUEST_TOKEN);
                    final DatabaseReference refGroup = refGroups.child(groupId);

                    final TaskSingleValueEventFor[] requestDataPrivateTask = new TaskSingleValueEventFor[1];
                    requestDataPrivateTask[0] = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(new Runnable1<DataSnapshot>() {
                        @Override
                        public void call(DataSnapshot dataSnapshot) {
                            final MyUser user = new MyUser(conn, request);

                            int count = 1;
                            boolean found = false;
                            Object value = dataSnapshot.getValue();

                            if (value == null) {
                                dataSnapshot.getRef().push().setValue(user.getUid());
                                requestDataPrivateTask[0].setRef(refGroup.child(Firebase.USERS).child(Firebase.QUEUE)).start();
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
                                createOrUpdateUserAccount(user, new Runnable() {
                                    @Override
                                    public void run() {
                                        registerUser(groupId, user, REQUEST_JOIN_GROUP, null, null);
                                    }
                                }, new Runnable1<Throwable>() {
                                    @Override
                                    public void call(Throwable error) {
                                        Misc.err(LOG, "onMessage:newGroup:",user, error);
                                        rejectUser(response, user.connection, groupId, user.getName(), "Cannot create group (code 17).");
                                    }
                                });

                            } else {
//                                Common.log(LOG, "onMessage:newGroup:", "user not found adding:", user.getUid());
//                                refGroups.child(Firebase.SECTION_GROUPS).child(groupId).setValue(user.getUid());
                                DatabaseReference nodeNumber = refGroups.child(groupId).child(Firebase.USERS).child(Firebase.QUEUE).push();
                                nodeNumber.setValue(user.getUid());
                                requestDataPrivateTask[0].setRef(refGroup.child(Firebase.USERS).child(Firebase.QUEUE)).start();
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

                                        Misc.log(LOG, "onMessage:checkRequest:" + conn.getRemoteSocketAddress(), "{ number:" + dataSnapshot.getValue(), "uid:" + dataSnapshot.getKey(), "control:" + check.getControl() + " }");

                                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                                        response.put(RESPONSE_CONTROL, check.getControl());
//                                        ipToCheck.put(ip, check);
                                        try {
                                            conn.send(response.toString());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else { // join as new member
                                        requestDataPrivateTask[0].setRef(refGroup.child(Firebase.USERS).child(Firebase.QUEUE)).start();
                                    }
                                }
                            });

                    TaskSingleValueEventFor groupOptionsTask = new TaskSingleValueEventFor<DataSnapshot>()
                            .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                @Override
                                public void call(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) {
                                        numberForKeyTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.KEYS).child(uid)).start();
                                    } else {
                                        rejectUser(response, conn, groupId, null, "This group is expired. (001)");
                                    }
                                }
                            });

                    if (uid != null) {
                        Misc.log(LOG, "onMessage:requestJoin:" + conn.getRemoteSocketAddress(), "{ groupId:" + groupId, "uid:" + uid + " }");
                        groupOptionsTask.setRef(refGroup.child(Firebase.OPTIONS)).start();
                    } else {
                        CheckReq check = new CheckReq();
                        check.setControl(Misc.getUnique());
                        check.setGroupId(groupId);

                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                        response.put(RESPONSE_CONTROL, check.getControl());
//                        ipToCheck.put(ip, check);
                        Misc.log(LOG, "onMessage:requestReconnect:" + conn.getRemoteSocketAddress(), "{ groupId:" + groupId, "} control:", check.getControl());
                        conn.send(response.toString());
                    }
                } else {
                    rejectUser(response, conn, null, null, "Wrong request (group not defined).");
                    System.out.println("JOIN:response:" + response);
                }
            } else if (REQUEST_CHECK_USER.equals(req)) {
                if (request.has(REQUEST_HASH)) {
                    final String hash = request.getString((REQUEST_HASH));
                    Misc.log(LOG, "onMessage:checkResponse:" + conn.getRemoteSocketAddress(), "hash:" + hash);
                    /*if (ipToCheck.containsKey(ip)) {
                        final CheckReq check = ipToCheck.get(ip);
                        ipToCheck.remove(ip);

                        Misc.log(LOG, "onMessage:checkFound:" + conn.getRemoteSocketAddress(), "{ name:" + check.getName(), "group:" + check.getGroupId(), "control:" + check.getControl() + " }");

                        final DatabaseReference refGroup = refGroups.child(check.getGroupId());

                        final TaskSingleValueEventFor userCheckTask = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(new Runnable1<DataSnapshot>() {
                            @Override
                            public void call(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) { //join as existing member
                                    try {
                                        String calculatedHash = Misc.getEncryptedHash(check.getControl() + ":" + ((HashMap) dataSnapshot.getValue()).get(REQUEST_UID));

                                        if (calculatedHash.equals(hash)) {
                                            Misc.log(LOG, "onMessage:joinAsExisting:" + conn.getRemoteSocketAddress(), "group:" + check.getGroupId(), "user:{ number:" + dataSnapshot.getKey(), "properties:" + dataSnapshot.getValue(), " }");

                                            try {
                                                final String customToken = createCustomToken(check.getUid());

                                                final Map<String, Object> update = new HashMap<>();
                                                update.put(Firebase.ACTIVE, true);
                                                update.put(Firebase.COLOR, Utils.selectColor((int) check.getNumber()));
                                                update.put(Firebase.CHANGED, new Date().getTime());
                                                if (check.getName() != null && check.getName().length() > 0) {
                                                    update.put(USER_NAME, check.getName());
                                                    check.getUser().setName(check.getName());
                                                }

                                                createOrUpdateUserAccount(check.getUser(), new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Task<Void> updateUserTask = refGroup.child(Firebase.USERS).child(Firebase.PUBLIC).child("" + check.getNumber()).updateChildren(update);
                                                        try {
                                                            Tasks.await(updateUserTask);
                                                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                                            response.put(RESPONSE_NUMBER, check.getNumber());
                                                            response.put(RESPONSE_SIGN, customToken);
                                                            conn.send(response.toString());

                                                            Misc.log(LOG, "onMessage:joined:" + conn.getRemoteSocketAddress(), "signToken: [provided]"*//*+customToken*//*);
                                                            conn.close();

                                                            putStaticticsUser(check.getGroupId(), check.getUid(), UserAction.USER_RECONNECTED, null);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }, new Runnable1<Throwable>() {
                                                    @Override
                                                    public void call(Throwable error) {
                                                        Misc.err(LOG, "onMessage:joinNotAuthenticated:" + conn.getRemoteSocketAddress(), "group:" + check.getGroupId(), check.getUser(), error);
                                                        rejectUser(response, conn, check.getGroupId(), check.getName(), "Cannot join to group (code 19).");
                                                    }
                                                });

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Misc.err(LOG, "onMessage:joinNotAuthenticated:" + conn.getRemoteSocketAddress(), "group:" + check.getGroupId(), "{ number:" + dataSnapshot.getKey(), "properties:" + dataSnapshot.getValue(), "} waited:", calculatedHash, " got:", hash);
                                            rejectUser(response, conn, check.getGroupId(), check.getName(), "Cannot join to group (user not authenticated).");
                                        }

                                    } catch (Exception e) {
                                        Misc.err(LOG, "onMessage:joinHashFailed:" + conn.getRemoteSocketAddress(), "group:" + check.getGroupId(), "{ number:" + dataSnapshot.getKey(), "properties:" + dataSnapshot.getValue(), "}");
                                        rejectUser(response, conn, check.getGroupId(), check.getName(), "Cannot join to group (user not authenticated).");
                                        e.printStackTrace();
                                    }

                                } else { // join as new member

                                    check.getUser().setNumber((int) check.getNumber());
                                    createOrUpdateUserAccount(check.getUser(), new Runnable() {
                                        @Override
                                        public void run() {
                                            registerUser(check.getGroupId(), check.getUser(), REQUEST_CHECK_USER, null, null);
                                            Misc.log(LOG, "onMessage:joinAsNew:" + check.getUser().connection.getRemoteSocketAddress());
                                        }
                                    }, new Runnable1<Throwable>() {
                                        @Override
                                        public void call(Throwable error) {
                                            Misc.err(LOG, "onMessage:joinAsNew:",check.getUser(), error);
                                            rejectUser(response, check.getUser().connection, check.getGroupId(), check.getUser().getName(),  "Cannot join to group (code 18).");
                                        }
                                    });
                                }
                            }
                        });

                        final TaskSingleValueEventFor userGetNumberTask = new TaskSingleValueEventFor<DataSnapshot>()
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null) {
                                            Misc.log(LOG, "onMessage:joinNumberFound:" + conn.getRemoteSocketAddress(), "number:", dataSnapshot.getValue().toString());
                                            check.setNumber(Long.parseLong(dataSnapshot.getValue().toString()));
                                            userCheckTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE).child(dataSnapshot.getValue().toString())).start();

                                        } else {
                                            Misc.err(LOG, "onMessage:joinNumberNotFound:" + conn.getRemoteSocketAddress());
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
                                                if (user != null && user.containsKey(REQUEST_UID)) {
                                                    String calculatedHash = Misc.getEncryptedHash(check.getControl() + ":" + user.get(REQUEST_UID).toString());
                                                    if (calculatedHash.equals(hash)) {
                                                        userGetNumberTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.KEYS).child(check.getUid())).start();
                                                        return;
                                                    }
                                                }
                                            }
                                            Misc.err(LOG, "onMessage:joinUserNotFound:" + conn.getRemoteSocketAddress());
                                            rejectUser(response, conn, check.getGroupId(), null, "This group is expired. (004)");
                                        } else {
                                            Misc.err(LOG, "onMessage:joinEmptyGroup:" + conn.getRemoteSocketAddress());
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
                                                userCheckTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE).child("" + check.getNumber())).start();
                                            } else {
                                                Misc.err(LOG, "onMessage:joinUserNotExists:" + conn.getRemoteSocketAddress());
                                                rejectUser(response, conn, check.getGroupId(), null, "This group is expired. (002)");
                                            }
                                        } else {
                                            userSearchTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE)).start();
                                        }
                                    }
                                });


                        groupOptionsTask.setRef(refGroup.child(Firebase.OPTIONS)).start();
                    } else {
                        Misc.err(LOG, "onMessage:joinNotAuthorized:" + conn.getRemoteSocketAddress());
                        rejectUser(response, conn, null, null, "Cannot join to group (user not authorized).");
                    }*/
                } else {
                    Misc.err(LOG, "onMessage:joinNotDefined:" + conn.getRemoteSocketAddress());
                    rejectUser(response, conn, null, null, "Cannot join to group (hash not defined).");
                }
            }
        } catch (Exception e) {
            Misc.err(LOG, "onMessage:error:" + e.getMessage(), "req:" + message);
            e.printStackTrace();
            conn.send("{\"status\":\"Request failed\"}");
        }
    }

    private void rejectUser(JSONObject response, DataProcessorConnection conn, String groupId, String userId, String message) {
        Misc.err(LOG, "rejectUser:" + userId, "groupId:" + groupId, "reason:" + message, "response:" + response);
        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
        response.put(RESPONSE_MESSAGE, message);
        conn.send(response.toString());
        conn.close();
        putStaticticsUser(groupId, userId, UserAction.USER_REJECTED, message);
    }

    @Override
    public void createGroup(final GroupRequest group, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {

        final JSONObject json = new JSONObject();

        Misc.log(LOG, "New group ID:", group.getId());

        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(group.getId()))
                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.WELCOME_MESSAGE, group.getWelcomeMessage());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.REQUIRES_PASSWORD, group.isRequiresPassword());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.TIME_TO_LIVE_IF_EMPTY, group.getTimeToLiveIfEmpty());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.PERSISTENT, group.isPersistent());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.DISMISS_INACTIVE, group.isDismissInactive());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.DELAY_TO_DISMISS, group.getDelayToDismiss());
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.CREATED, ServerValue.TIMESTAMP);
                            childUpdates.put(Firebase.OPTIONS + "/"
                                    + Firebase.CHANGED, ServerValue.TIMESTAMP);
                            refGroups.child(group.getId()).updateChildren(childUpdates);
//                            refGroups.child(Firebase.SECTION_GROUPS).child(group.getId()).setValue(0);

                            json.put(STATUS, STATUS_SUCCESS);
                            json.put(Rest.GROUP_ID, group.getId());

                            Misc.log(LOG, "createGroup:created:" + group.getId());

                            onsuccess.call(json);

                            putStaticticsGroup(group.getId(), group.isPersistent(), group.isPersistent() ? GroupAction.GROUP_CREATED_PERSISTENT : GroupAction.GROUP_CREATED_TEMPORARY, null);
                        } else {
                            json.put(STATUS, STATUS_ERROR);
                            json.put(Rest.GROUP_ID, group.getId());
                            json.put(MESSAGE, "Group " + group.getId() + " already exists.");
                            Misc.err(LOG, "createGroup:alreadyExists:" + group.getId());
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
                json.put(STATUS, STATUS_ERROR);
                json.put(MESSAGE, e.getMessage());
                Misc.err(LOG, "deleteGroup:" + groupId, "error:" + e.getMessage());
                onerror.call(json);
            }
        };

        Task<Void> deleteGroupTask = refGroups.child(groupId).removeValue();
        try {
            Tasks.await(deleteGroupTask);
            json.put(STATUS, STATUS_SUCCESS);
            Misc.log(LOG, "deleteGroup:" + groupId);
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
                res.put(STATUS, STATUS_ERROR);
                res.put(MESSAGE, e.getMessage());
                Misc.log(LOG, "switchPropertyInGroup:", property, e.getMessage());
                onerror.call(res);
            }
        };

        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(groupId).child(Firebase.OPTIONS).child(property))
                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Boolean value = (Boolean) dataSnapshot.getValue();
                        if (value != null) {
                            res.put(Rest.OLD_VALUE, value);
                            value = !value;
                            refGroups.child(groupId).child(Firebase.OPTIONS).child(property).setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    res.put(STATUS, STATUS_SUCCESS);
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

        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(groupId).child(Firebase.OPTIONS).child(property))
                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Serializable oldValue = (Serializable) dataSnapshot.getValue();
                        if (oldValue != null && value != null) {
                            res.put(Rest.OLD_VALUE, oldValue);
                            refGroups.child(groupId).child(Firebase.OPTIONS).child(property).setValue(value);
                            res.put(STATUS, STATUS_SUCCESS);
                            onsuccess.call(res);
                        } else {
                            Misc.err(LOG, "modifyPropertyInGroup:nullValue:", property);
                            res.put(STATUS, STATUS_ERROR);
                            onerror.call(res);
                        }
                    }
                }).start();
    }

    private void createOrUpdateUserAccount(final MyUser user, final Runnable onsuccess, final Runnable1<Throwable> onerror) {
        if(!user.isAccountAllowed()) {
            Misc.log(LOG, "createOrUpdateAccount:skipCreating:" + user.getUid(), user.getSignProvider());
            onsuccess.run();
            return;
        }

        TaskSingleValueEventFor createAccountTask = new TaskSingleValueEventFor<DataSnapshot>()
                .setRef(refAccounts.child(user.getUid()))
                .addOnSuccessListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Map<String, Object> accountPrivateData = new HashMap<>();

                        if (user.getName() != null && user.getName().length() > 0) {
                            accountPrivateData.put(Firebase.NAME, user.getName());
                        }
                        if (user.getSignProvider() != null) {
                            accountPrivateData.put(REQUEST_SIGN_PROVIDER, user.getSignProvider().toString());
                        }
                        accountPrivateData.put(Firebase.CHANGED, ServerValue.TIMESTAMP);

                        if (dataSnapshot.getValue() == null) {
                            accountPrivateData.put(REQUEST_MODEL, user.getModel());
                            accountPrivateData.put(REQUEST_OS, user.getOs());
                            accountPrivateData.put(Firebase.CREATED, ServerValue.TIMESTAMP);
                            Misc.log(LOG, "createOrUpdateAccount:createAccount:" + user.getUid(), accountPrivateData);
                            putStaticticsAccount(user.getUid(), ACCOUNT_CREATED.toString(), null, null, null);
                        } else {
                            Misc.log(LOG, "createOrUpdateAccount:updateAccount:" + user.getUid(), accountPrivateData);
                        }
                        final Task<Void> updateAccountTask = refAccounts.child(user.getUid()).child(Firebase.PRIVATE).updateChildren(accountPrivateData);

                        try {
                            Tasks.await(updateAccountTask);
                            Misc.log(LOG, "createOrUpdateAccount:accountDone:" + user.getUid());
                            onsuccess.run();
                        } catch (Exception e) {
                            Misc.err(LOG, "createOrUpdateAccount:accountError:" + user.getUid(), e);
                            onerror.call(e);
                        }
                    }
                })
                .addOnFailureListener(new Runnable1<Throwable>() {
                    @Override
                    public void call(Throwable error) {
                        Misc.err(LOG, "createOrUpdateAccount:accountCreateError:", user, error);
                        onerror.call(error);
                    }
                });
        createAccountTask.start();
    }


    @Override
    public void registerUser(final String groupId, final MyUser user, final String action, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        final JSONObject response = new JSONObject();

        final DatabaseReference refGroup = refGroups.child(groupId);

        if(REQUEST_NEW_GROUP.equals(action)) {
//            refGroups.child(groupId).setValue(user.getUid());
            DatabaseReference nodeNumber = refGroups.child(groupId).child(Firebase.USERS).child(Firebase.QUEUE).push();
            nodeNumber.setValue(user.getUid());
        }

        user.setColor(Utils.selectColor(user.getNumber()));

        final Map<String, Object> childUpdates = new HashMap<>();

        // public data inside group
        Map<String, Object> userPublicData = new HashMap<>();
        userPublicData.put(Firebase.COLOR, user.getColor());
        userPublicData.put(Firebase.NAME, user.getName());
        if(!user.getUid().startsWith("Administrator")) {
            userPublicData.put(Firebase.ACTIVE, true);
        }
        userPublicData.put(Firebase.CREATED, user.getCreated());
        userPublicData.put(Firebase.CHANGED, ServerValue.TIMESTAMP);

        childUpdates.put(Firebase.USERS + "/" + Firebase.PUBLIC + "/" + user.getNumber(), userPublicData);

        for (AbstractTrackingAction actionHolder : ((List<AbstractTrackingAction>) EventBus.getOrCreate(AbstractTrackingAction.EVENTBUS).getHoldersList())) {
            if (actionHolder.isSaveable()) {
                childUpdates.put(Firebase.PUBLIC + "/" + actionHolder.getType() + "/" + user.getNumber(), "{}");
            }
        }

        // user 'key - uid' inside group
        childUpdates.put(Firebase.USERS + "/" + Firebase.KEYS + "/" + user.getUid(), user.getNumber());

        // private data inside group
        Map<String, Object> userPrivateData = new HashMap<>();
        userPrivateData.put(REQUEST_UID, user.getUid());

        childUpdates.put(Firebase.USERS + "/" + Firebase.PRIVATE + "/" + user.getNumber(), userPrivateData);

        final Task<Void> updateUserTask = refGroup.updateChildren(childUpdates);
        try {
            Tasks.await(updateUserTask);

            Misc.log(LOG, "registerUser:" + user.getNumber(), "uid:" + user.getUid(), "group:" + groupId);

            if(action != null) {
                String customToken = createCustomToken(user.getUid());

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
            putStaticticsUser(groupId, user.getUid(), UserAction.USER_JOINED, null);
        } catch (Exception e) {
            e.printStackTrace();
            if(onerror != null) onerror.call(response);

            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
            response.put(RESPONSE_MESSAGE, "Cannot register (code 18).");
            Misc.err(LOG, "registerUser:error:",user, "groupId:", groupId, e);
            if (onerror != null) {
                onerror.call(response);
            } else {
                user.connection.send(response.toString());
                user.connection.close();
            }
            putStaticticsUser(groupId, user.getUid(), UserAction.USER_REJECTED, e.getMessage());
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
    public void removeUserFromGroup(final String groupId, final Long userNumber, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {

        final JSONObject res = new JSONObject();

//        final String user = String.valueOf(userNumber);

        res.put(Rest.GROUP_ID, groupId);
        res.put(Rest.USER_NUMBER, userNumber);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.put(STATUS, STATUS_ERROR);
                res.put(MESSAGE, e.getMessage());
                Misc.log(LOG, "removeUserFromGroup:error:", e.getMessage(), "userNumber:" + userNumber, "groupId:" + groupId);
                onerror.call(res);
            }
        };
//        onFailureListener.onFailure(new Exception("Not implemented yet."));

        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(groupId).child(Firebase.USERS).child(Firebase.PRIVATE).child(String.valueOf(userNumber)).child(REQUEST_UID))
                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot data) {

                        final Object value = data.getValue();
                        System.out.println(data.getValue());

                        if (value != null && value instanceof String) {
                            System.out.println("SEARCH");
                            new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(groupId).child(Firebase.USERS).child(Firebase.QUEUE))
                                    .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                        @Override
                                        public void call(DataSnapshot data) {
                                            for(DataSnapshot x: data.getChildren()) {
                                                System.out.println("ID:"+x.getValue());
                                                if(x.getValue().equals(value)) {
                                                    Map updates = new HashMap();
                                                    updates.put(Firebase.USERS + "/" + Firebase.PUBLIC + "/" + userNumber, null);
                                                    updates.put(Firebase.USERS + "/" + Firebase.QUEUE + "/" + x.getKey(), "removed:" + x.getValue());
                                                    updates.put(Firebase.USERS + "/" + Firebase.PRIVATE + "/" + userNumber, null);
                                                    updates.put(Firebase.USERS + "/" + Firebase.KEYS + "/" + value.toString(), null);

                                                    for (AbstractTrackingAction actionHolder : ((List<AbstractTrackingAction>) EventBus.getOrCreate(AbstractTrackingAction.EVENTBUS).getHoldersList())) {
                                                        if (actionHolder.isSaveable()) {
                                                            updates.put(Firebase.PUBLIC + "/" + actionHolder.getType() + "/" + userNumber, null);
                                                        }
                                                    }

                                                    refGroups.child(groupId).updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void result) {
                                                            res.put(STATUS, STATUS_SUCCESS);
                                                            Misc.log(LOG, "removeUserFromGroup:removed:userNumber:" + userNumber, "groupId:" + groupId);
                                                            onsuccess.call(res);
                                                            putStaticticsUser(groupId, value.toString(), UserAction.USER_REMOVED, null);
                                                        }
                                                    }).addOnFailureListener(onFailureListener);
                                                }
                                            }
                                        }
                                    }).start();
                        } else {
                            onFailureListener.onFailure(new Exception("User not found."));
                        }
                    }
                }).start();
    }

    @Override
    public void deleteAccount(final String accountId, Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        final JSONObject json = new JSONObject();

        json.put(Rest.UID, accountId);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                json.put(STATUS, STATUS_ERROR);
                json.put(MESSAGE, e.getMessage());
                Misc.err(LOG, "deleteAccount:" + accountId, "error:" + e.getMessage());
                onerror.call(json);
            }
        };

        Task<Void> deleteAccountTask = refAccounts.child(accountId).removeValue();
        try {
            Tasks.await(deleteAccountTask);

            json.put(STATUS, STATUS_SUCCESS);
            Misc.log(LOG, "deleteAccount:" + accountId);
            onsuccess.call(json);

            putStaticticsAccount(accountId, AccountAction.ACCOUNT_DELETED.toString(), null, null, accountId + " deleted.");

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            onFailureListener.onFailure(e);
        }
    }

    @Override
    public void switchPropertyForUser(final String groupId, final Long userNumber, final String property, final Boolean value, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {

        final JSONObject res = new JSONObject();
        res.put(Rest.PROPERTY, property);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.put(STATUS, STATUS_ERROR);
                res.put(MESSAGE, e.getMessage());
                Misc.log(LOG, "switchPropertyForUser:", property, e.getMessage());
                onerror.call(res);
            }
        };

        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(groupId).child(Firebase.USERS).child(Firebase.PUBLIC).child(String.valueOf(userNumber)).child(property))
                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Boolean oldValue = (Boolean) dataSnapshot.getValue();
                        if (oldValue != null) {
                            res.put(Rest.OLD_VALUE, oldValue);
                            Boolean newValue = !oldValue;
                            if (value != null) newValue = value;
                            refGroups.child(groupId).child(Firebase.USERS).child(Firebase.PUBLIC).child(String.valueOf(userNumber)).child(property).setValue(newValue).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    res.put(STATUS, STATUS_SUCCESS);
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

        refStat.child(Firebase.STAT_MISC).child(Firebase.STAT_MISC_GROUPS_CLEANED).setValue(ServerValue.TIMESTAMP);

        Misc.log(LOG, "Groups validation is performing, checking online users");
        new TaskSingleValueEventFor<JSONObject>(refGroups.child("/")).setFirebaseRest(createAccessToken()).addOnCompleteListener(new Runnable1<JSONObject>() {
            @Override
            public void call(JSONObject groups) {
                try {
                    Iterator<String> iter = groups.keys();
                    while (iter.hasNext()) {
                        final String group = iter.next();
                        if (group.startsWith("_") || "overview".equals(group)) {
                            Misc.log(LOG, "Key skipped: " + group);
                            continue;
                        }

                        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(group).child(Firebase.OPTIONS))
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        Map value = (Map) dataSnapshot.getValue();

                                        Misc.log(LOG, "Group found:", group/* + ", leader id:", leader, dataSnapshot.getValue()*/);

                                        if (value == null) {
                                            Misc.log(LOG, "--- corrupted group detected, removing ----- 1"); //TODO
                                            refGroups.child(group).removeValue();
                                            putStaticticsGroup(group, false, GroupAction.GROUP_DELETED, "corrupted group detected, removing ----- 1");
                                            return;
                                        }

                                        final boolean requiresPassword;
                                        final boolean dismissInactive;
                                        final boolean persistent;
                                        final long delayToDismiss;
                                        final long timeToLiveIfEmpty;


                                        Object object = value.get(Firebase.REQUIRES_PASSWORD);
                                        requiresPassword = object != null && (boolean) object;

                                        object = value.get(Firebase.DISMISS_INACTIVE);
                                        dismissInactive = object != null && (boolean) object;

                                        object = value.get(Firebase.PERSISTENT);
                                        persistent = object != null && (boolean) object;

                                        object = value.get(Firebase.DELAY_TO_DISMISS);
                                        if (object != null)
                                            delayToDismiss = Long.parseLong("0" + object.toString());
                                        else delayToDismiss = 0;

                                        object = value.get(Firebase.TIME_TO_LIVE_IF_EMPTY);
                                        if (object != null)
                                            timeToLiveIfEmpty = Long.parseLong("0" + object.toString());
                                        else timeToLiveIfEmpty = 0;

                                        new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(group).child(Firebase.USERS).child(Firebase.PUBLIC))
                                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                                    @Override
                                                    public void call(DataSnapshot dataSnapshot) {
                                                        Misc.log(LOG, "Users validation for group:", group);

                                                        ArrayList<Map<String, Serializable>> users = null;
                                                        try {
                                                            //noinspection unchecked
                                                            users = (ArrayList<Map<String, Serializable>>) dataSnapshot.getValue();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        if (users == null) {
                                                            Misc.log(LOG, "--- corrupted group detected, removing: ----- 2"); //TODO
                                                            refGroups.child(group).removeValue();
                                                            putStaticticsGroup(group, false, GroupAction.GROUP_DELETED, "corrupted group detected, removing: ----- 2");
                                                            return;
                                                        }
                                                        long groupChanged = 0;

                                                        for (int i = 0; i < users.size(); i++) {
                                                            Map<String, Serializable> user = users.get(i);
                                                            if (user == null) continue;

                                                            String name = (String) user.get(Firebase.NAME);
                                                            Long changed = (Long) user.get(Firebase.CHANGED);
                                                            if (changed != null && changed > groupChanged)
                                                                groupChanged = changed;
                                                            boolean active = false;
                                                            Object object = user.get(Firebase.ACTIVE);
                                                            if (object != null) {
                                                                active = (Boolean) object;
                                                            }

                                                            if (!active) continue;

                                                            if (dismissInactive) {
                                                                Long current = new Date().getTime();
                                                                if (changed == null) {
                                                                    Misc.log(LOG, "--- user:", i, "name:", name, "is NULL");
                                                                    dataSnapshot.getRef().child("" + i).child(Firebase.ACTIVE).setValue(false);
                                                                } else if (current - delayToDismiss * 1000 > changed) {
                                                                    Misc.log(LOG, "--- user:", i, "name:", name, "is EXPIRED for", ((current - delayToDismiss * 1000 - changed) / 1000), "seconds");
                                                                    dataSnapshot.getRef().child("" + i).child(Firebase.ACTIVE).setValue(false);
                                                                } else {
                                                                    dataSnapshot.getRef().getParent().getParent().child(Firebase.OPTIONS).child(Firebase.CHANGED).setValue(changed);
                                                                    Misc.log(LOG, "--- user:", i, "name:", name, "is OK");
                                                                }
                                                            }
                                                        }

                                                        if (!persistent && timeToLiveIfEmpty > 0 && new Date().getTime() - groupChanged > timeToLiveIfEmpty * 60 * 1000) {
                                                            String info = group + " expired for " + ((new Date().getTime() - groupChanged - timeToLiveIfEmpty * 60 * 1000) / 1000 / 60) + " minutes";
                                                            Misc.log(LOG, "--- removing group " + info);
                                                            refGroups.child(group).removeValue();
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

                new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(group).child(Constants.DATABASE.OPTIONS))
                        .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                            @Override
                            public void onEvent(DataSnapshot dataSnapshot) {
                                Map value = (Map) dataSnapshot.getValue();

                                Common.log(LOG, "Group found:", group*//* + ", leader id:", leader, dataSnapshot.getValue()*//*);

                                if (value == null) {
                                    Common.log(LOG, "--- corrupted group detected, removing ----- 1"); //TODO
                                    refGroups.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                    refGroups.child(group).removeValue();
                                    return;
                                }

                                final boolean requiresPassword;
                                final boolean dismissInactive;
                                final boolean persistent;
                                final long delayToDismiss;
                                final long timeToLiveIfEmpty;


                                Object object = value.get(Constants.DATABASE.REQUIRES_PASSWORD);
                                requiresPassword = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.DISMISS_INACTIVE);
                                dismissInactive = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.PERSISTENT);
                                persistent = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.DELAY_TO_DISMISS);
                                if (object != null)
                                    delayToDismiss = Long.parseLong("0" + object.toString());
                                else delayToDismiss = 0;

                                object = value.get(Constants.DATABASE.TIME_TO_LIVE_IF_EMPTY);
                                if (object != null)
                                    timeToLiveIfEmpty = Long.parseLong("0" + object.toString());
                                else timeToLiveIfEmpty = 0;

                                new TaskSingleValueEventFor<DataSnapshot>(refGroups.child(group).child(Constants.DATABASE.SECTION_USERS_DATA))
                                        .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                            @Override
                                            public void onEvent(DataSnapshot dataSnapshot) {
                                                Common.log(LOG, "Users validation for group:", group);

                                                ArrayList<Map<String, Serializable>> users = null;
                                                try {
                                                    users = (ArrayList<Map<String, Serializable>>) dataSnapshot.getValue();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                if (users == null) {
                                                    Common.log(LOG, "--- corrupted group detected, removing: ----- 2"); //TODO
                                                    refGroups.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                                    refGroups.child(group).removeValue();
                                                    return;
                                                }
                                                long groupChanged = 0;

                                                for (int i = 0; i < users.size(); i++) {
                                                    Map<String, Serializable> user = users.get(i);
                                                    if (user == null) continue;

                                                    String name = (String) user.get(Constants.DATABASE.NAME);
                                                    Long changed = (Long) user.get(Constants.DATABASE.CHANGED);
                                                    if (changed != null && changed > groupChanged)
                                                        groupChanged = changed;
                                                    boolean active = false;
                                                    Object object = user.get(Constants.DATABASE.ACTIVE);
                                                    if (object != null) {
                                                        active = (Boolean) object;
                                                    }

                                                    if (!active) continue;

                                                    if (dismissInactive) {
                                                        Long current = new Date().getTime();
                                                        if (changed == null) {
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is NULL");
                                                            dataSnapshot.getRef().child("" + i).child(Constants.DATABASE.ACTIVE).setValue(false);
                                                        } else if (current - delayToDismiss * 1000 > changed) {
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is EXPIRED for", ((current - delayToDismiss * 1000 - changed) / 1000), "seconds");
                                                            dataSnapshot.getRef().child("" + i).child(Constants.DATABASE.ACTIVE).setValue(false);
                                                        } else {
                                                            dataSnapshot.getRef().getParent().getParent().child(Constants.DATABASE.OPTIONS).child(Constants.DATABASE.CHANGED).setValue(changed);
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is OK");
                                                        }
                                                    }
                                                }

                                                if (!persistent && timeToLiveIfEmpty > 0 && new Date().getTime() - groupChanged > timeToLiveIfEmpty * 60 * 1000) {
                                                    Common.log(LOG, "--- removing group " + group + " expired for", (new Date().getTime() - groupChanged - timeToLiveIfEmpty * 60 * 1000) / 1000 / 60, "minutes");
                                                    refGroups.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                                    refGroups.child(group).removeValue();
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

    @Override
    public void validateAccounts() {
        refStat.child(Firebase.STAT_MISC).child(Firebase.STAT_MISC_ACCOUNTS_CLEANED).setValue(ServerValue.TIMESTAMP);

        Misc.log(LOG, "Accounts validation is performing, checking online users");

        new TaskSingleValueEventFor<JSONObject>(refAccounts).setFirebaseRest(createAccessToken()).addOnCompleteListener(new Runnable1<JSONObject>() {
            @Override
            public void call(JSONObject accounts) {
                try {
                    Iterator<String> iter = accounts.keys();
                    while (iter.hasNext()) {
                        final String uid = iter.next();

                        new TaskSingleValueEventFor<DataSnapshot>(refAccounts.child(uid).child(Firebase.PRIVATE))
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        try {
                                            Map value = (Map) dataSnapshot.getValue();
                                            boolean expired = false;
                                            boolean trusted = false;
                                            if (value.containsKey(REQUEST_SIGN_PROVIDER) && !"anonymous".equals(value.get(REQUEST_SIGN_PROVIDER))) {
                                                trusted = true;
                                            }

                                            if (value.containsKey(Firebase.CHANGED)) {
                                                if ((new Date().getTime() - (long) value.get(Firebase.CHANGED)) > 30 * 24 * 60 * 60 * 1000L) {
                                                    expired = true;
                                                }
                                            } else {
                                                expired = true;
                                            }

                                            if (!trusted && expired) {
                                                String message = Misc.durationToString(new Date().getTime() - (long) value.get(Firebase.CHANGED));
                                                Misc.log(LOG, "--- removing account: " + uid, "expired for: " +message);

                                                refAccounts.child(uid).setValue(null);
                                                putStaticticsAccount(uid, AccountAction.ACCOUNT_DELETED.toString(), null, null, "Expired for " + message);
                                            }
                                        } catch(Exception e) {
                                            Misc.err(LOG, "validateAccounts:failed:", uid, e.getMessage());
                                        }
                                    }
                                }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
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

    /**
     * This method requests and returns accessToken for Firebase. Depending on current installation type
     * it defines the properly request and performs it. Installation type can be defined in gradle.build.
     */
    public String createAccessToken() {
        String token = "";
        try {
            FileInputStream serviceAccount = new FileInputStream(OPTIONS.getFirebasePrivateKeyFile());
            GoogleCredential googleCred = GoogleCredential.fromStream(serviceAccount);
            GoogleCredential scoped = googleCred.createScoped(
                    Arrays.asList(
                            "https://www.googleapis.com/auth/firebase.database",
                            "https://www.googleapis.com/auth/userinfo.email"
                    )
            );
            scoped.refreshToken();
            token = scoped.getAccessToken();
        } catch (Exception e) {
            Misc.err(e);
        }
        return token;
    }

    public void putStaticticsGroup(String groupId, boolean isPersistent, GroupAction action, String errorMessage) {
        DatabaseReference referenceTotal;
        DatabaseReference referenceToday;
        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));

        referenceTotal = refStat.child(Firebase.STAT_TOTAL);
        referenceToday = refStat.child(Firebase.STAT_BY_DATE).child(today);

        switch(action) {
            case GROUP_CREATED_PERSISTENT:
                referenceTotal = referenceTotal.child(Firebase.STAT_GROUPS_CREATED_PERSISTENT);
                referenceToday = referenceToday.child(Firebase.STAT_GROUPS_CREATED_PERSISTENT);
                break;
            case GROUP_CREATED_TEMPORARY:
                referenceTotal = referenceTotal.child(Firebase.STAT_GROUPS_CREATED_TEMPORARY);
                referenceToday = referenceToday.child(Firebase.STAT_GROUPS_CREATED_TEMPORARY);
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

    public void putStaticticsUser(String groupId, String userId, UserAction action, String errorMessage) {

        putStaticticsAccount(userId, action.toString(), "group", groupId, errorMessage);

        DatabaseReference referenceTotal;
        DatabaseReference referenceToday;
        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));

        referenceTotal = refStat.child(Firebase.STAT_TOTAL);
        referenceToday = refStat.child(Firebase.STAT_BY_DATE).child(today);
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
            case USER_REMOVED:
                referenceTotal = referenceTotal.child(Firebase.STAT_USERS_REMOVED);
                referenceToday = referenceToday.child(Firebase.STAT_USERS_REMOVED);
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

    public void putStaticticsAccount(final String accountId, final String action, final String key, final Object value, String errorMessage) {
        DatabaseReference referenceTotal;
        DatabaseReference referenceToday;
        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));

        referenceTotal = refStat.child(Firebase.STAT_TOTAL);
        referenceToday = refStat.child(Firebase.STAT_BY_DATE).child(today);

        switch(action) {
            case Firebase.STAT_ACCOUNTS_CREATED:
                referenceTotal = referenceTotal.child(Firebase.STAT_ACCOUNTS_CREATED);
                referenceToday = referenceToday.child(Firebase.STAT_ACCOUNTS_CREATED);
                referenceToday.runTransaction(incrementValue);
                referenceTotal.runTransaction(incrementValue);
                break;
            case Firebase.STAT_ACCOUNTS_DELETED:
                referenceTotal = referenceTotal.child(Firebase.STAT_ACCOUNTS_DELETED);
                referenceToday = referenceToday.child(Firebase.STAT_ACCOUNTS_DELETED);
                referenceToday.runTransaction(incrementValue);
                referenceTotal.runTransaction(incrementValue);
                break;
        }

        if(errorMessage != null && errorMessage.length() > 0) {
            Map<String, String> map = new HashMap<>();
            map.put("account", accountId);
            map.put("action", action.toString());
            putStaticticsMessage(errorMessage, map);
        }

        if(key != null && accountId != null && accountId.length() > 0) {
            new TaskSingleValueEventFor<JSONObject>(refAccounts.child(accountId)).setFirebaseRest(createAccessToken()).addOnCompleteListener(new Runnable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    if(json.has(Firebase.PRIVATE) && json.getBoolean(Firebase.PRIVATE)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put(Firebase.TIMESTAMP, new Date().getTime());
                        map.put(Firebase.KEYS, key);
                        if (action != null) map.put(Firebase.MODE, action);

                        if (value instanceof Boolean) {
                            map.put(Firebase.VALUE, value);
                        } else if (value instanceof Number) {
                            map.put(Firebase.VALUE, value);
                        } else if (value instanceof String) {
                            if (((String) value).length() < 50) {
                                map.put(Firebase.VALUE, value);
                            } else {
                                map.put(Firebase.VALUE, ((String) value).substring(0, 40) + "...");
                            }
                        } else if (value instanceof ArrayList) {
                            map.put(Firebase.VALUE, "Array(" + ((ArrayList) value).size() + ")");
                        } else if (value != null) {
                            map.put(Firebase.VALUE, "[" + value.getClass().getSimpleName() + "]");
                        }
                        refAccounts.child(accountId).child(Firebase.PRIVATE).child(Firebase.HISTORY).push().setValue(map);
                        Misc.log(LOG, "putStaticticsAccount:", accountId, "action:", action);
                    }
                }
            }).start();

        }
    }

    public void putStaticticsMessage(String message, Map<String, String> map) {
        Calendar cal = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d %02d-%02d-%02d-%03d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));

        if(map == null) {
            map = new HashMap<>();
        }
        map.put("message", message);
        refStat.child(Firebase.STAT_MESSAGES).child(today).setValue(map);
    }

    @Override
    public void cleanStatisticsMessages(final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {

        final JSONObject res = new JSONObject();

        refStat.child(Firebase.STAT_MESSAGES).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                res.put(STATUS, STATUS_SUCCESS);
                Misc.log(LOG, "cleanStatisticsMessages:done");
                onsuccess.call(res);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.put(STATUS, STATUS_ERROR);
                res.put(MESSAGE, e.getMessage());
                Misc.err(LOG, "cleanStatisticsMessages:failed", e.getMessage());
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
            if(databaseError != null) {
                Misc.err(LOG, "incrementValue:onComplete:" + databaseError);
            }
        }
    };

}