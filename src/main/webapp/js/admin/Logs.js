/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 4/20/17.
 */
function Logs() {

    var title = "Logs";
    var task;

    var renderInterface = function() {

        div = document.getElementsByClassName("layout")[0];
        u.clear(div);
//        u.create("div", {className:"summary"}, div);
//        u.create("h2", "Groups", div);

        var logsTitleNode = u.create(HTML.H2, null, div).place(HTML.SPAN, "Logs");
        //buttons = u.create("div", {className:"buttons"}, logsTitleNode);
        renderButtons(logsTitleNode);

        table = u.table({
            id: "logs",
            caption: {
                className: "table-logs-caption",
                items: [
                    { className: "table-logs-caption-cell", label: "Logs" }
                ]
            },
            className: "table-logs",
            bodyClassName: "table-logs-body",
            placeholder: "Loading..."
        }, div);
        table.addEventListener("DOMNodeRemovedFromDocument", function(e) {
            if(e && e.srcElement === table && task && task.readyState === task.OPEN) {
                task.close();
            }
        }, {passive: true});

    };

    function updateData(){
        // var scroll = table.body.scrollTop;
        table.placeholder.show("Loading...");

        task = new EventSource("/admin/rest/logs/log");
        task.onmessage = function(e) {
            setTimeout(function(){
                table.head.cells[0].lastChild.innerHTML = "Logs (updated "+(new Date().toLocaleString())+")";
                table.add({
                    className: "table-logs-row",
                    cells: [
                        { className: "table-logs-row-cell", innerHTML: this }
                    ]
                });
                //table.body.scrollTop = scroll
            }.bind(e.data), 0);
        };
        task.onerror = function(error) {
            console.error(error);
            table.rows.clear();
            table.placeholder.show("Loading...");
        };
    }

    function renderButtons(div) {
        //u.clear(div);
        //u.create(HTML.BUTTON, { className:"button-clean", innerHTML: "clear_all", title:"Clean groups", onclick: cleanGroupsQuestion}, div);
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
            u.get("/admin/logs/clear")
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


