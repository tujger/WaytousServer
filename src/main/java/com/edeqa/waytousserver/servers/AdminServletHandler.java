package com.edeqa.waytousserver.servers;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.Content;
import com.edeqa.edequate.rest.Files;
import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.rest.admin.AccountDelete;
import com.edeqa.waytousserver.rest.admin.AccountsClean;
import com.edeqa.waytousserver.rest.admin.GroupCreate;
import com.edeqa.waytousserver.rest.admin.GroupDelete;
import com.edeqa.waytousserver.rest.admin.GroupModify;
import com.edeqa.waytousserver.rest.admin.GroupSwitch;
import com.edeqa.waytousserver.rest.admin.GroupsClean;
import com.edeqa.waytousserver.rest.admin.InitialData;
import com.edeqa.waytousserver.rest.admin.LogsClear;
import com.edeqa.waytousserver.rest.admin.LogsLog;
import com.edeqa.waytousserver.rest.admin.StatClean;
import com.edeqa.waytousserver.rest.admin.UserRemove;
import com.edeqa.waytousserver.rest.admin.UserSwitch;
import com.edeqa.waytousserver.rest.firebase.AdminToken;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import javax.servlet.ServletException;

import static com.edeqa.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.helpers.HtmlGenerator.SRC;
import static com.edeqa.helpers.HtmlGenerator.TITLE;
import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytousserver.helpers.Common.FIREBASE_JAVASCRIPT_VERSION;


/**
 * Created 10/5/16.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class AdminServletHandler extends com.edeqa.edequate.RestServletHandler {

//    private final LinkedHashMap<String, PageHolder> holders;
    private AdminToken adminToken;

    public AdminServletHandler(){
        super();

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
        registerAction(new InitialData());
        registerAction(new Files().setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("Holder");
            }
        }).setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("js/admin").setActionName("/rest/admin"));
    }

    /**
     * Initialize DataProcessorFirebase for installation type "google-appengine".
     */
    @Override
    public void init() throws ServletException {
        super.init();
        Common.getInstance().initOptions(getServletContext());
        Common.getInstance().initDataProcessor();
    }

    @Override
    public void perform(final RequestWrapper requestWrapper) throws IOException {
        if(requestWrapper.getRequestURI().getPath().startsWith("/admin/rest/")) {
            super.perform(requestWrapper);
        } else if(requestWrapper.getRequestURI().getPath().startsWith("/admin/")) {
            new Content().setMimeType(new MimeType().setMime(Mime.TEXT_HTML).setText(true)).setWebPath(new WebPath(OPTIONS.getWebRootDirectory(), "index-admin.html")).setResultCode(200).call(null, requestWrapper);
            return;
        } else if (requestWrapper.getRequestURI().getPath().startsWith("/admin")) {

            String ipRemote = requestWrapper.getRemoteAddress().getAddress().getHostAddress();
            Misc.log("Admin", "[" + ipRemote + "]", requestWrapper.getRequestURI().getPath());

            try {
//                String customToken = Common.getInstance().getDataProcessor().createCustomToken("Viewer");
//                if(adminToken == null) {
//                    adminToken = new AdminToken().setFirebasePrivateKeyFile(OPTIONS.getFirebasePrivateKeyFile());
//                }

                final JSONObject o = new JSONObject();
                new InitialData().setAdmin(true).call(o, requestWrapper);

                HtmlGenerator html = new HtmlGenerator();
                html.getHead().add(TITLE).with("Admin");

                html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/" + FIREBASE_JAVASCRIPT_VERSION + "/firebase.js");
                html.getHead().add(SCRIPT).with("data", o);
                html.getHead().add(SCRIPT).with("firebase.initializeApp(data.firebase_config);");
                html.getHead().add(SCRIPT).with(SRC, "/js/admin/Main.js");

                new Content()
                        .setMimeType(new MimeType().setMime(Mime.TEXT_HTML).setText(true).setGzip(true))
                        .setContent(html.build())
                        .setResultCode(200)
                        .call(null, requestWrapper);

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
