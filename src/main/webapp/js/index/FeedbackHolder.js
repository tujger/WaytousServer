/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 7/23/17.
 */

EVENTS.FEEDBACK = "feedback";

function FeedbackHolder(main) {

    this.type = "feedback";
    this.category = "info";
    this.title = u.lang.feedback;
    this.menu = u.lang.feedback;
    this.icon = "feedback";

    this.start = function() {
        console.log("START FEEDBACK");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.RELOAD:
                if(object != this.type) {
                    break;
                }
            case EVENTS.FEEDBACK:
                console.log("INDEX FEEBACK");
                var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
                u.post("/rest/v1/getContent", {resource: "index-feedback.html", locale: lang}).then(function(xhr){
                    u.byId("content").innerHTML = xhr.response;
                    u.byId("content").classList.add("content-feedback");
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