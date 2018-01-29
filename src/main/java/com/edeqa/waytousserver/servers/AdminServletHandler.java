package com.edeqa.waytousserver.servers;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.rest.admin.AccountDelete;
import com.edeqa.waytousserver.rest.admin.AccountsClean;
import com.edeqa.waytousserver.rest.admin.GroupCreate;
import com.edeqa.waytousserver.rest.admin.GroupDelete;
import com.edeqa.waytousserver.rest.admin.GroupModify;
import com.edeqa.waytousserver.rest.admin.GroupSwitch;
import com.edeqa.waytousserver.rest.admin.GroupsClean;
import com.edeqa.waytousserver.rest.admin.LogsClear;
import com.edeqa.waytousserver.rest.admin.LogsLog;
import com.edeqa.waytousserver.rest.admin.StatClean;
import com.edeqa.waytousserver.rest.admin.UserRemove;
import com.edeqa.waytousserver.rest.admin.UserSwitch;
import com.edeqa.waytousserver.rest.firebase.AccessToken;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import javax.servlet.ServletException;

import static com.edeqa.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.helpers.HtmlGenerator.SRC;
import static com.edeqa.helpers.HtmlGenerator.TITLE;
import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytousserver.helpers.Common.FIREBASE_JAVASCRIPT_VERSION;
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;


/**
 * Created 10/5/16.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class AdminServletHandler extends com.edeqa.edequate.RestServletHandler {

//    private final LinkedHashMap<String, PageHolder> holders;

    public AdminServletHandler(){
        super();
        setWebPrefix("/admin/rest/");

        registerAction(new AccountDelete());
        registerAction(new AccountsClean());
        registerAction(new GroupCreate());
        registerAction(new GroupDelete());
        registerAction(new GroupModify());
        registerAction(new GroupSwitch());
        registerAction(new GroupsClean());
        registerAction(new LogsClear());
        registerAction(new LogsLog());
        registerAction(new StatClean());
        registerAction(new UserRemove());
        registerAction(new UserSwitch());
    }

    /**
     * Initialize DataProcessorFirebaseV1 for installation type "google-appengine".
     */
    @Override
    public void init() throws ServletException {
        super.init();
        Common.getInstance().initOptions(getServletContext());
        Common.getInstance().initDataProcessor();
    }

    @Override
    public void perform(final RequestWrapper requestWrapper) throws IOException {
        if(requestWrapper.getRequestURI().getPath().startsWith(getWebPrefix())) {
            super.perform(requestWrapper);
        } else if (requestWrapper.getRequestURI().getPath().startsWith("/admin")) {

            String ipRemote = requestWrapper.getRemoteAddress().getAddress().getHostAddress();
            Misc.log("Admin", "[" + ipRemote + "]", requestWrapper.getRequestURI().getPath());

            try {
                String customToken = Common.getInstance().getDataProcessor("v1").createCustomToken("Viewer");
                String accessToken = new AccessToken().setFirebasePrivateKeyFile(OPTIONS.getFirebasePrivateKeyFile()).fetchToken();

                final JSONObject o = new JSONObject();
                o.put("version", SERVER_BUILD);
                o.put("HTTP_PORT", OPTIONS.getHttpPortMasked());
                o.put("HTTPS_PORT", OPTIONS.getHttpsPortMasked());
                o.put("WS_FB_PORT", OPTIONS.getWsPortFirebase());
                o.put("WSS_FB_PORT", OPTIONS.getWssPortFirebase());
                o.put("WS_PORT", OPTIONS.getWsPortDedicated());
                o.put("WSS_PORT", OPTIONS.getWssPortDedicated());
                o.put("firebase_config", OPTIONS.getFirebaseConfig());
                o.put("sign", customToken);
                o.put("access", accessToken);

                HtmlGenerator html = new HtmlGenerator();
                html.getHead().add(TITLE).with("Admin");

                html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/" + FIREBASE_JAVASCRIPT_VERSION + "/firebase.js");
                html.getHead().add(SCRIPT).with("data", o);
                html.getHead().add(SCRIPT).with("firebase.initializeApp(data.firebase_config);");
                html.getHead().add(SCRIPT).with(SRC, "/js/admin/Main.js");

                requestWrapper.sendResult(200, Mime.TEXT_HTML, html.build().getBytes());

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            URI uri = requestWrapper.getRequestURI();
            String host;
            try {
                host = requestWrapper.getRequestHeaders().get(HttpHeaders.HOST).get(0);
                host = host.split(":")[0];
            } catch(Exception e){
                e.printStackTrace();
                host = InetAddress.getLocalHost().getHostAddress();
            }
            String redirectLink = "https://" + host + Common.getWrappedHttpsPort() + uri.getPath();
//                Common.log("ASH","->", redirectLink);
            requestWrapper.sendRedirect(redirectLink);
        }
    }

}
