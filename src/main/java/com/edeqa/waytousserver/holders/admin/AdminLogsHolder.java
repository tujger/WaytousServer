
package com.edeqa.waytousserver.holders.admin;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.AdminServletHandler;
import com.google.api.client.http.HttpMethods;
import com.google.common.net.HttpHeaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;

import static com.edeqa.waytous.Constants.OPTIONS;


/**
 * Created 4/20/2017.
 */

@SuppressWarnings("unused")
public class AdminLogsHolder implements PageHolder {

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String HOLDER_TYPE = "logs";
    @SuppressWarnings("HardCodedStringLiteral")
    private static final String LOG = "ALH";

    private final AdminServletHandler server;
    private HtmlGenerator html;

    public AdminLogsHolder(AdminServletHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @Override
    public boolean perform(RequestWrapper requestWrapper) {

        URI uri = requestWrapper.getRequestURI();

        switch(requestWrapper.getRequestMethod()) {
            case HttpMethods.GET:
                switch (uri.getPath()) {
                    //noinspection HardCodedStringLiteral
                    case "/admin/logs/log":
                        printLog(requestWrapper);
                        return true;
                    default:
                        break;
                }
                break;
            case HttpMethods.PUT:
                switch (uri.getPath()) {
                    //noinspection HardCodedStringLiteral
                    case "/admin/logs/clear":
                        clearLog(requestWrapper);
                        return true;
                    default:
                        break;
                }
                break;
            case HttpMethods.POST:
                break;
        }
        return false;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void clearLog(RequestWrapper requestWrapper) {
        try {
            File file = new File(OPTIONS.getLogFile());
            Misc.log(LOG, "Clear:", file.getCanonicalPath());

            PrintWriter writer = new PrintWriter(file);
            writer.close();

            byte[] bytes = "".getBytes();

            requestWrapper.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, Mime.TEXT_PLAIN);
            requestWrapper.sendResponseHeaders(200, bytes.length);

            OutputStream os = requestWrapper.getResponseBody();
            os.write(bytes);
            os.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void printLog(final RequestWrapper requestWrapper) {
        try {
            final File file = new File(OPTIONS.getLogFile());
            Misc.log(LOG,"printLog:",file.getCanonicalPath());

            if(!file.exists()) {
                Misc.log(LOG,"printLog:", "file not found");
                requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, Mime.TEXT_PLAIN);
                requestWrapper.setHeader(HttpHeaders.SERVER, "Waytous/"+ Common.SERVER_BUILD);
                requestWrapper.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

                requestWrapper.sendResponseHeaders(500, 0);

                byte[] bytes = (file.toString() + " not found. Fix the key 'log_file' in your options file.").getBytes();

                OutputStream os = requestWrapper.getResponseBody();
                os.write(bytes);
                os.close();
                return;
            }

            boolean gzip = true;
            requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, Mime.TEXT_EVENT_STREAM);
            requestWrapper.setHeader(HttpHeaders.SERVER, "Waytous/"+ Common.SERVER_BUILD);
            requestWrapper.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            requestWrapper.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
            requestWrapper.setHeader(HttpHeaders.CONNECTION, "keep-alive");
            requestWrapper.setCharacterEncoding("UTF-8");

            requestWrapper.sendResponseHeaders(200, 0);

            final PrintWriter pw = requestWrapper.getPrintWriter();
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
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
