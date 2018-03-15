package com.edeqa.waytousserver.rest;

import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.net.URLEncoder;

import static com.edeqa.waytous.Constants.OPTIONS;

@SuppressWarnings("unused")
public class DynamicLink extends AbstractAction<RequestWrapper> {

    public static final String TYPE = "/rest/tracking/link";

    private boolean admin;
    private String host;
    private String groupId;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, RequestWrapper requestWrapper) {

        String mobileRedirect, webRedirect, mainLink;
        if(Common.getInstance().getDataProcessor().isServerMode()){

            Arguments arguments = ((Arguments) EventBus.getEventBus(AbstractAction.SYSTEMBUS).getHolder(Arguments.TYPE));

//                mobileRedirect = "waytous://" + host + "/track/" + tokenId;
            mobileRedirect = "https://" + getHost() + "/track/" + getGroupId();
            webRedirect = "https://" + getHost() + arguments.getWrappedHttpsPort() + "/group/" + getGroupId();
            mainLink = "https://" + getHost() + arguments.getWrappedHttpsPort() + "/group/" + getGroupId();
        } else {
//                mobileRedirect = "waytous://" + host + "/track/" + tokenId;
            mobileRedirect = "https://" + getHost() + "/track/" + getGroupId();
            webRedirect = "http://" + requestWrapper.getRequestHeader(HttpHeaders.HOST).get(0) + "/group/" + getGroupId();
            mainLink = "http://" + requestWrapper.getRequestHeader(HttpHeaders.HOST).get(0) + "/group/" + getGroupId();
        }

        String redirectLink;
        try {
// https://firebase.google.com/docs/reference/dynamic-links/link-shortener
            JSONObject linkJson = new JSONObject();
            JSONObject dynamicLinkInfo = new JSONObject();
            linkJson.put("dynamicLinkInfo", dynamicLinkInfo);
            dynamicLinkInfo.put("dynamicLinkDomain", OPTIONS.getFirebaseDynamicLinkHost());
            dynamicLinkInfo.put("link", mainLink);

            JSONObject child = new JSONObject();
            dynamicLinkInfo.put("androidInfo", child);
            child.put("androidPackageName", "com.edeqa.waytous");
            child.put("androidLink", mobileRedirect);
            child.put("androidFallbackLink", webRedirect);

//                child = new JSONObject();
//                dynamicLinkInfo.put("iosInfo", child);

            child = new JSONObject();
            dynamicLinkInfo.put("socialMetaTagInfo", child);
            child.put("socialTitle", "Waytous");
            child.put("socialDescription", "Be always on the same way with your friends");
            child.put("socialImageLink", "https://www.waytous.net/icons/favicon-256x256.png");

            child = new JSONObject();
            linkJson.put("suffix", child);
//                child.put("option", "UNGUESSABLE");
            child.put("option", "SHORT");

            linkJson = new JSONObject(Misc.getUrl("https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + URLEncoder.encode( OPTIONS.getFirebaseApiKey(), "UTF-8" ), linkJson.toString(), "UTF-8"));
            redirectLink = linkJson.getString("shortLink");
            Misc.log("DynamicLink", "payload:", linkJson);
        } catch (Exception e) {
            e.printStackTrace();
            redirectLink = "https://" + OPTIONS.getFirebaseDynamicLinkHost() + "/?"
                    + "link=" + mainLink
                    + "&apn=com.edeqa.waytous"
                    + "&al=" + mobileRedirect
                    + "&afl=" + webRedirect
                    + "&st=Waytous"
                    + "&sd=Be+always+on+the+same+way+with+your+friends"
                    + "&si=https://www.waytous.net/icons/favicon-256x256.png";
        }

        json.put(STATUS, STATUS_SUCCESS);
        json.put(MESSAGE, redirectLink);
    }

    public String fetchLink(RequestWrapper requestWrapper, String groupId) {
        setGroupId(groupId);
        JSONObject json = new JSONObject();
        call(json, requestWrapper);
        if(json.has(STATUS) && STATUS_SUCCESS.equals(json.getString(STATUS))) {
            return json.getString(MESSAGE);
        } else {
            return null;
        }
    }

    public DynamicLink setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    private String getGroupId() {
        return groupId;
    }

    public DynamicLink setHost(String host) {
        this.host = host;
        return this;
    }

    private String getHost() {
        return host;
    }
}
