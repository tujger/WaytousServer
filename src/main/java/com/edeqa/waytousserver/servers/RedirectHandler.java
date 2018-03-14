package com.edeqa.waytousserver.servers;

import com.edeqa.edequate.abstracts.AbstractServletHandler;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.edeqa.waytous.Constants.OPTIONS;

/**
 * Created 10/5/16.
 */
public class RedirectHandler extends AbstractServletHandler {

    private final static String LOG = "Redirect";

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        try {
            URI uri = exchange.getRequestURI();
            String host, referer = null;
            try {
                host = exchange.getRequestHeaders().getFirst(HttpHeaders.HOST);
                if(host == null) {
                    host = exchange.getLocalAddress().getHostName();
                }
                host = host.split(":")[0];
            } catch(Exception e){
                e.printStackTrace();
                host = InetAddress.getLocalHost().getHostAddress();
            }
            try {
                referer = exchange.getRequestHeaders().get(HttpHeaders.REFERER).get(0);
                if(referer.contains(host)) referer = null;
            } catch(Exception e){
            }

            Misc.log(LOG, exchange.getRemoteAddress(), host + uri.getPath() + (referer != null ? ", referer: " + referer : ""));

            ArrayList<String> parts = new ArrayList<>();
            parts.addAll(Arrays.asList(uri.getPath().split("/")));

            String tokenId = null;

            if(parts.size() >= 3){
                tokenId = parts.get(2);
            }

            if(uri.getPath().startsWith("/.well-known/") || uri.getPath().contains("googlee7b16def95e75693.html")) {
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set(HttpHeaders.CONTENT_TYPE, Mime.TEXT_PLAIN);
                responseHeaders.set(HttpHeaders.DATE, new Date().toString());
                exchange.sendResponseHeaders(200, 0);
                OutputStream os = exchange.getResponseBody();

                File root = new File(OPTIONS.getWebRootDirectory());
                File file = new File(root + uri.getPath()).getCanonicalFile();

                FileInputStream fs = new FileInputStream(file);

                final byte[] buffer = new byte[0x10000];
                int count;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
                fs.close();
                os.close();
            } else if(uri.getPath().startsWith("/track/") && tokenId != null) {
                String mobileRedirect = "waytous://" + host + "/track/" + tokenId;
                String webRedirect = "https://" + host + Common.getWrappedHttpsPort() + "/group/" + tokenId;
                String mainLink = "https://" + host + Common.getWrappedHttpsPort() + "/track/" + tokenId;

                String redirectLink = "http://" + OPTIONS.getFirebaseDynamicLinkHost() + "/?"
                        + "link=" + mainLink
                        + "&apn=com.edeqa.waytous"
                        + "&al=" + mobileRedirect
                        + "&afl=" + webRedirect
                        + "&ifl=" + webRedirect
                        + "&st=Waytous"
                        + "&sd=Be+always+on+the+same+way+with+your+friends"
                        + "&si=https://www.waytous.net/images/waytous-transparent-256.png";

                Misc.log(LOG,"->", redirectLink);

                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set(HttpHeaders.CONTENT_TYPE, Mime.TEXT_PLAIN);
                responseHeaders.set(HttpHeaders.DATE, new Date().toString());
                responseHeaders.set(HttpHeaders.LOCATION, redirectLink);
                exchange.sendResponseHeaders(302, 0);
                exchange.close();

            } else if(uri.getPath().startsWith("/admin")) {
                String redirectLink = "https://" + host + ":" + OPTIONS.getHttpsAdminPort() + uri.getPath();
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set(HttpHeaders.CONTENT_TYPE, Mime.TEXT_PLAIN);
                responseHeaders.set(HttpHeaders.DATE, new Date().toString());
                responseHeaders.set(HttpHeaders.LOCATION, redirectLink);
                exchange.sendResponseHeaders(302, 0);
                exchange.close();
            } else {
                redirect(exchange, host, uri.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            URI uri = exchange.getRequestURI();
            String host;
            try {
                host = exchange.getRequestHeaders().get(HttpHeaders.HOST).get(0);
                host = host.split(":")[0];
            } catch(Exception e1){
                e1.printStackTrace();
                host = InetAddress.getLocalHost().getHostAddress();
            }
            redirect(exchange, host, "/404.html");
        }
    }

    @Override
    public void perform(RequestWrapper requestWrapper) throws IOException {

    }

    public void redirect(HttpExchange exchange, String host, String path) throws IOException {
        String newUri = "https://" + host + Common.getWrappedHttpsPort() + path;

        Misc.log(LOG, exchange.getRemoteAddress(), "->", newUri);

        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set(HttpHeaders.CONTENT_TYPE, Mime.TEXT_PLAIN);
            responseHeaders.set(HttpHeaders.DATE, new Date().toString());
            responseHeaders.set(HttpHeaders.LOCATION, newUri);
            exchange.sendResponseHeaders(302, 0);
        }
    }

}
