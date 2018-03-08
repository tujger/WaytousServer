package com.edeqa.waytousserver.servers;

import com.edeqa.edequate.rest.Files;
import com.edeqa.edequate.rest.Locales;
import com.edeqa.edequate.rest.Resource;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.rest.InitialData;
import com.edeqa.waytousserver.rest.Join;
import com.edeqa.waytousserver.rest.TosAgreement;
import com.edeqa.waytousserver.rest.Version;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Comparator;

import javax.servlet.ServletException;

import static com.edeqa.waytous.Constants.OPTIONS;

/**
 * Created 1/19/17.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class RestServletHandler extends com.edeqa.edequate.RestServletHandler {

    public RestServletHandler() {
        super();
        useDefault();
        registerAction(new Resource().setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("content").setActionName("/rest/content"));
        registerAction(new Resource().setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("resources").setActionName("/rest/resources"));
        registerAction(new Resource().setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("data").setActionName("/rest/data"));
        registerAction(new InitialData());
        registerAction(new Join());
        registerAction(new Locales().setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("resources").setActionName("/rest/locales"));
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
        }).setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("sounds").setActionName("/rest/sounds"));
        registerAction(new Files().setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("pages-");
            }
        }).setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("data").setActionName("/rest/data/types"));
        registerAction(new Files().setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("Holder");
            }
        }).setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("js/main").setActionName("/rest/main"));
        registerAction(new Files().setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("Holder");
            }
        }).setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("js/tracking").setActionName("/rest/tracking"));

    }

    @Override
    public void init() throws ServletException {
        super.init();
        Common.getInstance().initOptions(getServletContext());
        Common.getInstance().initDataProcessor();
    }

}