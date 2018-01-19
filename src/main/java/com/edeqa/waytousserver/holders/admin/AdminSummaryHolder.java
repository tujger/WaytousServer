package com.edeqa.waytousserver.holders.admin;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.waytousserver.helpers.CheckReq;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.AdminServletHandler;
import com.sun.net.httpserver.HttpExchange;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.edeqa.helpers.HtmlGenerator.CLASS;
import static com.edeqa.helpers.HtmlGenerator.DIV;
import static com.edeqa.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.helpers.HtmlGenerator.SRC;
import static com.edeqa.helpers.HtmlGenerator.TITLE;
import static com.edeqa.waytousserver.helpers.Common.FIREBASE_JAVASCRIPT_VERSION;
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;


/**
 * Created 1/20/2017.
 */

@SuppressWarnings("unused")
public class AdminSummaryHolder implements PageHolder {

    private static final String HOLDER_TYPE = "summary";

    private final AdminServletHandler server;
    private HtmlGenerator html;

    public AdminSummaryHolder(AdminServletHandler server) {
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

    public HtmlGenerator create(HtmlGenerator html,ArrayList<String> query,HttpExchange exchange) {
        this.html = html;
        html.clear();

        header();

        return html;
    }

    private void header() {

        html.getHead().add(TITLE).with("Summary");

        JSONObject o = new JSONObject();
        o.put("general", Common.fetchGeneralInfo());
        o.put("tokens",fetchTokensData());
        o.put("ipToUser",fetchIpToUserData());
        o.put("ipToToken",fetchIpToTokenData());
        o.put("ipToCheck",fetchIpToCheckData());

        html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/" + FIREBASE_JAVASCRIPT_VERSION + "/firebase.js");
        html.getHead().add(SCRIPT).with("data", o);
        html.getHead().add(SCRIPT).with("firebase.initializeApp(data.firebase_config);");

        html.getHead().add(SCRIPT).with(SRC, "/js/admin/Summary.js");


        html.getBody().with(CLASS,"body");
        html.getBody().add(DIV).with(CLASS, "version").with("Build: "+SERVER_BUILD);

    }

    private JSONArray fetchTokensData() {
        JSONArray a = new JSONArray();

        for (Map.Entry<String, MyGroup> x : Common.getInstance().getDataProcessor("v1").getGroups().entrySet()) {
            JSONObject o = new JSONObject();
            a.put(o);

            o.put("id",x.getKey());
            o.put("owner",x.getValue().getOwner().substring(0, 30) + "...");
            o.put("created",new Date(x.getValue().getCreated()).toString());
            o.put("changed",new Date(x.getValue().getChanged()).toString());

            JSONArray ua = new JSONArray();
            o.put("users", ua);

            for (Map.Entry<String, MyUser> y : x.getValue().users.entrySet()) {
                JSONObject uo = new JSONObject();
                ua.put(uo);

                uo.put("number",y.getValue().getNumber());
                uo.put("model",y.getValue().getModel());
                uo.put("uid",y.getValue().getUid().substring(0, 30) + "...");
                uo.put("address",y.getValue().getAddress());
                uo.put("created",new Date(y.getValue().getCreated()).toString());
                uo.put("changed",new Date(y.getValue().getChanged()).toString());
                uo.put("control",y.getValue().getControl());

            }
        }
        return a;
    }

    private JSONArray fetchIpToUserData() {
        JSONArray a = new JSONArray();

        for(Map.Entry<String,MyUser> x: Common.getInstance().getDataProcessor("v1").getIpToUser().entrySet()){
            JSONArray ua = new JSONArray();
            a.put(ua);
            ua.put(x.getKey());
            ua.put(x.getValue().getUid().substring(0,30)+"...");
        }
        return a;
    }

    private JSONArray fetchIpToTokenData() {
        JSONArray a = new JSONArray();

        for(Map.Entry<String,MyGroup> x: Common.getInstance().getDataProcessor("v1").getIpToToken().entrySet()){
            JSONArray ta = new JSONArray();
            a.put(ta);
            ta.put(x.getKey());
            ta.put(x.getValue().getId());
        }
        return a;
    }

    private JSONArray fetchIpToCheckData() {
        JSONArray a = new JSONArray();

        for(Map.Entry<String,CheckReq> x: Common.getInstance().getDataProcessor("v1").getIpToCheck().entrySet()){
            JSONArray ca = new JSONArray();
            a.put(ca);
            ca.put(x.getKey());
            ca.put(x.getValue().getToken().getId());
            ca.put(x.getValue().getControl());
            ca.put(new Date(x.getValue().getTimestamp()).toString());
        }
        return a;
    }

    private boolean processQuery(Map<String, List<String>> query) {
        boolean processed = false;
        if(query.containsKey("action")){
            for(String x:query.get("action")){
                processAction(x,query);
                processed = true;
            }
        }
        return processed;
    }

    private void processAction(String action, Map<String, List<String>> query) {

        if("del".equals(action)){

//            String token=null,id=null;

//            if(query.containsKey("token")) token = query.get("token").get(0);
//            if(query.containsKey("id")) id = query.get("id").get(0);

//            server.getDataProcessor().removeUserFromGroup(token,id);

        }

    }

}
