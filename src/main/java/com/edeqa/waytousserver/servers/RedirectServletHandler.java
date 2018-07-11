package com.edeqa.waytousserver.servers;

import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.rest.DynamicLink;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created 10/5/16.
 */
public class RedirectServletHandler extends com.edeqa.edequate.RedirectServletHandler {

    private final static String LOG = "Redirect:Waytous";

    @Override
    public void perform(RequestWrapper requestWrapper) {
        try {
            URI uri = requestWrapper.getRequestURI();
            String host = requestWrapper.getRequestedHost();
            String referer = requestWrapper.getReferer();

            ArrayList<String> parts = new ArrayList<>(Arrays.asList(uri.getPath().split("/")));

            String tokenId = null;
            if(parts.size() >= 3){
                tokenId = parts.get(2);
            }

            if(uri.getPath().startsWith("/track/") && tokenId != null) {
                Misc.log(LOG, "[" + requestWrapper.getRemoteAddress() + "]", uri.getPath(), (referer != null ? "referer: " + referer : ""));
                String redirectLink = new DynamicLink().setHost(host).fetchLink(requestWrapper, tokenId);
                Misc.log(LOG, "->", redirectLink);
                requestWrapper.sendRedirect(redirectLink);
            } else {
                super.perform(requestWrapper);
            }
        } catch (Exception e) {
            super.perform(requestWrapper);
        }
    }
}
