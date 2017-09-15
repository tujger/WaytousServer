/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/19/17.
 */
function Groups() {

    var title = "Groups";

    var alertArea;
    var trhead;
    var tableSummary;
    var tableGroups;
    var user;
    var firebaseToken;
    var div;
    var groupNodes = {};
    var ref;

    var renderInterface = function() {

        div = document.getElementsByClassName("layout")[0];
        u.clear(div);
//        u.create("div", {className:"summary"}, div);
//        u.create("h2", "Groups", div);
        ref = database.ref();


        u.create(HTML.H2, "Summary", div);

        tableSummary = u.table({
            className: "option"
        }, div);

        function filterPersistent(row){
            return row.cells[2].innerHTML == "Yes";
        }
        function filterTemporary(row){
            return row.cells[2].innerHTML == "No";
        }
        tableSummary.groupsItem = tableSummary.add({
            onclick: function(e){
                tableGroups.filter.remove(filterPersistent);
                tableGroups.filter.remove(filterTemporary);
            },
            cells: [
                { className:"th", innerHTML: "Groups" },
                { className:"option", innerHTML: 0 }
            ]
        });

        tableSummary.groupsPersistentItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; persistent" },
                { className:"option", innerHTML: 0 },
            ],
            onclick: function(e){
                tableGroups.filter.remove(filterTemporary);
                tableGroups.filter.add(filterPersistent);
            }
        });
        tableSummary.groupsTemporaryItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; temporary" },
                { className:"option", innerHTML: 0 },
            ],
            onclick: function(e){
                tableGroups.filter.remove(filterPersistent);
                tableGroups.filter.add(filterTemporary);
            }
        });
        tableSummary.usersTotalItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Users total" },
                { className:"option", innerHTML: 0 },
            ]
        });
        tableSummary.usersOnlineItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; online" },
                { className:"option", innerHTML: 0 },
            ]
        });

        var groupsTitleNode = u.create(HTML.H2, "Groups", div);
        buttons = u.create("div", {className:"buttons"}, groupsTitleNode);
        renderButtons(buttons);

        tableGroups = u.table({
            id: "groups",
            caption: {
                items: [
                    { label: "ID" },
                    { label: "Requires password", className: "media-hidden" },
                    { label: "Persistent", className: "media-hidden" },
                    { label: "Time to live, min", className: "media-hidden" },
                    { label: "Dismiss inactive, sec", className: "media-hidden" },
                    { label: "Users" },
                    { label: "Created", className: "media-hidden" },
                    { label: "Updated" }
                ]
            },
            placeholder: "No data, try to refresh page."
        }, div);

        u.create("br", null, div);
    }


    function updateData(){

        var initial = true;
        setTimeout(function(){initial = false;}, 3000);
        var resign = true;

        tableGroups.placeholder.show();
        u.clear(tableGroups.body);

        ref.child(DATABASE.SECTION_GROUPS).off();
        ref.child(DATABASE.SECTION_GROUPS).on("child_added", function(data) {
            resign = false;
            ref.child(data.key).child(DATABASE.SECTION_OPTIONS).once("value").then(function(snapshot) {
                if(!snapshot || !snapshot.val()) return;

                var row = tableGroups.add({
                    id: data.key,
                    className: "highlight",
                    onclick: function(){
                        WTU.switchTo("/admin/group/"+data.key);
                        return false;
                    },
                    cells: [
                        { innerHTML: data.key },
                        { className: "media-hidden", innerHTML:snapshot.val()[DATABASE.OPTION_REQUIRES_PASSWORD] ? "Yes" : "No" },
                        { className: "media-hidden", innerHTML:snapshot.val()[DATABASE.OPTION_PERSISTENT] ? "Yes" : "No" },
                        { className: "media-hidden", innerHTML:snapshot.val()[DATABASE.OPTION_PERSISTENT] ? "&#150;" : snapshot.val()[DATABASE.OPTION_TIME_TO_LIVE_IF_EMPTY] },
                        { className: "media-hidden", innerHTML:snapshot.val()[DATABASE.OPTION_DISMISS_INACTIVE] ? snapshot.val()[DATABASE.OPTION_DELAY_TO_DISMISS] : "&#150;" },
                        { innerHTML: "..." },
                        { className: "media-hidden", sort: snapshot.val()[DATABASE.OPTION_DATE_CREATED], innerHTML:snapshot.val()[DATABASE.OPTION_DATE_CREATED] ? new Date(snapshot.val()[DATABASE.OPTION_DATE_CREATED]).toLocaleString() : "&#150;" },
                        { sort: 0, innerHTML:"..." }
                    ]
                });
                var usersNode = row.cells[5]
                var changedNode = row.cells[7]
                updateTableSummary();

                ref.child(data.key).child(DATABASE.SECTION_USERS_DATA).on("value", function(snapshot){
                    if(!snapshot.val()) return;

                    var changed = 0, active = 0, total = 0;
                    for(var i in snapshot.val()) {
                        total++;
                        var c = parseInt(snapshot.val()[i][DATABASE.USER_CREATED]);
                        if(c > changed) changed = c;
                        if(snapshot.val()[i][DATABASE.USER_ACTIVE]) active ++;
                    }
                    usersNode.innerHTML = active + " / " + total;

                    var changed = 0;
                    for(var i in snapshot.val()) {
                        var c = parseInt(snapshot.val()[i][DATABASE.USER_CHANGED]);
                        if(c > changed) changed = c;
                    }
                    changedNode.sort = changed;
                    changedNode.innerHTML = new Date(changed).toLocaleString();
                    if(!initial) row.classList.add("changed");
                    setTimeout(function(){row.classList.remove("changed")}, 5000);
                    tableGroups.update();
                    updateTableSummary()
                });
            }).catch(function(error){
                console.error(error);
                tableGroups.placeholder.show();
            });
        }, function(e) {
            console.warn("Resign because of",e.message);
            resign = true;
            WTU.resign(updateData);
        });
        ref.child(DATABASE.SECTION_GROUPS).on("child_removed", function(data) {
            for(var i in tableGroups.rows) {
                if(tableGroups.rows[i].id == data.key) {
                    tableGroups.body.removeChild(tableGroups.rows[i]);
                    tableGroups.rows.splice(i,1);
                }
            }
            u.toast.show("Group "+data.key+" was removed.");
            updateTableSummary()
        }, function(error){
            console.error("REMOVED",error);

        })
    }

    function renderButtons(div) {
        u.clear(div);
        u.create(HTML.BUTTON, { className:"button-clean", innerHTML: "clear_all", title:"Clean groups", onclick: cleanGroupsQuestion}, div);
    }

    function cleanGroupsQuestion(e){
        u.clear(buttons);
        u.create({className:"question", innerHTML: "This will immediately check for expired users and groups. Options for each group are important. Continue?"}, buttons);
        u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: function() {
            renderButtons(buttons);
            u.toast.show("Groups clean is performing.");
            u.get("/admin/rest/v1/groups/clean")
                .then(function(xhr){
                }).catch(function(code,xhr){
                var res = JSON.parse(xhr.responseText) || {};
                u.toast.show(res.message || xhr.statusText);
            });
        }}, buttons);
        u.create(HTML.BUTTON,{ innerHTML:"No", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
    }

    function updateTableSummary() {

        tableSummary.groupsItem.lastChild.innerHTML = tableGroups.rows.length;

        var usersTotal = 0, usersOnline = 0, groupPersistent = 0, groupTemporary = 0;
        for(var i in tableGroups.rows) {
            if(tableGroups.rows[i].cells[2].innerHTML == "Yes") groupPersistent ++;
            else groupTemporary++;

            var users = tableGroups.rows[i].cells[5].innerHTML;
            users = users.split("/");
            if(users.length > 1) {
                usersOnline += +users[0];
                usersTotal += +users[1];
            }
        }
        tableSummary.groupsPersistentItem.lastChild.innerHTML = groupPersistent;
        tableSummary.groupsTemporaryItem.lastChild.innerHTML = groupTemporary;
        tableSummary.usersTotalItem.lastChild.innerHTML = usersTotal;
        tableSummary.usersOnlineItem.lastChild.innerHTML = usersOnline;

    }

    return {
        start: function() {
            renderInterface();
            updateData();
        },
        page: "groups",
        icon: "group",
        title: title,
        menu: title,
        move:true,
    }
}


