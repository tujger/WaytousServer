package com.edeqa.waytousserver.servers;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.helpers.UserRequest;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;
import com.edeqa.waytousserver.rest.firebase.AccessToken;
import com.edeqa.waytousserver.rest.firebase.CleanStatistics;
import com.edeqa.waytousserver.rest.firebase.CreateAccount;
import com.edeqa.waytousserver.rest.firebase.CreateGroup;
import com.edeqa.waytousserver.rest.firebase.CustomToken;
import com.edeqa.waytousserver.rest.firebase.DeleteAccount;
import com.edeqa.waytousserver.rest.firebase.DeleteGroup;
import com.edeqa.waytousserver.rest.firebase.GroupProperty;
import com.edeqa.waytousserver.rest.firebase.RegisterUser;
import com.edeqa.waytousserver.rest.firebase.RejectUser;
import com.edeqa.waytousserver.rest.firebase.RemoveUser;
import com.edeqa.waytousserver.rest.firebase.StatisticsAccount;
import com.edeqa.waytousserver.rest.firebase.StatisticsGroup;
import com.edeqa.waytousserver.rest.firebase.StatisticsMessage;
import com.edeqa.waytousserver.rest.firebase.StatisticsUser;
import com.edeqa.waytousserver.rest.firebase.UserProperty;
import com.edeqa.waytousserver.rest.firebase.ValidateAccounts;
import com.edeqa.waytousserver.rest.firebase.ValidateGroups;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.REQUEST;
import static com.edeqa.waytous.Constants.REQUEST_CHECK_USER;
import static com.edeqa.waytous.Constants.REQUEST_HASH;
import static com.edeqa.waytous.Constants.REQUEST_JOIN_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;
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
import static com.edeqa.waytous.Constants.USER_NAME;

