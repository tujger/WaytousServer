/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 3/14/17.
 */

EVENTS.SHOW_HELP = "show_help";
EVENTS.HIDE_HELP = "hide_help";

function HelpHolder(main) {

    var type = "help";

    var dialog;

    function start() {
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                object.add({section: DRAWER.SECTION_MISCELLANEOUS, id: EVENTS.SHOW_HELP, name: u.lang.help, priority: -1, icon: "help_outline", callback: function(){
                    main.fire(EVENTS.SHOW_HELP);
                }});
                break;
            case EVENTS.HIDE_HELP:
                dialog && dialog.close();
                break;
            case EVENTS.SHOW_HELP:
                dialog = dialog || u.dialog({
                    className: "help-dialog",
                    title: {
                        label: u.lang.help,
                        filter: true
                    },
                    negative: {
                        label: u.lang.close,
                        onclick: function(){}
                    }
                }, main.right);
                if(dialog.opened) break;
                if(!object) {
                    dialog.classList.add("help-dialog");
                }
                dialog.clearItems();
                if(object) {
                    if(object["module"].help) {
                        dialog.add({
                            type: HTML.DIV,
                            className: "help-dialog-item-title",
                            innerHTML: object["module"].help()[object.article].title
                        });
                        dialog.add({
                            type: HTML.DIV,
                            className: "help-dialog-item-body",
                            innerHTML: object["module"].help()[object.article].body
                        });
                    }
                } else {
                    var modules = Object.assign({}, {main:main}, main.eventBus.holders);
                    for(var i in modules) {
                        if(!modules.hasOwnProperty(i)) continue;
                        // console.log(i);
                        if(modules[i] && modules[i].help && modules[i].help().title) {
                            var help = modules[i].help();
                            var title = help.title;
                            if(title && title instanceof HTMLElement) {
                                title = title.outerHTML;
                            }
                            title = title || modules[i].type;
                            dialog.add({
                                type:HTML.DIV,
                                innerHTML: title
                            });
                            for(var j in help) {
                                if(!help.hasOwnProperty(j)) continue;
                                if(j === "title" || help[j].ignore) continue;
                                title = help[j].title;
                                if(title && title instanceof HTMLElement) {
                                    title = title.outerHTML;
                                }
                                title = title || "";
                                var body = help[j].body;
                                if(body && body instanceof HTMLElement) {
                                    body = body.outerHTML;
                                }
                                body = body || "";

                                dialog.add({
                                    type:HTML.DIV,
                                    enclosed:true,
                                    label:j + ". " + title,
                                    body:body
                                });
                            }
                        }
                    }
                }
                dialog.open();
                break;
            default:
                break;
        }
        return true;
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent
    }
}