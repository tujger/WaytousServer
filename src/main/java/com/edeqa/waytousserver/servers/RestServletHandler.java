package com.edeqa.waytousserver.servers;

import com.edeqa.edequate.rest.Arguments;
import com.edeqa.edequate.rest.Files;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.rest.Api;
import com.edeqa.waytousserver.rest.InitialData;
import com.edeqa.waytousserver.rest.Join;
import com.edeqa.waytousserver.rest.TosAgreement;
import com.edeqa.waytousserver.rest.Version;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Comparator;

import javax.servlet.ServletException;

/**
 * Created 1/19/17.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class RestServletHandler extends com.edeqa.edequate.RestServletHandler {

    public RestServletHandler() {
        super();
        useDefault();
        registerActionsPool();
    }

    @Override
    public void useDefault() {
        super.useDefault();

        Arguments arguments = (Arguments) getSystemBus().getHolder(Arguments.TYPE);
        registerAction(new InitialData());
        registerAction(new Join());
        registerAction(new TosAgreement());
        registerAction(new Version());
        registerAction(new Files().setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp3");
            }
        }).setComparator(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if("none.mp3".equals(o1.getName())) return -1;
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        }).setWebDirectory(arguments.getWebRootDirectory()).setChildDirectory("sounds").setActionName("/rest/sounds"));
        registerAction(new Files().setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("Holder");
            }
        }).setWebDirectory(arguments.getWebRootDirectory()).setChildDirectory("js/tracking").setActionName("/rest/tracking"));

        registerAction(new Api());
    }

    @Override
    public void init() throws ServletException {
        super.init();
        Common.getInstance().initOptions(getServletContext());
        Common.getInstance().initDataProcessor();
    }

}