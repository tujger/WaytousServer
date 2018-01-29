/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 7/23/17.
 */

function SupportHolder(main) {

    this.category = DRAWER.SECTION_LAST;
    this.type = "support";
    this.title = u.lang.support;
    this.menu = u.lang.support;
    this.icon = "live_help";

    this.start = function() {
        console.log("Starting SupportHolder");
    };

    this.resume = function() {
        console.log("Resuming SupportHolder");
        u.progress.show(u.lang.loading);
        this.title = u.lang.support;
        this.menu = u.lang.support;
        u.clear(main.content);
        u.post("/rest/content", {resource: "main-support.html", locale: main.selectLang.value}).then(function(xhr){
            u.create(HTML.DIV, {className: "content-normal", innerHTML: xhr.response}, main.content);
            u.progress.hide();
        }).catch(function(error, json) {
            console.error(json);
            u.create(HTML.DIV, {className: "content-centered", innerHTML: "Error"}, main.content);
            u.progress.hide();
        });
    }

    this.onEvent = function(event, object) {
        console.log("onEvent", event, object);
//        switch(event) {
//            case EVENTS.API:
//                console.log("INDEX HOME");
//                u.byId("content").innerHTML = u.lang.api_body.innerHTML;
//                u.byId("content").classList.add("content-api");
//                if(object) object();
//                break;
//        }
        return true;
    }


}