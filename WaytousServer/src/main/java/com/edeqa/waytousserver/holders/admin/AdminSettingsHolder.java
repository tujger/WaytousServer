package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.AdminServletHandler;


/**
 * Created 1/23/2017.
 */

@SuppressWarnings("unused")
public class AdminSettingsHolder implements PageHolder {

    private static final String HOLDER_TYPE = "settings";

    private final AdminServletHandler server;
    private HtmlGenerator html;

    public AdminSettingsHolder(AdminServletHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @Override
    public boolean perform(RequestWrapper requestWrapper) {
        return false;
    }

}
