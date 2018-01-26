package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.NamedCall;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;

import static com.edeqa.waytous.Constants.OPTIONS;

@SuppressWarnings("unused")
public class LogsClear implements NamedCall<RequestWrapper> {

    @Override
    public String getName() {
        return "logs/clear";
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) throws Exception {

        File file = new File(OPTIONS.getLogFile());
        Misc.log(this.getClass().getSimpleName(), file.getCanonicalPath());

        PrintWriter writer = new PrintWriter(file);
        writer.close();

        json.put(STATUS, STATUS_SUCCESS);
    }
}
