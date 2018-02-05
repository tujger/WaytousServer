package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.UserRequest;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.RESPONSE_MESSAGE;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ERROR;

@SuppressWarnings("unused")
public class RejectUser extends AbstractFirebaseAction<RejectUser, String> {

    public static final String TYPE = "/rest/firebase/reject/user";

    private UserRequest userRequest;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean call(JSONObject response, String message) {

        Misc.err("RejectUser", "for", userRequest, "[" + message + "]");

        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
        response.put(RESPONSE_MESSAGE, message);

        getUserRequest().send(response.toString());
        getUserRequest().close();

        ((StatisticsUser) EventBus.getOrCreateEventBus().getHolder(StatisticsUser.TYPE))
                .setUserRequest(getUserRequest())
                .setAction(AbstractDataProcessor.UserAction.USER_REJECTED)
                .setMessage(message)
                .call(null, userRequest.getUid());

        response.put(STATUS, STATUS_SUCCESS);
        response.put(CODE, CODE_DELAYED);

        clear();
        return true;
    }

    public RejectUser clear() {
        setUserRequest(null);
        return this;
    }

    private UserRequest getUserRequest() {
        return userRequest;
    }

    public RejectUser setUserRequest(UserRequest userRequest) {
        this.userRequest = userRequest;
        return this;
    }
}
