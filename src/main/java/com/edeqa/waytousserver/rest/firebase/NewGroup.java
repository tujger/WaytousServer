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
        ((CreateAccount) getFireBus().getHolder(CreateAccount.TYPE)).setOnSuccess(() -> {
                    //noinspection unchecked
                    final Runnable1<JSONObject>[] onresult = new Runnable1[3];
                    onresult[0] = jsonRequestNewGroup -> {
                        ((RegisterUser) getFireBus().getHolder(RegisterUser.TYPE))
                                .setGroupId(groupRequest.getId())
                                .setAction(REQUEST_NEW_GROUP)
                                .setOnSuccess(jsonSuccess -> {
                                    getUserRequest().send(jsonSuccess.toString());
                                    getUserRequest().close();
                                })
                                .setOnError(jsonError -> {
                                    getUserRequest().send(jsonError.toString());
                                    getUserRequest().close();
                                })
                                .call(null, getUserRequest().fetchUser());
                    };
                    onresult[1] = jsonFetchNewId -> {
                        groupRequest.fetchNewId();
                        onresult[2].call(jsonFetchNewId);
                    };
                    onresult[2] = jsonGroupCreated -> {
                        ((CreateGroup) getFireBus().getHolder(CreateGroup.TYPE))
                                .setOnSuccess(onresult[0])
                                .setOnError(onresult[1])
                                .call(jsonGroupCreated, groupRequest);
                        ((StatisticsAccount) getFireBus().getHolder(StatisticsAccount.TYPE))
                                .setAction(AbstractDataProcessor.Action.GROUP_CREATED_TEMPORARY)
                                .setKey("group")
                                .setValue(groupRequest.getId())
                                .call(null, getUserRequest().getUid());
                    };
                    onresult[2].call(new JSONObject());
                })
                .setOnError(error -> {
                    System.out.println("FAULT");
                    ((RejectUser) getFireBus().getHolder(RejectUser.TYPE))
                            .setUserRequest(getUserRequest())
                            .call(json,"Cannot create group (code 16).");
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

