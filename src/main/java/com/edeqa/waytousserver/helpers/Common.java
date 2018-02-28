package com.edeqa.waytousserver.helpers;

import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.helpers.Mime;
import com.edeqa.waytous.Options;
import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.edeqa.waytousserver.servers.DataProcessorFirebase;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import static com.edeqa.helpers.HtmlGenerator.A;
import static com.edeqa.helpers.HtmlGenerator.CLASS;
import static com.edeqa.helpers.HtmlGenerator.DIV;
import static com.edeqa.helpers.HtmlGenerator.HEIGHT;
import static com.edeqa.helpers.HtmlGenerator.HREF;
import static com.edeqa.helpers.HtmlGenerator.IMG;
import static com.edeqa.helpers.HtmlGenerator.LINK;
import static com.edeqa.helpers.HtmlGenerator.NOSCRIPT;
import static com.edeqa.helpers.HtmlGenerator.REL;
import static com.edeqa.helpers.HtmlGenerator.SRC;
import static com.edeqa.helpers.HtmlGenerator.STYLESHEET;
import static com.edeqa.helpers.HtmlGenerator.TYPE;
import static com.edeqa.helpers.HtmlGenerator.WIDTH;
import static com.edeqa.waytous.Constants.OPTIONS;

/**
 * Created 1/23/2017.
 */

public class Common {

    public final static int SERVER_BUILD = 60;
    public final static String FIREBASE_JAVASCRIPT_VERSION = "4.9.0"; // https://firebase.google.com/docs/web/setup

    private volatile AbstractDataProcessor dataProcessor;

    private static final Common ourInstance = new Common();

    public static Common getInstance() {
        return ourInstance;
    }

    private Common() {
    }

    public static JSONObject fetchGeneralInfo() {
        JSONObject o = new JSONObject();
        try {
            String wss = "ws://" + InetAddress.getLocalHost().getHostAddress() + ":" + OPTIONS.getWssPortDedicated();
            o.put("uri", wss);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return o;
    }

    public static String getWrappedHttpPort(){
        return OPTIONS.getHttpPortMasked() == 80 ? "" : ":" + OPTIONS.getHttpPortMasked();
    }

    public static String getWrappedHttpsPort(){
        return OPTIONS.getHttpsPortMasked() == 443 ? "" : ":" + OPTIONS.getHttpsPortMasked();
    }

    public void initOptions(ServletContext servletContext) {
        if(OPTIONS == null) {
            //noinspection HardCodedStringLiteral
            String options = servletContext.getInitParameter("options");
            OPTIONS = new Options(new String[]{options});
        }
    }

    public void initDataProcessor() throws ServletException {
        if(getDataProcessor() == null) {
            setDataProcessor(new DataProcessorFirebase());
        }
        if(getDataProcessor().isServerMode()){
            throw new ServletException("\n\nThis configuration can not be runned in Google AppEngine mode. Set the installation type in build.gradle with the following property:\n\tdef installationType = 'google-appengine'\n");
        }
    }

    public AbstractDataProcessor getDataProcessor() {
        return dataProcessor;
    }

    public void setDataProcessor(AbstractDataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public static void addNoscript(HtmlGenerator html) {
        HtmlGenerator.Tag noscript = html.getBody().add(NOSCRIPT);

        noscript.add(LINK).with(TYPE, Mime.TEXT_CSS).with(REL, STYLESHEET).with(HREF, "/css/noscript.css");

        HtmlGenerator.Tag header = noscript.add(DIV).with(CLASS, "header");
        header.add(IMG).with(SRC, "/images/logo.svg").with(WIDTH, 24).with(HEIGHT, 24);
        header.with(" Waytous");

        noscript.add(DIV).with(CLASS, "text").with("This service requires to allow Javascript. Please enable Javascript in your browser or use other browser that supports Javascript and try again.");
        noscript.add(DIV).with(CLASS, "copyright").with("Waytous &copy;2017-18 ").add(A).with(CLASS, "link").with(HREF, "http://www.edeqa.com").with("Edeqa");
    }

}
