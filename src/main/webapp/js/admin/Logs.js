/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/20/17.
 */
function Logs() {

    var title = "Logs";

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

            }})
            .place(HTML.DIV, { className: "logs-header-label", innerHTML: "Autorefresh each, sec"})
            .place(HTML.INPUT, { className: "logs-header-input", value: 5})
            .place(HTML.BUTTON, {innerHTML:"Start", onclick: function(){
                clearInterval(refreshTask);
                refreshTask = setInterval(updateData, this.previousSibling.value*1000);
                this.classList.add("hidden");
                this.nextSibling.classList.remove("hidden");
                this.previousSibling.classList.add("disabled");
                this.previousSibling.disabled = true;
            }})
            .place(HTML.BUTTON, {className:"hidden", innerHTML:"Stop", onclick: function(){
                clearInterval(refreshTask);
                this.classList.add("hidden");
                this.previousSibling.classList.remove("hidden");
                this.previousSibling.previousSibling.classList.remove("disabled");
                this.previousSibling.previousSibling.disabled = false;
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

    };


    function updateData(){
        var scroll = table.body.scrollTop;
        table.placeholder.show("Loading...");
        u.get("/admin/logs/log").then(function(xhr){
            table.head.cells[0].lastChild.innerHTML = "Logs (updated "+(new Date().toLocaleString())+")";
            var rows = xhr.response.split("\n");
            for(var i in rows) {
                table.add({
                    className: "table-logs-row",
                    cells: [
                        { className: "table-logs-row-cell", innerHTML: rows[i] },
                    ]
                });
            }
            table.body.scrollTop = scroll
        }).catch(function(code,xhr){
            table.placeholder.show(xhr.response);
        });

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


