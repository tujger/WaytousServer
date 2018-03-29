package com.edeqa.waytousserver.rest;

import com.edeqa.waytous.Options;

import org.json.JSONObject;

import static com.edeqa.waytous.Constants.OPTIONS;

@SuppressWarnings("unused")
public class Arguments extends com.edeqa.edequate.rest.Arguments {

    public static final String TYPE = "/rest/arguments";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void call(JSONObject json, String[] args) {
        OPTIONS = new Options(args);
    }

    @Override
    public String getWebRootDirectory() {
        return OPTIONS.getWebRootDirectory();
    }

    @Override
    public String getSSLCertificatePassword() {
        return OPTIONS.getSSLCertificatePassword();
    }

    @Override
    public String getKeystoreFilename() {
        return OPTIONS.getKeystoreFilename();
    }

    @Override
    public boolean isDebugMode() {
        return OPTIONS.isDebugMode();
    }

    @Override
    public int getHttpPort() {
        return OPTIONS.getHttpPort();
    }

    @Override
    public int getHttpsPort() {
        return OPTIONS.getHttpsPort();
    }

    @Override
    public int getHttpsAdminPort() {
        return OPTIONS.getHttpsAdminPort();
    }

    @Override
    public int getHttpsPortMasked() {
        return OPTIONS.getHttpsPortMasked();
    }

    @Override
    public int getHttpPortMasked() {
        return OPTIONS.getHttpPortMasked();
    }

    @Override
    public String getLogFile() {
        return OPTIONS.getLogFile();
    }

    @Override
    public String getRealm() {
        return "waytous";
    }

    @Override
    public String getAppName() {
        return OPTIONS.getAppName();
    }
}
