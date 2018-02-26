/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 7/23/17.
 */

function HelpHolder(main) {

    this.category = DRAWER.SECTION_RESOURCES;
    this.type = "help";
    this.title = u.lang.help;
    this.menu = u.lang.help;
    this.icon = "help";

    this.start = function() {
        console.log("Starting HelpHolder");
    }

    this.resume = function() {
        console.log("Resuming HelpHolder");
        u.progress.show(u.lang.loading);
        this.title = u.lang.help;
        this.menu = u.lang.help;
        u.clear(main.content);
        u.post("/rest/content", {resource: "main-help.html", locale: main.selectLang.value}).then(function(xhr){
            u.create(HTML.DIV, {className: "content-normal", innerHTML: xhr.response}, main.content);
            u.progress.hide();
        }).catch(function(error, json) {
            console.error(json);
            u.create(HTML.DIV, {className: "content-centered", innerHTML: "Error"}, main.content);
            u.progress.hide();
        });
    }
}