/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 7/23/17.
 */

EVENTS.CONTACT = "contact";

function ContactHolder(main) {

    this.type = "contact";
    this.category = "info";
    this.title = u.lang.contact;
    this.menu = u.lang.contact;
    this.icon = "mail_outline";

    this.start = function() {
        console.log("START CONTACT");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.RELOAD:
                if(object !== this.type) {
                    break;
                }
            case EVENTS.CONTACT:
                console.log("INDEX CONTACT");
                var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
                u.post("/rest/v1/getContent", {resource: "index-contact.html", locale: lang}).then(function(xhr){
                    u.byId("content").innerHTML = xhr.response;
                    u.byId("content").classList.add("content-contact");
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