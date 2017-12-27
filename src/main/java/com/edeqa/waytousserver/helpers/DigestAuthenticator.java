package com.edeqa.waytousserver.helpers;

/**
 * Created 5/16/2017.
 */

import com.edeqa.helpers.Misc;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.edeqa.waytous.Constants.OPTIONS;


@SuppressWarnings("restriction")
public class DigestAuthenticator extends Authenticator {

    private static final byte COL = ':';

    private final Set<String> givenNonces = new HashSet<String>();
    private final String realm;

    private final SecureRandom random = new SecureRandom();

    public DigestAuthenticator(String realm) {
        this.realm = realm;
    }

    @Override
    public Result authenticate(HttpExchange httpExchange) {
        DigestContext context = getOrCreateContext(httpExchange);
        if (context.isAuthenticated()) {
            String auth = httpExchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if(auth == null || "Digest logout".equals(auth)) {
                Misc.log("DA", httpExchange.getRemoteAddress(), "Logout/" + context.getPrincipal().getName());

                httpExchange.setAttribute("digest-context", null);
                Headers responseHeaders = httpExchange.getResponseHeaders();
                responseHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Digest " + getChallenge(false));
                return new Authenticator.Retry(401);
            }
            return new Authenticator.Success(context.getPrincipal());
        }
        Headers requestHeaders = httpExchange.getRequestHeaders();
        if (!requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)) {
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Digest " + getChallenge(false));
            return new Authenticator.Retry(401);
        }

