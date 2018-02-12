package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.UserRequest;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;

/**
 * Performs request from user - https://domain.com/group/new, or from WebSocket.
 * Validates given {@link GroupRequest}
 */
@SuppressWarnings("unused")
public class NewGroup extends AbstractFirebaseAction<NewGroup, GroupRequest> {

    public static final String TYPE = "/rest/firebase/group/new";

    private UserRequest userRequest;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(final JSONObject json, final GroupRequest groupRequest) {

        Misc.log("NewGroup", "from", getUserRequest());

        ((CreateAccount) getFireBus().getHolder(CreateAccount.TYPE))
                .setOnSuccess(new Runnable() {
                    @Override
                    public void run() {
                        //noinspection unchecked
                        final Runnable1<JSONObject>[] onresult = new Runnable1[3];
                        onresult[0] = new Runnable1<JSONObject>() {
                            @Override
                            public void call(JSONObject json) {
                                ((RegisterUser) getFireBus().getHolder(RegisterUser.TYPE))
                                        .setGroupId(groupRequest.getId())
                                        .setAction(REQUEST_NEW_GROUP)
                                        .setOnSuccess(new Runnable1<JSONObject>() {
                                            @Override
                                            public void call(JSONObject json) {
                                                getUserRequest().send(json.toString());
                                                getUserRequest().close();
                                            }
                                        })
                                        .setOnError(new Runnable1<JSONObject>() {
                                            @Override
                                            public void call(JSONObject json) {
                                                getUserRequest().send(json.toString());
                                                getUserRequest().close();
                                            }
                                        })
                                        .call(null, getUserRequest().fetchUser());
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
                                ((CreateGroup) getFireBus().getHolder(CreateGroup.TYPE))
                                        .setOnSuccess(onresult[0])
                                        .setOnError(onresult[1])
                                        .call(arg, groupRequest);
                                ((StatisticsAccount) getFireBus().getHolder(StatisticsAccount.TYPE))
                                        .setAction(AbstractDataProcessor.GroupAction.GROUP_CREATED_TEMPORARY.toString())
                                        .setKey("group")
                                        .setValue(groupRequest.getId())
                                        .call(null, getUserRequest().getUid());
                            }
                        };
                        onresult[2].call(new JSONObject());
                    }
                })
                .setOnError(new Runnable1<Throwable>() {
                    @Override
                    public void call(Throwable error) {
                        ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                                .setUserRequest(getUserRequest())
                                .call(json,"Cannot create group (code 16).");
                    }
                })
                .call(null, getUserRequest().fetchUser());
    }

    private UserRequest getUserRequest() {
        return userRequest;
    }

    public NewGroup setUserRequest(UserRequest userRequest) {
        this.userRequest = userRequest;
        return this;
    }
}

