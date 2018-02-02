package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;

import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;

import static com.edeqa.waytous.Constants.OPTIONS;

@SuppressWarnings("unused")
public class LogsClear extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/logs/clear";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean onEvent(JSONObject json, final RequestWrapper request) throws Exception {

        File file = new File(OPTIONS.getLogFile());
        Misc.log(this.getClass().getSimpleName(), file.getCanonicalPath());

        PrintWriter writer = new PrintWriter(file);
        writer.close();

        json.put(STATUS, STATUS_SUCCESS);
        return true;
    }
}