        String authorization = requestHeaders.getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization.startsWith("Basic ")) {
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Digest " + getChallenge(false));
            return new Authenticator.Retry(401);
        }

        if (!authorization.startsWith("Digest ")) {
            throw new RuntimeException("Invalid 'Authorization' header.");
        }
        String challenge = authorization.substring(7);
        Map<String, String> challengeParameters = parseDigestChallenge(challenge);

        HttpPrincipal principal = validateUser(httpExchange, challengeParameters);
        if (principal == null) {
            if (challengeParameters.containsKey("nonce")) {
                useNonce(challengeParameters.get("nonce"));
            }

            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Digest " + getChallenge(false));
            return new Authenticator.Retry(401);
        }
        if (useNonce(challengeParameters.get("nonce"))) {
            context.principal = principal;
            Misc.log("DA", httpExchange.getRemoteAddress(), "Login/" + principal.getName());
            return new Authenticator.Success(principal);
        }
        Headers responseHeaders = httpExchange.getResponseHeaders();
        responseHeaders.add(HttpHeaders.WWW_AUTHENTICATE, "Digest " + getChallenge(true));
        return new Authenticator.Retry(401);
    }

    private HttpPrincipal validateUser(HttpExchange httpExchange, Map<String, String> challengeParameters) {
        String realm = challengeParameters.get("realm");
        String username = challengeParameters.get("username");

        if (realm == null || realm.length() == 0 || username == null || username.length() == 0) {
            return null;
        }
        if(!OPTIONS.getLogin().equals(username)) {
            return null;
        }

        String password = OPTIONS.getPassword();//passwords.getProperty(username);
        // FIXME implement check for login and password
        if (password == null) {
            return null;
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(username.getBytes());
            md5.update(COL);
            md5.update(realm.getBytes());
            md5.update(COL);
            md5.update(password.getBytes());

            byte[] ha1 = toHexBytes(md5.digest());

            md5.update(httpExchange.getRequestMethod().getBytes());
            md5.update(COL);
            md5.update(challengeParameters.get("uri").getBytes());

            byte[] ha2 = toHexBytes(md5.digest());

            md5.update(ha1);
            md5.update(COL);
            md5.update(challengeParameters.get("nonce").getBytes());
            md5.update(COL);
            md5.update(ha2);

            byte[] expectedResponse = toHexBytes(md5.digest());
            byte[] actualResponse = challengeParameters.get("response").getBytes();

            if (MessageDigest.isEqual(expectedResponse, actualResponse)) {
                return new HttpPrincipal(username, realm);
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalStateException("No MD5? Should not be possible", e);
        }

        return null;
    }


    private String getChallenge(boolean stale) {
        StringBuilder buf = new StringBuilder();
        buf.append("realm=\"").append(realm).append("\",");
        buf.append("nonce=\"").append(createNonce()).append("\"");
        if (stale == true) {
            buf.append(",stale=true");
        }
        return buf.toString();
    }

    private Map<String, String> parseDigestChallenge(String challenge) {
        Map<String, String> ret = new HashMap<String, String>();
        HeaderParser parser = new HeaderParser(challenge);
        while (parser.hasNext()) {
            HeaderParser.Parameter next = parser.next();
            ret.put(next.key, next.value);
        }
        return ret;
    }


    private class HeaderParser {

        private static final char DELIM = ',';
        private static final char EQ = '=';
        private static final char ESC = '\\';
        private static final char Q = '"';

        private final String header;
        private final int length;
        private int pos = 0;
        private boolean seenNext;

        HeaderParser(String message) {
            this.header = message;
            this.length = message.length();
        }

        boolean hasNext() {
            if (seenNext == true) {
                return true;
            }
            if (pos >= length) {
                return false;
            }
            int nextEquals = header.indexOf(EQ, pos);
            if (nextEquals < 0 || nextEquals >= length - 1) {
                return false;
            }

            seenNext = true;
            return true;
        }

        Parameter next() {
            if (!seenNext && !hasNext()) {
                return null;
            }
            Parameter ret = new Parameter();
            int equalsPos = header.indexOf(EQ, pos);
            ret.key = header.substring(pos, equalsPos).trim();
            pos = equalsPos + 1;
            int nextDelim = header.indexOf(DELIM, pos);
            int nextQ = header.indexOf(Q, pos);
            boolean quoted = false;
            if (nextQ > 0 && (nextDelim < 0 || nextQ < nextDelim)) {
                quoted = true;
            }

            if (quoted) {
                String invalid = header.substring(pos, nextQ).trim();
                if (!"".equals(invalid)) {
                    throw new IllegalArgumentException("Invalid header content '" + invalid + "' for " + ret.key);
                }
                pos = nextQ;
                int endQ = -1;
                while (endQ < 0) {
                    nextQ = header.indexOf(Q, nextQ + 1);
                    if (nextQ < 0) {
                        throw new IllegalArgumentException("No matching quote for " + ret.key);
                    }
                    if (header.charAt(nextQ - 1) != ESC) {
                        endQ = nextQ;
                    }
                }
                ret.value = header.substring(pos + 1, endQ);
                int nextDelim2 = header.indexOf(DELIM, pos);
                if (nextDelim2 > 0) {
                    pos = nextDelim2 + 1;
                }
            } else {
                int nextDelim2 = header.indexOf(DELIM, pos);
                if (nextDelim2 > 0) {
                    ret.value = header.substring(pos, nextDelim2).trim();
                    pos = nextDelim2 + 1;
                } else {
                    ret.value = header.substring(pos, length - 1).trim();
                    pos = length + 1;
                }

            }
            seenNext = false;
            return ret;
        }
        class Parameter {
            String key;
            String value;
        }

    }

    public String createNonce() {
        byte[] ret = new byte[16];
        random.nextBytes(ret);
        String retStr = toHexString(ret);
        synchronized (givenNonces) {
            givenNonces.add(retStr);
        }
        return retStr;
    }

    public boolean useNonce(String nonceToUse) {
        synchronized (givenNonces) {
            return givenNonces.remove(nonceToUse);
        }

    }
    private DigestContext getOrCreateContext(HttpExchange httpExchange) {
        DigestContext ret = (DigestContext)httpExchange.getAttribute("digest-context");
        if (ret == null) {
            ret = new DigestContext();
            httpExchange.setAttribute("digest-context", ret);
        }
        return ret;
    }

    private static class DigestContext {
        private HttpPrincipal principal = null;

        boolean isAuthenticated() {
            return principal != null;
        }

        HttpPrincipal getPrincipal() {
            return principal;
        }
    }


    static final byte[] HEX_BYTES = new byte[]
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String toHexString(byte[] digest) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < digest.length; ++i) {
            sb.append(Integer.toHexString((digest[i] & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }

    public static byte[] toHexBytes(byte[] toBeConverted) {
        if (toBeConverted == null) {
            throw new NullPointerException("Parameter to be converted can not be null");
        }

        byte[] converted = new byte[toBeConverted.length * 2];
        for (int i = 0; i < toBeConverted.length; i++) {
            byte b = toBeConverted[i];
            converted[i * 2] = HEX_BYTES[b >> 4 & 0x0F];
            converted[i * 2 + 1] = HEX_BYTES[b & 0x0F];
        }

        return converted;
    }

}