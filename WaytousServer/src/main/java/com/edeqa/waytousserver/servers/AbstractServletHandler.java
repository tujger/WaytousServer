/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.edeqa.waytousserver.servers;

import com.edeqa.waytous.SensitiveData;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.edeqa.waytous.Constants.SENSITIVE;

abstract public class AbstractServletHandler extends HttpServlet implements HttpHandler {

    AbstractServletHandler() {
    }

    @Override
    public void init() throws ServletException {
        super.init();

        if(SENSITIVE == null) {
            //noinspection HardCodedStringLiteral
            String sensitiveData = getServletContext().getInitParameter("sensitiveData");
            SENSITIVE = new SensitiveData(new String[]{sensitiveData});
        }
    }

    public void initDataProcessor() throws ServletException {
        if(Common.getInstance().getDataProcessor(DataProcessorFirebaseV1.VERSION) == null) {
            try {
                Common.getInstance().setDataProcessor(new DataProcessorFirebaseV1());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(Common.getInstance().getDataProcessor(DataProcessorFirebaseV1.VERSION).isServerMode()){
                throw new ServletException("\n\nThis configuration can not be runned in Google AppEngine mode. Set the installation type in build.gradle with the following property:\n\tdef installationType = 'google-appengine'\n");
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setHttpServletRequest(req);
        requestWrapper.setHttpServletResponse(resp);
        internalPerform(requestWrapper);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setHttpServletRequest(req);
        requestWrapper.setHttpServletResponse(resp);
        internalPerform(requestWrapper);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setHttpExchange(exchange);
        internalPerform(requestWrapper);
    }

    abstract public void perform(RequestWrapper requestWrapper) throws IOException;

    private void internalPerform(RequestWrapper requestWrapper) throws IOException {
        perform(requestWrapper);
    }

}
