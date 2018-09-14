package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.Version;
import com.edeqa.edequate.rest.Uptime;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class Api extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/rest/api";

    private static final String SUMMARY = "summary";
    private static final String VERSION = "version";
    private static final String VERSION_NAME = "version_name";
    private static final String VERSION_CODE = "version_code";
    private static final String VIEW = "view";

    private JSONObject options;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper request) {

        setOptions(request.fetchOptions());
//        JSONObject resultObject = new JSONObject();
        JSONArray resultArray = new JSONArray();

        boolean delayed = false;
        AtomicInteger actionsNumber = new AtomicInteger();
        AtomicInteger actionsDone = new AtomicInteger();

        if(getOptions().has(VERSION)) {
            actionsNumber.getAndIncrement();
            new Thread(() -> {
                Misc.log("Api", "-> version");

                JSONObject resultObject = new JSONObject();
                resultObject.put(VERSION_NAME, Common.SERVER_VERSION + "." + Common.SERVER_BUILD);
                resultObject.put(VERSION_CODE, Common.SERVER_BUILD);
                json.put(VERSION, resultObject);

                JSONObject edequateObject = new JSONObject();
                edequateObject.put(VERSION_CODE, Version.getVersionCode());
                edequateObject.put(VERSION_NAME, Version.getVersionName());
                edequateObject.put(VERSION, Version.getVersion());
                json.put("edequate", edequateObject);

                json.put(STATUS, STATUS_SUCCESS);
                if(actionsDone.incrementAndGet() == actionsNumber.get()) {
                    request.sendResult(json);
                }
            }).start();
            delayed = true;
        }
        if(getOptions().has(VIEW)) {
            String groupId = getOptions().getString(VIEW);
            actionsNumber.getAndIncrement();
            new Thread(() -> {
                Misc.log("Api", "-> view group:", groupId);
                json.put("view", groupId);
                json.put(STATUS, STATUS_SUCCESS);
                if(actionsDone.incrementAndGet() == actionsNumber.get()) {
                    request.sendResult(json);
                }
            }).start();
            delayed = true;
        }
        if(getOptions().has(SUMMARY)) {
            String groupId = getOptions().getString(SUMMARY);
            actionsNumber.getAndIncrement();
            if(groupId.isEmpty()) {
                new Thread(() -> {
                    Misc.log("Api", "-> summary server:", groupId);

                    JSONObject summary = new JSONObject();

                    Uptime uptime = (Uptime) ((EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS)).getHolder(Uptime.TYPE);
                    uptime.call(summary, null);

                    json.put("summary_server", summary);
                    json.put(STATUS, STATUS_SUCCESS);
                    if(actionsDone.incrementAndGet() == actionsNumber.get()) {
                        request.sendResult(json);
                    }
                }).start();
            } else {
                new Thread(() -> {
                    Misc.log("Api", "-> summary group:", groupId);
                    json.put("summary_group", groupId);
                    json.put(STATUS, STATUS_SUCCESS);
                    if(actionsDone.incrementAndGet() == actionsNumber.get()) {
                        request.sendResult(json);
                    }
                }).start();
            }
            delayed = true;
        }

        if(delayed) {
            json.put(STATUS, STATUS_DELAYED);
            json.put(CODE, CODE_JSON);
        } else {
            json.put(STATUS, STATUS_ERROR);
            json.put(CODE, ERROR_METHOD_NOT_ALLOWED);
            json.put(MESSAGE, "Not enough arguments");
            Misc.err("Api", "failed because of not enough arguments");
        }
    }

    public JSONObject getOptions() {
        return options;
    }

    public void setOptions(JSONObject options) {
        this.options = options;
    }

}
