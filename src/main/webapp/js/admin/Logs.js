/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
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

        var refreshTask;

        var divHeader = u.create({className: "logs-header"}, div)
            .place(HTML.BUTTON, {innerHTML:"Refresh", onclick: function(){
                updateData();
            }})
            .place({ className: "logs-header-label hidden", content: u.create(HTML.DIV)
                .place(HTML.DIV, { className: "logs-header-label question", innerHTML: "Clear logs?"})
                .place(HTML.BUTTON, { className: "question", innerHTML:"Yes, clear logs", onclick: function(){
                    this.parentNode.parentNode.hide();
                    this.parentNode.parentNode.nextSibling.classList.remove("hidden");
                    u.get("/admin/logs/clear")
                        .then(updateData);
                }})
                .place(HTML.BUTTON, { innerHTML:"No", onclick: function(){
                    this.parentNode.parentNode.hide();
                    this.parentNode.parentNode.nextSibling.classList.remove("hidden");
                }})
            })
            .place(HTML.BUTTON, { innerHTML:"Clear logs", onclick: function(){
                this.previousSibling.classList.add("hidden");
                this.classList.add("hidden");
                this.previousSibling.show();
            }});

        table = u.table({
            id: "logs",
            caption: {
                className: "table-logs-caption",
                items: [
                    { className: "table-logs-caption-cell", label: "Logs" },
                ]
            },
            className: "table-logs",
            bodyClassName: "table-logs-body",
            placeholder: "Loading..."
        }, div);
        table.addEventListener("DOMNodeRemovedFromDocument", function(e) {
            if(e && e.srcElement === table && task && task.readyState == task.OPEN) {
                task.close();
            }
        });

    };


    function updateData(){
        var scroll = table.body.scrollTop;
        table.placeholder.show("Loading...");

        task = new EventSource("/admin/logs/log");
        task.onmessage = function(e) {
            setTimeout(function(){
                table.head.cells[0].lastChild.innerHTML = "Logs (updated "+(new Date().toLocaleString())+")";
                table.add({
                    className: "table-logs-row",
                    cells: [
                        { className: "table-logs-row-cell", innerHTML: this },
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


