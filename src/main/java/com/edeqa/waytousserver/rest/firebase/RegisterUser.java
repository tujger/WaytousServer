package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.edeqa.waytous.Constants.REQUEST_CHECK_USER;
import static com.edeqa.waytous.Constants.REQUEST_JOIN_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_UID;
import static com.edeqa.waytous.Constants.RESPONSE_MESSAGE;
import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.Constants.RESPONSE_SIGN;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ERROR;
import static com.edeqa.waytous.Constants.RESPONSE_TOKEN;

@SuppressWarnings("unused")
public class RegisterUser extends AbstractFirebaseAction<RegisterUser, Object> {

    public static final String TYPE = "/rest/firebase/register/user";

    private Runnable1<JSONObject> onSuccess;
    private Runnable1<JSONObject> onError;
    private String groupId;
    private MyUser user;
    private String action;
    private HashMap<String, RequestHolder> requestHolders;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean call(final JSONObject json, Object object) {

        final JSONObject response = new JSONObject();

        DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);
        final DatabaseReference refGroup = refGroups.child(getGroupId());

        if(REQUEST_NEW_GROUP.equals(getAction())) {
//            refGroups.child(groupId).setValue(user.getUid());
            DatabaseReference nodeNumber = refGroups.child(getGroupId()).child(Firebase.USERS).child(Firebase.QUEUE).push();
            nodeNumber.setValue(getUser().getUid());
        }

        getUser().setColor(Utils.selectColor(getUser().getNumber()));

        final Map<String, Object> childUpdates = new HashMap<>();

        // public data inside group
        Map<String, Object> userPublicData = new HashMap<>();
        userPublicData.put(Firebase.COLOR, getUser().getColor());
        userPublicData.put(Firebase.NAME, getUser().getName());
        if(!getUser().getUid().startsWith("Administrator")) {
            userPublicData.put(Firebase.ACTIVE, true);
        }
        userPublicData.put(Firebase.CREATED, getUser().getCreated());
        userPublicData.put(Firebase.CHANGED, ServerValue.TIMESTAMP);

        childUpdates.put(Firebase.USERS + "/" + Firebase.PUBLIC + "/" + getUser().getNumber(), userPublicData);

        for (Map.Entry<String, RequestHolder> entry : getRequestHolders().entrySet()) {
            if (entry.getValue().isSaveable()) {
                childUpdates.put(Firebase.PUBLIC + "/" + entry.getKey() + "/" + getUser().getNumber(), "{}");
                childUpdates.put(Firebase.PRIVATE + "/" + entry.getKey() + "/" + getUser().getNumber(), "{}");
            }
        }

        // user 'key - uid' inside group
        childUpdates.put(Firebase.USERS + "/" + Firebase.KEYS + "/" + getUser().getUid(), getUser().getNumber());

        // private data inside group
        Map<String, Object> userPrivateData = new HashMap<>();
        userPrivateData.put(REQUEST_UID, getUser().getUid());

        childUpdates.put(Firebase.USERS + "/" + Firebase.PRIVATE + "/" + getUser().getNumber(), userPrivateData);

        final Task<Void> updateUserTask = refGroup.updateChildren(childUpdates);
        try {
            Tasks.await(updateUserTask);

            Misc.log("RegisterUser", getAction(), "with number", getUser().getNumber(), "uid:", getUser().getUid(), "[" + getGroupId() + "]");

            if(getAction() != null) {
                String customToken = ((CustomToken) EventBus.getOrCreateEventBus().getHolder(CustomToken.TYPE)).fetchToken(getUser().getUid());

                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                if (!REQUEST_JOIN_GROUP.equals(getAction()) && !REQUEST_CHECK_USER.equals(action)) {
                    response.put(RESPONSE_TOKEN, getGroupId());
                }
                response.put(RESPONSE_NUMBER, getUser().getNumber());
                response.put(RESPONSE_SIGN, customToken);

            }
            if(getOnSuccess() != null) {
                getOnSuccess().call(response);
            } else {
                getUser().connection.send(response.toString());
                getUser().connection.close();
            }
            ((StatisticsUser) EventBus.getOrCreateEventBus().getHolder(StatisticsUser.TYPE))
                    .setGroupId(getGroupId())
                    .setAction(AbstractDataProcessor.UserAction.USER_JOINED)
                    .call(null, getUser().getUid());
        } catch (Exception e) {
            e.printStackTrace();
            if(getOnError() != null) getOnError().call(response);

            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
            response.put(RESPONSE_MESSAGE, "Cannot register (code 18).");
            Misc.err("RegisterUser", getUser(), "not registered in group:", getGroupId(), "error:", e);
            if (getOnError() != null) {
                getOnError().call(response);
            } else {
                getUser().connection.send(response.toString());
                getUser().connection.close();
            }
            ((StatisticsUser) EventBus.getOrCreateEventBus().getHolder(StatisticsUser.TYPE))
                    .setGroupId(getGroupId())
                    .setAction(AbstractDataProcessor.UserAction.USER_REJECTED)
                    .setMessage(e.getMessage())
                    .call(null, getUser().getUid());
        }
        clear();
        return true;
    }

    public void clear() {
        setOnSuccess(null);
        setOnError(null);
        setAction(null);
        setUser(null);
        setGroupId(null);
    }

    public Runnable1<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public RegisterUser setOnSuccess(Runnable1<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Runnable1<JSONObject> getOnError() {
        return onError;
    }

    public RegisterUser setOnError(Runnable1<JSONObject> onError) {
        this.onError = onError;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public RegisterUser setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public MyUser getUser() {
        return user;
    }

    public RegisterUser setUser(MyUser user) {
        this.user = user;
        return this;
    }

    public String getAction() {
        return action;
    }

    public RegisterUser setAction(String action) {
        this.action = action;
        return this;
    }

    public RegisterUser setRequestHolders(HashMap<String, RequestHolder> requestHolders) {
        this.requestHolders = requestHolders;
        return this;
    }

    public HashMap<String, RequestHolder> getRequestHolders() {
        return requestHolders;
    }
}
