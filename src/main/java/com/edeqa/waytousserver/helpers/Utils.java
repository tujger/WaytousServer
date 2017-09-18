package com.edeqa.waytousserver.helpers;

import com.edeqa.helpers.Mime;
import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.helpers.interfaces.Runnable3;
import com.edeqa.helpers.interfaces.Runnable4;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.OutputStream;
import java.util.Date;
import java.util.Random;

import static com.edeqa.waytousserver.helpers.Common.SERVER_BUILD;

/**
 * Created 10/8/16.
 */

public class Utils {


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
