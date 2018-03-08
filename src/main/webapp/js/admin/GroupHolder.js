/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 1/19/17.
 */
function GroupHolder(main) {
    this.type = "group";
    this.title = "Group";
    this.icon = "group";

    var tableSummary;
    var div;
    var database;
    var utils = main.arguments.utils;
    var active;
    var groupId;
    var buttons;
    var tableUsers;
    var map;
    var divMapGroup;
    var positions;
    var markers;

    this.start = function() {
        database = firebase.database();
        div = main.content;
    };

    this.resume = function(group_id) {
        groupId = group_id;

        window.history.pushState({}, null, "/admin/group/" + groupId);

        var ref = database.ref().child(DATABASE.SECTION_GROUPS);
        u.clear(div);
        u.create(HTML.H2, "Summary", div);

        var divSummaryMap = u.create(HTML.DIV, {className: "two-divs"}, div);
        var divSummary = u.create(HTML.DIV, {className: "summary-place"}, divSummaryMap);

        tableSummary = u.table({
            className: "option",
            placeholder: "Loading..."
        }, divSummary);

        var url = new URL(window.location.href);
        url = "https://" + url.hostname + (url.port ? (data.HTTPS_PORT === 443 ? "" : ":"+ data.HTTPS_PORT) : "");

        var td = u.create()
            .place(HTML.A, { href: url + "/track/"+groupId, innerHTML:groupId, target:"_blank", rel:"noopener"})
            .place(HTML.SPAN, " ")
            .place(HTML.A, { href: url + "/group/"+groupId, innerHTML:"(Force open in browser)", target:"_blank", rel:"noopener"});

        tableSummary.add({ cells: [
                { className: "th", innerHTML: "ID" },
                { className: "option", content: td }
            ]});

        /*var requiresPasswordNode = tableSummary.add({
            onclick: function() {
                this.lastChild.innerHTML += " ...wait";
                u.post("/admin/rest/group/switch", JSON.stringify({group_id:groupId, property:DATABASE.REQUIRES_PASSWORD}))
                .catch(function(code,xhr){
                   console.error(code,xhr);
                   WTU.resign(updateSummary);
                   var res = JSON.parse(xhr.responseText) || {};
                   u.toast.show(res.message || xhr.statusText);
               });
            },
            cells: [
                { className: "th", innerHTML: "Requires password" },
                { className: "option", innerHTML: snapshot.val()[DATABASE.REQUIRES_PASSWORD] ? "Yes" : "No" }
        ]});


        var passwordNode = tableSummary.add({
            className: snapshot.val()[DATABASE.REQUIRES_PASSWORD] ? "" : "hidden",
            cells: [
                { className: "th", innerHTML: "&#150; password" },
                { className: "option", innerHTML: u.create(HTML.DIV, {innerHTML: "Not set"}).place(HTML.BUTTON, {className:"group-button-set-password", innerHTML:"Set password", onclick: function(e){
                      e.stopPropagation();
                      passwordNode.childNodes[1].firstChild.firstChild.nodeValue = "Not set "+Math.random()
                  }}) }
        ]});*/

        tableSummary.welcomeMessageNode = tableSummary.add({
            onclick: function() {
                ref.child(groupId).child(DATABASE.OPTIONS).child(DATABASE.WELCOME_MESSAGE).once("value").then(function(snapshot){
                    tableSummary.welcomeMessageNode.dialog = tableSummary.welcomeMessageNode.dialog || u.dialog({
                        title: "Welcome message",
                        items: [
                            { type:HTML.INPUT, className:"welcome-input", value: tableSummary.welcomeMessageNode.lastChild.innerHTML }
                        ],
                        positive: {
                            label: u.create(HTML.SPAN, "OK"),
                            onclick: function(items) {
                                var newValue = items[0].value;
                                if(tableSummary.welcomeMessageNode.lastChild.innerHTML !== newValue) {
                                    tableSummary.welcomeMessageNode.lastChild.innerHTML += " ...wait";
                                    u.post("/admin/rest/group/modify", JSON.stringify({group_id:groupId, property:DATABASE.WELCOME_MESSAGE, value:newValue}))
                                        .catch(function(code,xhr){
                                            console.error(code,xhr);
                                            var res = JSON.parse(xhr.responseText) || {};
                                            u.toast.show(res.message || xhr.statusText);
                                            window.location = window.location.href;
                                        });
                                }
                            }
                        },
                        negative: { label:u.create(HTML.SPAN, "Cancel")}
                    });
                    tableSummary.welcomeMessageNode.dialog.open();
                }).catch(function(error){
                    console.warn("Resign because of",error);
                    window.location = window.location.href;
                });
            },
            cells: [
                { className: "th", innerHTML: "Welcome message" },
                { className: "option", innerHTML: "..." }
            ]});
        tableSummary.persistentNode = tableSummary.add({
            onclick: function() {
                this.lastChild.innerHTML += " ...wait";
                u.post("/admin/rest/group/switch", JSON.stringify({group_id:groupId, property:DATABASE.PERSISTENT}))
                    .catch(function(code,xhr){
                        console.warn("Resign because of",code,xhr);
                        var res = JSON.parse(xhr.responseText) || {};
                        u.toast.show(res.message || xhr.statusText);
                        window.location = window.location.href;
                    });
            },
            cells: [
                { className: "th", innerHTML: "Persistent group" },
                { className: "option", innerHTML: "..." }
            ]});
        tableSummary.timeToLiveNode = tableSummary.add({
            className: "hidden",
            onclick: function() {
                tableSummary.timeToLiveNode.dialog = tableSummary.timeToLiveNode.dialog || u.dialog({
                    title: "Time to live",
                    items: [
                        { type:HTML.DIV, innerHTML:"Set time to live (in minutes) if the group is empty (i.e. all users are offline)." },
                        { type:HTML.NUMBER, label:"Time to live, min", value:parseInt(tableSummary.timeToLiveNode.lastChild.innerHTML || 15) }
                    ],
                    positive: {
                        label: u.create(HTML.SPAN, "OK"),
                        onclick: function(items) {
                            var newValue = items[1].value;
                            if(tableSummary.timeToLiveNode.lastChild.innerHTML !== newValue) {
                                tableSummary.timeToLiveNode.lastChild.innerHTML += " ...wait";
                                u.post("/admin/rest/group/modify", JSON.stringify({group_id:groupId, property:DATABASE.TIME_TO_LIVE_IF_EMPTY, value:newValue}))
                                    .catch(function(code,xhr){
                                        console.warn("Resign because of",code,xhr);
                                        var res = JSON.parse(xhr.responseText) || {};
                                        u.toast.show(res.message || xhr.statusText);
                                        window.location = window.location.href;
                                    });
                            }
                        }
                    },
                    negative: { label: u.create(HTML.SPAN, "Cancel")}
                });
                tableSummary.timeToLiveNode.dialog.open();
            },
            cells: [
                { className: "th", innerHTML: "&#150; time to live, min" },
                { className: "option", innerHTML: "..." }
            ]
        });
        tableSummary.dismissInactiveNode = tableSummary.add({
            onclick: function() {
                this.lastChild.innerHTML += " ...wait";
                u.post("/admin/rest/group/switch", JSON.stringify({group_id:groupId, property:DATABASE.DISMISS_INACTIVE}))
                    .catch(function(code,xhr){
                        console.warn("Resign because of",code,xhr);
                        var res = JSON.parse(xhr.responseText) || {};
                        u.toast.show(res.message || xhr.statusText);
                        window.location = window.location.href;
                    });
            },
            cells: [
                { className: "th", innerHTML: "Dismiss inactive" },
                { className: "option", innerHTML: "..." }
            ]});
        tableSummary.delayToDismissNode = tableSummary.add({
            className: "hidden",
            onclick: function() {
                tableSummary.delayToDismissNode.dialog = tableSummary.delayToDismissNode.dialog || u.dialog({
                    title: "Delay to dismiss",
                    items: [
                        { type:HTML.DIV, innerHTML:"Switch user offline if he is not active at least (in seconds)." },
                        { type:HTML.NUMBER, label:"Delay to dismiss, sec", value:parseInt(tableSummary.delayToDismissNode.lastChild.innerHTML || 300) }
                    ],
                    positive: {
                        label: u.create(HTML.SPAN, "OK"),
                        onclick: function(items) {
                            var newValue = items[1].value;
                            if(tableSummary.delayToDismissNode.lastChild.innerHTML !== newValue) {
                                tableSummary.delayToDismissNode.lastChild.innerHTML += " ...wait";
                                u.post("/admin/rest/group/modify", JSON.stringify({group_id:groupId, property:DATABASE.DELAY_TO_DISMISS, value:newValue}))
                                    .catch(function(code,xhr){
                                        console.warn("Resign because of",code,xhr);
                                        var res = JSON.parse(xhr.responseText) || {};
                                        u.toast.show(res.message || xhr.statusText);
                                        window.location = window.location.href;
                                    });
                            }
                        }
                    },
                    negative: { label: u.create(HTML.SPAN, "Cancel")}
                });
                tableSummary.delayToDismissNode.dialog.open();
            },
            cells: [
                { className: "th", innerHTML: "&#150; dismiss after, sec" },
                { className: "option", innerHTML: "..." }
            ]});
        tableSummary.createdNode = tableSummary.add({ cells: [
                { className: "th", innerHTML: "Created" },
                { className: "option", innerHTML: "..." }
            ]});
        tableSummary.changedNode = tableSummary.add({ cells: [
                { className: "th", innerHTML: "Changed" },
                { className: "option highlight", innerHTML: "..." }
            ]});
        tableSummary.limitUsers = tableSummary.add({
            onclick: function() {
                tableSummary.limitUsers.dialog = tableSummary.limitUsers.dialog || u.dialog({
                    title: "Limit users",
                    items: [
                        { type:HTML.NUMBER, label:"Limit amount of users to", value:parseInt(tableSummary.limitUsers.lastChild.innerHTML || 0) },
                        { type:HTML.DIV, innerHTML:"0 - no limit." }
                    ],
                    positive: {
                        label: u.create(HTML.SPAN, "OK"),
                        onclick: function(items) {
                            var newValue = items[0].value;
                            if(tableSummary.limitUsers.lastChild.innerHTML !== newValue) {
                                tableSummary.limitUsers.lastChild.innerHTML += " ...wait";
                                u.post("/admin/rest/group/modify", JSON.stringify({group_id:groupId, property:DATABASE.LIMIT_USERS, value:newValue || "0"}))
                                    .then(function(xhr){})
                                    .catch(function(code,xhr){
                                        console.warn("Resign because of",code,xhr);
                                        var res = JSON.parse(xhr.responseText) || {};
                                        u.toast.show(res.message || xhr.statusText);
                                        window.location = window.location.href;
                                    });
                            }
                        }
                    },
                    negative: { label: u.create(HTML.SPAN, "Cancel")}
                });
                tableSummary.limitUsers.dialog.open();
            },
            cells: [
                { className: "th", innerHTML: "Limit users" },
                { className: "option", innerHTML: "..." }
            ]});

        function filterActive(row){
            return !row.classList.contains("inactive");
        }
        function filterEnabled(row){
            return !row.classList.contains("disabled");
        }
        tableSummary.usersNode = tableSummary.add({
            onclick: function(){
                tableUsers.filter.remove(filterActive);
                tableUsers.filter.remove(filterEnabled);
            },
            cells: [
                { className: "th", innerHTML: "Users total" },
                { className: "option highlight", innerHTML: 0 }
            ]});
        tableSummary.activeUsersNode = tableSummary.add({
            onclick: function(){
                tableUsers.filter.add(filterActive);
            },
            cells: [
                { className: "th", innerHTML: "&#150; active" },
                { className: "option highlight", innerHTML: 0 }
            ]});
        tableSummary.enabledUsersNode = tableSummary.add({
            onclick: function(){
                tableUsers.filter.add(filterEnabled);
            },
            cells: [
                { className: "th", innerHTML: "&#150; enabled" },
                { className: "option highlight", innerHTML: 0 }
            ]});

        active = true;
        tableSummary.addEventListener("DOMNodeRemovedFromDocument", function(e) {
            if(e && e.srcElement === tableSummary) {
                active = false;
            }
        }, {passive: true});

        divMapGroup = u.create(HTML.DIV, {className: "map"}, u.create(HTML.DIV, {
            className: "map-place"
        }, divSummaryMap));

        u.create("br", null, div);
        buttons = u.create("div", {className:"buttons"}, div);
        renderButtons(buttons);

        u.create(HTML.H2, "Users", div);

        tableUsers = u.table({
            id: "admin:users",
            className: "group-users",
            caption: {
                items: [
                    { label: "#", width: "5%" },
                    { label: "Name" },
                    { label: "Color", width: "5%" },
                    { label: "Created" },
                    { label: "Updated" },
                    { label: "Platform", selectable: true },
                    { label: "Device", selectable: true },
                    { label: "Sign provider", selectable: true }
                ]
            },
            placeholder: "Loading..."
        }, div);

        if(divMapGroup.offsetHeight) {
            if(!map && !(window.google && google.maps)) {
                window.initMap = initMap;
                u.require("https://maps.googleapis.com/maps/api/js?key="+data.firebase_config.apiKey+"&callback=initMap&libraries=geometry,places").then(function(){});
            } else {
                initMap();
            }
            updateSummary();
        } else {
            updateAll();
        }
    };

    function updateSummary() {
        if(!groupId) {
            u.eventBus.holders.groups.resume();
            return;
        }

        var ref = database.ref().child(DATABASE.SECTION_GROUPS);

        ref.child(groupId).child(DATABASE.OPTIONS).once("value").then(function(snapshot) {
            if(!active) return;
            if(!snapshot || !snapshot.val()) return;

            ref.child(groupId).child(DATABASE.OPTIONS).off();
            ref.child(groupId).child(DATABASE.OPTIONS).on("value", function(snapshot){
                if(!snapshot.val()) return;

//                tableSummary.requiresPasswordNode.lastChild.innerHTML = snapshot.val()[DATABASE.REQUIRES_PASSWORD] ? "Yes" : "No";
//                tableSummary.passwordNode[snapshot.val()[DATABASE.REQUIRES_PASSWORD] ? "show":"hide"]();

                tableSummary.welcomeMessageNode.lastChild.innerHTML = u.clear(snapshot.val()[DATABASE.WELCOME_MESSAGE] || "&lt;not defined&gt;");

                tableSummary.persistentNode.lastChild.innerHTML = snapshot.val()[DATABASE.PERSISTENT] ? "Yes" : "No";
                tableSummary.timeToLiveNode[snapshot.val()[DATABASE.PERSISTENT] ? "hide":"show"]();

                tableSummary.timeToLiveNode.lastChild.innerHTML = u.clear(snapshot.val()[DATABASE.TIME_TO_LIVE_IF_EMPTY] || 15);

                var number = parseInt(snapshot.val()[DATABASE.LIMIT_USERS] || 0);
                tableSummary.limitUsers.lastChild.innerHTML = number || "&#150;";

                tableSummary.dismissInactiveNode.lastChild.innerHTML = snapshot.val()[DATABASE.DISMISS_INACTIVE] ? "Yes" : "No";
                tableSummary.delayToDismissNode[snapshot.val()[DATABASE.DISMISS_INACTIVE] ? "show":"hide"]();

                tableSummary.delayToDismissNode.lastChild.innerHTML = u.clear(snapshot.val()[DATABASE.DELAY_TO_DISMISS] || 300);

                tableSummary.createdNode.lastChild.innerHTML = new Date(snapshot.val()[DATABASE.CREATED]).toLocaleString();
//                tableSummary.changedNode.lastChild.innerHTML = new Date(snapshot.val()[DATABASE.CHANGED]).toLocaleString();

            });
        }).catch(function(error){
            console.warn("Resign because of",error);
            window.location = window.location.href;
        });
    }

    function updateAll() {
        updateSummary();
        updateData();
    }

    function updateData(){

        var ref = database.ref().child(DATABASE.SECTION_GROUPS);
        tableUsers.placeholder.show();
        u.clear(tableUsers.body);
        var reload = false;
        var initial = true;
        setTimeout(function(){initial = false;}, 3000);

        tableSummary.usersNode.lastChild.innerHTML = 0;
        tableSummary.activeUsersNode.lastChild.innerHTML = 0;
        tableSummary.enabledUsersNode.lastChild.innerHTML = 0;

        ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).off();
        ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PRIVATE).off();

        ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).on("child_added", function(snapshot) {
            if(!active) return;
            if(!snapshot || !snapshot.val()){
                tableUsers.placeholder.show("No users");
                return;
            }
            setTimeout(function() {
                var snapshot = this;

                reload = false;
                var userNumber = snapshot.key;

                var row = tableUsers.add({
                    className: "highlight inactive disabled",
                    onclick: function(){
                        main.turn("user", [groupId, userNumber]);
                        return false;
                    },
                    cells: [
                        { innerHTML: u.clear(userNumber), sort: parseInt(userNumber) },
                        { innerHTML: u.clear(snapshot.val()[DATABASE.NAME]) },
                        { style: { backgroundColor: utils.getHexColor(u.clear(snapshot.val()[DATABASE.COLOR])), opacity: 0.5 } },
                        { sort: snapshot.val()[DATABASE.CREATED], innerHTML: snapshot.val()[DATABASE.CREATED] ? new Date(snapshot.val()[DATABASE.CREATED]).toLocaleString() : "&#150;" },
                        { sort: 0, innerHTML: "..." },
                        { innerHTML: "..." },
                        { innerHTML: "..." },
                        { innerHTML: "..." }
                    ]
                });
                var userNameNode = row.cells[1];
                var userChangedNode = row.cells[4];
                var userOsNode = row.cells[5];
                var userDeviceNode = row.cells[6];
                var userSignProviderNode = row.cells[7];

                tableSummary.usersNode.lastChild.innerHTML = +tableSummary.usersNode.lastChild.innerHTML + 1;
//            tableSummary.activeUsersNode.lastChild.innerHTML = +tableSummary.activeUsersNode.lastChild.innerHTML + 1;
//            if(snapshot.val()[DATABASE.ACTIVE]) {
//                tableSummary.activeUsersNode.lastChild.innerHTML = +tableSummary.activeUsersNode.lastChild.innerHTML + 1;
//            }
//            className: "highlight" + (snapshot.val()[DATABASE.ACTIVE] ? "" : " inactive") + (isEnabledTime(snapshot.val()[DATABASE.CHANGED]) ? "" : " disabled"),
//
//            if(isEnabledTime(snapshot.val()[DATABASE.CHANGED])) {
//                tableSummary.activeUsersNode.lastChild.innerHTML = +tableSummary.activeUsersNode.lastChild.innerHTML + 1;
//            }

                ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(userNumber).child(DATABASE.CHANGED).on("value", function(snapshot){
                    if(!active) return;
                    if(!snapshot.val()) return;
                    userChangedNode.sort = snapshot.val();
                    userChangedNode.innerHTML = new Date(snapshot.val()).toLocaleString();

                    var enabled = isEnabledTime(snapshot.val());

                    if(enabled && row.classList.contains("disabled")) {
                        tableSummary.enabledUsersNode.lastChild.innerHTML = +tableSummary.enabledUsersNode.lastChild.innerHTML + 1;
                    } else if(!enabled && !row.classList.contains("disabled")) {
                        tableSummary.enabledUsersNode.lastChild.innerHTML = +tableSummary.enabledUsersNode.lastChild.innerHTML - 1;
                    }
                    row.classList[enabled ? "remove" : "add"]("disabled");

                    if(!initial) row.classList.add("changed");
                    setTimeout(function(){row.classList.remove("changed")}, 5000);
                    tableSummary.changedNode.lastChild.innerHTML = new Date(snapshot.val()).toLocaleString();
                    tableUsers.update();
                });
                ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(userNumber).child(DATABASE.ACTIVE).on("value", function(snapshot){
                    if(!active) return;
                    var isActive = !!snapshot.val();
                    if(isActive && row.classList.contains("inactive")) {
                        tableSummary.activeUsersNode.lastChild.innerHTML = +tableSummary.activeUsersNode.lastChild.innerHTML + 1;
                    } else if(!isActive && !row.classList.contains("inactive")) {
                        tableSummary.activeUsersNode.lastChild.innerHTML = +tableSummary.activeUsersNode.lastChild.innerHTML - 1;
                    }
                    row.classList[isActive ? "remove" : "add"]("inactive");
                    tableUsers.update();
                });
                ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(userNumber).child(DATABASE.NAME).on("value", function(snapshot){
                    if(!active) return;
                    userNameNode.innerHTML = u.clear(snapshot.val() || "&lt;Friend "+userNumber+"&gt;");
                    tableUsers.update();
                });
                ref.child(groupId).child(DATABASE.PUBLIC).child("tracking").child(userNumber).limitToLast(1).on("child_added", function(snapshot){
                    if(!active) return;
                    var position = snapshot.val();
                    if(position) {
                        row.classList.remove("italic");
                        if(map) {
                            positions[userNumber] = utils.latLng({coords:{latitude:position[USER.LATITUDE], longitude:position[USER.LONGITUDE]}});
                            var bounds = new google.maps.LatLngBounds();

                            if(u.keys(positions).length > 1) {
                                for(var x in positions) {
                                    bounds.extend(positions[x]);
                                }
                                map.fitBounds(bounds);
                            } else {
                                for(x in positions) {
                                    map.setCenter(positions[x]);
                                    map.setZoom(15);
                                }
                            }

                            if(!markers[userNumber]) {
                                markers[userNumber] = new google.maps.Marker({
                                    position: positions[userNumber],
                                    map: map,
                                    label: userNumber
                                });
                            }
                            markers[userNumber].setPosition(positions[userNumber]);
                            markers[userNumber].row = row;
                            markers[userNumber].addListener("mouseover", function(){
                                this.row.classList.add("selected");
                            });
                            markers[userNumber].addListener("mouseout", function(){
                                this.row.classList.remove("selected");
                            });
                        }
                    }
                });
                ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PRIVATE).child(userNumber).once("value").then(function(snapshot){
                    if(!active) return;
                    if(!snapshot.val()) return;

                    // var uid = snapshot.val()[REQUEST.UID];
                    // if(uid) {
                    //     ref.child(DATABASE.SECTION_USERS).child(uid).child(DATABASE.PRIVATE).once("value")
                    //         .then(function(snapshot){
                    //             if(snapshot.val()) {
                                    userOsNode.innerHTML = u.clear(snapshot.val()[REQUEST.OS] || "&#150");
                                    userDeviceNode.innerHTML = u.clear(snapshot.val()[REQUEST.MODEL] || "&#150;");
                                    userSignProviderNode.innerHTML = u.clear(snapshot.val()[REQUEST.SIGN_PROVIDER] || "anonymous");
                                    tableUsers.update();
                    //             }
                    //         });
                    // }
                }).catch(function(error){
                    if(!reload) {
                        reload = true;
                        console.warn("Resign because of",error);
                        window.location = window.location.href;
                    }
                });
            }.bind(snapshot), 0);
        }, function(error){
            console.warn("Resign because of",error);
            window.location = window.location.href
        });
        ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).on("child_removed", function(snapshot) {
            if(!active) return;
            reload = true;

            updateAll();
            //var userNumber = snapshot.key;
            //
            //var row = tableUsers.items[userNumber];
            //row.parentNode.removeChild(row);
            //delete tableUsers.items[userNumber];
            //tableUsers.update();

        }, function(error){
            console.warn("Resign because of",error);
            window.location = window.location.href
        });
    }

    function isEnabledTime(time) {
        return (new Date().getTime() - (+time) < 120000);
    }

    function renderButtons(div) {
        u.clear(div);
        u.create(HTML.BUTTON, { innerHTML:"Delete group", onclick: deleteGroupQuestion}, div);
    }

    function deleteGroupQuestion(){
        u.clear(buttons);
        u.create({className:"question", innerHTML: "Are you sure you want to delete group "+groupId+"?"}, buttons);
        u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: function() {
                u.post("/admin/rest/group/delete", JSON.stringify({group_id:groupId}))
                    .then(function(){
                        main.turn("groups");
                        u.toast.show("Group "+groupId+" was deleted.");
                    }).catch(function(code,xhr){
                    console.warn("Resign because of",code,xhr);
                    var res = JSON.parse(xhr.responseText) || {};
                    u.toast.show(res.message || xhr.statusText);
                    renderButtons(buttons);
                    window.location = window.location.href;
                });
            }}, buttons);
        u.create(HTML.BUTTON,{ innerHTML:"No", onclick: function(){
                renderButtons(buttons);
            }}, buttons);
    }

    function initMap() {
        // Create a map object and specify the DOM element for display.
        map = new google.maps.Map(divMapGroup, {
            scrollwheel: true,
            panControl: true,
            zoom: 2,
            center: {lat: 4.0, lng:-16.0},
            zoomControl: true,
            mapTypeControl: true,
            scaleControl: true,
            streetViewControl: false,
            fullscreenControl: true,
            overviewMapControl: true,
            rotateControl: true
        });
        positions = {};
        markers = {};

        u.create(HTML.BUTTON, {
            className: "map-place-switch icon notranslate",
            innerHTML: "flip_to_front",
            onclick: function() {
                if(divMapGroup.parentNode.classList.contains("modal")) {
                    divMapGroup.parentNode.classList.remove("modal");
                } else {
                    divMapGroup.parentNode.classList.add("modal");
                }

            }
        }, divMapGroup);
        updateAll();
    }
}


