package com.edeqa.waytousserver.rest.admin;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import static com.edeqa.waytous.Constants.OPTIONS;

@SuppressWarnings("unused")
public class LogsLog extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/admin/rest/logs/log";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean call(JSONObject json, final RequestWrapper request) throws IOException {
        json.put(STATUS, STATUS_SUCCESS);
        json.put(CODE, CODE_DELAYED);

        //noinspection HardCodedStringLiteral
        final File file = new File(OPTIONS.getLogFile());
        Misc.log(this.getClass().getSimpleName(), file.getCanonicalPath());

        if(!file.exists()) {
            Misc.log(this.getClass().getSimpleName(), "file not found");
            request.setHeader(HttpHeaders.CONTENT_TYPE, Mime.TEXT_PLAIN);
            request.setHeader(HttpHeaders.SERVER, "Waytous/"+ Common.SERVER_BUILD);
            request.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

            request.sendResponseHeaders(500, 0);

            byte[] bytes = (file.toString() + " not found. Fix the key 'log_file' in your options file.").getBytes();

            OutputStream os = request.getResponseBody();
            os.write(bytes);
            os.close();
            return true;
        }

        boolean gzip = true;
        request.setHeader(HttpHeaders.CONTENT_TYPE, Mime.TEXT_EVENT_STREAM);
        request.setHeader(HttpHeaders.SERVER, "Waytous/"+ Common.SERVER_BUILD);
        request.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        request.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        request.setHeader(HttpHeaders.CONNECTION, "keep-alive");
        request.setCharacterEncoding("UTF-8");

        request.sendResponseHeaders(200, 0);

        final PrintWriter pw = request.getPrintWriter();
        final BufferedReader input = new BufferedReader(new FileReader(file));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String currentLine = null;
                    long counter = 0;

                    pw.println();
                    while (!pw.checkError()) {
                        if ((currentLine = input.readLine()) != null) {
                            pw.print("data: ");
                            pw.println(currentLine);
                            pw.println();
                            pw.flush();
                            continue;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    pw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return true;
    }
}
