package com.edeqa.waytousserver.servers;

import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.helpers.Mime;
import com.edeqa.waytousserver.helpers.Common;
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

import static com.edeqa.helpers.HtmlGenerator.ONLOAD;
import static com.edeqa.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.helpers.HtmlGenerator.SRC;
import static com.edeqa.helpers.HtmlGenerator.TITLE;
import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytousserver.helpers.Common.FIREBASE_JAVASCRIPT_VERSION;
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;


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

        String host = null, referer = null;
        try {
            host = requestWrapper.getRequestHeader(HttpHeaders.HOST).get(0);
            host = host.split(":")[0];
        } catch(Exception e){
            e.printStackTrace();
            host = InetAddress.getLocalHost().getHostAddress();
        }
        try {
            referer = requestWrapper.getRequestHeaders().get(HttpHeaders.REFERER).get(0);
            if(referer.contains(host)) referer = null;
        } catch(Exception e){
        }

        Common.log("Tracking",requestWrapper.getRemoteAddress(),host + uri.getPath() + (referer != null ? ", referer: " + referer : ""));

        ArrayList<String> parts = new ArrayList<>();
        parts.addAll(Arrays.asList(uri.getPath().split("/")));

//        File root = new File(OPTIONS.getWebRootDirectory());
//        File file = new File(root + uri.getPath()).getCanonicalFile();

        if(uri.getPath().startsWith("/track/")) {

            String tokenId = "";
            if (parts.size() >= 3) {
                tokenId = parts.get(2);
            }

            String mobileRedirect, webRedirect, mainLink;
            if(Common.getInstance().getDataProcessor(DataProcessorFirebaseV1.VERSION).isServerMode()){
//                mobileRedirect = "waytous://" + host + "/track/" + tokenId;
                mobileRedirect = "https://" + host + "/track/" + tokenId;
                webRedirect = "https://" + host + Common.getWrappedHttpsPort() + "/group/" + tokenId;
                mainLink = "https://" + host + Common.getWrappedHttpsPort() + "/group/" + tokenId;
            } else {
//                mobileRedirect = "waytous://" + host + "/track/" + tokenId;
                mobileRedirect = "https://" + host + "/track/" + tokenId;
                webRedirect = "http://" + requestWrapper.getRequestHeader(HttpHeaders.HOST).get(0) + "/group/" + tokenId;
                mainLink = "http://" + requestWrapper.getRequestHeader(HttpHeaders.HOST).get(0) + "/group/" + tokenId;
            }

            String redirectLink = "https://" + OPTIONS.getFirebaseDynamicLinkHost() + "/?"
                    + "link=" + mainLink
                    + "&apn=com.edeqa.waytous"
                    + "&al=" + mobileRedirect
                    + "&afl=" + webRedirect
                    + "&st=Waytous"
                    + "&sd=Be+always+on+the+same+way+with+your+friends"
                    + "&si=https://www.waytous.net/icons/favicon-256x256.png";

            Common.log("Tracking", "->", redirectLink);

            requestWrapper.sendRedirect(redirectLink);
            return;
        }


        html.clear();
        html.getHead().add(TITLE).with("Waytous");

        JSONObject o = new JSONObject();
        o.put("request", parts);
        o.put("version", SERVER_BUILD);
        o.put("HTTP_PORT", OPTIONS.getHttpPortMasked());
        o.put("HTTPS_PORT", OPTIONS.getHttpsPortMasked());
        o.put("WS_FB_PORT", OPTIONS.getWsPortFirebase());
        o.put("WSS_FB_PORT", OPTIONS.getWssPortFirebase());
        o.put("WS_PORT", OPTIONS.getWsPortDedicated());
        o.put("WSS_PORT", OPTIONS.getWssPortDedicated());
        o.put("firebase_config", OPTIONS.getFirebaseConfig());
        o.put("is_stand_alone", Common.getInstance().getDataProcessor(DataProcessorFirebaseV1.VERSION).isServerMode());
        if(OPTIONS.isDebugMode()) o.put("is_debug_mode", true);
        o.put("google_analytics_tracking_id", OPTIONS.getGoogleAnalyticsTrackingId());

        html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/" + FIREBASE_JAVASCRIPT_VERSION + "/firebase.js").with("nonce", "waytous");
        html.getHead().add(SCRIPT).with("var inline = 1;").with("nonce", "waytous");
        html.getHead().add(SCRIPT).with("data", o).with("nonce", "waytous");
        html.getHead().add(SCRIPT).with("firebase.initializeApp(data.firebase_config);").with("nonce", "waytous");

        html.getHead().add(SCRIPT).with("(function checkVersion(){var l=localStorage;if(l){var w=\"waytous:version\";var d=data.version;var i=parseInt(l[w]||0);if(i<d){l[w]=d;console.warn(\"Forced reloading because of version \"+d+\" is newer than \"+i);window.location.reload(true);}}})();").with("nonce", "waytous");
        html.getHead().add(SCRIPT).with(SRC, "/js/tracking/Main.js").with("async",true).with("defer",true).with(ONLOAD, "(window.WTU = new Main()).start();").with("nonce", "waytous");


        // FIXME - need to check by https://observatory.mozilla.org/analyze.html?host=waytous.net
        requestWrapper.setHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS, "nosniff");
        requestWrapper.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, "frame-ancestors 'self'");
        requestWrapper.setHeader(HttpHeaders.X_FRAME_OPTIONS, "SAMEORIGIN");
        requestWrapper.setHeader(HttpHeaders.X_XSS_PROTECTION, "1; mode=block");
        requestWrapper.setHeader(HttpHeaders.STRICT_TRANSPORT_SECURITY, "max-age=63072000; includeSubDomains; preload");
//        requestWrapper.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, "script-src 'unsafe-inline' 'unsafe-eval' https: 'nonce-waytous' 'strict-dynamic'");
//        requestWrapper.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://www.gstatic.com https://www.googletagmanager.com https://cdnjs.cloudflare.com https://www.google-analytics.com https://connect.facebook.net https://platform.twitter.com https://maps.googleapis.com https://apis.google.com");
        requestWrapper.setHeader(HttpHeaders.VARY, "Accept-Encoding");
        String etag = "W/1976-" + uri.getPath().hashCode();
        requestWrapper.setHeader(HttpHeaders.ETAG, etag);

        requestWrapper.sendResult(200, Mime.TEXT_HTML, html.build().getBytes());

    }

}
