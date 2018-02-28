package com.edeqa.waytousserver;

import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Options;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.DigestAuthenticator;
import com.edeqa.waytousserver.servers.AdminServletHandler;
import com.edeqa.waytousserver.servers.DataProcessorFirebase;
import com.edeqa.waytousserver.servers.MainServletHandler;
import com.edeqa.waytousserver.servers.RedirectHandler;
import com.edeqa.waytousserver.servers.RestServletHandler;
import com.edeqa.waytousserver.servers.TrackingServletHandler;
import com.edeqa.waytousserver.servers.WaytousWebsocketServer;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;


/**
 * Created 10/2/16.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class WaytousServer {

    private static final String LOG = "WaytousServer";
    private static WaytousWebsocketServer wsServer;
    private static WaytousWebsocketServer wssServer;

    @SuppressWarnings("AppEngineForbiddenCode")
    public static void main(final String[] args ) throws Exception {

        Misc.log(LOG, "====== Waytous server v1."+SERVER_BUILD+". Copyright (C) Edeqa. http://www.edeqa.com ======");
        OPTIONS = new Options(args);

        Common.getInstance().setDataProcessor(new DataProcessorFirebase());

        if(!Common.getInstance().getDataProcessor().isServerMode()){
            throw new RuntimeException("\n\nThis configuration can not be runned in stand-alone server mode. Set the installation type in build.gradle with the following property:\n\tdef installationType = 'standalone-server'\n");
        }

        Misc.log(LOG,"Server web root directory: "+new File(OPTIONS.getWebRootDirectory()).getCanonicalPath());

        String storePassword = OPTIONS.getSSLCertificatePassword();

        KeyStore keyStore = KeyStore.getInstance("JKS");
        File kf = new File(OPTIONS.getKeystoreFilename());

        if(OPTIONS.isDebugMode()) {
            Misc.log(LOG, "Keystore file: " + kf.getCanonicalPath());
        }
        keyStore.load(new FileInputStream(kf), storePassword.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509"/*KeyManagerFactory.getDefaultAlgorithm()*/);
        kmf.init(keyStore, storePassword.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509"/*KeyManagerFactory.getDefaultAlgorithm()*/);
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        HttpServer server = HttpServer.create();
        try {
            server.bind(new InetSocketAddress(OPTIONS.getHttpPort()), 0);
        } catch(BindException e) {
            Misc.err(LOG, "detects port in use: " + OPTIONS.getHttpPort() + ", server exits.");
            System.exit(1);
        }

        RedirectHandler redirectServer = new RedirectHandler();
        MainServletHandler mainServer = new MainServletHandler();
        RestServletHandler restServer = new RestServletHandler();
        TrackingServletHandler trackingServer = new TrackingServletHandler();
        AdminServletHandler adminServer = new AdminServletHandler();

        HttpsServer sslServer = HttpsServer.create();
        try {
            sslServer.bind(new InetSocketAddress(OPTIONS.getHttpsPort()), 0);
        } catch(BindException e) {
            Misc.err(LOG, "detects secured port in use:", OPTIONS.getHttpsPort() + ", server exits.");
            System.exit(1);
        }
        HttpsServer sslAdminServer = HttpsServer.create();
        try {
            sslAdminServer.bind(new InetSocketAddress(OPTIONS.getHttpsAdminPort()), 0);
        } catch(BindException e) {
            Misc.err(LOG, "detects admin port in use:", OPTIONS.getHttpsAdminPort() + ", server exits.");
            System.exit(1);
        }

 /*           SSLContext sslContext = SSLContext.getInstance("TLS");

            // initialise the keystore
            char[] password = OPTIONS.getSSLCertificatePassword().toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream(OPTIONS.getKeystoreFilename());
            ks.load(fis, password);

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
*/
        sslServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                try {
                    // initialise the SSL context
                    SSLContext context = SSLContext.getDefault();
                    SSLEngine engine = context.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    // get the default parameters
                    SSLParameters defaultSSLParameters = context.getDefaultSSLParameters();
                    params.setSSLParameters(defaultSSLParameters);

                } catch (Exception ex) {
                    Misc.log(LOG, "is failing to configure SSL server");
                }
            }
        });

        sslAdminServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                try {
                    // initialise the SSL context
                    SSLContext context = SSLContext.getDefault();
                    SSLEngine engine = context.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    // get the default parameters
                    SSLParameters defaultSSLParameters = context.getDefaultSSLParameters();
                    params.setSSLParameters(defaultSSLParameters);

                } catch (Exception ex) {
                    Misc.log(LOG, "is failing to configure admin SSL server");
                }
            }
        });

        server.createContext("/", redirectServer);
        Misc.log(LOG, "starting", RedirectHandler.class.getSimpleName(), "on HTTP:", OPTIONS.getHttpPort(), (OPTIONS.getHttpPort() == OPTIONS.getHttpPortMasked() ? "(masked by "+ OPTIONS.getHttpPortMasked() +")" : ""), "[/]");

        sslServer.createContext("/", mainServer);
        Misc.log(LOG, "starting", MainServletHandler.class.getSimpleName(), "on HTTPS:", OPTIONS.getHttpsPort(), (OPTIONS.getHttpsPort() == OPTIONS.getHttpsPortMasked() ? "(masked by "+ OPTIONS.getHttpsPortMasked() +")" : ""), "[/, /*]");

        sslServer.createContext("/track/", trackingServer);
        sslServer.createContext("/track2/", trackingServer);
        sslServer.createContext("/group/", trackingServer);
        Misc.log(LOG, "starting", TrackingServletHandler.class.getSimpleName(), "on HTTPS:", OPTIONS.getHttpsPort(), (OPTIONS.getHttpsPort() == OPTIONS.getHttpsPortMasked() ? "(masked by "+ OPTIONS.getHttpsPortMasked() +")" : ""), "[/track/, /group/]");

        sslServer.createContext("/rest/", restServer);
        Misc.log(LOG, "starting", RestServletHandler.class.getSimpleName(), "on HTTPS:", OPTIONS.getHttpsPort(), (OPTIONS.getHttpsPort() == OPTIONS.getHttpsPortMasked() ? "(masked by "+ OPTIONS.getHttpsPortMasked() +")" : ""), "[/rest/]");

        sslAdminServer.createContext("/rest/", restServer);
        sslAdminServer.createContext("/", adminServer).setAuthenticator(new DigestAuthenticator("waytous"));
        sslAdminServer.createContext("/admin/logout", adminServer);
        Misc.log(LOG, "starting", AdminServletHandler.class.getSimpleName(), "on HTTPS:", OPTIONS.getHttpsAdminPort(), "[/]");

        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        sslServer.setExecutor(executor);
        sslAdminServer.setExecutor(executor);

        server.start();
        sslServer.start();
        sslAdminServer.start();

        Misc.log(LOG, "handles web link", "http://" + InetAddress.getLocalHost().getHostAddress() + Common.getWrappedHttpPort());
        Misc.log(LOG, "handles track link", "http://" + InetAddress.getLocalHost().getHostAddress() + Common.getWrappedHttpPort() + "/track/");
        Misc.log(LOG, "handles admin link", "https://" + InetAddress.getLocalHost().getHostAddress() + ":" + OPTIONS.getHttpsAdminPort() + "/admin/");

        /*
         * Websocket part
         */
        wsServer = new WaytousWebsocketServer(OPTIONS.getWsPortFirebase());
        wssServer = new WaytousWebsocketServer(OPTIONS.getWssPortFirebase());


        DefaultSSLWebSocketServerFactory socket = new DefaultSSLWebSocketServerFactory(sslContext);
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
                    e.printStackTrace();
                }
            }
        }.start();
    }
}