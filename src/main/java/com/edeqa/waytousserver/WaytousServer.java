package com.edeqa.waytousserver;

import com.edeqa.edequate.EdequateServer;
import com.edeqa.edequate.helpers.DigestAuthenticator;
import com.edeqa.edequate.helpers.ServletHandlerOptions;
import com.edeqa.edequate.rest.SecureContext;
import com.edeqa.helpers.Misc;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.rest.Arguments;
import com.edeqa.waytousserver.servers.AdminServletHandler;
import com.edeqa.waytousserver.servers.DataProcessorFirebase;
import com.edeqa.waytousserver.servers.MainServletHandler;
import com.edeqa.waytousserver.servers.RedirectServletHandler;
import com.edeqa.waytousserver.servers.RestServletHandler;
import com.edeqa.waytousserver.servers.TrackingServletHandler;
import com.edeqa.waytousserver.servers.WaytousWebsocketServer;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import java.net.InetAddress;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;


/**
 * Created 10/2/16.
 */
@SuppressWarnings({"HardCodedStringLiteral", "GoogleAppEngineForbiddenCode"})
public class WaytousServer extends EdequateServer {

    private static final String LOG = "WaytousServer";
    private static WaytousWebsocketServer wsServer;
    private static WaytousWebsocketServer wssServer;

    @SuppressWarnings("AppEngineForbiddenCode")
    public static void main(final String[] args ) throws Exception {

        Misc.log(LOG, "====== Waytous server v1."+SERVER_BUILD+". Copyright (C) 2017-18, Edeqa. http://www.edeqa.com ======");

        setArguments(new Arguments());
        getArguments().call(null, args);
        getSystemBus().registerOrUpdate(getArguments());

        prepareServer();
        setupServletHandlers();
        startServer();
    }

    protected static void setupServletHandlers(){
        RedirectServletHandler redirectServer = new RedirectServletHandler();
        MainServletHandler mainServer = new MainServletHandler();
        RestServletHandler restServer = new RestServletHandler();
        TrackingServletHandler trackingServer = new TrackingServletHandler();
        AdminServletHandler adminServer = new AdminServletHandler();

        ServletHandlerOptions.getOrCreate(getServer()).putIfAbsent(new ServletHandlerOptions().setContext("/").setServletHandler(redirectServer));

        ServletHandlerOptions.getOrCreate(getSslServer()).putIfAbsent(new ServletHandlerOptions().setContext("/").setServletHandler(mainServer));
        ServletHandlerOptions.getOrCreate(getSslServer()).putIfAbsent(new ServletHandlerOptions().setContext("/track").setServletHandler(trackingServer));
        ServletHandlerOptions.getOrCreate(getSslServer()).putIfAbsent(new ServletHandlerOptions().setContext("/track2").setServletHandler(trackingServer));
        ServletHandlerOptions.getOrCreate(getSslServer()).putIfAbsent(new ServletHandlerOptions().setContext("/group").setServletHandler(trackingServer));
        ServletHandlerOptions.getOrCreate(getSslServer()).putIfAbsent(new ServletHandlerOptions().setContext("/rest").setServletHandler(restServer));

        ServletHandlerOptions.getOrCreate(getAdminServer()).putIfAbsent(new ServletHandlerOptions().setContext("/rest").setServletHandler(restServer));
        ServletHandlerOptions.getOrCreate(getAdminServer()).putIfAbsent(new ServletHandlerOptions().setContext("/").setServletHandler(adminServer).setAuthenticator(new DigestAuthenticator("waytous")));
        ServletHandlerOptions.getOrCreate(getAdminServer()).putIfAbsent(new ServletHandlerOptions().setContext("/admin/logout").setServletHandler(adminServer));
    }

    protected static void startServer() throws Exception {
        EdequateServer.startServer();

        Misc.log(LOG, "handles track link", "http://" + InetAddress.getLocalHost().getHostAddress() + getArguments().getWrappedHttpPort() + "/track/");

        /*
         * Websocket part
         */

        Common.getInstance().setDataProcessor(new DataProcessorFirebase());

        if(!Common.getInstance().getDataProcessor().isServerMode()){
            Misc.err(LOG, "This configuration can not be runned in stand-alone server mode. Set the installation type in build.gradle with the following property:\n\tdef installationType = 'standalone-server'\n");
            System.exit(1);
        }

        wsServer = new WaytousWebsocketServer(OPTIONS.getWsPortFirebase());
        wssServer = new WaytousWebsocketServer(OPTIONS.getWssPortFirebase());

        SecureContext secureContext = (SecureContext) getSystemBus().getHolder(SecureContext.TYPE);

        DefaultSSLWebSocketServerFactory socket = new DefaultSSLWebSocketServerFactory(secureContext.getSslContext());
        wssServer.setWebSocketFactory(socket);

        new Thread() {
            public void run() {
                try {
                    WebSocketImpl.DEBUG = false;
                    Misc.log(LOG, "starting", WaytousWebsocketServer.class.getSimpleName(), "with", DataProcessorFirebase.class.getSimpleName(), "on port", OPTIONS.getWsPortFirebase());
                    wsServer.start();
                    Misc.log(LOG, "starting", WaytousWebsocketServer.class.getSimpleName(), "with", DataProcessorFirebase.class.getSimpleName(), "on secured port", OPTIONS.getWssPortFirebase());
                    wssServer.start();

                        /*BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
                        while (true) {
    //                        if(!wssServer.parse(sysin)) break;
                            String in = sysin.readLine();
                            Common.log(LOG, "READ:" + in);
    //                        s.sendToAll(in);
                            if (in.equals("exit")) {
                                wssServer.stop();
                                break;
                            }
                        }*/
                } catch (Throwable e) {
                    Misc.err(LOG, "Error starting WebsocketServer:", e);
                    System.exit(1);
                }
            }
        }.start();
    }

}