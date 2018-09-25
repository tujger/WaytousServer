/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 1/19/17.
 */
function GroupsHolder(main) {
    this.category = DRAWER.SECTION_MAIN;
    this.type = "groups";
    this.title = "Groups";
    this.menu = "Groups";
    this.icon = "supervisor_account";

    var tableSummary;
    var tableGroups;
    var div;
    var ref;
    var database;
    var active;

    var utils = main && main.arguments.utils;

    this.start = function() {
        database = firebase.database();
        div = main.content;
    };

    this.resume = function() {
        u.clear(div);
        ref = database.ref();

        u.create(HTML.H2, "Summary", div);

        tableSummary = u.table({
            className: "option"
        }, div);

        function filterPersistent(row){
            return row.cells[2].innerHTML === "Yes";
        }
        function filterTemporary(row){
            return row.cells[2].innerHTML === "No";
        }
        tableSummary.groupsItem = tableSummary.add({
            onclick: function(){
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
                { className:"option", innerHTML: 0 }
            ],
            onclick: function(){
                tableGroups.filter.remove(filterTemporary);
                tableGroups.filter.add(filterPersistent);
            }
        });
        tableSummary.groupsTemporaryItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; temporary" },
                { className:"option", innerHTML: 0 }
            ],
            onclick: function(){
                tableGroups.filter.remove(filterPersistent);
                tableGroups.filter.add(filterTemporary);
            }
        });
        tableSummary.usersTotalItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Users total" },
                { className:"option", innerHTML: 0 }
            ]
        });
        tableSummary.usersOnlineItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; online" },
                { className:"option", innerHTML: 0 }
            ]
        });
        tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Maintenance" },
                { className:"option", innerHTML: "" }
            ]
        });
        tableSummary.lastGroupsClean = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; groups cleaned" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.lastUsersClean = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; users cleaned" },
                { className:"option", innerHTML: "never" }
            ]
        });
        active = true;
        tableSummary.addEventListener("DOMNodeRemovedFromDocument", function(e) {
            if(e && e.srcElement === tableSummary) {
                active = false;
            }
        }, {passive: true});

        var groupsTitleNode = u.create(HTML.H2, "Groups", div);
        renderButtons(groupsTitleNode);

        tableGroups = u.table({
            id: "groups",
            className: "groups",
            caption: {
                items: [
                    { label: "ID" },
                    { label: "Requires password", selectable: true },
                    { label: "Persistent", selectable: true },
                    { label: "Time to live, min", selectable: true },
                    { label: "Dismiss inactive, sec", selectable: true },
                    { label: "Users" },
                    { label: "Created" },
                    { label: "Updated" }
                ]
            },
            placeholder: "No data, try to refresh page."
        }, div);

        u.create("br", null, div);
        updateData();
    };

    function updateData(){
        var initial = true;
        setTimeout(function(){initial = false;}, 3000);
        var resign = true;

        tableGroups.placeholder.show();
        u.clear(tableGroups.body);
        tableGroups.groups = [];

        ref.child(DATABASE.SECTION_GROUPS).off();

        u.getJSON(ref.toString() + "_g.json?shallow=true&print=pretty&access_token=" + window.data.access).then(function(json) {
            for(var x in json) {
                if(!json[x]) continue;

                var groupId = x;
                resign = false;
                tableGroups.groups[groupId] = tableGroups.add({
                    id: groupId,
                    className: "highlight",
                    onclick: function(){
                        main.turn("group", this.id);
                        return false;
                    },
                    cells: [
                        { innerHTML: u.clear(groupId) },
                        { innerHTML: "..." },
                        { innerHTML: "..." },
                        { innerHTML: "..." },
                        { innerHTML: "..." },
                        { innerHTML: "..." },
                        { sort: 0, innerHTML: "..." },
                        { sort: 0, innerHTML: "..." }
                    ]
                });

                if(!active) return;
                setTimeout(function(){
                    var groupId = this.toString();
                    ref.child(DATABASE.SECTION_GROUPS).child(groupId).child(DATABASE.OPTIONS).once("value").then(function(snapshot) {
                        if(!active) return;
                        if(!snapshot || !snapshot.val()) return;

                        var groupId = snapshot.getRef().getParent().key;

                        var row = tableGroups.groups[groupId];
                        row.cells[1].innerHTML = snapshot.val()[DATABASE.REQUIRES_PASSWORD] ? "Yes" : "No";
                        row.cells[2].innerHTML = snapshot.val()[DATABASE.PERSISTENT] ? "Yes" : "No";
                        row.cells[3].innerHTML = snapshot.val()[DATABASE.PERSISTENT] ? "&#150;" : u.clear(snapshot.val()[DATABASE.TIME_TO_LIVE_IF_EMPTY]);
                        row.cells[4].innerHTML = snapshot.val()[DATABASE.DISMISS_INACTIVE] ? u.clear(snapshot.val()[DATABASE.DELAY_TO_DISMISS]) : "&#150;";
                        row.cells[6].sort = snapshot.val()[DATABASE.CREATED];
                        row.cells[6].innerHTML = snapshot.val()[DATABASE.CREATED] ? new Date(snapshot.val()[DATABASE.CREATED]).toLocaleString() : "&#150;";
                        row.cells[7].innerHTML = snapshot.val()[DATABASE.REQUIRES_PASSWORD] ? "Yes" : "No";

                        var usersLimit = parseInt(snapshot.val()[DATABASE.LIMIT_USERS] || 0);

                        var usersNode = row.cells[5];
                        var changedNode = row.cells[7];
                        updateTableSummary();

                        ref.child(DATABASE.SECTION_GROUPS).child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).on("value", function(snapshot){
                            if(!active) return;
                            if(!snapshot.val()) return;

                            var changed = 0, activeUser = 0, total = 0;
                            for(var i in snapshot.val()) {
                                total++;
                                var c = parseInt(snapshot.val()[i][DATABASE.CREATED]);
                                if(c > changed) changed = c;
                                if(snapshot.val()[i][DATABASE.ACTIVE]) activeUser ++;
                            }
                            usersNode.innerHTML = activeUser + " / " + total + (usersLimit ? " / " + usersLimit : "");

                            changed = 0;
                            for(i in snapshot.val()) {
                                c = parseInt(snapshot.val()[i][DATABASE.CHANGED]);
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

                    ref.child(DATABASE.SECTION_GROUPS).on("child_removed", function(data) {
                        if(!active) return;
                        for(var i in tableGroups.rows) {
                            if(tableGroups.rows[i].id === data.key) {
                                tableGroups.body.removeChild(tableGroups.rows[i]);
                                tableGroups.rows.splice(i,1);
                            }
                        }
                        u.toast.show("Group "+data.key+" was removed.");
                        updateTableSummary()
                    }, function(error){
                        console.error("REMOVED",error);

                    });
                    ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_MISC).child(DATABASE.STAT_MISC_GROUPS_CLEANED).off();
                    ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_MISC).child(DATABASE.STAT_MISC_GROUPS_CLEANED).on("value",function(data) {
                        tableSummary.lastGroupsClean.lastChild.innerHTML = new Date(data.val()).toLocaleString() + " (" + utils.toDateString(new Date().getTime() - new Date(data.val())) + " ago)";
                    },function(error){
                        console.error("REMOVED",error);
                    });
                }.bind(groupId), 0);
            }
        }).catch(function(error) {
            console.error("FAILED",error);
            window.location = window.location.href;
        });
    }

    function renderButtons(div) {
        u.create(HTML.BUTTON, { className:"icon notranslate button-inline", innerHTML: "add", title:"Create group", onclick: function(){
                self.scrollTop = main.content.scrollTop;
                main.turn("groups/create");
            }}, div);
        var clear = u.create(HTML.BUTTON, { className:"icon notranslate button-inline", innerHTML: "clear_all", title:"Clean groups", onclick: function(){
            clear.hide();
            question.show();
            yes.show();
            no.show();
        }}, div);

        var question = u.create(HTML.DIV, {className:"buttons hidden", innerHTML: u.create(HTML.DIV, {className:"question", innerHTML:"This will immediately check for expired users and groups. Options for each group are important. Continue?"})}, div);
        var yes = u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: function() {
            clear.show();
            question.hide();
            yes.hide();
            no.hide();
            u.toast.show("Groups clean is performing.");
            u.get("/admin/rest/groups/clean")
                .then(function(xhr){
                }).catch(function(code,xhr){
                var res = JSON.parse(xhr.responseText) || {};
                u.toast.show(res.message || xhr.statusText);
            });
        }}, question);
        var no = u.create(HTML.BUTTON,{ innerHTML:"No", onclick: function(){
            clear.show();
            question.hide();
            yes.hide();
            no.hide();
        }}, question);
    }

    function updateTableSummary() {
        tableSummary.groupsItem.lastChild.innerHTML = tableGroups.rows.length;

        var usersTotal = 0, usersOnline = 0, groupPersistent = 0, groupTemporary = 0;
        for(var i in tableGroups.rows) {
            if(tableGroups.rows[i].cells[2].innerHTML === "Yes") groupPersistent ++;
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
}