package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytous.SignProvider;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.GroupRequest;
import com.edeqa.waytousserver.helpers.MyUser;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;

@SuppressWarnings("unused")
public class GroupCreate extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/group/create";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) {
        json.put(STATUS, STATUS_DELAYED);
        request.processBody(buf -> {
            String options = buf.toString();

            //noinspection HardCodedStringLiteral
            Misc.log(GroupCreate.this.getClass().getSimpleName(), options);

            JSONObject jsonOptions = new JSONObject(options);

//                final MyGroup group = new MyGroup();
            final GroupRequest groupRequest = new GroupRequest();
            if(jsonOptions.has(Rest.GROUP_ID)) groupRequest.setId(jsonOptions.getString(Rest.GROUP_ID));
            if(jsonOptions.has(Firebase.REQUIRES_PASSWORD)) groupRequest.setRequiresPassword(jsonOptions.getBoolean(Firebase.REQUIRES_PASSWORD));
            if(jsonOptions.has("password")) groupRequest.setPassword(jsonOptions.get("password").toString());
            if(jsonOptions.has(Firebase.WELCOME_MESSAGE)) groupRequest.setWelcomeMessage(jsonOptions.getString(Firebase.WELCOME_MESSAGE));
            if(jsonOptions.has(Firebase.PERSISTENT)) groupRequest.setPersistent(jsonOptions.getBoolean(Firebase.PERSISTENT));
            if(jsonOptions.has(Firebase.TIME_TO_LIVE_IF_EMPTY)) {
                try {
                    groupRequest.setTimeToLiveIfEmpty(Integer.parseInt(jsonOptions.getString(Firebase.TIME_TO_LIVE_IF_EMPTY)));
                } catch (Exception e) {
                    groupRequest.setTimeToLiveIfEmpty(15);
                }
            }
            if(jsonOptions.has(Firebase.DISMISS_INACTIVE)) groupRequest.setDismissInactive(jsonOptions.getBoolean(Firebase.DISMISS_INACTIVE));
            if(jsonOptions.has(Firebase.DELAY_TO_DISMISS)) {
                try {
                    groupRequest.setDelayToDismiss(Integer.parseInt(jsonOptions.getString(Firebase.DELAY_TO_DISMISS)));
                } catch(Exception e){
                    groupRequest.setDelayToDismiss(300);
                }
            }
            if(jsonOptions.has(Firebase.LIMIT_USERS)) {
                try {
                    groupRequest.setLimitUsers(Integer.parseInt(jsonOptions.getString(Firebase.LIMIT_USERS)));
                } catch(Exception e){
                    groupRequest.setLimitUsers(10000);
                }
            }


        /*final Runnable1<JSONObject>[] onresult = new Runnable1[2];
        onresult[0] = new Runnable1<JSONObject>() {
            @Override
            public void onEvent(JSONObject json) {
                ref.child(Constants.DATABASE.SECTION_GROUPS).child(group.getId()).setValue(user.getUid());
                DatabaseReference nodeNumber = ref.child(group.getId()).child(Constants.DATABASE.USERS_ORDER).push();
                nodeNumber.setValue(user.getUid());

                registerUser(group.getId(), user, request);
            }
        };
        onresult[1] = new Runnable1<JSONObject>() {
            @Override
            public void onEvent(JSONObject json) {
                group.fetchNewId();
                createGroup(group, onresult[0], onresult[1]);
            }
        };*/

            Common.getInstance().getDataProcessor().createGroup(groupRequest, jsonSuccess -> {
                        MyUser user = new MyUser(null, "Administrator:" + request.getUserName());
                        user.setSignProvider(SignProvider.ADMIN);
                        user.setName(request.getUserName());
                        user.setOs(System.getProperty("os.name"));
                        user.setModel(OPTIONS.getAppName() + " 1." + Common.SERVER_BUILD);

                        Common.getInstance().getDataProcessor().registerUser(groupRequest.getId(), user, REQUEST_NEW_GROUP, request::sendResult, jsonError -> request.sendError(500, jsonError));
                    }, jsonError -> request.sendError(500, jsonError));

        }, exception -> {
            Misc.err(GroupCreate.this.getClass().getSimpleName(), exception);
            JSONObject jsonResult = new JSONObject();
            jsonResult.put(STATUS, STATUS_ERROR);
            jsonResult.put(MESSAGE, "Incorrect request.");
            jsonResult.put(EXTRA, exception.getMessage());
            request.sendError(400, jsonResult);
        });
    }
}
