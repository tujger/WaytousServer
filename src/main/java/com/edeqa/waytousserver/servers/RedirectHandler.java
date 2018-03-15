package com.edeqa.waytousserver.servers;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.abstracts.AbstractServletHandler;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.edequate.helpers.WebPath;
import com.edeqa.edequate.rest.Content;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Mime;
import com.edeqa.helpers.MimeType;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.rest.Arguments;
import com.edeqa.waytousserver.rest.DynamicLink;
import com.google.common.net.HttpHeaders;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import static com.edeqa.waytous.Constants.OPTIONS;

/**
 * Created 10/5/16.
 */
public class RedirectHandler extends AbstractServletHandler {

    private final static String LOG = "RedirectHandler";

    @Override
    public void perform(RequestWrapper requestWrapper) throws IOException {
        try {
            URI uri = requestWrapper.getRequestURI();
            String host = requestWrapper.getRequestedHost();
            String referer = null;
            try {
                referer = requestWrapper.getRequestHeaders().get(HttpHeaders.REFERER).get(0);
                if(referer.contains(host)) referer = null;
            } catch(Exception e){
            }

            Misc.log(LOG, "[" + requestWrapper.getRemoteAddress() + "]", uri.getPath(), (referer != null ? "referer: " + referer : ""));

            ArrayList<String> parts = new ArrayList<>();
            parts.addAll(Arrays.asList(uri.getPath().split("/")));

            String tokenId = null;

            if(parts.size() >= 3){
                tokenId = parts.get(2);
            }

            if(uri.getPath().startsWith("/.well-known/") || uri.getPath().contains("googlee7b16def95e75693.html")) {
                Arguments arguments = ((Arguments) EventBus.getEventBus(AbstractAction.SYSTEMBUS).getHolder(Arguments.TYPE));
                WebPath webPath = new WebPath(arguments.getWebRootDirectory(), uri.getPath());

                if(webPath.path().exists()) {
                    new Content().setMimeType(new MimeType().setMime(Mime.TEXT_PLAIN)).setWebPath(webPath).setResultCode(200).call(null, requestWrapper);
                } else {
                    requestWrapper.sendError(404, "Not found");
                }
            } else if(uri.getPath().startsWith("/track/") && tokenId != null) {

                String redirectLink = new DynamicLink().setHost(host).fetchLink(requestWrapper, tokenId);
                Misc.log(LOG, "->", redirectLink);
                requestWrapper.sendRedirect(redirectLink);
            } else if(uri.getPath().startsWith("/admin")) {
                String redirectLink = "https://" + host + ":" + OPTIONS.getHttpsAdminPort() + uri.getPath();
                requestWrapper.sendRedirect(redirectLink);
            } else {
                Arguments arguments = ((Arguments) EventBus.getEventBus(AbstractAction.SYSTEMBUS).getHolder(Arguments.TYPE));
                requestWrapper.sendRedirect("https://" + host + arguments.getWrappedHttpsPort() + uri.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Arguments arguments = ((Arguments) EventBus.getEventBus(AbstractAction.SYSTEMBUS).getHolder(Arguments.TYPE));
            requestWrapper.sendRedirect("https://" + requestWrapper.getRequestedHost() + arguments.getWrappedHttpsPort() + "/404.html");
        }
    }

}
