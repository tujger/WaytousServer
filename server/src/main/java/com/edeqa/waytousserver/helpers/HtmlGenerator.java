package com.edeqa.waytousserver.helpers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 10/18/16.
 */

public class HtmlGenerator {

    public static final String META = "meta";
    public static final String STYLE = "style";
    public static final String CLASS = "class";
    public static final String DIV = "div";
    public static final String SCRIPT = "script";
    public static final String TITLE = "title";
    public static final String ID = "id";
    public static final String SRC = "src";
    public static final String HTTP_EQUIV = "http-equiv";
    public static final String CONTENT = "content";
    public static final String SIZES = "sizes";
    public static final String TABLE = "table";
    public static final String TR = "tr";
    public static final String TH = "th";
    public static final String TD = "td";
    public static final String H1 = "h1";
    public static final String H2 = "h2";
    public static final String H3 = "h3";
    public static final String H4 = "h4";
    public static final String H5 = "h5";
    public static final String H6 = "h6";
    public static final String H7 = "h7";
    public static final String BORDER = "border";
    public static final String COLSPAN = "colspan";
    public static final String ROWSPAN = "rowspan";
    public static final String A = "a";
    public static final String HREF = "href";
    public static final String TARGET = "target";
    public static final String SMALL = "small";
    public static final String LINK = "link";
    public static final String REL = "rel";
    public static final String STYLESHEET = "stylesheet";
    public static final String TYPE = "type";
    public static final String BR = "br";
    public static final String ONLOAD = "onload";

    public static final String FORM = "form";
    public static final String NAME = "name";
    public static final String INPUT = "input";
    public static final String SUBMIT = "submit";
    public static final String TEXT = "text";
    public static final String VALUE = "value";

    public static final String MANIFEST = "manifest";


    ArrayList<String> notClosableTags = new ArrayList<>(Arrays.asList(new String[]{BR,META,INPUT}));
    private Tag body;
    private Tag head;
    private int level = 0;
    Map<String,String> properties = new HashMap<>();

    public HtmlGenerator() {
        head = new Tag("head");
        body = new Tag("body");
    }

    public Tag getHead(){
        return head;
    }

    public Tag getBody(){
        return body;
    }

    public String build(){
        String res = "<!DOCTYPE html>\n";
        ArrayList<String> parts = new ArrayList<>();
        parts.add("html");
        for(Map.Entry<String,String> entry: properties.entrySet()){
            if(entry.getValue() != null && entry.getValue().length() > 0) {
                parts.add(entry.getKey() + "=\"" + entry.getValue() + "\"");
            } else {
                parts.add(entry.getKey());
            }
        }
        res += "<" + Utils.join(" ", parts) + ">";
        res += head.build();
        res += body.build();
        res += "</html>";
        return res;
    }

    public void clear(){
        head = new Tag("head");
        body = new Tag("body");
        properties.clear();
    }

    public HtmlGenerator with(String key,String value){
        properties.put(key,value);
        return this;
    }


    public class Tag {
        String tag;
//        String text;

        ArrayList<Object> inner = new ArrayList<>();
        Map<String,String> properties = new HashMap<>();

        public Tag(String tag){
            this.tag = tag;
        }

        public Tag add(String type){
            Tag n = new Tag(type);
            inner.add(n);
            return n;
        }

        public String build(){
//            String res = "\n";
            StringBuffer buf = new StringBuffer();
            buf.append("\n");
            for(int i=0;i<level;i++) buf.append("   ");

            buf.append("<"+tag);

            if(!properties.isEmpty()){
                for(Map.Entry<String,String> x:properties.entrySet()){
                    String key = x.getKey();
                    String value = x.getValue();
                    key = key.replaceAll("\\\"","&quot;");
                    value = value.replaceAll("\\\"","&quot;");
                    buf.append(" "+key+"=\""+ value +"\"");
                }
            }

            buf.append(">");
            boolean indent = false;
            for(Object x:inner){
                if(x instanceof Tag) {
                    indent = true;
                    level ++;
                    buf.append(((Tag)x).build());
                    level --;
                } else if(x instanceof String){
                    buf.append(x);
                }
            }
//            if(text != null) buf.append(text);
            if(indent) {
                buf.append("\n");
                for (int i = 0; i < level; i++) buf.append("   ");
            }
            if(!notClosableTags.contains(tag)) {
                buf.append("</" + tag + ">");
            }
            return buf.toString();
        }

        public Tag with(String key,String value){
            properties.put(key,value);
            return this;
        }

        public Tag with(String key,boolean value){
            properties.put(key,"" + value);
            return this;
        }

        public Tag with(String key,int value){
            properties.put(key,String.valueOf(value));
            return this;
        }

        public Tag with(String key,JSONObject value){
            inner.add("var " + key + " = " + value.toString(4) + ";");
            return this;
        }

        public Tag with(String key,JSONArray value){
            inner.add("var " + key + " = " + value.toString(4) + ";");
            return this;
        }

        public Tag with(String text){
            inner.add(text);
            return this;
        }

        public Tag with(Number number){
            inner.add(number.toString());
            return this;
        }

    }

}
