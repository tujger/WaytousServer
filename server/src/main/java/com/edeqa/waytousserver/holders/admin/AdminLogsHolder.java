
package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytous.Mime;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.AdminServletHandler;
import com.google.api.client.http.HttpMethods;
import com.google.common.net.HttpHeaders;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.zip.GZIPOutputStream;

import static com.edeqa.waytous.Constants.SENSITIVE;


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
            File file = new File(SENSITIVE.getLogFile());
            Common.log(LOG, "Clear:", file.getCanonicalPath());

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
    private void printLog(RequestWrapper requestWrapper) {
        try {
            File file = new File(SENSITIVE.getLogFile());

            Common.log(LOG,"Update:",file.getCanonicalPath());

            if(!file.exists()) {
                Common.log(LOG,"File not found.");
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
            requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, Mime.TEXT_PLAIN);
            requestWrapper.setHeader(HttpHeaders.SERVER, "Waytous/"+ Common.SERVER_BUILD);
            requestWrapper.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

            if(gzip){
                requestWrapper.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            } else {
                requestWrapper.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));
            }

            requestWrapper.sendResponseHeaders(200, 0);

            OutputStream os;
            if(gzip) {
                os = new BufferedOutputStream(new GZIPOutputStream(requestWrapper.getResponseBody()));
            } else {
                os = requestWrapper.getResponseBody();
            }

            FileInputStream fs = new FileInputStream(file);
            final byte[] buffer = new byte[0x10000];

            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
            fs.close();
            os.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
