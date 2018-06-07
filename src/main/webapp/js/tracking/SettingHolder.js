/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 3/29/17.
 */
EVENTS.SHOW_SETTINGS = "show_settings";

function SettingHolder(main) {

    var type = "options";
    var optionsDialog;
    var sections;
    var categories;
    var options;

    function start() {
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                object.add({section: DRAWER.SECTION_MISCELLANEOUS, id: EVENTS.SHOW_SETTINGS, name: u.lang.settings, icon: "settings", callback: function(){
                    main.fire(EVENTS.SHOW_SETTINGS);
                }});
                break;
            case EVENTS.SHOW_SETTINGS:
                initOptionsDialog();
                populateOptionsDialog();
                optionsDialog.open();
                break;
            default:
                break;
        }
        return true;
    }

    function initOptionsDialog() {
        if(optionsDialog) return;

        optionsDialog = u.dialog({
            title: {
                label: u.lang.settings,
                filter: true
            },
            className: "options-dialog",
            positive: {
                label: u.lang.ok,
                onclick: function(e, event) {
                    for(var i in options) {
                        try {
                            options[i].onaccept && options[i].onaccept(e, event);
                        } catch(e) {
                            console.error(e);
                        }
                    }
                }
            },
            neutral: {
                dismiss: false,
                label: u.lang.apply,
                onclick: function(e, event) {
                    for(var i in options) {
                        try {
                            options[i].onaccept && options[i].onaccept(e, event);
                        } catch(e) {
                            console.error(e);
                        }
                    }
                }
            },
            negative: {
                label: u.lang.cancel,
                onclick: function(){}
            }
        }, main.layout);

        sections = {};
        categories = {};
        options = {};

        var modules = Object.assign({}, {main:main}, main.eventBus.holders);

        for(var i in modules) {
            if(modules[i] && modules[i].options && modules[i].options().title) {
                var option = modules[i].options();
                var title = option.title;
                if(title && title instanceof HTMLElement) {
                    title = title.outerHTML;
                }
                title = title || modules[i].type;

                if(!option.id || !sections[option.id]) {
                    optionsDialog.add({
                        type:HTML.DIV,
                        className:"options-dialog-title",
                        innerHTML: title
                    });
                    sections[option.id] = optionsDialog.add({
                        id: option.id || "",
                        type:HTML.DIV,
                        className:"options-dialog-section"
                    });
                }
                for(var j in option.categories) {
                    if(j === "title" || option.categories[j].ignore) continue;
                    var category = option.categories[j];
                    title = category.title;
                    if(title && title instanceof HTMLElement) {
                        title = title.outerHTML;
                    }
                    title = title || "";

                    categories[category.id || ""] = categories[category.id || ""] || optionsDialog.add({
                        id: category.id || "",
                        type: HTML.DIV,
                        className: "options-dialog-item",
                        enclosed: true,
                        label: title
                    }, sections[option.id]);
                    for(var k in category.items) {
                        var item = category.items[k];
                        var id = i + ":" + j + ":" + k;
                        options[id] = optionsDialog.add(item, categories[category.id || ""].lastChild);
                        options[id].onaccept = item.onaccept;
                        options[id].dialog = optionsDialog;
                        if(item.onchange) options[id].addEventListener("change", item.onchange, {passive: true});
                    }
                }
            }
        }
    }

    function populateOptionsDialog() {
        var modules = main.eventBus.holders;
        modules.main = main;
        for(var i in options) {
            var path = i.split(":");
            var o = modules[path[0]].options();
            var item = o.categories[path[1]].items[path[2]];

            switch(item.type) {
                case HTML.SELECT:
                    if(item.default != undefined) {
                        for(var j = 0; j < options[i].options.length; j++) {
                            options[i].options[j].selected = options[i].options[j].value == item.default;
                        }
                    }
                    break;
                case HTML.INPUT:
                    if(item.default != undefined) {
                        options[i].value = item.default;
                    }
                    break;
                case HTML.CHECKBOX:
                    options[i].checked = item.checked || false;
                    break;
            }

            if(item.onshow) {
                item.onshow(options[i]);
            }

        }
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent
    }
}