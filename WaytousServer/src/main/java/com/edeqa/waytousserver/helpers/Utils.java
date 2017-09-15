package com.edeqa.waytousserver.helpers;

import com.edeqa.waytous.Mime;
import com.edeqa.waytous.interfaces.Runnable2;
import com.edeqa.waytous.interfaces.Runnable3;
import com.edeqa.waytous.interfaces.Runnable4;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;

/**
 * Created 10/8/16.
 */

public class Utils {


    public static final int DIGEST_METHOD_MD2 = 2;
    public static final int DIGEST_METHOD_MD5 = 5;
    public static final int DIGEST_METHOD_SHA1 = 1;
    public static final int DIGEST_METHOD_SHA256 = 256;
    public static final int DIGEST_METHOD_SHA512 = 512;

    public static String getEncryptedHash(String str) {
        return getEncryptedHash(str, 5);
    }

    public static String getEncryptedHash(String str, int type) {
        String sType;
        switch (type) {
            case 1:
                sType = "SHA-1";
                break;
            case 2:
                sType = "MD2";
                break;
            case 5:
                sType = "MD5";
                break;
            case 256:
                sType = "SHA-256";
                break;
            case 512:
                sType = "SHA-512";
                break;
            default:
                sType = "SHA-512";
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(sType);
            messageDigest.update(str.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes) {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getUnique() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(48, random).toString(32).toUpperCase();
    }

    public static void pause(int i) {
        try {
            Thread.sleep(i*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int selectColor(int number) {
        Random randomGenerator = new Random();
        int red = randomGenerator.nextInt(256);
        int green = randomGenerator.nextInt(256);
        int blue = randomGenerator.nextInt(256);

        return getRGB(red,green,blue);

//        int color = colors.get(number).getRGB();
//        return color;
    }

    public static int getRGB(int red, int green, int blue) {
        red = (red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        green = (green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        blue = blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | red | green | blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    public static String join(String conjunction, List<String> list) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first)
                first = false;
            else
                sb.append(conjunction);
            sb.append(item);
        }
        return sb.toString();
    }


    public static String getUrl(String url, String post, String urlCharset) throws IOException {

        if(urlCharset == null) urlCharset = "UTF-8";

        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();


        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.12) Gecko/20080201 Firefox");
//        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
        con.setRequestProperty(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

//        String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

        // Send post request
        con.setDoOutput(true);

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
        outputStreamWriter.write(URLEncoder.encode(post,urlCharset));
        outputStreamWriter.flush();
        outputStreamWriter.close();

        int responseCode = con.getResponseCode();
//        int responseCode = con.getResponseCode();
//        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + post);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        return response.toString();
    }

    public static String getUrl(String url, String urlCharset) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        InputStream in;
        URLConnection feedUrl;
        feedUrl = new URL(url).openConnection();
        feedUrl.setConnectTimeout(5000);
        feedUrl.setRequestProperty(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.12) Gecko/20080201 Firefox");
        feedUrl.setRequestProperty(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        in = feedUrl.getInputStream();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(in, urlCharset))) {
            while ((line = reader.readLine()) != null) {
                sb.append(new String(line.getBytes("UTF-8"))).append("\n");
            }
        }
        in.close();

        return sb.toString();
    }

    public static final Runnable2<RequestWrapper,JSONObject> sendResultJson = new Runnable2<RequestWrapper,JSONObject>() {
        @Override
        public void call(RequestWrapper requestWrapper, JSONObject json) {
            sendResult.call(requestWrapper, 200, Mime.APPLICATION_JSON, json.toString().getBytes());
        }
    };

    public static final Runnable3<RequestWrapper,Integer,JSONObject> sendError = new Runnable3<RequestWrapper,Integer,JSONObject>() {
        @Override
        public void call(RequestWrapper requestWrapper, Integer code, JSONObject json) {
            sendResult.call(requestWrapper, code, Mime.APPLICATION_JSON, json.toString().getBytes());
        }
    };

    public static final Runnable4<RequestWrapper,Integer,String,byte[]> sendResult = new Runnable4<RequestWrapper,Integer,String,byte[]>() {
        @Override
        public void call(RequestWrapper requestWrapper, Integer code, String contentType, byte[] bytes) {
            try {
                requestWrapper.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                if(contentType != null) requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
                requestWrapper.setHeader(HttpHeaders.SERVER, "Waytous/" + SERVER_BUILD);
                requestWrapper.setHeader(HttpHeaders.DATE, new Date().toString());
                requestWrapper.sendResponseHeaders(code, bytes.length);

                OutputStream os = requestWrapper.getResponseBody();
                os.write(bytes);
                os.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    };

}
