package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.rest.admin.AccountDelete;
import com.edeqa.waytousserver.rest.admin.AccountsClean;
import com.edeqa.waytousserver.rest.admin.GroupCreate;
import com.edeqa.waytousserver.rest.admin.GroupDelete;
import com.edeqa.waytousserver.rest.admin.GroupModify;
import com.edeqa.waytousserver.rest.admin.GroupSwitch;
import com.edeqa.waytousserver.rest.admin.GroupsClean;
import com.edeqa.waytousserver.rest.admin.InitialData;
import com.edeqa.waytousserver.rest.admin.StatClean;
import com.edeqa.waytousserver.rest.admin.UserRemove;
import com.edeqa.waytousserver.rest.admin.UserSwitch;

import javax.servlet.ServletException;


/**
 * Created 10/5/16.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class AdminServletHandler extends com.edeqa.edequate.AdminServletHandler {

//    private final LinkedHashMap<String, PageHolder> holders;

    public AdminServletHandler(){
        super();

        registerAction(new AccountDelete());
        registerAction(new AccountsClean());
        registerAction(new GroupCreate());
        registerAction(new GroupDelete());
        registerAction(new GroupModify());
        registerAction(new GroupSwitch());
        registerAction(new GroupsClean());
        registerAction(new StatClean());
        registerAction(new UserRemove());
        registerAction(new UserSwitch());
        registerAction(new InitialData());

        registerActionsPool();
    }

    /**
     * Initialize DataProcessorFirebase for installation type "google-appengine".
     */
    @Override
    public void init() throws ServletException {
        super.init();
        Common.getInstance().initOptions(getServletContext());
        Common.getInstance().initDataProcessor();
    }
}
