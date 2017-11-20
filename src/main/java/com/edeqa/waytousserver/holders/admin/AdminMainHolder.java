package com.edeqa.waytousserver.holders.admin;

import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.AdminServletHandler;
import com.sun.net.httpserver.HttpExchange;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.edeqa.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.helpers.HtmlGenerator.SRC;
import static com.edeqa.helpers.HtmlGenerator.TITLE;
import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;


/**
 * Created 1/20/2017.
 */

public class AdminMainHolder implements PageHolder {

    @SuppressWarnings("HardCodedStringLiteral")
    public static final String HOLDER_TYPE = "main";
    @SuppressWarnings("HardCodedStringLiteral")
    private static final String LOG = "AMH";

    @SuppressWarnings("unused")
    private final AdminServletHandler server;
    private String part;
    private ArrayList<String> request;

    public AdminMainHolder(AdminServletHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @Override
    public boolean perform(RequestWrapper requestWrapper) {
        return false;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public HtmlGenerator create(HtmlGenerator html, ArrayList<String> query, HttpExchange exchange) {
        html.clear();

        html.getHead().add(TITLE).with("Admin");

        JSONObject o = new JSONObject();
//        o.put("page", part);
        o.put("version", SERVER_BUILD);
        o.put("HTTP_PORT", OPTIONS.getHttpPortMasked());
        o.put("HTTPS_PORT", OPTIONS.getHttpsPortMasked());
        o.put("WS_FB_PORT", OPTIONS.getWsPortFirebase());
        o.put("WSS_FB_PORT", OPTIONS.getWssPortFirebase());
        o.put("WS_PORT", OPTIONS.getWsPortDedicated());
        o.put("WSS_PORT", OPTIONS.getWssPortDedicated());
        o.put("firebase_config", OPTIONS.getFirebaseConfig());

        html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/4.6.2/firebase.js");
        html.getHead().add(SCRIPT).with("data", o);
        html.getHead().add(SCRIPT).with("firebase.initializeApp(data.firebase_config);");

        html.getHead().add(SCRIPT).with(SRC, "/js/admin/Main.js");

        return html;
    }

}
