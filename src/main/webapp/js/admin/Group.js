/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/19/17.
 */
function Group() {

    var title = "Group";

    var alertArea;
    var user;
    var firebaseToken;
    var div;
    var groupId;
    var summary;
    var buttons;
    var tableSummary;
    var tableUsers;
    var map;
    var divMapGroup;
    var positions;
    var markers;

    var renderInterface = function() {

        var ref = database.ref();
        u.clear(div);
        u.create(HTML.H2, "Summary", div);

        var divSummaryMap = u.create(HTML.DIV, {className: "two-divs"}, div);
        var divSummary = u.create(HTML.DIV, {className: "summary-place"}, divSummaryMap);

        tableSummary = u.table({
            className: "option",
            placeholder: "Loading..."
        }, divSummary);

        var url = new URL(window.location.href);
        url = "https://" + url.hostname + (url.port ? (data.HTTPS_PORT == 443 ? "" : ":"+ data.HTTPS_PORT) : "");

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
                u.post("/admin/rest/v1/group/switch", JSON.stringify({group_id:groupId, property:DATABASE.REQUIRES_PASSWORD}))
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
                    u.dialog({
                        title: "Welcome message",
                        items: [
                            { type:HTML.INPUT, className:"welcome-input", value: tableSummary.welcomeMessageNode.lastChild.innerHTML }
                        ],
                        positive: {
                            label: u.create(HTML.SPAN, "OK"),
                            onclick: function(items) {
                                var newValue = items[0].value;
                                if(tableSummary.welcomeMessageNode.lastChild.innerHTML != newValue) {
                                    tableSummary.welcomeMessageNode.lastChild.innerHTML += " ...wait";
                                    u.post("/admin/rest/v1/group/modify", JSON.stringify({group_id:groupId, property:DATABASE.WELCOME_MESSAGE, value:newValue}))
                                        .catch(function(code,xhr){
                                            console.error(code,xhr);
                                            WTU.resign(updateSummary);
                                            var res = JSON.parse(xhr.responseText) || {};
                                            u.toast.show(res.message || xhr.statusText);
                                        });
                                }
                            }
                        },
                        negative: { label:u.create(HTML.SPAN, "Cancel")}
                    }).open();
                }).catch(function(error){
                    console.warn("Resign because of",error);
                    WTU.resign(updateSummary);
                });
            },
            cells: [
                { className: "th", innerHTML: "Welcome message" },
                { className: "option", innerHTML: "..." }
            ]});

        tableSummary.persistentNode = tableSummary.add({
            onclick: function() {
                this.lastChild.innerHTML += " ...wait";
                u.post("/admin/rest/v1/group/switch", JSON.stringify({group_id:groupId, property:DATABASE.PERSISTENT}))
                    .catch(function(code,xhr){
                        console.warn("Resign because of",code,xhr);
                        WTU.resign(updateSummary);
                        var res = JSON.parse(xhr.responseText) || {};
                        u.toast.show(res.message || xhr.statusText);
                    });
            },
            cells: [
                { className: "th", innerHTML: "Persistent group" },
                { className: "option", innerHTML: "..." }
            ]});

        tableSummary.timeToLiveNode = tableSummary.add({
            className: "hidden",
            onclick: function() {
                u.dialog({
                    title: "Time to live",
                    items: [
                        { type:HTML.DIV, innerHTML:"Set time to live (in minutes) if the group is empty (i.e. all users are offline)." },
                        { type:HTML.NUMBER, label:"Time to live, min", value:parseInt(tableSummary.timeToLiveNode.lastChild.innerHTML || 15) }
                    ],
                    positive: {
                        label: u.create(HTML.SPAN, "OK"),
                        dismiss: false,
                        onclick: function(items) {
                            var newValue = items[1].value;
                            if(tableSummary.timeToLiveNode.lastChild.innerHTML != newValue) {
                                tableSummary.timeToLiveNode.lastChild.innerHTML += " ...wait";
                                u.post("/admin/rest/v1/group/modify", JSON.stringify({group_id:groupId, property:DATABASE.TIME_TO_LIVE_IF_EMPTY, value:newValue}))
                                    .catch(function(code,xhr){
                                        console.warn("Resign because of",code,xhr);
                                        WTU.resign(updateSummary);
                                        var res = JSON.parse(xhr.responseText) || {};
                                        u.toast.show(res.message || xhr.statusText);
                                    });
                            }
                        }
                    },
                    negative: { label: u.create(HTML.SPAN, "Cancel")}
                }).open();
            },
            cells: [
                { className: "th", innerHTML: "&#150; time to live, min" },
                { className: "option", innerHTML: "..." }
            ]
        });

        tableSummary.dismissInactiveNode = tableSummary.add({
            onclick: function() {
                this.lastChild.innerHTML += " ...wait";
                u.post("/admin/rest/v1/group/switch", JSON.stringify({group_id:groupId, property:DATABASE.DISMISS_INACTIVE}))
                    .catch(function(code,xhr){
                        console.warn("Resign because of",code,xhr);
                        WTU.resign(updateSummary);
                        var res = JSON.parse(xhr.responseText) || {};
                        u.toast.show(res.message || xhr.statusText);
                    });
            },
            cells: [
                { className: "th", innerHTML: "Dismiss inactive" },
                { className: "option", innerHTML: "..." }
            ]});

        tableSummary.delayToDismissNode = tableSummary.add({
            className: "hidden",
            onclick: function() {
                u.dialog({
                    title: "Delay to dismiss",
                    items: [
                        { type:HTML.DIV, innerHTML:"Switch user offline if he is not active at least (in seconds)." },
                        { type:HTML.NUMBER, label:"Delay to dismiss, sec", value:parseInt(tableSummary.delayToDismissNode.lastChild.innerHTML || 300) }
                    ],
                    positive: {
                        label: u.create(HTML.SPAN, "OK"),
                        onclick: function(items) {
                            var newValue = items[1].value;
                            if(tableSummary.delayToDismissNode.lastChild.innerHTML != newValue) {
                                tableSummary.delayToDismissNode.lastChild.innerHTML += " ...wait";
                                u.post("/admin/rest/v1/group/modify", JSON.stringify({group_id:groupId, property:DATABASE.DELAY_TO_DISMISS, value:newValue}))
                                    .catch(function(code,xhr){
                                        console.warn("Resign because of",code,xhr);
                                        WTU.resign(updateSummary);
                                        var res = JSON.parse(xhr.responseText) || {};
                                        u.toast.show(res.message || xhr.statusText);
                                    });
                            }
                        }
                    },
                    negative: { label: u.create(HTML.SPAN, "Cancel")}
                }).open();
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

        function filterActive(row){
            return !row.classList.contains("inactive");
        }
        tableSummary.usersNode = tableSummary.add({
            onclick: function(e){
                tableUsers.filter.remove(filterActive);
            },
            cells: [
                { className: "th", innerHTML: "Users total" },
                { className: "option highlight", innerHTML: 0 }
            ]});

        tableSummary.activeUsersNode = tableSummary.add({
            onclick: function(e){
                tableUsers.filter.add(filterActive);
            },
            cells: [
                { className: "th", innerHTML: "&#150; online" },
                { className: "option highlight", innerHTML: 0 }
            ]});

        divMapGroup = u.create(HTML.DIV, {className: "map"}, u.create(HTML.DIV, {
            className: "map-place"
        }, divSummaryMap));

        u.create("br", null, div);
        buttons = u.create("div", {className:"buttons"}, div);
        renderButtons(buttons);

        u.create(HTML.H2, "Users", div);

        tableUsers = u.table({
            id: "admin:users",
            caption: {
                items: [
                    { label: "#", width: "5%" },
                    { label: "Name" },
                    { label: "Color", width: "5%" },
                    { label: "Created", className: "media-hidden" },
                    { label: "Updated" },
                    { label: "Platform", className: "media-hidden" },
                    { label: "Device", className: "media-hidden" },
                    { label: "Sign provider", className: "media-hidden" }
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
        } else {
            updateAll();
        }

    };

    function updateSummary() {
        if(!groupId) {
            WTU.switchTo("/admin/groups/");
            return;
        }

        var ref = database.ref();

        ref.child(groupId).child(DATABASE.OPTIONS).once("value").then(function(snapshot) {
            if(!snapshot || !snapshot.val()) return;

            ref.child(groupId).child(DATABASE.OPTIONS).off();
            ref.child(groupId).child(DATABASE.OPTIONS).on("value", function(snapshot){
                if(!snapshot.val()) return;

//                tableSummary.requiresPasswordNode.lastChild.innerHTML = snapshot.val()[DATABASE.REQUIRES_PASSWORD] ? "Yes" : "No";
//                tableSummary.passwordNode[snapshot.val()[DATABASE.REQUIRES_PASSWORD] ? "show":"hide"]();

                tableSummary.welcomeMessageNode.lastChild.innerHTML = snapshot.val()[DATABASE.WELCOME_MESSAGE] || "";

                tableSummary.persistentNode.lastChild.innerHTML = snapshot.val()[DATABASE.PERSISTENT] ? "Yes" : "No";
                tableSummary.timeToLiveNode[snapshot.val()[DATABASE.PERSISTENT] ? "hide":"show"]();

                tableSummary.timeToLiveNode.lastChild.innerHTML = snapshot.val()[DATABASE.TIME_TO_LIVE_IF_EMPTY] || 15;

                tableSummary.dismissInactiveNode.lastChild.innerHTML = snapshot.val()[DATABASE.DISMISS_INACTIVE] ? "Yes" : "No";
                tableSummary.delayToDismissNode[snapshot.val()[DATABASE.DISMISS_INACTIVE] ? "show":"hide"]();

                tableSummary.delayToDismissNode.lastChild.innerHTML = snapshot.val()[DATABASE.DELAY_TO_DISMISS] || 300;

                tableSummary.createdNode.lastChild.innerHTML = new Date(snapshot.val()[DATABASE.CREATED]).toLocaleString();
//                tableSummary.changedNode.lastChild.innerHTML = new Date(snapshot.val()[DATABASE.CHANGED]).toLocaleString();

            });
        }).catch(function(error){
            console.warn("Resign because of",error);
            WTU.resign(updateAll);
        });

    }

    function updateAll() {
        updateSummary();
        updateData();
    }

    function updateData(){

        var ref = database.ref();
        tableUsers.placeholder.show();
        u.clear(tableUsers.body);
        var reload = false;
        var initial = true;
        setTimeout(function(){initial = false;}, 3000);

        tableSummary.usersNode.lastChild.innerHTML = 0;
        tableSummary.activeUsersNode.lastChild.innerHTML = 0;

        ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).off();
        ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PRIVATE).off();

        ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).on("child_added", function(snapshot) {
            if(!snapshot || !snapshot.val()){
                tableUsers.placeholder.show("No users");
                return;
            }
            reload = false;
            var userNumber = snapshot.key;

            var row = tableUsers.add({
                className: "italic highlight" + (snapshot.val()[DATABASE.ACTIVE] ? "" : " inactive"),
                onclick: function(){
                    WTU.switchTo("/admin/user/"+groupId+"/"+userNumber);
                    return false;
                },
                cells: [
                    { innerHTML: userNumber, sort: parseInt(userNumber) },
                    { innerHTML: snapshot.val()[DATABASE.NAME] },
                    { style: { backgroundColor: utils.getHexColor(snapshot.val()[DATABASE.COLOR]), opacity: 0.5 } },
                    { className: "media-hidden", sort: snapshot.val()[DATABASE.CREATED], innerHTML: snapshot.val()[DATABASE.USER_CREATED] ? new Date(snapshot.val()[DATABASE.USER_CREATED]).toLocaleString() : "&#150;" },
                    { sort: 0, innerHTML: "..." },
                    { className: "media-hidden", innerHTML: "..." },
                    { className: "media-hidden", innerHTML: "..." },
                    { className: "media-hidden", innerHTML: "..." }
                ]
            });
            var userNameNode = row.cells[1];
            var userChangedNode = row.cells[4];
            var userOsNode = row.cells[5];
            var userDeviceNode = row.cells[6];
            var userSignProviderNode = row.cells[7];

            tableSummary.usersNode.lastChild.innerHTML = +tableSummary.usersNode.lastChild.innerHTML + 1;
            if(snapshot.val()[DATABASE.ACTIVE]) {
                tableSummary.activeUsersNode.lastChild.innerHTML = +tableSummary.activeUsersNode.lastChild.innerHTML + 1;
            }

            ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(userNumber).child(DATABASE.CHANGED).on("value", function(snapshot){
                if(!snapshot.val()) return;
                userChangedNode.sort = snapshot.val();
                userChangedNode.innerHTML = new Date(snapshot.val()).toLocaleString();
                if(!initial) row.classList.add("changed");
                setTimeout(function(){row.classList.remove("changed")}, 5000);
                tableSummary.changedNode.lastChild.innerHTML = new Date(snapshot.val()).toLocaleString();
                tableUsers.update();
            });
            ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(userNumber).child(DATABASE.ACTIVE).on("value", function(snapshot){
                var active = !!snapshot.val();
                var wasInactive = row.classList.contains("inactive");
                row.classList[active ? "remove" : "add"]("inactive");
                var usersCount = +tableSummary.activeUsersNode.lastChild.innerHTML;
                if(active && wasInactive) {
                    tableSummary.activeUsersNode.lastChild.innerHTML = ++usersCount;
                } else if(!active && !wasInactive) {
                    tableSummary.activeUsersNode.lastChild.innerHTML = --usersCount;
                }
                tableUsers.update();
            });
            ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(userNumber).child(DATABASE.NAME).on("value", function(snapshot){
                userNameNode.innerHTML = snapshot.val() || "&lt;Friend "+userNumber+"&gt;";
                tableUsers.update();
            });
            ref.child(groupId).child(DATABASE.PUBLIC).child("tracking").child(userNumber).limitToLast(1).on("child_added", function(snapshot){
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
                            for(var x in positions) {
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
                        markers[userNumber].addListener("mouseover", function(e){
                            this.row.classList.add("selected");
                        });
                        markers[userNumber].addListener("mouseout", function(e){
                            this.row.classList.remove("selected");
                        });
                    }
                }
            });
            ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(userNumber).once("value").then(function(snapshot){
                if(!snapshot.val()) return;
                userOsNode.innerHTML = snapshot.val()[REQUEST.OS];
                userDeviceNode.innerHTML = snapshot.val()[REQUEST.MODEL];
                userSignProviderNode.innerHTML = snapshot.val()[REQUEST.SIGN_PROVIDER] || "anonymous";
                tableUsers.update();
            }).catch(function(error){
                if(!reload) {
                    reload = true;
                    console.warn("Resign because of",error);
                    WTU.resign(updateData);
                } else {
//                    console.error("ERROR, ALREADY RESIGNING");
                }
            });
        }, function(error){
            console.warn("Resign because of",error);
            WTU.resign(updateAll);
        });

    }

    function renderButtons(div) {
        u.clear(div);
        u.create(HTML.BUTTON, { innerHTML:"Delete group", onclick: deleteGroupQuestion}, div);
    }

    function deleteGroupQuestion(e){
        u.clear(buttons);
        u.create({className:"question", innerHTML: "Are you sure you want to delete group "+groupId+"?"}, buttons);
        u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: function() {
            u.post("/admin/rest/v1/group/delete", JSON.stringify({group_id:groupId}))
                .then(function(){
                    WTU.switchTo("/admin/groups");
                    u.toast.show("Group "+groupId+" was deleted.");
                }).catch(function(code,xhr){
                console.warn("Resign because of",code,xhr);
                WTU.resign(updateSummary);
                var res = JSON.parse(xhr.responseText) || {};
                u.toast.show(res.message || xhr.statusText);
                renderButtons(buttons);
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
            rotateControl: true,
        });
        positions = {};
        markers = {};

        updateAll();
    }

    return {
        start: function(request) {
            if(request) {
                this.page = request[2] + "/" + request[3];
                groupId = request[3];
            } else {
                var parts = window.location.pathname.split("/");
                this.page = parts[2] + "/" + parts[3];
                groupId = parts[3];
            }
            div = document.getElementsByClassName("layout")[0];
            u.clear(div);

            renderInterface();
        },
        page: "group",
        title: title,
        move:true
    }
}


