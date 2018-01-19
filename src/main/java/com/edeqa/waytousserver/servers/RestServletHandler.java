package com.edeqa.waytousserver.servers;

import com.edeqa.edequate.rest.Content;
import com.edeqa.edequate.rest.Files;
import com.edeqa.edequate.rest.Locales;
import com.edeqa.waytousserver.helpers.Common;
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
        registerAction(new Content().setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("content"));
        registerAction(new Content().setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("resources"));
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
        }).setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("sounds"));

        registerAction(new Files().setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("Holder");
            }
        }).setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("js/main").setActionName("main"));
        registerAction(new Files().setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("Holder");
            }
        }).setWebDirectory(OPTIONS.getWebRootDirectory()).setChildDirectory("js/tracking").setActionName("tracking"));

        registerAction(new Locales().setWebDirectory(OPTIONS.getWebRootDirectory()));
    }

    @Override
    public void init() throws ServletException {
        super.init();
        Common.getInstance().initOptions(getServletContext());
        Common.getInstance().initDataProcessor();
    }

}