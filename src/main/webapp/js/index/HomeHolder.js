/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/25/17.
 */

EVENTS.HOME = "home";

function HomeHolder(main) {

    this.type = "home";
    this.category = "main";
    this.title = u.lang.home;
    this.menu = u.lang.home;
    this.icon = "home";

    this.start = function() {
        console.log("START TRACK");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.RELOAD:
                if(object != this.type) {
                    break;
                }
            case EVENTS.HOME:
                console.log("INDEX HOME");
                var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
                u.post("/rest/v1/getContent", {resource: "index-home.html", locale: lang}).then(function(xhr){
                    u.byId("content").innerHTML = xhr.response;
                    u.byId("content").classList.add("content-home");
                    if(object) object();
                }).catch(function(error, json) {
                    u.byId("content").innerHTML = "Error";
                    u.byId("content").classList.add("content-error");
                    if(object) object();
                });
                break;
        }
        return true;
    }
}