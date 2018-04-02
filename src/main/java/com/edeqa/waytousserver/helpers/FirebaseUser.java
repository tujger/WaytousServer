package com.edeqa.waytousserver.helpers;

public class FirebaseUser {


    public String uid;
    public String os;
    public String signProvider;
    public String model;
    public String manufacturer;

    public FirebaseUser() {
    }

    @Override
    public String toString() {
        return "FirebaseUser{" +
                       "uid='" + uid + '\'' +
                       ", os='" + os + '\'' +
                       ", signProvider='" + signProvider + '\'' +
                       ", model='" + model + '\'' +
                       ", manufacturer='" + manufacturer + '\'' +
                       '}';
    }
}
