/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 7/23/17.
 */

function ContactHolder(main) {

    this.category = DRAWER.SECTION_LAST;
    this.type = "contact";
    this.title = u.lang.contact;
    this.menu = u.lang.contact;
    this.icon = "mail_outline";

    this.start = function() {
        console.log("Starting ContactHolder");
    }

    this.resume = function() {
        console.log("Resuming ContactHolder");
        u.progress.show(u.lang.loading);
        this.title = u.lang.contact;
        this.menu = u.lang.contact;
        u.clear(main.content);
        u.post("/rest/content", {resource: "main-contact.html", locale: main.selectLang.value}).then(function(xhr){
            u.create(HTML.DIV, {className: "content-normal", innerHTML: xhr.response}, main.content);
            u.progress.hide();
        }).catch(function(error, json) {
            console.error(json);
            u.create(HTML.DIV, {className: "content-centered", innerHTML: "Error"}, main.content);
            u.progress.hide();
        });
    }
}