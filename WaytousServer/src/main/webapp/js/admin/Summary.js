/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/19/17.
 */
function Summary() {

    var title = "Summary";

    var alertArea;
    var tokens;
    var ipToUser;
    var ipToToken;
    var ipToCheck;
    var user;
    var firebaseToken;
    var div;

    var start = function() {
        div = document.getElementsByClassName("content")[0];
        u.clear(div);

        renderInterface();
//        start();
        /*messaging.getToken().then(function(currentToken) {
            if (currentToken) {
                firebaseToken = currentToken;
                console.log(currentToken);
                connectWss();
            } else {
                messaging.requestPermission()
                    .then(function(){
                        start();
                        console.log('Notification permission granted.')
                })
                .catch(function(err){
                    start();
                    console.log('Unable to get permission to notify. ', err)
                });
            }
          })
          .catch(function(err) {
            console.log('An error occurred while retrieving token. ', err);
                utils.showAlert("Data will not be updated. For instant updating you must allow notifications for this page.")
          });*/
    }

    var renderInterface = function() {

//        div.appendChild(renderAlertArea());

        div.appendChild(renderInterfaceTokensHeader());

        var tt = u.create({
            className: "two_tables",
        }, div);

        tt.appendChild(renderInterfaceIpToUserHeader());
        tt.appendChild(renderInterfaceIpToTokenHeader());

        div.appendChild(renderInterfaceIpToCheckHeader());

        renderInterfaceTokens();
        renderInterfaceIpToUser();
        renderInterfaceIpToToken();
        renderInterfaceIpToCheck();

    }

    var renderInterfaceTokensHeader = function () {

        var div = u.create("div", {className:"tokens"});
        u.create("h2", "Groups", div);

        var table = u.create("table", {id:"tokens", className:"summary"}, div);

        var thead = u.create("thead", null, table);
        var trhead = u.create("tr", null, thead);

        u.create("th",{
            rowspan: 2,
            innerHTML: "Group"
        }, trhead);
        u.create("th",{
            rowspan: 2,
            innerHTML: "Owner"
        }, trhead);
        u.create("th",{
            rowspan: 2,
            innerHTML: "Created"
        }, trhead);
        u.create("th",{
            rowspan: 2,
            innerHTML: "Changed"
        }, trhead);
        u.create("th",{
            colspan: 8,
            innerHTML: "Users"
        }, trhead);

        var trhead = u.create("tr", null, thead);

        u.create("th", "#", trhead);
        u.create("th", "Device", trhead);
        u.create("th", "Address", trhead);
        u.create("th", "Created", trhead);
        u.create("th", "Changed", trhead);
        u.create("th", "Control", trhead);
        u.create("th", "Pos", trhead);
        u.create("th", "X", trhead);

        table.appendChild(thead);

        tokens = u.create("tbody", null, table);

        return div;

    }

   var renderInterfaceIpToSome = function(node,some,columnCounter) {
        u.clear(node)
        if(some && some.length > 0) {
            for(var i in some) {
                var tr = u.create("tr", {}, node);
                for(var j in some[i]) {
                    u.create("td", { innerHTML: some[i][j] }, tr);
                }
            }
        } else {
            u.create("td", {
                colspan: columnCounter,
                align: "center",
                innerHTML: "No data"
            }, u.create("tr", {}, node));
        }
    }

    var renderInterfaceTokens = function() {
        u.clear(tokens);

        if(data && data.tokens && data.tokens.length > 0) {
            for(var i in data.tokens) {
                var users = data.tokens[i].users;

                var tr = u.create("tr", {}, tokens);

                u.create("a", { innerHTML: data.tokens[i].id, href: "/track/" + data.tokens[i].id, target: "_blank" }, u.create("td", { rowspan: users.length }, tr));
                u.create("td", { rowspan: users.length, innerHTML: data.tokens[i].owner }, tr);
                u.create("td", { rowspan: users.length, innerHTML: data.tokens[i].created }, tr);
                u.create("td", { rowspan: users.length, innerHTML: data.tokens[i].changed }, tr);

                var indent = 0;

                for(var j in users) {
                    if(indent > 0) tr = u.create("tr", {}, tokens);
                    u.create("td", { innerHTML: users[j].number }, tr);

                    u.create("a", { innerHTML: users[j].model, href: "/admin/user/" + data.tokens[i].id + "/" + j + "/set"}, u.create("td", {}, tr));

                    u.create("td", { innerHTML: users[j].address || "&#150;" }, tr);
                    u.create("td", { innerHTML: users[j].created }, tr);
                    u.create("td", { innerHTML: users[j].changed }, tr);
                    u.create("td", { innerHTML: users[j].control }, tr);
                    u.create("td", { innerHTML: "&#150;" }, tr);
                    u.create("a", { innerHTML: "Del", href: "/admin/user/" + data.tokens[i].id + "/" + j + "/set"}, u.create("td", {}, tr));

                    indent ++;
                }
            }
        } else {
            u.create("td", {
                colspan: 12,
                align: "center",
                innerHTML: "No data"
            }, u.create("tr", {}, tokens));
        }

    }

    var renderInterfaceIpToUserHeader = function () {

        var div = u.create("div", {className: "two_tables_one"});
        u.create("h2", { innerHTML: "IP to User corresponds" }, div);
        var table = u.create("table", {className:"summary"}, div);

        var thead = u.create("thead", {}, table);
        var trhead = u.create("tr",{},thead);

        u.create("th",{
            innerHTML: "IP"
        }, trhead);
        u.create("th",{
            innerHTML: "Device ID"
        }, trhead);

        table.appendChild(thead);

        ipToUser = u.create("tbody", {}, table);

        return div;

    }

    var renderInterfaceIpToUser = function () {
        renderInterfaceIpToSome(ipToUser, data.ipToUser, 2);
    }

    var renderInterfaceIpToTokenHeader = function () {

       var div = u.create("div", {className: "two_tables_one"});
        u.create("h2", { innerHTML: "IP to Group corresponds" }, div);
        var table = u.create("table", {className:"summary"}, div);

        var thead = u.create("thead", {}, table);
        var trhead = u.create("tr",{},thead);

        u.create("th",{
            innerHTML: "IP"
        }, trhead);
        u.create("th",{
            innerHTML: "Group ID"
        }, trhead);

        table.appendChild(thead);

        ipToToken = u.create("tbody", {}, table);

        return div;

    }

    var renderInterfaceIpToToken = function () {
        renderInterfaceIpToSome(ipToToken, data.ipToToken, 2);
    }

    var renderInterfaceIpToCheckHeader = function () {

       var div = u.create("div");
        u.create("h2", { innerHTML: "Checks" }, div);
        var table = u.create("table", {className:"summary"}, div);

        var thead = u.create("thead", {}, table);
        var trhead = u.create("tr",{},thead);

        u.create("th",{
            innerHTML: "IP"
        }, trhead);
        u.create("th",{
            innerHTML: "Group"
        }, trhead);
        u.create("th",{
            innerHTML: "Control"
        }, trhead);
        u.create("th",{
            innerHTML: "Timestamp"
        }, trhead);

        table.appendChild(thead);

        ipToCheck = u.create("tbody", {}, table);

        return div;

    }

    var renderInterfaceIpToCheck = function () {
        renderInterfaceIpToSome(ipToCheck, data.ipToCheck, 4);
    }

    var connectWss = function () {
        socket = new WebSocket(data.general.uri);

        socket.onmessage = function(event) {
            console.log("MESSAGE",event);

            var incomingMessage = event.data;
            showMessage(incomingMessage);
        };

        socket.onopen = function(event) {
            console.log("OPEN",event);
            var o = { "client":"admin" };
            socket.send(JSON.stringify(o));
        };

        socket.onclose = function(event) {
            console.log("CLOSE",event);
        };

        socket.onerror = function(event) {
            console.log("ERROR",event);
        };

        function showMessage(message) {
            console.log("MESSAGE",event);
            var messageElem = document.createElement('div');
            messageElem.appendChild(document.createTextNode(message));
            document.getElementById('subscribe').appendChild(messageElem);
        }
    };

    return {
        start: start,
        page: "summary",
        icon: "list",
        title: title,
        menu: title,
        move:true,
    }
}
