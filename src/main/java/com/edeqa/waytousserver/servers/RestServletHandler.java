package com.edeqa.waytousserver.servers;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Rest;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.HttpDPConnection;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;

import static com.edeqa.waytous.Constants.OPTIONS;


/**
 * Created 1/19/17.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class RestServletHandler extends AbstractServletHandler {

    private static final String LOG = "RSH";
    private V1 v1;

    public RestServletHandler() {
        super();
        v1 = new V1();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        initDataProcessor();
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public void perform(RequestWrapper requestWrapper) throws IOException {

        try {
            URI uri = requestWrapper.getRequestURI();
            String host = null, referer = null;
            try {
                host = requestWrapper.getRequestHeader(HttpHeaders.HOST).get(0);
                host = host.split(":")[0];
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                referer = requestWrapper.getRequestHeaders().get(HttpHeaders.REFERER).get(0);
                if(referer.contains(host)) referer = null;
            } catch(Exception e){
//                e.printStackTrace();
            }
            Misc.log(LOG, host + uri.getPath(), requestWrapper.getRemoteAddress() + (referer != null ? ", referer: " + referer : ""));

//        List<String> parts = Arrays.asList(uri.getPath().split("/"));
            JSONObject json = new JSONObject();
            boolean printRes;

//        switch(exchange.getRequestMethod()) {
//            case HttpMethods.GET:
            json.put(Rest.STATUS, "success");

            switch (uri.getPath()) {
                case "/rest/v1/getVersion":
                    printRes = v1.getVersion(json);
                    break;
                case "/rest/v1/getSounds":
                    printRes = v1.getSounds(json);
                    break;
                case "/rest/v1/getContent":
                    printRes = v1.getContent(json, requestWrapper);
                    break;
                case "/rest/v1/getLocales":
                    printRes = v1.getLocales(json, requestWrapper);
                    break;
                case "/rest/v1/join":
                    printRes = v1.join(json, requestWrapper);
                    break;
                case "/rest/v1/tosAgreement":
                    printRes = v1.tosAgreement(json, requestWrapper);
                    break;
                default:
                    printRes = noAction(json);
                    break;
            }
//                break;
//            case HttpMethods.PUT:
//                break;
//            case HttpMethods.POST:
//                break;
//        }

            if (printRes) requestWrapper.sendResult(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean noAction(JSONObject json) {
        Misc.log(LOG, "perform:noAction", json);
        json.put(Rest.STATUS, "error");
        json.put(Rest.REASON, "Action not supported");
        json.put(Rest.MESSAGE, "Action not supported");
        return true;
    }


    abstract class RestComponent {
        abstract boolean getVersion(JSONObject json);
    }


    public class V1 extends RestComponent {
        @Override
        boolean getVersion(JSONObject json) {
            json.put("version", Common.SERVER_BUILD);
            return true;
        }

        boolean getSounds(JSONObject json) {
            File dir = new File(OPTIONS.getWebRootDirectory() + "/sounds");
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".mp3");
                }
            });
            ArrayList<String> list = new ArrayList<>();
            list.add("none.mp3");
            if (files != null) {
                for (File file : files) {
                    if (!list.contains(file.getName())) list.add(file.getName());
                }
            }
            json.put("files", list);
            return true;
        }

        boolean join(JSONObject json, RequestWrapper requestWrapper) {
            try {
                InputStreamReader isr = new InputStreamReader(requestWrapper.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String body = br.readLine();
                br.close();

                Misc.log("Rest", requestWrapper.getRemoteAddress(), "joinV1:", body);
                Common.getInstance().getDataProcessor(requestWrapper.getRequestURI().getPath().split("/")[3]).onMessage(new HttpDPConnection(requestWrapper), body);
            } catch (Exception e) {
                e.printStackTrace();
                json.put(Rest.STATUS, "error");
                json.put(Rest.REASON, "Action failed");
                json.put(Rest.MESSAGE, e.getMessage());
                requestWrapper.sendResult(json);
            }
            return false;
        }

        boolean getLocales(final JSONObject json, final RequestWrapper requestWrapper) {
            File dir = new File(OPTIONS.getWebRootDirectory() + "/resources");
            try {
                File[] files = dir.listFiles(/*new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return dir.isDirectory();
                }
            }*/);
                Map<String, String> map = new LinkedHashMap<>();
                map.put("en", "En");

                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            String name = file.getName().toLowerCase();
                            name = name.substring(0, 1).toUpperCase() + name.substring(1);
                            map.put(file.getName(), name);
                        }
                    }
                }
                json.put("locales", map);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }

        boolean getContent(final JSONObject json, final RequestWrapper requestWrapper) {

        /*requestWrapper.processBody(new Runnable1<StringBuilder>() {
            @Override
            public void call(StringBuilder buf) {
                try {
                    JSONObject options = new JSONObject(buf.toString());
                    Common.log(LOG, "Content requested: " + options);

                    ArrayList<File> files = new ArrayList<>();

                    if (options.has("type")) {
                        if (options.has("locale") && options.has("resource")) {
                            files.add(new File(OPTIONS.getWebRootDirectory() + "/" + options.getString("type") + "/" + options.getString("locale") + "/" + options.getString("resource")));
                        }
                        if (options.has("resource")) {
                            files.add(new File(OPTIONS.getWebRootDirectory() + "/" + options.getString("type") + "/" + options.getString("resource")));
                        }
                    } else {
                        if (options.has("locale") && options.has("resource")) {
                            files.add(new File(OPTIONS.getWebRootDirectory() + "/content/" + options.getString("locale") + "/" + options.getString("resource")));
                        }
                        if (options.has("resource")) {
                            files.add(new File(OPTIONS.getWebRootDirectory() + "/content/" + options.getString("resource")));
                        }
                    }

                    boolean exists = false;
                    File file = null;
                    for (File f : files) {
                        if (f.getCanonicalPath().equals(f.getAbsolutePath()) && f.exists()) {
                            file = f;
                            exists = true;
                            break;
                        }
                    }

                    if (exists) {
                        Common.log(LOG, "File found: " + file.toString());
                        boolean gzip = true;
                        requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, Constants.MIME.TEXT_PLAIN);
                        requestWrapper.setHeader(HttpHeaders.SERVER, "Waytous/" + Constants.SERVER_BUILD);
                        requestWrapper.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

                        if (gzip) {
                            requestWrapper.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                        } else {
                            requestWrapper.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));
                        }

                        requestWrapper.sendResponseHeaders(200, 0);

                        OutputStream os;
                        if (gzip) {
                            os = new BufferedOutputStream(new GZIPOutputStream(requestWrapper.getResponseBody()));
                        } else {
                            os = requestWrapper.getResponseBody();
                        }

                        FileInputStream fs = new FileInputStream(file);
                        final byte[] buffer = new byte[0x10000];

                        int count = 0;
                        while ((count = fs.read(buffer)) >= 0) {
                            os.write(buffer, 0, count);
                        }
                        fs.close();
                        os.close();
                    } else {
                        Common.log(LOG, "Content not found.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Runnable1<Exception>() {
            @Override
            public void call(Exception arg) {

            }
        });
        if(true) return false;
*/

            requestWrapper.processBody(new Runnable1<StringBuilder>() {
                @Override
                public void call(StringBuilder body) {
                    JSONObject options = new JSONObject(body.toString());
                    Misc.log(LOG, "Content requested: " + options);

                    ArrayList<File> files = new ArrayList<>();

                    if (options.has("type")) {
                        if (options.has("locale") && options.has("resource")) {
                            files.add(new File(OPTIONS.getWebRootDirectory() + "/" + options.getString("type") + "/" + options.getString("locale") + "/" + options.getString("resource")));
                        }
                        if (options.has("resource")) {
                            files.add(new File(OPTIONS.getWebRootDirectory() + "/" + options.getString("type") + "/en/" + options.getString("resource")));
                        }
                    } else {
                        if (options.has("locale") && options.has("resource")) {
                            files.add(new File(OPTIONS.getWebRootDirectory() + "/content/" + options.getString("locale") + "/" + options.getString("resource")));
                        }
                        if (options.has("resource")) {
                            files.add(new File(OPTIONS.getWebRootDirectory() + "/content/en/" + options.getString("resource")));
                        }
                    }

                    boolean exists = false;
                    File file = null;
                    for (File f : files) {
//                    Common.log(LOG,"Content: " + f.getCanonicalPath() +":"+f.getAbsolutePath());
//                    if (f.getCanonicalPath().equals(f.getAbsolutePath()) && f.exists()) {
                        if (f.exists()) {
                            file = f;
                            exists = true;
                            break;
                        }
                    }

                    if (exists) {
                        String path = file.getAbsolutePath().replace(OPTIONS.getWebRootDirectory(), "").replaceAll("\\\\", "/");
                        Misc.log(LOG, "->", path);
                        try {
                            requestWrapper.sendRedirect(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Misc.log(LOG, "Content not found: " + files);
                    }
                }
            }, new Runnable1<Exception>() {
                @Override
                public void call(Exception arg) {
                    Misc.err(LOG, "getContent:", arg);
                    json.put(Rest.STATUS, "error");
                    json.put(Rest.REASON, "Incorrect request");
                    json.put(Rest.MESSAGE, arg.getMessage());
                    requestWrapper.sendError(413, json);
                }
            });
            return false;

            /*try {
                StringBuilder buf = new StringBuilder();
                InputStream is = requestWrapper.getRequestBody();
                int b;
                while ((b = is.read()) != -1) {
                    buf.append((char) b);
                }
                is.close();

                Common.log(LOG,"Content requested: " + buf.toString());
                JSONObject options = new JSONObject(buf.toString());
                Common.log(LOG,"Content requested: " + options);

                ArrayList<File> files = new ArrayList<>();

                if(options.has("type")) {
                    if (options.has("locale") && options.has("resource")) {
                        files.add(new File(OPTIONS.getWebRootDirectory() + "/" + options.getString("type") + "/" + options.getString("locale") + "/" + options.getString("resource")));
                    }
                    if (options.has("resource")) {
                        files.add(new File(OPTIONS.getWebRootDirectory() + "/" + options.getString("type") + "/en/" + options.getString("resource")));
                    }
                } else {
                    if (options.has("locale") && options.has("resource")) {
                        files.add(new File(OPTIONS.getWebRootDirectory() + "/content/" + options.getString("locale") + "/" + options.getString("resource")));
                    }
                    if (options.has("resource")) {
                        files.add(new File(OPTIONS.getWebRootDirectory() + "/content/en/" + options.getString("resource")));
                    }
                }

                boolean exists = false;
                File file = null;
                for (File f : files) {
//                    Common.log(LOG,"Content: " + f.getCanonicalPath() +":"+f.getAbsolutePath());
//                    if (f.getCanonicalPath().equals(f.getAbsolutePath()) && f.exists()) {
                    if (f.exists()) {
                        file = f;
                        exists = true;
                        break;
                    }
                }

                if(exists) {
                    String path = file.getAbsolutePath().replace(OPTIONS.getWebRootDirectory(), "");
//                    path = "https://" + OPTIONS.getServerHost() + Common.getWrappedHttpsPort() + path;
                    Common.log(LOG,"->", path);
                    requestWrapper.sendRedirect(path);
                    return false;
                } else {
                    Common.log(LOG,"Content not found: " + files);
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }*/

        }


        public boolean tosAgreement(JSONObject json, RequestWrapper requestWrapper) {
            try {
                InputStreamReader isr = new InputStreamReader(requestWrapper.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String body = br.readLine();
                br.close();

                Misc.log("Rest", requestWrapper.getRemoteAddress(), "tosAgreement:", body);
                Common.getInstance().getDataProcessor(requestWrapper.getRequestURI().getPath().split("/")[3]).onMessage(new HttpDPConnection(requestWrapper), body);
            } catch (Exception e) {
                e.printStackTrace();
                json.put(Rest.STATUS, "error");
                json.put(Rest.REASON, "Action failed");
                json.put(Rest.MESSAGE, e.getMessage());
                requestWrapper.sendResult(json);
            }
            return false;
        }
    }
}