package com.edeqa.waytousserver.helpers;

import com.edeqa.helpers.interfaces.Runnable1;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created 6/9/2017.
 */

public class RequestWrapper  {

    protected final static int MODE_SERVLET = 0;
    protected final static int MODE_EXCHANGE = 1;

    protected final static long MAX_BODY_LENGTH = 1024 * 1024;

    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;
    private String charset;

    private HttpExchange httpExchange;

    private int mode;
    private boolean gzip;

    @Override
    public String toString() {
        return "RequestWrapper{}";
    }

    public RequestWrapper() {
        this.gzip = false;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
        setMode(MODE_SERVLET);
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
        setMode(MODE_SERVLET);
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public void setHttpExchange(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        setMode(MODE_EXCHANGE);
    }

    public HttpExchange getHttpExchange() {
        return httpExchange;
    }

    public URI getRequestURI() {
        if(mode == MODE_SERVLET) {
            try {
                return new URI(httpServletRequest.getRequestURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return null;
            }
        } else if(mode == MODE_EXCHANGE) {
            return httpExchange.getRequestURI();
        }
        return null;
    }

    public void setHeader(String name, String value) {
        if(mode == MODE_SERVLET) {
            httpServletResponse.setHeader(name, value);
        } else if(mode == MODE_EXCHANGE) {
            httpExchange.getResponseHeaders().set(name, value);
        }
    }

    public void addHeader(String name, String value) {
        if(mode == MODE_SERVLET) {
            httpServletResponse.addHeader(name, value);
        } else if(mode == MODE_EXCHANGE) {
            httpExchange.getResponseHeaders().add(name, value);
        }
    }

    public void sendResponseHeaders(int code, int arg1) {
        if(mode == MODE_SERVLET) {
        } else if(mode == MODE_EXCHANGE) {
            if(isGzip()) {
                setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            }

            if(charset != null) {
                List<String> contentTypes = httpExchange.getResponseHeaders().get(HttpHeaders.CONTENT_TYPE);
                for (String contentType : contentTypes) {
                    if (!contentType.toLowerCase().contains("; charset=")) {
                        contentType = contentType + "; charset=" + charset;
                        httpExchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, contentType);
                    }
                }
            }

            try {
                httpExchange.sendResponseHeaders(code, arg1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendRedirect(String redirectLink) throws IOException {
        if(mode == MODE_SERVLET) {
            httpServletResponse.sendRedirect(redirectLink);
        } else if(mode == MODE_EXCHANGE) {
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.set(HttpHeaders.CONTENT_TYPE, "text/plain");
            responseHeaders.set(HttpHeaders.DATE, new Date().toString());
            responseHeaders.set(HttpHeaders.LOCATION, redirectLink);
            httpExchange.sendResponseHeaders(302, 0);
            httpExchange.close();
        }
    }

    public OutputStream getOutputStream() throws IOException {
        if(mode == MODE_SERVLET) {
            return httpServletResponse.getOutputStream();
        } else if(mode == MODE_EXCHANGE) {
            if(isGzip()) {
                return new BufferedOutputStream(new GZIPOutputStream(httpExchange.getResponseBody()));
            } else {
                return httpExchange.getResponseBody();
            }
        }
        return null;
    }

    public OutputStream getResponseBody() throws IOException {
        return getOutputStream();
    }

    public InputStream getInputStream() throws IOException {
        if(mode == MODE_SERVLET) {
            return httpServletRequest.getInputStream();
        } else if(mode == MODE_EXCHANGE) {
            return httpExchange.getRequestBody();
        }
        return null;
    }

    public InputStream getRequestBody() throws IOException {
        return getInputStream();
    }

    public void setCharacterEncoding(String charset) {
        if(mode == MODE_SERVLET) {
            httpServletResponse.setCharacterEncoding(charset);
        } else if(mode == MODE_EXCHANGE) {
            this.charset = charset;
        }
    }

    public PrintWriter getPrintWriter() throws IOException {
        if(mode == MODE_SERVLET) {
            return httpServletResponse.getWriter();
        } else if(mode == MODE_EXCHANGE) {
            return new PrintWriter(httpExchange.getResponseBody());
        }
        return null;
    }

    public InetSocketAddress getRemoteAddress() {
        if(mode == MODE_SERVLET) {
            return new InetSocketAddress(httpServletRequest.getRemoteAddr(), httpServletRequest.getRemotePort());
        } else if(mode == MODE_EXCHANGE) {
            return httpExchange.getRemoteAddress();
        }
        return null;
    }

    public Map<String, List<String>> getRequestHeaders() {
        if(mode == MODE_SERVLET) {
//            Headers implements Map<String, List<String>> {
            Map<String, List<String>> headers = new HashMap<>();
            String x;
            Enumeration<String> names = httpServletRequest.getHeaderNames();
            while(names.hasMoreElements()) {
                x = names.nextElement();
                Enumeration<String> h = httpServletRequest.getHeaders(x);
                headers.put(x, Collections.list(h) );
            }
            return headers;
        } else if(mode == MODE_EXCHANGE) {
            Map<String, List<String>> headers = new HashMap<>();
            Map.Entry<String, List<String>> entry;

            Iterator<Map.Entry<String, List<String>>> iter = httpExchange.getRequestHeaders().entrySet().iterator();
            while(iter.hasNext()) {
                entry = iter.next();
                headers.put(entry.getKey(), entry.getValue() );
            }
            return headers;
        }
        return null;
    }

    public List<String> getRequestHeader(String name) {
        if(mode == MODE_SERVLET) {
            return Collections.list(httpServletRequest.getHeaders(name));
        } else if(mode == MODE_EXCHANGE) {
            Headers headers = httpExchange.getRequestHeaders();
            if(headers.containsKey(name)) {
                return httpExchange.getRequestHeaders().get(name);
            } else {
                return Collections.emptyList();
            }
        }
        return null;
    }

    public String getRequestMethod() {
        if(mode == MODE_SERVLET) {
            return httpServletRequest.getMethod();
        } else if(mode == MODE_EXCHANGE) {
            return httpExchange.getRequestMethod();
        }
        return null;
    }

    public String getMethod() {
        return getRequestMethod();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public void processBody(Runnable1<StringBuilder> callback, Runnable1<Exception> fallback) {

        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = this.getRequestBody();
            int b;
            long count = 0;
            while ((b = is.read()) != -1) {
                if(count++ > MAX_BODY_LENGTH) {
                    fallback.call(new IllegalArgumentException("Body size is bigger than " + MAX_BODY_LENGTH + " byte(s)."));
                    return;
                }
                buf.append((char) b);
            }
            is.close();

            if(buf.length() > 0) {
                callback.call(buf);
            } else {
                fallback.call(new IllegalArgumentException("Empty body"));
            }

        } catch(Exception e) {
            e.printStackTrace();
            if(fallback != null) fallback.call(e);
        }

    }

}
