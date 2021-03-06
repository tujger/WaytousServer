/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 9/11/17.
 */
function StatisticsHolder(main) {
    this.category = DRAWER.SECTION_MISCELLANEOUS;
    this.type = "statistics";
    this.title = "Statistics";
    this.menu = "Statistics";
    this.icon = "trending_up";

    var tableSummaryGroups;
    var tableSummaryUsers;
    var tableSummaryAccounts;
    var tableMessages;
    var div;
    var ref;
    var groupsChart;
    var groupsStat;
    var usersChart;
    var usersStat;
    var accountsChart;
    var accountsStat;
    var messagesCounterNode;
    var groupsChartOptions;
    var usersChartOptions;
    var accountsChartOptions;
    var active;
    var database;

    var actions = {};
    actions[DATABASE.STAT_GROUPS_CREATED_PERSISTENT] = "Group created (persistent)";
    actions[DATABASE.STAT_GROUPS_CREATED_TEMPORARY] = "Group created (temporary)";
    actions[DATABASE.STAT_GROUPS_DELETED] = "Group deleted";
    actions[DATABASE.STAT_GROUPS_REJECTED] = "Group rejected";
    actions[DATABASE.STAT_USERS_JOINED] = "User joined";
    actions[DATABASE.STAT_USERS_RECONNECTED] = "User reconnected";
    actions[DATABASE.STAT_USERS_REJECTED] = "User rejected";
    actions[DATABASE.STAT_ACCOUNTS_CREATED] = "Account created";
    actions[DATABASE.STAT_ACCOUNTS_DELETED] = "Account deleted";
    actions[DATABASE.STAT_MISC_GROUPS_CLEANED] = "Groups cleaning";
    actions[DATABASE.STAT_MISC_ACCOUNTS_CLEANED] = "Accounts cleaning";


    this.start = function() {
        div = main.content;
        database = firebase.database();
    };

    this.resume = function() {
        var self = this;
        u.clear(div);
        u.require("https://www.google.com/jsapi", function () {
                google.load("visualization", "1.1", { "callback": renderInterface, "packages": ["corechart", "line"] });
            })
            .catch(function(){
                console.log("FAIL");
            });
    };

    function renderInterface() {
        ref = database.ref();

        u.create(HTML.H2, "Summary", div);

        var columns = u.create(HTML.DIV, {className: "two-divs"}, div);

        tableSummaryGroups = u.table({
            className: "option",
            sort: false,
            filter: false,
            caption: {
                items: [
                    { label: "Groups" },
                    { label: "Today" },
                    { label: "Total" }
                ]
            }
        }, u.create(HTML.DIV, null, columns));
        tableSummaryGroups.groupsCreatedPersistentItem = tableSummaryGroups.add({
            cells: [
                { className:"th", innerHTML: "New persistent" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" }
            ]
        });
        tableSummaryGroups.groupsCreatedTemporaryItem = tableSummaryGroups.add({
            cells: [
                { className:"th", innerHTML: "New temporary" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" }
            ]
        });
        tableSummaryGroups.groupsDeletedItem = tableSummaryGroups.add({
            cells: [
                { className:"th", innerHTML: "Deleted" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" }
            ]
        });
        tableSummaryGroups.groupsRejectedItem = tableSummaryGroups.add({
            cells: [
                { className:"th", innerHTML: "Rejected" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" }
            ]
        });

        u.create(HTML.DIV, "&nbsp;&nbsp;&nbsp;", columns);
        tableSummaryUsers = u.table({
            className: "option",
            sort: false,
            filter: false,
            caption: {
                items: [
                    { label: "Users" },
                    { label: "Today" },
                    { label: "Total" }
                ]
            }
        }, u.create(HTML.DIV, null, columns));
        tableSummaryUsers.usersJoinedItem = tableSummaryUsers.add({
            cells: [
                { className:"th", innerHTML: "Joined" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" }
            ]
        });
        tableSummaryUsers.usersReconnectedItem = tableSummaryUsers.add({
            cells: [
                { className:"th", innerHTML: "Reconnected" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" }
            ]
        });
        tableSummaryUsers.usersRejectedItem = tableSummaryUsers.add({
            cells: [
                { className:"th", innerHTML: "Rejected" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" }
            ]
        });

        active = true;
        tableSummaryUsers.addEventListener("DOMNodeRemovedFromDocument", function(e) {
            if(e && e.srcElement === tableSummaryUsers) {
                active = false;
            }
        }, {passive: true});

        u.create(HTML.DIV, "&nbsp;&nbsp;&nbsp;", columns);
        tableSummaryAccounts = u.table({
            className: "option",
            sort: false,
            filter: false,
            caption: {
                items: [
                    { label: "Accounts" },
                    { label: "Today" },
                    { label: "Total" }
                ]
            }
        }, u.create(HTML.DIV, null, columns));
        tableSummaryAccounts.accountsCreatedItem = tableSummaryAccounts.add({
            cells: [
                { className:"th", innerHTML: "Created" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" }
            ]
        });
        tableSummaryAccounts.accountsDeletedItem = tableSummaryAccounts.add({
            cells: [
                { className:"th", innerHTML: "Deleted" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" }
            ]
        });

        var groupsChartNode = u.create(HTML.DIV, {className: "statistics-chart"}, div);
        var usersChartNode = u.create(HTML.DIV, {className: "statistics-chart"}, div);
        var accountsChartNode = u.create(HTML.DIV, {className: "statistics-chart"}, div);

        var node = u.create(HTML.H2, null, div);
        u.create(HTML.SPAN, "Messages (", node);
        messagesCounterNode = u.create(HTML.SPAN, "0", node);
        u.create(HTML.SPAN, ")", node);
        renderButtons(node);

        tableMessages = u.table({
            id: "messages",
            className: "statistics",
            caption: {
                items: [
                    { label: "Timestamp" },
                    { label: "Action", selectable: true },
                    { label: "Group ID" },
                    { label: "User ID" },
                    { label: "Message" }
                ]
            },
            placeholder: "No data, try to refresh page."
        }, div);

        u.create("br", null, div);

        // Create the data table.
        groupsStat = new google.visualization.DataTable();
        groupsStat.addColumn("string", "Date");
        groupsStat.addColumn("number", "Persistent groups created");
        groupsStat.addColumn("number", "Temporary groups created");
        groupsStat.addColumn("number", "Deleted");
        groupsStat.addColumn("number", "Rejected");
        groupsStat.addRow(["Loading...",0,0,0,0]);

        // Set chart options
        groupsChartOptions = {
            title: "Groups",
            legend: { position: 'bottom', alignment: 'start' },
            hAxis: {slantedText:false, slantedTextAngle:90 }
        };

        // Create the data table.
        usersStat = new google.visualization.DataTable();
        usersStat.addColumn("string", "Date");
        usersStat.addColumn("number", "Joined");
        usersStat.addColumn("number", "Reconnected");
        usersStat.addColumn("number", "Rejected");
        usersStat.addRow(["Loading...",0,0,0]);

        // Set chart options
        usersChartOptions = {
            title: "Users",
            legend: { position: 'bottom', alignment: 'start' },
            hAxis: {slantedText:false, slantedTextAngle:90 }
        };

        // Create the data table.
        accountsStat = new google.visualization.DataTable();
        accountsStat.addColumn("string", "Date");
        accountsStat.addColumn("number", "Created");
        accountsStat.addColumn("number", "Deleted");
        accountsStat.addRow(["Loading...",0,0]);

        // Set chart options
        accountsChartOptions = {
            title: "Accounts",
            legend: { position: 'bottom', alignment: 'start' },
            hAxis: {slantedText:false, slantedTextAngle:90 }
        };

        // Instantiate and draw our chart, passing in some options.
        groupsChart = new google.visualization.LineChart(groupsChartNode);
        //groupsChart = new google.charts.Line(groupsChartNode);
        google.visualization.events.addOneTimeListener(groupsChart, "ready", function(){
            groupsStat.removeRow(0);
            usersChart = new google.visualization.LineChart(usersChartNode);
            //usersChart = new google.charts.Line(usersChartNode);
            google.visualization.events.addOneTimeListener(usersChart, "ready", function(){
                usersStat.removeRow(0);
                accountsChart = new google.visualization.LineChart(accountsChartNode);
                google.visualization.events.addOneTimeListener(accountsChart, "ready", function(){
                    accountsStat.removeRow(0);
                    updateData();
                });
                accountsChart.draw(usersStat, usersChartOptions);
            });
            usersChart.draw(usersStat, usersChartOptions);
        });
        groupsChart.draw(groupsStat, groupsChartOptions);
    }

    function updateData(){
        tableMessages.placeholder.show();
        u.clear(tableMessages.body);

        var updateValue = function(node, value) {
            if(!active) return;
            if(value === undefined) return;
            value = +value;
            var oldValue = +node.innerHTML;
            if(value !== oldValue) {
                node.updateHTML(""+value, {noflick: !oldValue});
            }
        };

        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_TOTAL).off();
        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_TOTAL).on("value", function(data) {
            var json = data.val();

            updateValue(tableSummaryGroups.groupsCreatedPersistentItem.cells[2], json[DATABASE.STAT_GROUPS_CREATED_PERSISTENT]);
            updateValue(tableSummaryGroups.groupsCreatedTemporaryItem.cells[2], json[DATABASE.STAT_GROUPS_CREATED_TEMPORARY]);
            updateValue(tableSummaryGroups.groupsDeletedItem.cells[2], json[DATABASE.STAT_GROUPS_DELETED]);
            updateValue(tableSummaryGroups.groupsRejectedItem.cells[2], json[DATABASE.STAT_GROUPS_REJECTED]);
            updateValue(tableSummaryUsers.usersJoinedItem.cells[2], json[DATABASE.STAT_USERS_JOINED]);
            updateValue(tableSummaryUsers.usersReconnectedItem.cells[2], json[DATABASE.STAT_USERS_RECONNECTED]);
            updateValue(tableSummaryUsers.usersRejectedItem.cells[2], json[DATABASE.STAT_USERS_REJECTED]);
            updateValue(tableSummaryAccounts.accountsCreatedItem.cells[2], json[DATABASE.STAT_ACCOUNTS_CREATED]);
            updateValue(tableSummaryAccounts.accountsDeletedItem.cells[2], json[DATABASE.STAT_ACCOUNTS_DELETED]);

        }, function(err) {
            console.err("ERR", err);
        });

        var chartsReady = false;
        var addValueToChart = function(data) {
            if(!active) return;
            var json = data.val;

            var date = new Date();
            date = "%04d-%02d-%02d".sprintf(date.getFullYear(), date.getMonth()+1, date.getDate());
            if(data.key === date) {
                updateValue(tableSummaryGroups.groupsCreatedPersistentItem.cells[1], json[DATABASE.STAT_GROUPS_CREATED_PERSISTENT]);
                updateValue(tableSummaryGroups.groupsCreatedTemporaryItem.cells[1], json[DATABASE.STAT_GROUPS_CREATED_TEMPORARY]);
                updateValue(tableSummaryGroups.groupsDeletedItem.cells[1], json[DATABASE.STAT_GROUPS_DELETED]);
                updateValue(tableSummaryGroups.groupsRejectedItem.cells[1], json[DATABASE.STAT_GROUPS_REJECTED]);
                updateValue(tableSummaryUsers.usersJoinedItem.cells[1], json[DATABASE.STAT_USERS_JOINED]);
                updateValue(tableSummaryUsers.usersReconnectedItem.cells[1], json[DATABASE.STAT_USERS_RECONNECTED]);
                updateValue(tableSummaryUsers.usersRejectedItem.cells[1], json[DATABASE.STAT_USERS_REJECTED]);
                updateValue(tableSummaryAccounts.accountsCreatedItem.cells[1], json[DATABASE.STAT_ACCOUNTS_CREATED]);
                updateValue(tableSummaryAccounts.accountsDeletedItem.cells[1], json[DATABASE.STAT_ACCOUNTS_DELETED]);
            }

            var groupsData = [data.key,0,0,0,0];
            if(json[DATABASE.STAT_GROUPS_CREATED_PERSISTENT]) {
                groupsData[1] = json[DATABASE.STAT_GROUPS_CREATED_PERSISTENT];
            }
            if(json[DATABASE.STAT_GROUPS_CREATED_TEMPORARY]) {
                groupsData[2] = json[DATABASE.STAT_GROUPS_CREATED_TEMPORARY];
            }
            if(json[DATABASE.STAT_GROUPS_DELETED]) {
                groupsData[3] = json[DATABASE.STAT_GROUPS_DELETED];
            }
            if(json[DATABASE.STAT_GROUPS_REJECTED]) {
                groupsData[4] = json[DATABASE.STAT_GROUPS_REJECTED];
            }
            var index = groupsStat.getFilteredRows([{column:0, value:data.key}])[0];
            if(index !== undefined) {
                for(var i in groupsData) {
                    groupsStat.setValue(index, +i, groupsData[i]);
                }
            } else {
                groupsStat.addRow(groupsData);
            }

            var usersData = [data.key,0,0,0];
            if(json[DATABASE.STAT_USERS_JOINED]) {
                usersData[1] = json[DATABASE.STAT_USERS_JOINED];
            }
            if(json[DATABASE.STAT_USERS_RECONNECTED]) {
                usersData[2] = json[DATABASE.STAT_USERS_RECONNECTED];
            }
            if(json[DATABASE.STAT_USERS_REJECTED]) {
                usersData[3] = json[DATABASE.STAT_USERS_REJECTED];
            }
            index = usersStat.getFilteredRows([{column:0, value:data.key}])[0];
            if(index !== undefined) {
                for(i in usersData) {
                    usersStat.setValue(index, +i, usersData[i]);
                }
            } else {
                usersStat.addRow(usersData);
            }

            var accountsData = [data.key,0,0];
            if(json[DATABASE.STAT_ACCOUNTS_CREATED]) {
                accountsData[1] = json[DATABASE.STAT_ACCOUNTS_CREATED];
            }
            if(json[DATABASE.STAT_ACCOUNTS_DELETED]) {
                accountsData[2] = json[DATABASE.STAT_ACCOUNTS_DELETED];
            }
            index = accountsStat.getFilteredRows([{column:0, value:data.key}])[0];
            if(index !== undefined) {
                for(i in accountsData) {
                    accountsStat.setValue(index, +i, accountsData[i]);
                }
            } else {
                accountsStat.addRow(accountsData);
            }
            if(chartsReady) {
                groupsChart.draw(groupsStat, groupsChartOptions);
                usersChart.draw(usersStat, usersChartOptions);
                accountsChart.draw(accountsStat, accountsChartOptions);
            }
        };

        var addValueToChartError = function(e) {
            console.warn("Resign because of",e.message);
            window.location = window.location.href;
        };

        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_BY_DATE).off();
        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_BY_DATE).once("value", function(data) {
            var values = data.val();
            var length = u.keys(values).length;
            var count = 0;
            for(var x in values) {
                if((++count) >= length) chartsReady = true;
                addValueToChart({key:x, val:values[x]});
            }
        }, addValueToChartError);
        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_BY_DATE).on("child_changed", function(data) {
            addValueToChart({key:data.key, val:data.val()});
        }, addValueToChartError);

        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_MESSAGES).off();
        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_MESSAGES).on("child_added", function(data) {
            setTimeout(function() {
                if(!active) return;
                var data = this;
                var json = data.val();
                tableMessages.add({
                    id: data.key,
                    className: "statistics-row highlight",
                    tabindex: -1,
//                  onclick: function(){
//                      WTU.switchTo("/admin/group/"+data.key);
//                      return false;
//                  },
                    cells: [
                        { innerHTML: u.clear(data.key) },
                        { innerHTML: u.clear(actions[json["action"]] || json["action"] || "&#150;")},
                        { innerHTML: u.clear(json["group"] || "&#150;") },
                        { innerHTML: u.clear(json["user"] || json["account"] || "&#150;") },
                        { innerHTML: u.clear(json["message"] || "&#150;") }
                    ]
                });
                messagesCounterNode.innerHTML = +messagesCounterNode.innerHTML + 1;

            }.bind(data), 0);
        }, function(error) {
            console.error("REMOVE",error);
        });
        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_MESSAGES).on("child_removed", function(data) {
            for(var i in tableMessages.rows) {
                if(tableMessages.rows[i].id === data.key) {
                    tableMessages.body.removeChild(tableMessages.rows[i]);
                    tableMessages.rows.splice(i,1);
                }
            }
            messagesCounterNode.innerHTML = +messagesCounterNode.innerHTML - 1;
            u.toast.show("Message at "+data.key+" was removed.");
        }, function(error){
            console.error("REMOVED",error);
        })
    }

    function renderButtons(div) {
        var clear = u.create(HTML.BUTTON, {className: "icon button-inline", innerHTML:"clear_all", title: "Clean messages", onclick: function(){
            clear.hide();
            question.show();
            yes.show();
            no.show();
        }}, div);
        var question = u.create(HTML.DIV, {className:"buttons hidden", innerHTML: u.create(HTML.DIV, {className:"question", innerHTML: "All messages will be removed. Continue?"})}, div);
        var yes = u.create(HTML.BUTTON,{className: "question", innerHTML:"Yes", onclick: function() {
            clear.show();
            question.hide();
            yes.hide();
            no.hide();
            u.toast.show("Messages removing is performing.");
            u.get("/admin/rest/stat/clean")
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
            no.hide()
        }}, question);
    }
}


