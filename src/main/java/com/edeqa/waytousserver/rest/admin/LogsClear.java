package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.interfaces.RestAction;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.Misc;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

import static com.edeqa.waytous.Constants.OPTIONS;

@SuppressWarnings("unused")
public class LogsClear implements RestAction {

    @Override
    public String getActionName() {
        return "logs/clear";
    }

    @Override
    public void call(JSONObject json, final RequestWrapper request) throws Exception {
        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_DELAYED);

        File file = new File(OPTIONS.getLogFile());
        Misc.log(this.getClass().getSimpleName(), file.getCanonicalPath());

        PrintWriter writer = new PrintWriter(file);
        writer.close();

        byte[] bytes = "".getBytes();

        request.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        request.setHeader(HttpHeaders.CONTENT_TYPE, Mime.TEXT_PLAIN);
        request.sendResponseHeaders(200, bytes.length);

        OutputStream os = request.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
