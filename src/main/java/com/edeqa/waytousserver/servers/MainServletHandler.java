package com.edeqa.waytousserver.servers;

import com.edeqa.edequate.helpers.Replacement;
import com.edeqa.edequate.helpers.Replacements;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.helpers.Mime;
import com.edeqa.waytousserver.helpers.Common;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.IOException;

import javax.servlet.ServletException;

import static com.edeqa.waytous.Constants.OPTIONS;

/**
 * Created 1/19/17.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class MainServletHandler extends com.edeqa.edequate.MainServletHandler {

    private static final String PATTERN_APP_DATA = "\\$\\{APP_DATA\\}";
    private static final String PATTERN_APP_NAME = "\\$\\{APP_NAME\\}";
    private static final String PATTERN_REFERER = "\\$\\{REFERER\\}";
    private static final String PATTERN_SERVER_BUILD = "\\$\\{SERVER_BUILD\\}";
    private static final String PATTERN_SUPPORT_EMAIL = "\\$\\{SUPPORT_EMAIL\\}";
    private static final String PATTERN_WEB_PAGE = "\\$\\{WEB_PAGE\\}";

    public MainServletHandler() {
        if(OPTIONS != null) {
            setMimeTypes(OPTIONS.getMimeTypes());
            setWebDirectory(OPTIONS.getWebRootDirectory());
            initReplacements();
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        Common.getInstance().initOptions(getServletContext());
        setMimeTypes(OPTIONS.getMimeTypes());
        setWebDirectory(OPTIONS.getWebRootDirectory());
        initReplacements();
    }

    private void initReplacements() {
        setReplacements(new Replacements());

        getReplacements().add(new Replacement().setPattern(PATTERN_SERVER_BUILD).setReplace("" + Common.SERVER_BUILD));
        getReplacements().add(new Replacement().setPattern(PATTERN_APP_NAME).setReplace(OPTIONS.getAppName() + (OPTIONS.isDebugMode() ? " &beta;" : "")));
        getReplacements().add(new Replacement().setPattern(PATTERN_SUPPORT_EMAIL).setReplace(OPTIONS.getSupportEmail()));
        getReplacements().add(new Replacement().setPattern(PATTERN_WEB_PAGE).setReplace(OPTIONS.getAppLink()));

        JSONObject o = new JSONObject();
        o.put("version", Common.SERVER_BUILD);
        o.put("is_stand_alone", true);//Common.getInstance().getDataProcessor().isServerMode());
        if(OPTIONS.isDebugMode()) o.put("is_debug_mode", true);
        o.put("google_analytics_tracking_id", OPTIONS.getGoogleAnalyticsTrackingId());
        getReplacements().add(new Replacement().setPattern(PATTERN_APP_DATA).setReplace("var data = " + o.toString()));

        getReplacements().disableFor(Mime.TEXT_CSS);
    }

    @Override
    public void perform(RequestWrapper requestWrapper) throws IOException {
        getReplacements().add(new Replacement().setPattern(PATTERN_REFERER).setReplace("https://" + OPTIONS.getServerHost()));
        try {
            //noinspection LoopStatementThatDoesntLoop
            for (String x : requestWrapper.getRequestHeader(HttpHeaders.REFERER)) {
                getReplacements().add(new Replacement().setPattern(PATTERN_REFERER).setReplace(x));
                break;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        super.perform(requestWrapper);
    }
}
