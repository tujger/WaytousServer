package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytous.SignProvider;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;

@SuppressWarnings("unused")
public class GroupCreate implements RestAction {

    @Override
    public String getActionName() {
        return "group/create";
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) {
        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_DELAYED);
        request.processBody(new Runnable1<StringBuilder>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(StringBuilder buf) {
                String options = buf.toString();

                //noinspection HardCodedStringLiteral
                Misc.log(GroupCreate.this.getClass().getSimpleName(), options);

                JSONObject json = new JSONObject(options);

                final MyGroup group = new MyGroup();
                if(json.has(Rest.GROUP_ID)) group.setId(json.getString(Rest.GROUP_ID));
                if(json.has(Firebase.REQUIRES_PASSWORD)) group.setRequiresPassword(json.getBoolean(Firebase.REQUIRES_PASSWORD));
                if(json.has("password")) group.setPassword(json.get("password").toString());
                if(json.has(Firebase.WELCOME_MESSAGE)) group.setWelcomeMessage(json.getString(Firebase.WELCOME_MESSAGE));
                if(json.has(Firebase.PERSISTENT)) group.setPersistent(json.getBoolean(Firebase.PERSISTENT));
                if(json.has(Firebase.TIME_TO_LIVE_IF_EMPTY)) {
                    try {
                        group.setTimeToLiveIfEmpty(Integer.parseInt(json.getString(Firebase.TIME_TO_LIVE_IF_EMPTY)));
                    } catch (Exception e) {
                        group.setTimeToLiveIfEmpty(15);
                    }
                }
                if(json.has(Firebase.DISMISS_INACTIVE)) group.setDismissInactive(json.getBoolean(Firebase.DISMISS_INACTIVE));
                if(json.has(Firebase.DELAY_TO_DISMISS)) {
                    try {
                        group.setDelayToDismiss(Integer.parseInt(json.getString(Firebase.DELAY_TO_DISMISS)));
                    } catch(Exception e){
                        group.setDelayToDismiss(300);
                    }
                }


            /*final Runnable1<JSONObject>[] onresult = new Runnable1[2];
            onresult[0] = new Runnable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    ref.child(Constants.DATABASE.SECTION_GROUPS).child(group.getId()).setValue(user.getUid());
                    DatabaseReference nodeNumber = ref.child(group.getId()).child(Constants.DATABASE.USERS_ORDER).push();
                    nodeNumber.setValue(user.getUid());

                    registerUser(group.getId(), user, request);
                }
            };
            onresult[1] = new Runnable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    group.fetchNewId();
                    createGroup(group, onresult[0], onresult[1]);
                }
            };*/

                Common.getInstance().getDataProcessor("v1").createGroup(group,
                        new Runnable1<JSONObject>() {
                            @Override
                            public void call(JSONObject json) {
                                MyUser user = new MyUser(null, "Administrator:" + OPTIONS.getLogin());
                                user.setSignProvider(SignProvider.ADMIN);
                                user.setName(OPTIONS.getLogin());
                                user.setOs(System.getProperty("os.name"));
                                user.setModel(OPTIONS.getAppName() + " 1." + Common.SERVER_BUILD);

                                Common.getInstance().getDataProcessor("v1").registerUser(group.getId(), user, REQUEST_NEW_GROUP, new Runnable1<JSONObject>() {
                                    @Override
                                    public void call(JSONObject json) {
                                        request.sendResult(json);
                                    }
                                }, new Runnable1<JSONObject>() {
                                    @Override
                                    public void call(JSONObject json) {
                                        request.sendError(500, json);
                                    }
                                });
                            }
                        }, new Runnable1<JSONObject>() {
                            @Override
                            public void call(JSONObject json) {
                                request.sendError(500, json);
                            }
                        });

            }
        }, new Runnable1<Exception>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(Exception e) {
                Misc.err(GroupCreate.this.getClass().getSimpleName(), e);
                JSONObject json = new JSONObject();
                json.put(STATUS, STATUS_ERROR);
                json.put(MESSAGE, "Incorrect request.");
                json.put(EXTRA, e.getMessage());
                request.sendError(400, json);
            }
        });
    }
}
