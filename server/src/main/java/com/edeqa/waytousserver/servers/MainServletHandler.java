package com.edeqa.waytousserver.servers;

import com.edeqa.waytous.Mime;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.google.common.net.HttpHeaders;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.edeqa.waytous.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;


/**
 * Created 1/19/17.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class MainServletHandler extends AbstractServletHandler {

    private Map<String, String> substitutions;


    private void initSubstitutions(RequestWrapper requestWrapper) {
        if(substitutions == null) {
            substitutions = new LinkedHashMap<>();
            substitutions.put("\\$\\{SERVER_BUILD\\}", "" + SERVER_BUILD);
            substitutions.put("\\$\\{APP_NAME\\}", SENSITIVE.getAppName() + (SENSITIVE.isDebugMode() ? " &beta;" : ""));
            substitutions.put("\\$\\{SUPPORT_EMAIL\\}", SENSITIVE.getSupportEmail());
            substitutions.put("\\$\\{WEB_PAGE\\}", SENSITIVE.getAppLink());
            substitutions.put("\\$\\{REFERER\\}", "https://" + SENSITIVE.getServerHost());

            try {
                //noinspection LoopStatementThatDoesntLoop
                for (String x : requestWrapper.getRequestHeader(HttpHeaders.REFERER)) {
                    substitutions.put("\\$\\{REFERER\\}", x);
                    break;
                }
            }catch(Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void perform(RequestWrapper requestWrapper) {
        initSubstitutions(requestWrapper);

        try {
            String ifModifiedSince = null;

            URI uri = requestWrapper.getRequestURI();
            if ("/_ah/start".equals(uri.getPath())) {
                requestWrapper.sendResponseHeaders(200,0);
                requestWrapper.getOutputStream().close();
                return;
            } else if("/_ah/stop".equals(uri.getPath())) {
                requestWrapper.sendResponseHeaders(200,0);
                requestWrapper.getOutputStream().close();
                return;
            }

            File root = new File(SENSITIVE.getWebRootDirectory());
            File file = new File(root + uri.getPath()).getCanonicalFile();
            int resultCode = 200;

            Common.log("Main", uri.getPath(), "[" + (file.exists() ? file.length() + " byte(s)" : "not found") + "]");

            String etag = "W/1976-" + ("" + file.lastModified()).hashCode();


            String path = uri.getPath().toLowerCase();
            if (!file.getCanonicalPath().startsWith(root.getCanonicalPath())) {
                // Suspected path traversal attack: reject with 403 error.
                resultCode = 403;
                file = new File(root + "/403.html");
//                Utils.sendResult.call(exchange, 403, Constants.MIME.TEXT_PLAIN, "403 Forbidden\n".getBytes());
            } else if (file.isDirectory()) {
                file = new File(file.getCanonicalPath() + "/index.html");
//            } else if (etag.equals(ifModifiedSince)) {
//                resultCode = 304;
//                file = new File(root + "/304.html");
//                Utils.sendResult.call(exchange, 304, null, "304 Not Modified\n".getBytes());
            } else if (!uri.getPath().endsWith("/") && !file.exists()) {
                requestWrapper.sendRedirect(uri.getPath() + "/");
                return;
            } else if (!file.isFile() || path.startsWith("/WEB-INF") || path.startsWith("/META-INF") || path.startsWith("/.idea")) {
                // Object does not exist or is not a file: reject with 404 error.
                resultCode = 404;
                file = new File(root + "/404.html");
            }
            {
                // Object exists and it is a file: accept with response code 200.

                boolean gzip = false;

                for (String s : requestWrapper.getRequestHeader(HttpHeaders.ACCEPT_ENCODING)) {
                    if(s.toLowerCase().contains("gzip")) {
                        gzip = true;
                        break;
                    }
                }

                boolean text = false;
                String type = Mime.APPLICATION_OCTET_STREAM;
                JSONArray types = SENSITIVE.getTypes();
                types.put(new JSONObject("{\"type\":\"\",\"mime\":\"application/unknown\"}"));
                JSONObject json = null;
                for (int i = 0; i < types.length(); i++) {
                    if(types.isNull(i)) continue;
                    json = types.getJSONObject(i);
                    if (json.has("name") && file.getName().toLowerCase().equals(json.getString("name"))) {
                        type = json.getString("mime");
                        break;
                    } else if (json.has("type") && file.getName().toLowerCase().endsWith("." + json.getString("type"))) {
                        type = json.getString("mime");
                        break;
                    }
                }

                assert json != null;
                if(type.startsWith("text") || (json.has("text") && json.getBoolean("text"))) text = true;
                if (json.has("gzip") && !json.getBoolean("gzip")) gzip = false;
                if(!SENSITIVE.isGzip()) gzip = false;

                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
                String lastModified = dateFormat.format(file.lastModified());

                requestWrapper.setHeader(HttpHeaders.LAST_MODIFIED, lastModified);
                requestWrapper.setHeader(HttpHeaders.CACHE_CONTROL, SENSITIVE.isDebugMode() ? "max-age=10" : "max-age=60");
                requestWrapper.setHeader(HttpHeaders.ETAG, etag);
                requestWrapper.setHeader(HttpHeaders.SERVER, "Waytous/" + SERVER_BUILD);
                requestWrapper.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

                // FIXME - need to check by https://observatory.mozilla.org/analyze.html?host=waytous.net
                requestWrapper.setHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS, "nosniff");
                requestWrapper.setHeader(HttpHeaders.CONTENT_SECURITY_POLICY, "frame-ancestors 'self'");
                requestWrapper.setHeader(HttpHeaders.X_FRAME_OPTIONS, "SAMEORIGIN");
                requestWrapper.setHeader(HttpHeaders.X_XSS_PROTECTION, "1; mode=block");
                requestWrapper.setHeader(HttpHeaders.STRICT_TRANSPORT_SECURITY, "max-age=60; includeSubDomains; preload");
//                requestWrapper.setHeader(HttpHeaders.STRICT_TRANSPORT_SECURITY, "max-age=63072000; includeSubDomains; preload");

                // FIXME http://nibbler.silktide.com/en_US/reports/waytous.net
                // FIXME https://gtmetrix.com/reports/waytous.net/6i4B5kR2
                requestWrapper.setHeader(HttpHeaders.VARY, "Accept-Encoding");


                requestWrapper.setGzip(gzip);

                if (text) {
                    if(!type.toLowerCase().matches(";\\s*charset\\s*=")) {
                        type += "; charset=UTF-8";
                    }

                    FileReader reader = new FileReader(file);
                    int c;
                    StringBuilder fileContent = new StringBuilder();
                    while ((c = reader.read()) != -1) {
                        fileContent.append((char) c);
                    }
                    reader.close();

                    byte[] bytes = fileContent.toString().getBytes(); //Files.readAllBytes(file.toPath());
                    Charset charset = StandardCharsets.ISO_8859_1;
                    if(bytes[0] == -1 && bytes[1] == -2) charset = StandardCharsets.UTF_16;
                    else if(bytes[0] == -2 && bytes[1] == -1) charset = StandardCharsets.UTF_16;


                    String string = new String(bytes, charset);
                    for(Map.Entry<String,String> x: substitutions.entrySet()) {
                        string = string.replaceAll(x.getKey(), x.getValue());
                    }

                    requestWrapper.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                    requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, type);
                    if(!gzip) requestWrapper.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(string.length()));
                    requestWrapper.sendResponseHeaders(resultCode, 0);

                    try {
                        OutputStream os = requestWrapper.getResponseBody();
                        os.write(string.getBytes(charset));
                        os.close();
                    } catch(Exception e){
                        System.out.println("C:"+requestWrapper.getRequestURI());
                        e.printStackTrace();
                    }
                } else {
                    requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, type);
                    if(!gzip) requestWrapper.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));
                    requestWrapper.sendResponseHeaders(resultCode, 0);
                    OutputStream os = requestWrapper.getResponseBody();

                    FileInputStream fs = new FileInputStream(file);
                    final byte[] buffer = new byte[0x10000];
                    int count;
                    while ((count = fs.read(buffer)) >= 0) {
                        os.write(buffer, 0, count);
                    }
                    fs.close();
                    os.close();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
