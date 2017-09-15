package com.edeqa.waytousserver;

import com.edeqa.waytous.SensitiveData;
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

import static com.edeqa.waytous.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;


/**
 * Created 10/2/16.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class WaytousServer {

    private static final String LOG = "Server";
    private static MyWsServer wsServer;
    private static MyWsServer wssServer;

    public static void main(final String[] args ) throws Exception {

        Common.log(LOG, "====== Waytous server v1."+SERVER_BUILD+". Copyright (C) Edeqa. http://www.edeqa.com ======");
        SENSITIVE = new SensitiveData(args);

        Common.getInstance().setDataProcessor(new DataProcessorFirebaseV1());

        if(!Common.getInstance().getDataProcessor(DataProcessorFirebaseV1.VERSION).isServerMode()){
            throw new RuntimeException("\n\nThis configuration can not be runned in stand-alone server mode. Set the installation type in build.gradle with the following property:\n\tdef installationType = 'standalone-server'\n");
        }

        wsServer = new MyWsServer(SENSITIVE.getWsPortFirebase());
        wssServer = new MyWsServer(SENSITIVE.getWssPortFirebase());

        Common.log(LOG,"Server web root directory: "+new File(SENSITIVE.getWebRootDirectory()).getCanonicalPath());

        String storePassword = SENSITIVE.getSSLCertificatePassword();

        KeyStore keyStore = KeyStore.getInstance("JKS");
        File kf = new File(SENSITIVE.getKeystoreFilename());

        Common.log(LOG, "Keystore file: " + kf.getCanonicalPath());
        keyStore.load(new FileInputStream(kf), storePassword.toCharArray());

        Common.log(LOG, "Server \t\t\t\t| Port \t| Path");
        Common.log(LOG, "----------------------------------------------");

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509"/*KeyManagerFactory.getDefaultAlgorithm()*/);
        kmf.init(keyStore, storePassword.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509"/*KeyManagerFactory.getDefaultAlgorithm()*/);
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        DefaultSSLWebSocketServerFactory socket = new DefaultSSLWebSocketServerFactory(sslContext);
        wssServer.setWebSocketFactory(socket);

        new Thread() {
            public void run() {
                try {
                    WebSocketImpl.DEBUG = false;
                    Common.log(LOG, "WS FB\t\t\t\t| " + SENSITIVE.getWsPortFirebase() + "\t|");
                    wsServer.start();
                    Common.log(LOG, "WSS FB\t\t\t\t| " + SENSITIVE.getWssPortFirebase() + "\t|");
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

        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(SENSITIVE.getHttpPort()), 0);

        RedirectHandler redirectServer = new RedirectHandler();
        Common.log(LOG, "Redirect HTTP\t\t| " + SENSITIVE.getHttpPort() + "\t| " + "/" + (SENSITIVE.getHttpPort() == SENSITIVE.getHttpPortMasked() ? " (masked by "+SENSITIVE.getHttpPortMasked() +")" : ""));
        server.createContext("/", redirectServer);

        MainServletHandler mainServer = new MainServletHandler();
        RestServletHandler restServer = new RestServletHandler();
        TrackingServletHandler trackingServer = new TrackingServletHandler();
        AdminServletHandler adminServer = new AdminServletHandler();

        HttpsServer sslServer = HttpsServer.create(new InetSocketAddress(SENSITIVE.getHttpsPort()), 0);
        HttpsServer sslAdminServer = HttpsServer.create(new InetSocketAddress(SENSITIVE.getHttpsAdminPort()), 0);

 /*           SSLContext sslContext = SSLContext.getInstance("TLS");

            // initialise the keystore
            char[] password = SENSITIVE.getSSLCertificatePassword().toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream(SENSITIVE.getKeystoreFilename());
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
                    Common.log(LOG,"Failed to configure SSL server");
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
                    Common.log(LOG,"Failed to configure admin SSL server");
                }
            }
        });

        sslServer.createContext("/", mainServer);
        Common.log(LOG, "Main HTTPS\t\t\t| " + SENSITIVE.getHttpsPort() + "\t| /, /*" + (SENSITIVE.getHttpsPort() == SENSITIVE.getHttpsPortMasked() ? " (masked by "+SENSITIVE.getHttpsPortMasked() +")" : ""));

        sslServer.createContext("/track/", trackingServer);
        Common.log(LOG, "Tracking HTTPS\t\t| " + SENSITIVE.getHttpsPort() + "\t| /track/" + (SENSITIVE.getHttpsPort() == SENSITIVE.getHttpsPortMasked() ? " (masked by "+SENSITIVE.getHttpsPortMasked() +")" : ""));

        sslServer.createContext("/group/", trackingServer);
        Common.log(LOG, "Tracking HTTPS\t\t| " + SENSITIVE.getHttpsPort() + "\t| /group/" + (SENSITIVE.getHttpsPort() == SENSITIVE.getHttpsPortMasked() ? " (masked by "+SENSITIVE.getHttpsPortMasked() +")" : ""));

        sslServer.createContext("/rest/", restServer);
        Common.log(LOG, "Rest HTTPS\t\t\t| " + SENSITIVE.getHttpsPort() + "\t| /rest/" + (SENSITIVE.getHttpsPort() == SENSITIVE.getHttpsPortMasked() ? " (masked by "+SENSITIVE.getHttpsPortMasked() +")" : ""));

        sslAdminServer.createContext("/rest/", restServer);
        sslAdminServer.createContext("/", adminServer).setAuthenticator(new DigestAuthenticator("waytous"));
        sslAdminServer.createContext("/admin/logout", adminServer);
        Common.log(LOG, "Admin HTTPS\t\t\t| " + SENSITIVE.getHttpsAdminPort() + "\t| " + "/");

//        sslAdminServer.createContext("/", mainServer);
//        Common.log(LOG, "Main HTTPS\t\t\t| " + SENSITIVE.getHttpsAdminPort() + "\t| /, /*");

        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        sslServer.setExecutor(executor);
        sslAdminServer.setExecutor(executor);

        server.start();
        sslServer.start();
        sslAdminServer.start();

        Common.log("Web\t\t", "http://" + InetAddress.getLocalHost().getHostAddress() + Common.getWrappedHttpPort());
        Common.log("Track\t", "http://" + InetAddress.getLocalHost().getHostAddress() + Common.getWrappedHttpPort() + "/track/");
        Common.log("Admin\t", "https://" + InetAddress.getLocalHost().getHostAddress() + ":" + SENSITIVE.getHttpsAdminPort() + "/admin/");

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