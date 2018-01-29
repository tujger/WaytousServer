package com.edeqa.waytousserver.rest.firebase;

import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.RESPONSE_MESSAGE;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ERROR;

@SuppressWarnings("unused")
public class RejectUser extends AbstractAction<RejectUser, String> {

    private StatisticsUser statisticsUser;
    private String groupId;
    private String userId;
    private DataProcessorConnection dataProcessorConnection;

    @Override
    public String getName() {
        return "register/user";
    }

    @Override
    public void call(JSONObject response, String message) {

        Misc.err("RejectUser", "for uid:", getUserId(), "in group:" + groupId, "reason:" + message, "response:" + response);
        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
        response.put(RESPONSE_MESSAGE, message);
        getDataProcessorConnection().send(response.toString());
        getDataProcessorConnection().close();
        getStatisticsUser().setGroupId(getGroupId()).setUserId(getUserId()).setAction(AbstractDataProcessor.UserAction.USER_REJECTED).setMessage(message).call(null,null);

        response.put(STATUS, STATUS_SUCCESS);
        response.put(CODE, CODE_DELAYED);

        clear();
    }

    public String getGroupId() {
        return groupId;
    }

    public RejectUser setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public RejectUser clear() {
        setUserId(null);
        setDataProcessorConnection(null);
        setGroupId(null);
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public RejectUser setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public StatisticsUser getStatisticsUser() {
        return statisticsUser;
    }

    public RejectUser setStatisticsUser(StatisticsUser statisticsUser) {
        this.statisticsUser = statisticsUser;
        return this;
    }

    public DataProcessorConnection getDataProcessorConnection() {
        return dataProcessorConnection;
    }

    public RejectUser setDataProcessorConnection(DataProcessorConnection dataProcessorConnection) {
        this.dataProcessorConnection = dataProcessorConnection;
        return this;
    }
}
