package com.edeqa.waytousserver.servers;

import com.edeqa.waytous.Mime;
import com.edeqa.waytousserver.helpers.Common;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.edeqa.waytous.Constants.SENSITIVE;

/**
 * Created 10/5/16.
 */
public class RedirectHandler implements HttpHandler {

    private final static String LOG = "Redirect";

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        try {
            URI uri = exchange.getRequestURI();
            String host;
            try {
                host = exchange.getRequestHeaders().get(HttpHeaders.HOST).get(0);
                host = host.split(":")[0];
            } catch(Exception e){
                e.printStackTrace();
                host = InetAddress.getLocalHost().getHostAddress();
            }

            Common.log(LOG, exchange.getRemoteAddress(), host + uri.getPath());

            ArrayList<String> parts = new ArrayList<>();
            parts.addAll(Arrays.asList(uri.getPath().split("/")));

            String tokenId = null;

            if(parts.size() >= 3){
                tokenId = parts.get(2);
            }

            if(uri.getPath().startsWith("/.well-known/")) {
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set(HttpHeaders.CONTENT_TYPE, Mime.TEXT_PLAIN);
                responseHeaders.set(HttpHeaders.DATE, new Date().toString());
                exchange.sendResponseHeaders(200, 0);
                OutputStream os = exchange.getResponseBody();

                File root = new File(SENSITIVE.getWebRootDirectory());
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

                String redirectLink = "http://" + SENSITIVE.getFirebaseDynamicLinkHost() + "/?"
                        + "link=" + mainLink
                        + "&apn=com.edeqa.waytous"
                        + "&al=" + mobileRedirect
                        + "&afl=" + webRedirect
                        + "&ifl=" + webRedirect
                        + "&st=Waytous"
                        + "&sd=Be+always+on+the+same+way+with+your+friends"
                        + "&si=https://www.waytous.net/images/waytous-transparent-256.png";

                Common.log(LOG,"->", redirectLink);

                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set(HttpHeaders.CONTENT_TYPE, Mime.TEXT_PLAIN);
                responseHeaders.set(HttpHeaders.DATE, new Date().toString());
                responseHeaders.set(HttpHeaders.LOCATION, redirectLink);
                exchange.sendResponseHeaders(302, 0);
                exchange.close();

            } else if(uri.getPath().startsWith("/admin")) {
                String redirectLink = "https://" + host + ":" + SENSITIVE.getHttpsAdminPort() + uri.getPath();
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

    public void redirect(HttpExchange exchange, String host, String path) throws IOException {
        String newUri = "https://" + host + Common.getWrappedHttpsPort() + path;

        Common.log(LOG, exchange.getRemoteAddress(), "->", newUri);

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
