/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 7/23/17.
 */


function ApiHolder(main) {

    this.category = DRAWER.SECTION_RESOURCES;
    this.type = "api";
    this.title = u.lang.api;
    this.menu = u.lang.api;
    this.icon = "extension";

    this.start = function() {
        console.log("Starting ApiHolder");
    }

    this.resume = function() {
        console.log("Resuming ApiHolder");
        u.progress.show(u.lang.loading);
        this.title = u.lang.api;
        this.menu = u.lang.api;
        u.clear(main.content);
        u.post("/rest/content", {resource: "main-api.html", locale: main.selectLang.value}).then(function(xhr){
            u.create(HTML.DIV, {className: "content-normal", innerHTML: xhr.response}, main.content);
            u.progress.hide();
        }).catch(function(error, json) {
            console.error(json);
            u.create(HTML.DIV, {className: "content-centered", innerHTML: "Error"}, main.content);
            u.progress.hide();
        });
    }
}