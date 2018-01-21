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
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;

/**
 * Created 1/19/17.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class MainServletHandler extends com.edeqa.edequate.MainServletHandler {

    public MainServletHandler() {
        setMimeTypes(OPTIONS.getMimeTypes());
        setWebDirectory(OPTIONS.getWebRootDirectory());
        initSubstitutions();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        Common.getInstance().initOptions(getServletContext());
        initSubstitutions();
    }

    private void initSubstitutions() {
        setReplacements(new Replacements());

        getReplacements().add(new Replacement().setPattern("\\$\\{SERVER_BUILD\\}").setReplace("" + SERVER_BUILD));
        getReplacements().add(new Replacement().setPattern("\\$\\{APP_NAME\\}").setReplace(OPTIONS.getAppName() + (OPTIONS.isDebugMode() ? " &beta;" : "")));
        getReplacements().add(new Replacement().setPattern("\\$\\{SUPPORT_EMAIL\\}").setReplace(OPTIONS.getSupportEmail()));
        getReplacements().add(new Replacement().setPattern("\\$\\{WEB_PAGE\\}").setReplace(OPTIONS.getAppLink()));

        JSONObject o = new JSONObject();
        o.put("version", SERVER_BUILD);
        o.put("is_stand_alone", Common.getInstance().getDataProcessor(DataProcessorFirebaseV1.VERSION).isServerMode());
        if(OPTIONS.isDebugMode()) o.put("is_debug_mode", true);
        o.put("google_analytics_tracking_id", OPTIONS.getGoogleAnalyticsTrackingId());
        getReplacements().add(new Replacement().setPattern("\\$\\{APP_DATA\\}").setReplace("var data = " + o.toString()));

        getReplacements().disableFor(Mime.TEXT_CSS);
    }

    @Override
    public void perform(RequestWrapper requestWrapper) throws IOException {
        getReplacements().add(new Replacement().setPattern("\\$\\{REFERER\\}").setReplace("https://" + OPTIONS.getServerHost()));
        try {
            //noinspection LoopStatementThatDoesntLoop
            for (String x : requestWrapper.getRequestHeader(HttpHeaders.REFERER)) {
                getReplacements().add(new Replacement().setPattern("\\$\\{REFERER\\}").setReplace(x));
                break;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        super.perform(requestWrapper);
    }
}
