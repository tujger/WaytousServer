/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 4/20/17.
 */
function LogsHolder() {
    this.category = DRAWER.SECTION_MISCELLANEOUS;
    this.type = "logs";
    this.title = "Logs";
    this.menu = "Logs";
    this.icon = "receipt";

    var task;
    var logBody;
    var logCaption;

    this.start = function() {
        div = document.getElementsByClassName("layout")[0];
    }

    this.resume = function() {

        div = document.getElementsByClassName("layout")[0];
        u.clear(div);

        var logsTitleNode = u.create(HTML.H2, null, div).place(HTML.SPAN, "Logs");
        renderButtons(logsTitleNode);

        var logView = u.create(HTML.DIV, {className: "logs"}, div);

        logCaption = u.create(HTML.DIV, {className: "logs-caption", innerHTML: "Logs"}, logView);
        logBody = u.create(HTML.TEXTAREA, {className: "logs-body", value: "Loading...", readOnly: true}, logView);

        logBody.addEventListener("DOMNodeRemovedFromDocument", function(e) {
            if(e && e.srcElement === logBody && task && task.readyState === task.OPEN) {
                task.close();
            }
        }, {passive: true});

        updateData();
    }

    function updateData(){
        u.clear(logBody);
        try {
            if(task) task.close();
        } catch(e) {
            console.error(e);
        }

        task = new EventSource("/admin/rest/logs/log");
        var timestamp = new Date().getTime();
        task.onmessage = function(e) {
            newTimestamp = new Date().getTime();
            if(newTimestamp - timestamp > 1000) {
                timestamp = newTimestamp;
                logCaption.innerHTML = "Logs (updated "+(new Date().toLocaleString())+")";
            }
            logBody.value += e.data + "\n";
        };
        task.onerror = function(error) {
            console.error(error);
          u.clear(logBody);
          logBody.value = "Loading...";
        };
    }

    function renderButtons(div) {
        div.place(HTML.BUTTON, {className: "icon button-inline", innerHTML:"refresh", title: "Refresh logs", onclick: function(){
            updateData();
        }});
        var clearAll = u.create(HTML.BUTTON, {className: "icon button-inline", innerHTML:"clear_all", title: "Clear logs",
            onclick: function(){
                clearAll.hide();
                question.show();
                yes.show();
                no.show();
            }}, div);
        var question = u.create(HTML.DIV, { className: "question hidden", innerHTML: "Clear logs?"}, div);
        var yes = u.create(HTML.BUTTON, { className: "question hidden", innerHTML:"Yes, clear logs", onclick: function(){
            clearAll.show();
            question.hide();
            yes.hide();
            no.hide();
            u.get("/admin/rest/logs/clear")
                .then(updateData);
        }}, div);
        var no = u.create(HTML.BUTTON, { className: "hidden", innerHTML:"No", onclick: function(){
            clearAll.show();
            question.hide();
            yes.hide();
            no.hide();
        }}, div);
    }
}


