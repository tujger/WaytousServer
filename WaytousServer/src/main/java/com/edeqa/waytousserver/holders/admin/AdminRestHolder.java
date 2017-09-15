
package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.Rest;
import com.edeqa.waytous.interfaces.Runnable1;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.AdminServletHandler;
import com.google.api.client.http.HttpMethods;

import org.json.JSONObject;

import java.net.URI;

import static com.edeqa.waytous.Constants.REQUEST_NEW_GROUP;


/**
 * Created 4/20/2017.
 */

@SuppressWarnings("unused")
public class AdminRestHolder implements PageHolder {

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String HOLDER_TYPE = "rest";
    @SuppressWarnings("HardCodedStringLiteral")
    private static final String LOG = "ARH";

    private final AdminServletHandler server;
    private HtmlGenerator html;

    @SuppressWarnings("HardCodedStringLiteral")
    public AdminRestHolder(AdminServletHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public boolean perform(RequestWrapper requestWrapper) {

        URI uri = requestWrapper.getRequestURI();

        Common.log(LOG, requestWrapper.getRemoteAddress(), uri.getPath());

        switch(requestWrapper.getRequestMethod()) {
            case HttpMethods.GET:
            case HttpMethods.PUT:
                switch (uri.getPath()) {
                    case "/admin/rest/v1/groups/clean":
                        cleanGroupsV1(requestWrapper);
                        return true;
                    case "/admin/rest/v1/stat/clean":
                        cleanStatMessagesV1(requestWrapper);
                        return true;
                    default:
                        actionNotSupported(requestWrapper);
                        return true;
                }
            case HttpMethods.POST:
                switch (uri.getPath()) {
                    case "/admin/rest/v1/group/create":
                        createGroupV1(requestWrapper);
                        return true;
                    case "/admin/rest/v1/group/delete":
                        deleteGroupV1(requestWrapper);
                        return true;
                    case "/admin/rest/v1/group/modify":
                        modifyPropertyInGroupV1(requestWrapper);
                        return true;
                    case "/admin/rest/v1/group/switch":
                        switchPropertyInGroupV1(requestWrapper);
                        return true;
                    case "/admin/rest/v1/user/remove":
                        removeUserV1(requestWrapper);
                        return true;
                    case "/admin/rest/v1/user/switch":
                        switchPropertyForUserV1(requestWrapper);
                        return true;
                    default:
                        actionNotSupported(requestWrapper);
                        return true;
                }
        }

        return false;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void cleanGroupsV1(RequestWrapper requestWrapper) {
        try {
            //noinspection HardCodedStringLiteral
            Common.log(LOG, "cleanGroupsV1");

            Common.getInstance().getDataProcessor("v1").validateGroups();

            JSONObject json = new JSONObject();
            json.put(Rest.STATUS, Rest.SUCCESS);
            json.put(Rest.MESSAGE, "Clean started.");
            Utils.sendResultJson.call(requestWrapper, json);

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Rest.STATUS, Rest.ERROR);
            json.put(Rest.MESSAGE, "Incorrect request.");
            Utils.sendError.call(requestWrapper, 400, json);
        }

    }

    private void createGroupV1(final RequestWrapper requestWrapper) {
        requestWrapper.processBody(new Runnable1<StringBuilder>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(StringBuilder buf) {
                String options = buf.toString();

                //noinspection HardCodedStringLiteral
                Common.log(LOG, "createGroupV1:", options);

                JSONObject json = new JSONObject(options);

                final MyGroup group = new MyGroup();
                if(json.has(Rest.GROUP_ID)) group.setId(json.getString(Rest.GROUP_ID));
                if(json.has(Firebase.OPTION_REQUIRES_PASSWORD)) group.setRequiresPassword(json.getBoolean(Firebase.OPTION_REQUIRES_PASSWORD));
                if(json.has("password")) group.setPassword(json.get("password").toString());
                if(json.has(Firebase.OPTION_WELCOME_MESSAGE)) group.setWelcomeMessage(json.getString(Firebase.OPTION_WELCOME_MESSAGE));
                if(json.has(Firebase.OPTION_PERSISTENT)) group.setPersistent(json.getBoolean(Firebase.OPTION_PERSISTENT));
                if(json.has(Firebase.OPTION_TIME_TO_LIVE_IF_EMPTY)) {
                    try {
                        group.setTimeToLiveIfEmpty(Integer.parseInt(json.getString(Firebase.OPTION_TIME_TO_LIVE_IF_EMPTY)));
                    } catch (Exception e) {
                        group.setTimeToLiveIfEmpty(15);
                    }
                }
                if(json.has(Firebase.OPTION_DISMISS_INACTIVE)) group.setDismissInactive(json.getBoolean(Firebase.OPTION_DISMISS_INACTIVE));
                if(json.has(Firebase.OPTION_DELAY_TO_DISMISS)) {
                    try {
                        group.setDelayToDismiss(Integer.parseInt(json.getString(Firebase.OPTION_DELAY_TO_DISMISS)));
                    } catch(Exception e){
                        group.setDelayToDismiss(300);
                    }
                }


            /*final Runnable1<JSONObject>[] onresult = new Runnable1[2];
            onresult[0] = new Runnable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    ref.child(Constants.DATABASE.SECTION_GROUPS).child(group.getId()).setValue(user.getUid());
                    DatabaseReference nodeNumber = ref.child(group.getId()).child(Constants.DATABASE.SECTION_USERS_ORDER).push();
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

                                MyUser user = new MyUser(null, "server:" + Utils.getUnique());

                                Common.getInstance().getDataProcessor("v1").registerUser(group.getId(), user, REQUEST_NEW_GROUP, new Runnable1<JSONObject>() {
                                    @Override
                                    public void call(JSONObject json) {
                                        Utils.sendResultJson.call(requestWrapper, json);
                                    }
                                }, new Runnable1<JSONObject>() {
                                    @Override
                                    public void call(JSONObject json) {
                                        Utils.sendError.call(requestWrapper, 500, json);
                                    }
                                });
                            }
                        }, new Runnable1<JSONObject>() {
                            @Override
                            public void call(JSONObject json) {
                                Utils.sendError.call(requestWrapper, 500, json);
                            }
                        });

            }
        }, new Runnable1<Exception>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(Exception e) {
                Common.err(LOG, "createGroupV1:", e);
                JSONObject json = new JSONObject();
                json.put(Rest.STATUS, Rest.ERROR);
                json.put(Rest.MESSAGE, "Incorrect request.");
                json.put(Rest.REASON, e.getMessage());
                Utils.sendError.call(requestWrapper, 400, json);
            }
        });
    }

    private void deleteGroupV1(final RequestWrapper requestWrapper) {
        requestWrapper.processBody(new Runnable1<StringBuilder>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(StringBuilder buf) {
                String options = buf.toString();

                //noinspection HardCodedStringLiteral
                Common.log(LOG, "deleteGroupV1:", options);

                JSONObject json = new JSONObject(options);
                String groupId = json.getString(Rest.GROUP_ID);

                Common.getInstance().getDataProcessor("v1").deleteGroup(groupId,new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendResultJson.call(requestWrapper, json);
                    }
                }, new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendError.call(requestWrapper, 500, json);
                    }
                });
            }
        }, new Runnable1<Exception>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(Exception e) {
                Common.err(LOG, "deleteGroupV1:", e);
                JSONObject json = new JSONObject();
                json.put(Rest.STATUS, Rest.ERROR);
                json.put(Rest.MESSAGE, "Incorrect request.");
                json.put(Rest.REASON, e.getMessage());
                Utils.sendError.call(requestWrapper, 400, json);
            }
        });
    }

    private void removeUserV1(final RequestWrapper requestWrapper) {
        requestWrapper.processBody(new Runnable1<StringBuilder>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(StringBuilder buf) {
                String options = buf.toString();

                //noinspection HardCodedStringLiteral
                Common.log(LOG, "removeUserV1:", options);

                JSONObject json = new JSONObject(options);
                String groupId = json.getString(Rest.GROUP_ID);
                Long userNumber = Long.parseLong(json.get(Rest.USER_NUMBER).toString());

                Common.getInstance().getDataProcessor("v1").removeUser(groupId,userNumber,new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendResultJson.call(requestWrapper, json);
                    }
                }, new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendError.call(requestWrapper, 500, json);
                    }
                });
            }
        }, new Runnable1<Exception>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(Exception e) {
                Common.err(LOG, "removeUserV1:", e);
                JSONObject json = new JSONObject();
                json.put(Rest.STATUS, Rest.ERROR);
                json.put(Rest.MESSAGE, "Incorrect request.");
                json.put(Rest.REASON, e.getMessage());
                Utils.sendError.call(requestWrapper, 400, json);
            }
        });
    }

    private void switchPropertyInGroupV1(final RequestWrapper requestWrapper) {
        requestWrapper.processBody(new Runnable1<StringBuilder>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(StringBuilder buf) {
                String options = buf.toString();

                //noinspection HardCodedStringLiteral
                Common.log(LOG, "switchPropertyInGroupV1:", options);

                JSONObject json = new JSONObject(options);
                String groupId = json.getString(Rest.GROUP_ID);
                String property = json.getString(Rest.PROPERTY);

                Common.getInstance().getDataProcessor("v1").switchPropertyInGroup(groupId,property,new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendResultJson.call(requestWrapper, json);
                    }
                }, new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendError.call(requestWrapper, 500, json);
                    }
                });
            }
        }, new Runnable1<Exception>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(Exception e) {
                Common.err(LOG, "switchPropertyInGroupV1:", e);
                JSONObject json = new JSONObject();
                json.put(Rest.STATUS, Rest.ERROR);
                json.put(Rest.MESSAGE, "Incorrect request.");
                json.put(Rest.REASON, e.getMessage());
                Utils.sendError.call(requestWrapper, 400, json);
            }
        });

    }

    private void switchPropertyForUserV1(final RequestWrapper requestWrapper) {
        requestWrapper.processBody(new Runnable1<StringBuilder>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(StringBuilder buf) {
                String options = buf.toString();

                //noinspection HardCodedStringLiteral
                Common.log(LOG, "switchPropertyForUserV1:", options);

                JSONObject json = new JSONObject(options);
                String groupId = json.getString(Rest.GROUP_ID);
                Long userNumber = Long.parseLong(json.getString(Rest.USER_NUMBER));
                String property = json.getString(Rest.PROPERTY);
                Boolean value = json.getBoolean(Rest.VALUE);

                Common.getInstance().getDataProcessor("v1").switchPropertyForUser(groupId,userNumber,property,value,new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendResultJson.call(requestWrapper, json);
                    }
                }, new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendError.call(requestWrapper, 500, json);
                    }
                });
            }
        }, new Runnable1<Exception>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(Exception e) {
                Common.err(LOG, "switchPropertyForUserV1:", e);
                JSONObject json = new JSONObject();
                json.put(Rest.STATUS, Rest.ERROR);
                json.put(Rest.MESSAGE, "Incorrect request.");
                json.put(Rest.REASON, e.getMessage());
                Utils.sendError.call(requestWrapper, 400, json);
            }
        });
    }

    private void actionNotSupported(final RequestWrapper requestWrapper) {
        requestWrapper.processBody(new Runnable1<StringBuilder>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(StringBuilder buf) {
                String options = buf.toString();

                Common.log(LOG, "actionNotSupported:", requestWrapper.getRequestURI().getPath());

                JSONObject json = new JSONObject();
                json.put(Rest.STATUS, Rest.ERROR);
                json.put(Rest.MESSAGE, "Action not supported.");
                json.put(Rest.REQUEST, options);
                Utils.sendError.call(requestWrapper, 400, json);
            }
        }, new Runnable1<Exception>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(Exception e) {
                Common.err(LOG, "actionNotSupported:", e);
                JSONObject json = new JSONObject();
                json.put(Rest.STATUS, Rest.ERROR);
                json.put(Rest.MESSAGE, "Incorrect request.");
                json.put(Rest.REASON, e.getMessage());
                Utils.sendError.call(requestWrapper, 400, json);
            }
        });
    }

    private void modifyPropertyInGroupV1(final RequestWrapper requestWrapper) {
        requestWrapper.processBody(new Runnable1<StringBuilder>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(StringBuilder buf) {
                String options = buf.toString();

                //noinspection HardCodedStringLiteral
                Common.log(LOG, "modifyPropertyInGroupV1:", options);

                JSONObject json = new JSONObject(options);
                String groupId = json.getString(Rest.GROUP_ID);
                String property = json.getString(Rest.PROPERTY);
                String value = json.getString(Rest.VALUE);

                Common.getInstance().getDataProcessor("v1").modifyPropertyInGroup(groupId,property,value,new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendResultJson.call(requestWrapper, json);
                    }
                }, new Runnable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendError.call(requestWrapper, 500, json);
                    }
                });
            }
        }, new Runnable1<Exception>() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void call(Exception e) {
                Common.err(LOG, "modifyPropertyInGroupV1:", e);
                JSONObject json = new JSONObject();
                json.put(Rest.STATUS, Rest.ERROR);
                json.put(Rest.MESSAGE, "Incorrect request.");
                json.put(Rest.REASON, e.getMessage());
                Utils.sendError.call(requestWrapper, 400, json);
            }
        });
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void cleanStatMessagesV1(final RequestWrapper requestWrapper) {
        Common.log(LOG, "cleanStatMessagesV1:started");
        Common.getInstance().getDataProcessor("v1").cleanStatisticsMessages(new Runnable1<JSONObject>() {
            @Override
            public void call(JSONObject json) {
                Common.log(LOG, "cleanStatMessagesV1:done");
                Utils.sendResultJson.call(requestWrapper, json);
            }
        }, new Runnable1<JSONObject>() {
            @Override
            public void call(JSONObject json) {
                Common.err(LOG, "cleanStatMessagesV1:failed");
                json.put(Rest.STATUS, Rest.ERROR);
                json.put(Rest.MESSAGE, "Messages cleaning failed.");
                Utils.sendError.call(requestWrapper, 500, json);
            }
        });
    }


}
