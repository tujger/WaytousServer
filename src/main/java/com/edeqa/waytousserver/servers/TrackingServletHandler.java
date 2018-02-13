package com.edeqa.waytousserver.servers;

import com.edeqa.edequate.abstracts.AbstractServletHandler;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.Content;
import com.edeqa.helpers.HtmlGenerator;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.rest.DynamicLink;
import com.edeqa.waytousserver.rest.InitialData;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.IOException;
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


/**
 * Created 1/19/17.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class TrackingServletHandler extends AbstractServletHandler {

    private final HtmlGenerator html = new HtmlGenerator();

    public TrackingServletHandler(){
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
    public void perform(RequestWrapper requestWrapper) throws IOException {

        URI uri = requestWrapper.getRequestURI();

        String host = requestWrapper.getRequestedHost(), referer = null;
        try {
            referer = requestWrapper.getRequestHeaders().get(HttpHeaders.REFERER).get(0);
            if(referer.contains(host)) referer = null;
        } catch(Exception e){
        }

        Misc.log("Tracking", "[" + requestWrapper.getRemoteAddress().getAddress().getHostAddress() + ":" + requestWrapper.getRemoteAddress().getPort() + "]", host + uri.getPath() + (referer != null ? ", referer: " + referer : ""));

        ArrayList<String> parts = new ArrayList<>();
        parts.addAll(Arrays.asList(uri.getPath().split("/")));

//        File root = new File(OPTIONS.getWebRootDirectory());
//        File file = new File(root + uri.getPath()).getCanonicalFile();

        if(uri.getPath().startsWith("/track2/")) {
            new Content().setMimeType(new MimeType().setMime(Mime.TEXT_HTML).setText(true)).setWebPath(new WebPath(OPTIONS.getWebRootDirectory(), "index-tracking.html")).setResultCode(200).call(null, requestWrapper);
            return;
        } else if(uri.getPath().startsWith("/track/")) {

            String tokenId = "";
            if (parts.size() >= 3) {
                tokenId = parts.get(2);
            }

            String redirectLink = new DynamicLink().setHost(host).fetchLink(requestWrapper, tokenId);
            Misc.log("Tracking", "->", redirectLink);
            requestWrapper.sendRedirect(redirectLink);
            return;
        }

        html.clear();
        html.getHead().add(TITLE).with("Waytous");

        JSONObject o = new JSONObject();
        new InitialData().call(o, requestWrapper);
//        o.put("request", parts);

        html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/" + FIREBASE_JAVASCRIPT_VERSION + "/firebase.js").with("nonce", "waytous");
//        html.getHead().add(SCRIPT).with("var inline = 1;").with("nonce", "waytous");
        html.getHead().add(SCRIPT).with("data", o).with("nonce", "waytous");
        html.getHead().add(SCRIPT).with("firebase.initializeApp(data.firebase_config);").with("nonce", "waytous");

        html.getHead().add(SCRIPT).with("(function checkVersion(){var l=localStorage;if(l){var w=\"waytous:version\";var d=data.version;var i=parseInt(l[w]||0);if(i<d){l[w]=d;console.warn(\"Forced reloading because of version \"+d+\" is newer than \"+i);window.location.reload(true);}}})();").with("nonce", "waytous");
        html.getHead().add(SCRIPT).with(SRC, "/js/tracking/Main.js").with("async",true).with("defer",true).with(ONLOAD, "(window.WTU = new Main()).start();").with("nonce", "waytous");

        Common.addNoscript(html);

        // FIXME - need to check by https://observatory.mozilla.org/analyze.html?host=waytous.net
        new Content()
                .setMimeType(new MimeType().setMime(Mime.TEXT_HTML).setText(true).setGzip(true))
                .setContent(html.build())
                .setResultCode(200)
                .call(null, requestWrapper);
    }

}
