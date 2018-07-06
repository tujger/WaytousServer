package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.rest.tracking.AbstractTrackingAction;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.edeqa.waytous.Constants.REQUEST_CHECK_USER;
import static com.edeqa.waytous.Constants.REQUEST_JOIN_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_MODEL;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;
import static com.edeqa.waytous.Constants.REQUEST_OS;
import static com.edeqa.waytous.Constants.REQUEST_SIGN_PROVIDER;
import static com.edeqa.waytous.Constants.REQUEST_UID;
import static com.edeqa.waytous.Constants.RESPONSE_MESSAGE;
import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.Constants.RESPONSE_SIGN;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ERROR;
import static com.edeqa.waytous.Constants.RESPONSE_TOKEN;

@SuppressWarnings("unused")
public class RegisterUser extends AbstractFirebaseAction<RegisterUser, MyUser> {

    public static final String TYPE = "/rest/firebase/register/user";

    private Consumer<JSONObject> onSuccess;
    private Consumer<JSONObject> onError;
    private String groupId;
    private String action;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(final JSONObject json, final MyUser user) {
        if(getGroupId() == null && getAction() == null) {
            Misc.err("RegisterUser", new Exception("Not enough data"));
        }

        final JSONObject response = new JSONObject();

        DatabaseReference refGroups = getFirebaseReference().child(Firebase.SECTION_GROUPS);
        final DatabaseReference refGroup = refGroups.child(getGroupId());

        if(REQUEST_NEW_GROUP.equals(getAction())) {
            DatabaseReference nodeNumber = refGroups.child(getGroupId()).child(Firebase.USERS).child(Firebase.QUEUE).push();
            nodeNumber.setValue(user.getUid(), (error, ref) -> {
                if (error != null) {
                    Misc.err("RegisterUser", error);
                }
            });
        }

        user.setColor(MyUser.selectColor(user.getNumber()));

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

        final List<String> records = new ArrayList<>();
        for (Map.Entry<String,AbstractTrackingAction> entry : getTrackingBus().getHolders().entrySet()) {
            if (entry.getValue().isSaveable()) {
                childUpdates.put(Firebase.PUBLIC + "/" + entry.getKey() + "/" + user.getNumber(), "{}");
                childUpdates.put(Firebase.PRIVATE + "/" + entry.getKey() + "/" + user.getNumber(), "{}");
                records.add(entry.getKey());
            }
        }

        // user 'key - uid' inside group
        childUpdates.put(Firebase.USERS + "/" + Firebase.KEYS + "/" + user.getUid(), user.getNumber());

        // private data inside group
        Map<String, Object> userPrivateData = new HashMap<>();
        userPrivateData.put(REQUEST_UID, user.getUid());
        userPrivateData.put(REQUEST_MODEL, user.getModel());
        userPrivateData.put(REQUEST_OS, user.getOs());
        if (user.getSignProvider() != null) {
            userPrivateData.put(REQUEST_SIGN_PROVIDER, user.getSignProvider().toString());
        }
        childUpdates.put(Firebase.USERS + "/" + Firebase.PRIVATE + "/" + user.getNumber(), userPrivateData);

        refGroup.updateChildren(childUpdates, (error, ref) -> {
            if(error == null) {
                Misc.log("RegisterUser", getAction(), "with number", user.getNumber(), "[" + user.getUid() + "]", "in group", getGroupId(), records);

                if(getAction() != null) {
                    String customToken = ((CustomToken) getFireBus().getHolder(CustomToken.TYPE)).fetchToken(user.getUid());
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                    if (!REQUEST_JOIN_GROUP.equals(getAction()) && !REQUEST_CHECK_USER.equals(action)) {
                        response.put(RESPONSE_TOKEN, getGroupId());
                    }
                    response.put(RESPONSE_NUMBER, user.getNumber());
                    response.put(RESPONSE_SIGN, customToken);
                }
                if(getOnSuccess() != null) {
                    getOnSuccess().accept(response);
                } else {
                    user.connection.send(response.toString());
                    user.connection.close();
                }
                ((StatisticsUser) getFireBus().getHolder(StatisticsUser.TYPE))
                        .setGroupId(getGroupId())
                        .setAction(AbstractDataProcessor.Action.USER_JOINED)
                        .call(null, user.getUid());
                clear();

            } else {
                error.toException().printStackTrace();
                if(getOnError() != null) getOnError().accept(response);

                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                response.put(RESPONSE_MESSAGE, "Cannot register (code 18).");
                Misc.err("RegisterUser", user, "not registered in group:", getGroupId(), "error:", error.toException());
                if (getOnError() != null) {
                    getOnError().accept(response);
                } else {
                    user.connection.send(response.toString());
                    user.connection.close();
                }
                ((StatisticsUser) getFireBus().getHolder(StatisticsUser.TYPE))
                        .setGroupId(getGroupId())
                        .setAction(AbstractDataProcessor.Action.USER_REJECTED)
                        .setMessage(error.toException().getMessage())
                        .call(null, user.getUid());
                clear();

            }
        });
    }

    public void clear() {
        setOnSuccess(null);
        setOnError(null);
        setAction(null);
        setGroupId(null);
    }

    public Consumer<JSONObject> getOnSuccess() {
        return onSuccess;
    }

    public RegisterUser setOnSuccess(Consumer<JSONObject> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public Consumer<JSONObject> getOnError() {
        return onError;
    }

    public RegisterUser setOnError(Consumer<JSONObject> onError) {
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

    public String getAction() {
        return action;
    }

    public RegisterUser setAction(String action) {
        this.action = action;
        return this;
    }

}
