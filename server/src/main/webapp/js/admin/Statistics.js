/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 9/11/17.
 */
function Statistics() {

    var title = "Statistics";

    var alertArea;
    var trhead;
    var tableSummaryGroups;
    var tableSummaryUsers;
    var tableMessages;
    var user;
    var firebaseToken;
    var div;
    var groupNodes = {};
    var ref;
    var chartNode;
    var groupsChart;
    var groupsStat;
    var usersChart;
    var usersStat;
    var messagesCounterNode;
    var groupsChartOptions;
    var usersChartOptions;


    var initInterface = function() {
        u.require("https://www.google.com/jsapi")
            .then(function () {
                google.load("visualization", "1.1", { "callback": renderInterface, "packages": ["corechart", "line"] });
            })
            .catch(function(){
                console.log("FAIL");
            })
    };

    function renderInterface() {

        div = document.getElementsByClassName("layout")[0];
        u.clear(div);
//        u.create("div", {className:"summary"}, div);
//        u.create("h2", "Groups", div);
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
//        tableSummaryGroups.add({
//            cells: [
//                { className:"th", innerHTML: "Groups" },
//                { className:"option", innerHTML: 0 }
//            ]
//        });
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
                { className:"option", innerHTML: "0" },
            ]
        });
        tableSummaryGroups.groupsDeletedItem = tableSummaryGroups.add({
            cells: [
                { className:"th", innerHTML: "Deleted" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" },
            ]
        });
        tableSummaryGroups.groupsRejectedItem = tableSummaryGroups.add({
            cells: [
                { className:"th", innerHTML: "Rejected" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" },
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
                { className:"option", innerHTML: "0" },
            ]
        });
        tableSummaryUsers.usersReconnectedItem = tableSummaryUsers.add({
            cells: [
                { className:"th", innerHTML: "Reconnected" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" },
            ]
        });
        tableSummaryUsers.usersRejectedItem = tableSummaryUsers.add({
            cells: [
                { className:"th", innerHTML: "Rejected" },
                { className:"option", innerHTML: "0" },
                { className:"option", innerHTML: "0" },
            ]
        });

        groupsChartNode = u.create(HTML.DIV, {className: "statistics-chart"}, div);
        usersChartNode = u.create(HTML.DIV, {className: "statistics-chart"}, div);

       // Create the data table.
        groupsStat = new google.visualization.DataTable();
        groupsStat.addColumn("string", "Date");
        groupsStat.addColumn("number", "Persistent groups created");
        groupsStat.addColumn("number", "Temporary groups created");
        groupsStat.addColumn("number", "Deleted");
        groupsStat.addColumn("number", "Rejected");
        groupsStat.addRow(["Loading...",0,0,0,0]);
//        groupsStat.addRows([
//            ['2004',  1,1,1,1],
//            ['2005',  2,3,4,2],
//            ['2006',  1,4,12,2],
//            ['2007',  5,4,6,23]
//        ]);

        // Set chart options
        groupsChartOptions = {
                  title: "Groups",
//                  legend: { position: "bottom" },
                };

       // Create the data table.
        usersStat = new google.visualization.DataTable();
        usersStat.addColumn("string", "Date");
        usersStat.addColumn("number", "Joined");
        usersStat.addColumn("number", "Reconnected");
        usersStat.addColumn("number", "Rejected");
        usersStat.addRow(["Loading...",0,0,0]);
//        usersStat.addRows([
//            ['2004',  1,1,1],
//            ['2005',  2,3,4],
//            ['2006',  1,4,1],
//            ['2007',  5,4,6]
//        ]);

        // Set chart options
        usersChartOptions = {
                  title: "Users",
//                  legend: { position: "bottom" },
                };

        // Instantiate and draw our chart, passing in some options.
        groupsChart = new google.charts.Line(groupsChartNode);
        google.visualization.events.addOneTimeListener(groupsChart, "ready", function(){
            groupsStat.removeRow(0);
            usersChart = new google.charts.Line(usersChartNode);
            google.visualization.events.addOneTimeListener(usersChart, "ready", function(){
                usersStat.removeRow(0);
                updateData();
            });
            usersChart.draw(usersStat);
        });
        groupsChart.draw(groupsStat);


        var node = u.create(HTML.H2, null, div);
        u.create(HTML.SPAN, "Messages (", node);
        messagesCounterNode = u.create(HTML.SPAN, "0", node);
        u.create(HTML.SPAN, ")", node);
        buttons = u.create("div", {className:"buttons"}, node);
        renderButtons(buttons);

        tableMessages = u.table({
            id: "messages",
            caption: {
                items: [
                    { label: "Timestamp" },
                    { label: "Action", className: "media-hidden" },
                    { label: "Group ID", className: "media-hidden" },
                    { label: "User ID", className: "media-hidden" },
                    { label: "Message" }
                ]
            },
            placeholder: "No data, try to refresh page."
        }, div);

        u.create("br", null, div);
    }


    function updateData(){

        var resign = true;

        tableMessages.placeholder.show();
        u.clear(tableMessages.body);

        var updateValue = function(node, value) {
            if(!value) return;
            value = +value;
            var oldValue = +node.innerHTML;
            if(value != oldValue) {
                node.updateHTML(value, {noflick: !oldValue});
            }
        }

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

        }, function(err) {
            console.err("ERR", err);
        })


        var addValueToChart = function(data) {
              resign = false;
              var json = data.val();

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
              var index = groupsStat.getFilteredRows([{column:0, value:data.key}])[0]
              if(index != undefined) {
                  var row = groupsStat.getRowProperties(index);
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
              var index = usersStat.getFilteredRows([{column:0, value:data.key}])[0]
              if(index != undefined) {
                  var row = usersStat.getRowProperties(index);
                  for(var i in usersData) {
                      usersStat.setValue(index, +i, usersData[i]);
                  }
              } else {
                  usersStat.addRow(usersData);
              }

            google.visualization.events.addOneTimeListener(groupsChart, "ready", function(){
                usersChart.draw(usersStat, google.charts.Line.convertOptions(usersChartOptions));
            });
            groupsChart.draw(groupsStat, google.charts.Line.convertOptions(groupsChartOptions));

            var date = new Date();
            date = "%04d-%02d-%02d".sprintf(date.getFullYear(), date.getMonth()+1, date.getDate());
            if(data.key == date) {
                updateValue(tableSummaryGroups.groupsCreatedPersistentItem.cells[1], json[DATABASE.STAT_GROUPS_CREATED_PERSISTENT]);
                updateValue(tableSummaryGroups.groupsCreatedTemporaryItem.cells[1], json[DATABASE.STAT_GROUPS_CREATED_TEMPORARY]);
                updateValue(tableSummaryGroups.groupsDeletedItem.cells[1], json[DATABASE.STAT_GROUPS_DELETED]);
                updateValue(tableSummaryGroups.groupsRejectedItem.cells[1], json[DATABASE.STAT_GROUPS_REJECTED]);
                updateValue(tableSummaryUsers.usersJoinedItem.cells[1], json[DATABASE.STAT_USERS_JOINED]);
                updateValue(tableSummaryUsers.usersReconnectedItem.cells[1], json[DATABASE.STAT_USERS_RECONNECTED]);
                updateValue(tableSummaryUsers.usersRejectedItem.cells[1], json[DATABASE.STAT_USERS_REJECTED]);
           }
        };

        var addValueToChartError = function(e) {
            console.warn("Resign because of",e.message);
            resign = true;
            WTU.resign(updateData);
        };

        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_BY_DATE).off();
        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_BY_DATE).on("child_added", addValueToChart, addValueToChartError);
        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_BY_DATE).on("child_changed", addValueToChart, addValueToChartError);

        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_MESSAGES).off();
        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_MESSAGES).on("child_added", function(data) {
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
                      { innerHTML: data.key },
                      { className: "media-hidden", innerHTML: json["action"]},
                      { className: "media-hidden", innerHTML: json["group"] },
                      { className: "media-hidden", innerHTML: json["user"] },
                      { innerHTML: json["message"] }
                  ]
              });
            messagesCounterNode.innerHTML = +messagesCounterNode.innerHTML + 1;
        }, function(error) {
            console.error("REMOVE",error);
        });
        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_MESSAGES).on("child_removed", function(data) {
            for(var i in tableMessages.rows) {
                if(tableMessages.rows[i].id == data.key) {
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
        u.clear(div);
        u.create(HTML.BUTTON, { className: "button-clean", innerHTML:"clear_all", title: "Clean messages", onclick: cleanMessagesQuestion}, div);
    }

    function cleanMessagesQuestion(e){
        u.clear(buttons);
        u.create({className:"question", innerHTML: "All messages will be removed. Continue?"}, buttons);
        u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: function() {
           renderButtons(buttons);
           u.toast.show("Messages removing is performing.");
           u.get("/admin/rest/v1/stat/clean")
            .then(function(xhr){
//               WTU.switchTo("/admin/groups");
            }).catch(function(code,xhr){
//               console.warn("Resign because of",code,xhr);
//               WTU.resign(updateData);
               var res = JSON.parse(xhr.responseText) || {};
               u.toast.show(res.message || xhr.statusText);
             });
        }}, buttons);
        u.create(HTML.BUTTON,{ innerHTML:"No", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
    }

    return {
        start: function() {
            initInterface();
//            updateData();
        },
        page: "statistics",
        icon: "trending_up",
        title: title,
        menu: title,
        move: true,
    }
}


