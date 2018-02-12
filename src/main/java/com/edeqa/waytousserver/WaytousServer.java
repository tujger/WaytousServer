package com.edeqa.waytousserver;

import com.edeqa.helpers.Misc;
import com.edeqa.waytous.Options;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.DigestAuthenticator;
import com.edeqa.waytousserver.servers.AdminServletHandler;
import com.edeqa.waytousserver.servers.DataProcessorFirebaseV1;
import com.edeqa.waytousserver.servers.MainServletHandler;
import com.edeqa.waytousserver.servers.MyWsServer;
import com.edeqa.waytousserver.servers.RedirectHandler;
import com.edeqa.waytousserver.servers.RestServletHandler;
import com.edeqa.waytousserver.servers.TrackingServletHandler;
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
    private static MyWsServer wsServer;
    private static MyWsServer wssServer;

    @SuppressWarnings("AppEngineForbiddenCode")
    public static void main(final String[] args ) throws Exception {

        Misc.log(LOG, "====== Waytous server v1."+SERVER_BUILD+". Copyright (C) Edeqa. http://www.edeqa.com ======");
        OPTIONS = new Options(args);

        Common.getInstance().setDataProcessor(new DataProcessorFirebaseV1());

        if(!Common.getInstance().getDataProcessor(DataProcessorFirebaseV1.VERSION).isServerMode()){
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

        Misc.log(LOG, "Server \t\t\t\t| Port \t| Path");
        Misc.log(LOG, "----------------------------------------------");

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
            Misc.err(LOG, "Port in use: " + OPTIONS.getHttpPort() + ", server exits.");
            System.exit(1);
        }

        RedirectHandler redirectServer = new RedirectHandler();
        Misc.log(LOG, "Redirect HTTP\t\t| " + OPTIONS.getHttpPort() + "\t| " + "/" + (OPTIONS.getHttpPort() == OPTIONS.getHttpPortMasked() ? " (masked by "+ OPTIONS.getHttpPortMasked() +")" : ""));
        server.createContext("/", redirectServer);

        MainServletHandler mainServer = new MainServletHandler();
        RestServletHandler restServer = new RestServletHandler();
        TrackingServletHandler trackingServer = new TrackingServletHandler();
        AdminServletHandler adminServer = new AdminServletHandler();

        HttpsServer sslServer = HttpsServer.create();
        try {
            sslServer.bind(new InetSocketAddress(OPTIONS.getHttpsPort()), 0);
        } catch(BindException e) {
            Misc.err(LOG, "Secured port in use: " + OPTIONS.getHttpsPort() + ", server exits.");
            System.exit(1);
        }
        HttpsServer sslAdminServer = HttpsServer.create();
        try {
            sslAdminServer.bind(new InetSocketAddress(OPTIONS.getHttpsAdminPort()), 0);
        } catch(BindException e) {
            Misc.err(LOG, "Admin port in use: " + OPTIONS.getHttpsAdminPort() + ", server exits.");
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
                    Misc.log(LOG, "Failed to configure SSL server");
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
                    Misc.log(LOG,"Failed to configure admin SSL server");
                }
            }
        });

        sslServer.createContext("/", mainServer);
        Misc.log(LOG, "Main HTTPS\t\t\t| " + OPTIONS.getHttpsPort() + "\t| /, /*" + (OPTIONS.getHttpsPort() == OPTIONS.getHttpsPortMasked() ? " (masked by "+ OPTIONS.getHttpsPortMasked() +")" : ""));

        sslServer.createContext("/track/", trackingServer);
        Misc.log(LOG, "Tracking HTTPS\t\t| " + OPTIONS.getHttpsPort() + "\t| /track/" + (OPTIONS.getHttpsPort() == OPTIONS.getHttpsPortMasked() ? " (masked by "+ OPTIONS.getHttpsPortMasked() +")" : ""));

        sslServer.createContext("/group/", trackingServer);
        Misc.log(LOG, "Tracking HTTPS\t\t| " + OPTIONS.getHttpsPort() + "\t| /group/" + (OPTIONS.getHttpsPort() == OPTIONS.getHttpsPortMasked() ? " (masked by "+ OPTIONS.getHttpsPortMasked() +")" : ""));

        sslServer.createContext("/rest/", restServer);
        Misc.log(LOG, "Rest HTTPS\t\t\t| " + OPTIONS.getHttpsPort() + "\t| /rest/" + (OPTIONS.getHttpsPort() == OPTIONS.getHttpsPortMasked() ? " (masked by "+ OPTIONS.getHttpsPortMasked() +")" : ""));

        sslAdminServer.createContext("/rest/", restServer);
        sslAdminServer.createContext("/", adminServer).setAuthenticator(new DigestAuthenticator("waytous"));
        sslAdminServer.createContext("/admin/logout", adminServer);
        Misc.log(LOG, "Admin HTTPS\t\t\t| " + OPTIONS.getHttpsAdminPort() + "\t| " + "/");

//        sslAdminServer.createContext("/", mainServer);
//        Common.log(LOG, "Main HTTPS\t\t\t| " + OPTIONS.getHttpsAdminPort() + "\t| /, /*");

        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        sslServer.setExecutor(executor);
        sslAdminServer.setExecutor(executor);

        server.start();
        sslServer.start();
        sslAdminServer.start();


        /*
         * Websocket part
         */
        wsServer = new MyWsServer(OPTIONS.getWsPortFirebase());
        wssServer = new MyWsServer(OPTIONS.getWssPortFirebase());


        DefaultSSLWebSocketServerFactory socket = new DefaultSSLWebSocketServerFactory(sslContext);
        wssServer.setWebSocketFactory(socket);

        new Thread() {
            public void run() {
                try {
                    WebSocketImpl.DEBUG = false;
                    Misc.log(LOG, "WS FB\t\t\t\t| " + OPTIONS.getWsPortFirebase() + "\t|");
                    wsServer.start();
                    Misc.log(LOG, "WSS FB\t\t\t\t| " + OPTIONS.getWssPortFirebase() + "\t|");
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

        Misc.log("Web\t\t", "http://" + InetAddress.getLocalHost().getHostAddress() + Common.getWrappedHttpPort());
        Misc.log("Track\t", "http://" + InetAddress.getLocalHost().getHostAddress() + Common.getWrappedHttpPort() + "/track/");
        Misc.log("Admin\t", "https://" + InetAddress.getLocalHost().getHostAddress() + ":" + OPTIONS.getHttpsAdminPort() + "/admin/");

    }


    static class User {

        public String username;
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }


        @Override
        public String toString() {
            return "User{" +
                    "username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

}