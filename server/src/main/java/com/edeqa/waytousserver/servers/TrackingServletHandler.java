package com.edeqa.waytousserver.servers;

import com.edeqa.waytous.Mime;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.edeqa.waytousserver.helpers.Utils;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;

import static com.edeqa.waytous.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.ONLOAD;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SRC;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.TITLE;


/**
 * Created 1/19/17.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class TrackingServletHandler extends AbstractServletHandler {

    private HtmlGenerator html = new HtmlGenerator();

    public TrackingServletHandler(){
    }

    /**
     * Initialize DataProcessorFirebaseV1 for installation type "google-appengine".
     */
    @Override
    public void init() throws ServletException {
        super.init();
        initDataProcessor();
    }

    @Override
    public void perform(RequestWrapper requestWrapper) throws IOException {

        URI uri = requestWrapper.getRequestURI();

        String host = null;
        try {
            host = requestWrapper.getRequestHeader(HttpHeaders.HOST).get(0);
            host = host.split(":")[0];
        } catch(Exception e){
            e.printStackTrace();
            host = InetAddress.getLocalHost().getHostAddress();
        }

        Common.log("Tracking",requestWrapper.getRemoteAddress(),host + uri.getPath() );

        ArrayList<String> parts = new ArrayList<>();
        parts.addAll(Arrays.asList(uri.getPath().split("/")));

//        File root = new File(SENSITIVE.getWebRootDirectory());
//        File file = new File(root + uri.getPath()).getCanonicalFile();

        if(uri.getPath().startsWith("/track/")) {

            String tokenId = "";
            if (parts.size() >= 3) {
                tokenId = parts.get(2);
            }

            String mobileRedirect, webRedirect, mainLink;
            if(Common.getInstance().getDataProcessor(DataProcessorFirebaseV1.VERSION).isServerMode()){
                mobileRedirect = "waytous://" + host + "/track/" + tokenId;
                webRedirect = "https://" + host + Common.getWrappedHttpsPort() + "/group/" + tokenId;
                mainLink = "https://" + host + Common.getWrappedHttpsPort() + "/group/" + tokenId;
            } else {
                mobileRedirect = "waytous://" + host + "/track/" + tokenId;
                webRedirect = "http://" + requestWrapper.getRequestHeader(HttpHeaders.HOST).get(0) + "/group/" + tokenId;
                mainLink = "http://" + requestWrapper.getRequestHeader(HttpHeaders.HOST).get(0) + "/group/" + tokenId;
            }

            String redirectLink = "http://" + SENSITIVE.getFirebaseDynamicLinkHost() + "/?"
                    + "link=" + mainLink
                    + "&apn=com.edeqa.waytous"
                    + "&al=" + mobileRedirect
                    + "&afl=" + webRedirect
                    + "&ifl=" + webRedirect
                    + "&st=Waytous"
                    + "&sd=Be+always+on+the+same+way+with+your+friends"
                    + "&si=https://www.waytous.net/icons/favicon-256x256.png";

            Common.log("Tracking", "->", redirectLink);

            requestWrapper.sendRedirect(redirectLink);
            return;
        }

        JSONObject o = new JSONObject();
        o.put("request", parts);
        o.put("version", SERVER_BUILD);
        o.put("HTTP_PORT", SENSITIVE.getHttpPortMasked());
        o.put("HTTPS_PORT", SENSITIVE.getHttpsPortMasked());
        o.put("WS_FB_PORT", SENSITIVE.getWsPortFirebase());
        o.put("WSS_FB_PORT", SENSITIVE.getWssPortFirebase());
        o.put("WS_PORT", SENSITIVE.getWsPortDedicated());
        o.put("WSS_PORT", SENSITIVE.getWssPortDedicated());
        o.put("firebase_config", SENSITIVE.getFirebaseConfig());
        o.put("isStandAlone", Common.getInstance().getDataProcessor(DataProcessorFirebaseV1.VERSION).isServerMode());
        if(SENSITIVE.isDebugMode()) o.put("isDebugMode", true);


        html.clear();
        html.getHead().add(TITLE).with("Waytous");
//        html.getHead().add(META).with(NAME, "theme-color").with(CONTENT, "#aaeeee");
        html.getHead().add(SCRIPT).with("data", o);
        html.getHead().add(SCRIPT).with("(function checkVersion(){var l=localStorage;if(l){var w=\"waytous:version\";var d=data.version;var i=parseInt(l[w]||0);if(i<d){l[w]=d;console.warn(\"Forced reloading because of version \"+d+\" is newer than \"+i);window.location.reload(true);}}})();");
        html.getHead().add(SCRIPT).with(SRC, "/js/tracking/Main.js").with("async",true).with("defer",true).with(ONLOAD, "(window.WTU = new Main()).start();");


        // FIXME - need to check by https://observatory.mozilla.org/analyze.html?host=waytous.net
        requestWrapper.setHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS, "nosniff");
        requestWrapper.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, "frame-ancestors 'self'");
        requestWrapper.setHeader(HttpHeaders.X_FRAME_OPTIONS, "SAMEORIGIN");
        requestWrapper.setHeader(HttpHeaders.X_XSS_PROTECTION, "1; mode=block");
        requestWrapper.setHeader(HttpHeaders.STRICT_TRANSPORT_SECURITY, "max-age=63072000; includeSubDomains; preload");
        requestWrapper.setHeader(HttpHeaders.VARY, "Accept-Encoding");
        String etag = "W/1976-" + uri.getPath().hashCode();
        requestWrapper.setHeader(HttpHeaders.ETAG, etag);

        Utils.sendResult.call(requestWrapper, 200, Mime.TEXT_HTML, html.build().getBytes());

    }

}
