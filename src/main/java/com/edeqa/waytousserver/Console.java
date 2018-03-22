package com.edeqa.waytousserver;


import com.edeqa.edequate.abstracts.AbstractAction;
import com.edeqa.edequate.helpers.RequestWrapper;
import com.edeqa.eventbus.EventBus;
import com.edeqa.waytousserver.rest.Arguments;
import com.edeqa.waytousserver.servers.AdminServletHandler;
import com.edeqa.waytousserver.servers.RestServletHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.edeqa.edequate.abstracts.AbstractAction.RESTBUS;
import static com.edeqa.edequate.abstracts.AbstractAction.SYSTEMBUS;

public class Console extends AdminServletHandler {

    private static Arguments arguments;

    public static void main(final String[] args ) throws Exception {

        EventBus<AbstractAction> systemBus = (EventBus<AbstractAction>) EventBus.getOrCreate(SYSTEMBUS);
        EventBus<AbstractAction> restBus = (EventBus<AbstractAction>) EventBus.getOrCreate(RESTBUS);

        arguments = new Arguments();
        arguments.call(null, args);
        systemBus.registerOrUpdate(arguments);

        Console console = new Console();
        console.useDefault();

        console.registerActionsPool();

        System.out.println("A");
        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            //                        if(!wssServer.parse(sysin)) break;
            System.out.print("> ");
            try {
                String in = sysin.readLine();
                if (in.equals("exit")) {
                    break;
                } else if (in.equals("list")) {
                    System.out.println("list:");
                    for(Map.Entry entry: restBus.getHolders().entrySet()) {
                        System.out.println(entry.getKey());
                    }
                } else if (in.startsWith("/")) {
                    List parts = Arrays.asList(in.split("[()]"));
                    List argms = null;

                    String id = (String) parts.get(0);

                    JSONObject json = new JSONObject();
                    RequestWrapper req = new RequestWrapper();
                    if(parts.size() > 1) {
                        argms = Arrays.asList(((String)parts.get(1)).split("[,]"));

                        try {
                            json = new JSONObject(argms.get(0));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println("rest="+id);

                    restBus.getHolder(id).call(json, req);
                    System.out.println("result="+json.toString(4));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Console() {
        useDefault();
        new RestServletHandler().useDefault();

    }

}
