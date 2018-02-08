/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 4/20/17.
 */
function Logs() {

    var title = "Logs";
    var task;
    var logBody;
    var logCaption;

    var renderInterface = function() {

        div = document.getElementsByClassName("layout")[0];
        u.clear(div);
//        u.create("div", {className:"summary"}, div);
//        u.create("h2", "Groups", div);

        var logsTitleNode = u.create(HTML.H2, null, div).place(HTML.SPAN, "Logs");
        //buttons = u.create("div", {className:"buttons"}, logsTitleNode);
        renderButtons(logsTitleNode);

        var logView = u.create(HTML.DIV, {className: "logs"}, div);

        logCaption = u.create(HTML.DIV, {className: "logs-caption", innerHTML: "Logs"}, logView);
        logBody = u.create(HTML.DIV, {className: "logs-body", innerHTML: "Loading..."}, logView);

        logBody.addEventListener("DOMNodeRemovedFromDocument", function(e) {
            if(e && e.srcElement === logBody && task && task.readyState === task.OPEN) {
                task.close();
            }
        }, {passive: true});

    };

    function updateData(){
        // var scroll = table.body.scrollTop;
        u.clear(logBody);

        try {
            if(task) task.close();
        } catch(e) {
            console.error(e);
        }

        task = new EventSource("/admin/rest/logs/log");
        task.onmessage = function(e) {
//            setTimeout(function(){
//                logCaption.innerHTML = "Logs (updated "+(new Date().toLocaleString())+")";
                logBody.textContent += e.data + "\n";
                //table.body.scrollTop = scroll
//            }.bind(e.data), 0);
        };
        task.onerror = function(error) {
            console.error(error);
          u.clear(logBody);
          logBody.textContent = "Loading...";
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

    return {
        start: function() {
            renderInterface();
            updateData();
        },
        page: "logs",
        icon: "receipt",
        title: title,
        menu: title,
        move:true
    }
}


