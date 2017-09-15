/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 8/26/17.
 */

EVENTS.PRIVACY = "privacy";

function PrivacyPolicyHolder(main) {

    this.type = "privacy";
    this.category = "docs";
    this.title = u.lang.privacy_policy;
    this.menu = u.lang.privacy_policy;
    this.icon = "help";

    this.start = function() {
        console.log("START PRIBACY");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.RELOAD:
                if(object != this.type) {
                    break;
                }
            case EVENTS.PRIVACY:
                console.log("INDEX PRIVACY");
                var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
                u.post("/rest/v1/getContent", {resource: "privacy-policy.html", locale: lang}).then(function(xhr){
                    u.byId("content").innerHTML = xhr.response;
                    u.byId("content").classList.add("content-privacy");
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