/**
 * Created 10/5/16.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class DataProcessorFirebaseV1 extends AbstractDataProcessor {

    public static final String VERSION = "v1";
    private static String LOG = "DPF1";
    private DatabaseReference refGroups;
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
            refGroups = refRoot.child(Firebase.SECTION_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventBus<AbstractAction> fireBus = (EventBus<AbstractAction>) EventBus.getOrCreateEventBus();
        fireBus.register(new AccessToken().setFirebasePrivateKeyFile(OPTIONS.getFirebasePrivateKeyFile()));
        fireBus.register(new CreateAccount().setFirebaseReference(refRoot));
        fireBus.register(new CreateGroup().setFirebaseReference(refRoot));
        fireBus.register(new CustomToken());
        fireBus.register(new RegisterUser().setFirebaseReference(refRoot).setRequestHolders(requestHolders));
        fireBus.register(new RejectUser());
        fireBus.register(new StatisticsAccount().setFirebaseReference(refRoot));
        fireBus.register(new StatisticsGroup().setFirebaseReference(refRoot));
        fireBus.register(new StatisticsMessage().setFirebaseReference(refRoot));
        fireBus.register(new StatisticsUser().setFirebaseReference(refRoot));
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
            final JSONObject request, response = new JSONObject();
            try {
                request = new JSONObject(message);
            } catch (JSONException e) {
                Misc.err(LOG, "onMessage:request" + e.getMessage());
                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                response.put(RESPONSE_MESSAGE, "Wrong request");
                conn.send(response.toString());
                conn.close();
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
                    ((StatisticsUser) EventBus.getOrCreateEventBus().getHolder(StatisticsUser.TYPE))
                            .setAction(UserAction.USER_REJECTED)
                            .setMessage(message)
                            .onEvent(null, uid);
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
//                    final MyGroup group = new MyGroup();
                    final MyUser user = new MyUser(conn, request);
                    Misc.log(LOG, "onMessage:requestNew:" + conn.getRemoteSocketAddress(), "{ uid:" + uid + " }");

                    ((CreateAccount) EventBus.getOrCreateEventBus().getHolder(CreateAccount.TYPE))
                            .setOnSuccess(new Runnable() {
                        @Override
                        public void run() {
                            //noinspection unchecked
                            final Runnable1<JSONObject>[] onresult = new Runnable1[3];
                            onresult[0] = new Runnable1<JSONObject>() {
                                @Override
                                public void call(JSONObject json) {
                                    ((RegisterUser) EventBus.getOrCreateEventBus().getHolder(RegisterUser.TYPE))
                                            .setGroupId(groupRequest.getId())
                                            .setUser(user)
                                            .setAction(REQUEST_NEW_GROUP)
                                            .setOnSuccess(new Runnable1<JSONObject>() {
                                                @Override
                                                public void call(JSONObject json) {
                                                    user.connection.send(json.toString());
                                                    user.connection.close();
                                                }
                                            })
                                            .setOnError(new Runnable1<JSONObject>() {
                                                @Override
                                                public void call(JSONObject json) {
                                                    user.connection.send(json.toString());
                                                    user.connection.close();
                                                }
                                            })
                                            .onEvent(null, null);
                                }
                            };
                            onresult[1] = new Runnable1<JSONObject>() {
                                @Override
                                public void call(JSONObject json) {
                                    groupRequest.fetchNewId();
                                    onresult[2].call(json);
                                }
                            };
                            onresult[2] = new Runnable1<JSONObject>() {
                                @Override
                                public void call(JSONObject arg) {
                                    ((CreateGroup) EventBus.getOrCreateEventBus().getHolder(CreateGroup.TYPE))
                                            .setOnSuccess(onresult[0])
                                            .setOnError(onresult[1])
                                            .onEvent(arg, groupRequest);
                                    ((StatisticsAccount) EventBus.getOrCreateEventBus().getHolder(StatisticsAccount.TYPE))
                                            .setAction(GroupAction.GROUP_CREATED_TEMPORARY.toString())
                                            .setKey("group")
                                            .setValue(groupRequest.getId())
                                            .onEvent(null, user.getUid());
                                }
                            };
                            onresult[2].call(new JSONObject());
                        }
                    })
                            .setOnError(new Runnable1<Throwable>() {
                                @Override
                                public void call(Throwable error) {
                                    ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                            .setUserRequest(new UserRequest(user.connection))
                                            .onEvent(response,"Cannot create group (code 16).");
                                }
                            })
                            .onEvent(null, user);
                } else {
                    ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(new UserRequest(conn))
                            .onEvent(response,"Cannot create group (code 15).");
                }
            } else if (REQUEST_JOIN_GROUP.equals(req)) {
                if (request.has(REQUEST_TOKEN)) {

                    final String groupId = request.getString(REQUEST_TOKEN);
                    final DatabaseReference refGroup = refGroups.child(groupId);

                    final UserRequest userRequest = new UserRequest(conn)
                            .setGroupId(groupId)
                            .setUid(uid)
                            .setGroupReference(refGroup);
                    userRequest.parse(request);

                    getUserRequests().add(userRequest);

                    final TaskSingleValueEventFor[] requestDataPrivateTask = new TaskSingleValueEventFor[1];
                    requestDataPrivateTask[0] = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(new Runnable1<DataSnapshot>() {
                        @Override
                        public void call(DataSnapshot dataSnapshot) {

                            int count = 1;
                            boolean found = false;
                            Object value = dataSnapshot.getValue();

                            if (value == null) {
                                dataSnapshot.getRef().push().setValue(userRequest.getUid());
                                requestDataPrivateTask[0].setRef(refGroup.child(Firebase.USERS).child(Firebase.QUEUE)).start();
                                return;
                            }

                            //noinspection unchecked
                            TreeMap<String, String> map = new TreeMap<>((HashMap<String, String>) dataSnapshot.getValue());

                            for (Map.Entry<String, String> x : map.entrySet()) {
                                if (userRequest.getUid().equals(x.getValue())) {
                                    found = true;
                                    break;
                                }
                                ++count;
                            }
                            if (found) {
                                userRequest.setNumber(count);
                                final MyUser user = userRequest.fetchUser();
                                ((CreateAccount) EventBus.getOrCreateEventBus().getHolder(CreateAccount.TYPE))
                                        .setOnSuccess(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((RegisterUser) EventBus.getOrCreateEventBus().getHolder(RegisterUser.TYPE))
                                                .setGroupId(userRequest.getGroupId())
                                                .setUser(user)
                                                .setAction(REQUEST_JOIN_GROUP)
                                                .onEvent(null, null);
                                    }
                                }).setOnError(new Runnable1<Throwable>() {
                                    @Override
                                    public void call(Throwable error) {
                                        ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                                .setUserRequest(userRequest)
                                                .onEvent(response,"Cannot create group (code 17).");
                                    }
                                }).onEvent(null, user);
                            } else {
                                DatabaseReference nodeNumber = refGroups.child(userRequest.getGroupId()).child(Firebase.USERS).child(Firebase.QUEUE).push();
                                nodeNumber.setValue(userRequest.getUid());
                                requestDataPrivateTask[0].setRef(refGroup.child(Firebase.USERS).child(Firebase.QUEUE)).start();
                            }
                        }
                    });

                    final TaskSingleValueEventFor numberForKeyTask = new TaskSingleValueEventFor<DataSnapshot>()
                            .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                @Override
                                public void call(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) { //join as existing member, go to check
                                        userRequest.setNumber(Integer.parseInt(dataSnapshot.getValue().toString()));

                                        Misc.log(LOG, "onMessage:checkRequest:", userRequest.toString());

                                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                                        response.put(RESPONSE_CONTROL, userRequest.getControl());
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
                                        numberForKeyTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.KEYS).child(userRequest.getUid())).start();
                                    } else {
                                        ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                                .setUserRequest(userRequest)
                                                .onEvent(response,"This group is expired. (001)");
                                    }
                                }
                            });

                    if (uid != null) {
                        Misc.log(LOG, "onMessage:requestJoin:" + userRequest.toString());
                        groupOptionsTask.setRef(refGroup.child(Firebase.OPTIONS)).start();
                    } else {
                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                        response.put(RESPONSE_CONTROL, userRequest.getControl());
                        Misc.log(LOG, "onMessage:requestReconnect:" + userRequest.toString());
                        userRequest.send(response.toString());
                    }
                } else {
                    ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(new UserRequest(conn))
                            .onEvent(response,"Wrong request (group not defined).");
                }
            } else if (REQUEST_CHECK_USER.equals(req)) {
                if (request.has(REQUEST_HASH)) {
                    final String hash = request.getString((REQUEST_HASH));

                    Misc.log(LOG, "onMessage:checkResponse:" + conn.getRemoteSocketAddress(), "hash:" + hash);
                    final UserRequest userRequest = getUserRequests().findByConnection(conn);
                    if (userRequest != null) {
                        userRequest.setDataProcessorConnection(conn);

                        Misc.log(LOG, "onMessage:checkFound:", userRequest.toString());

                        final DatabaseReference refGroup = refGroups.child(userRequest.getGroupId());

                        final TaskSingleValueEventFor userCheckTask = new TaskSingleValueEventFor<DataSnapshot>().addOnCompleteListener(new Runnable1<DataSnapshot>() {
                            @Override
                            public void call(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) { //join as existing member
                                    try {
                                        if (userRequest.checkControl((String) ((HashMap) dataSnapshot.getValue()).get(REQUEST_UID), hash)) {
                                            Misc.log(LOG, "onMessage:joinAsExisting:", userRequest.toString());

                                            try {
                                                final String customToken = createCustomToken(userRequest.getUid());

                                                final Map<String, Object> update = new HashMap<>();
                                                update.put(Firebase.ACTIVE, true);
                                                update.put(Firebase.COLOR, Utils.selectColor((int) userRequest.getNumber()));
                                                update.put(Firebase.CHANGED, new Date().getTime());
                                                if (userRequest.getName() != null && userRequest.getName().length() > 0) {
                                                    update.put(USER_NAME, userRequest.getName());
                                                }

                                                ((CreateAccount) EventBus.getOrCreateEventBus().getHolder(CreateAccount.TYPE))
                                                        .setOnSuccess(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Task<Void> updateUserTask = refGroup.child(Firebase.USERS).child(Firebase.PUBLIC).child("" + userRequest.getNumber()).updateChildren(update);
                                                        try {
                                                            Tasks.await(updateUserTask);
                                                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                                            response.put(RESPONSE_NUMBER, userRequest.getNumber());
                                                            response.put(RESPONSE_SIGN, customToken);

                                                            userRequest.send(response.toString());

                                                            Misc.log(LOG, "onMessage:joined:" + userRequest.getAddress(), "signToken: [provided]"/*+customToken*/);

                                                            ((StatisticsUser) EventBus.getOrCreateEventBus().getHolder(StatisticsUser.TYPE))
                                                                    .setGroupId(userRequest.getGroupId())
                                                                    .setAction(UserAction.USER_RECONNECTED)
                                                                    .onEvent(null,userRequest.getUid());
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }).setOnError(new Runnable1<Throwable>() {
                                                    @Override
                                                    public void call(Throwable error) {
                                                        ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                                                .setUserRequest(userRequest)
                                                                .onEvent(response,"Cannot join to group (not authenticated).");
                                                    }
                                                }).onEvent(null, userRequest.fetchUser());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                                    .setUserRequest(userRequest)
                                                    .onEvent(response,"Cannot join to group (hash incorrect).");
                                        }

                                    } catch (Exception e) {
                                        ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                                .setUserRequest(userRequest)
                                                .onEvent(response,"Cannot join to group (hash failed).");
                                        e.printStackTrace();
                                    }

                                } else { // join as new member
                                    ((CreateAccount) EventBus.getOrCreateEventBus().getHolder(CreateAccount.TYPE))
                                            .setOnSuccess(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((RegisterUser) EventBus.getOrCreateEventBus().getHolder(RegisterUser.TYPE))
                                                    .setGroupId(userRequest.getGroupId())
                                                    .setUser(userRequest.fetchUser())
                                                    .setAction(REQUEST_CHECK_USER)
                                                    .onEvent(null, null);
                                        }
                                    }).setOnError(new Runnable1<Throwable>() {
                                        @Override
                                        public void call(Throwable error) {
                                            ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                                    .setUserRequest(userRequest)
                                                    .onEvent(response,"Cannot join to group (code 18). " + error.getMessage());
                                        }
                                    }).onEvent(null, userRequest.fetchUser());
                                }
                            }
                        });

                        final TaskSingleValueEventFor userGetNumberTask = new TaskSingleValueEventFor<DataSnapshot>()
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() != null) {
                                            Misc.log(LOG, "onMessage:joinNumberFound:" + userRequest.getAddress(), "number:", dataSnapshot.getValue().toString());
//                                            check.setNumber(Long.parseLong(dataSnapshot.getValue().toString()));
                                            userRequest.setNumber(Integer.parseInt(dataSnapshot.getValue().toString()));
                                            userCheckTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE).child(dataSnapshot.getValue().toString())).start();

                                        } else {
                                            ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                                    .setUserRequest(userRequest)
                                                    .onEvent(response,"This group is expired (number not found).");
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
                                                    if (userRequest.checkControl(user.get(REQUEST_UID).toString(), hash)) {
                                                        userGetNumberTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.KEYS).child(userRequest.getUid())).start();
                                                        return;
                                                    }
                                                }
                                            }
                                            ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                                    .setUserRequest(userRequest)
                                                    .onEvent(response,"This group is expired (user not found).");
                                        } else {
                                            ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                                    .setUserRequest(userRequest)
                                                    .onEvent(response,"This group is expired (or empty).");
                                        }
                                    }
                                });

                        TaskSingleValueEventFor groupOptionsTask = new TaskSingleValueEventFor<DataSnapshot>()
                                .addOnCompleteListener(new Runnable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        if (userRequest.getUid() != null) {
                                            if (dataSnapshot.getValue() != null) {
                                                userCheckTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE).child("" + userRequest.getNumber())).start();
                                            } else {
                                                ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                                        .setUserRequest(userRequest)
                                                        .onEvent(response,"This group is expired (user not exists).");
                                            }
                                        } else {
                                            userSearchTask.setRef(refGroup.child(Firebase.USERS).child(Firebase.PRIVATE)).start();
                                        }
                                    }
                                });
                        groupOptionsTask.setRef(refGroup.child(Firebase.OPTIONS)).start();
                    } else {
                        ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                                .setUserRequest(new UserRequest(conn))
                                .onEvent(response,"Cannot join to group (user not authorized).");
                    }
                } else {
                    ((RejectUser) EventBus.getOrCreateEventBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(new UserRequest(conn))
                            .onEvent(response,"Cannot join to group (hash not defined).");
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
        ((CreateGroup) EventBus.getOrCreateEventBus().getHolder(CreateGroup.TYPE))
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .onEvent(new JSONObject(), groupRequest);
    }

    @Override
    public void deleteGroup(final String groupId, final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        new DeleteGroup()
                .setFirebaseReference(refRoot)
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .onEvent(null, groupId);
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
                .onEvent(null, null);
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
                .onEvent(null, null);
    }

    @Override
    public void registerUser(String groupId, MyUser user, String action, Runnable1<JSONObject> onsuccess, Runnable1<JSONObject> onerror) {
        ((RegisterUser) EventBus.getOrCreateEventBus().getHolder(RegisterUser.TYPE))
                .setGroupId(groupId)
                .setUser(user)
                .setAction(action)
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .onEvent(null, null);
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
                .setRequestHolders(requestHolders)
                .onEvent(null, null);
    }

    @Override
    public void deleteAccount(final String accountId, Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        new DeleteAccount()
                .setFirebaseReference(refRoot)
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .onEvent(null, accountId);
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
                .onEvent(null, null);
    }

    public void validateGroups() {
        new ValidateGroups()
                .setFirebaseReference(refRoot)
                .onEvent(null, null);
    }

    @Override
    public void validateUsers() {

    }

    @Override
    public void validateAccounts() {
        new ValidateAccounts()
                .setFirebaseReference(refRoot)
                .onEvent(null,null);
    }

    @Override
    public String createCustomToken(String uid) {
        return ((CustomToken) EventBus.getOrCreateEventBus().getHolder(CustomToken.TYPE)).fetchToken(uid);
    }

    @Override
    public void cleanStatisticsMessages(final Runnable1<JSONObject> onsuccess, final Runnable1<JSONObject> onerror) {
        new CleanStatistics()
                .setOnSuccess(onsuccess)
                .setOnError(onerror)
                .setFirebaseReference(refRoot)
                .onEvent(null, null);
    }

}
