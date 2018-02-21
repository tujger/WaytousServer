/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 1/23/17.
 */
function UserHolder(main) {
    var positions;
    var div;
    var groupId;
    var userNumber;
    var tableSummary;
    var tableLocations;
    var divMap;
    var map;
    var bounds;
    var drawTrackTask;
    var track;
    var limit = 1000;
    var marker;

     this.type = "user";
     this.title = "User";

     var tableSummary;
     var tableGroups;
     var div;
     var ref;
     var database;
     var utils = main.arguments.utils;

     this.start = function() {
         database = firebase.database();

         div = document.getElementsByClassName("layout")[0];

//         if(request) {
//             groupId = request[3];
//             userNumber = request[4];
//         } else {
//             var parts = window.location.pathname.split("/");
//             groupId = parts[3];
//             userNumber = parts[4];
//         }
//         this.page = "user" + "/" + groupId + "/" + userNumber;
//         div = document.getElementsByClassName("layout")[0];
//         u.clear(div);
//
//         renderInterface();
     }

    this.resume = function(group_id, user_number) {
        groupId = group_id;
        userNumber = user_number;

        window.history.pushState({}, null, "/admin/user/" + groupId + "/" + userNumber);

        u.clear(div);

        u.create(HTML.H2, "Summary", div);

        var divSummaryMap = u.create(HTML.DIV, {className: "two-divs"}, div);
        var divSummary = u.create(HTML.DIV, {className: "summary-place"}, divSummaryMap);

        tableSummary = u.table({
            className: "option",
            placeholder: "Loading..."
        }, divSummary);

        tableSummary.userNumberNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Number" },
            { className: "option", innerHTML: userNumber }
        ] });

        tableSummary.userNameNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Name" },
            { className: "option", innerHTML: "..." }
        ] });

        tableSummary.userActiveNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Active" },
            { className: "option highlight", innerHTML: "..." }
        ] });

        tableSummary.add({
            onclick: function(){
                main.turn("group", groupId);
                return false;
            },
            cells: [
                { className: "th", innerHTML: "Group" },
                { className: "option", innerHTML: groupId }
            ]
        });

        tableSummary.userUidNode = tableSummary.add({
            onclick: function(){
                if(tableSummary.userUidNode.cells[1].innerHTML && tableSummary.userUidNode.cells[1].innerHTML !== "[invalid]") {
                    main.turn("account", tableSummary.userUidNode.cells[1].innerHTML);
                }
                return false;
            },
            cells: [
                { className: "th", innerHTML: "UID" },
                { className: "option", innerHTML: "..." }
            ]
        });

        tableSummary.userColorNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Color" },
            { style: { /*backgroundColor: utils.getHexColor(snapshot.val().color), */opacity: 0.5 } }
        ]});

        tableSummary.userCreatedNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Created" },
            { className: "option", innerHTML: "..." }
        ]});

        tableSummary.userUpdatedNode = tableSummary.add({
            cells: [
                { className: "th", innerHTML: "Updated" },
                { className: "highlight option", innerHTML: "..." }
            ]
        });

        tableSummary.userOsNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Platform" },
            { className: "option", innerHTML: "..." }
        ]});

        tableSummary.userDeviceNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Device" },
            { className: "option", innerHTML: "..." }
        ]});

        tableSummary.userSignProviderNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Sign provider" },
            { className: "option", innerHTML: "..." }
        ]});

        tableSummary.userLocations = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Locations" },
            { className: "option", innerHTML: 0 }
        ]});

        divMap = u.create(HTML.DIV, {className: "map"}, u.create(HTML.DIV, {
            className: "map-place"
        }, divSummaryMap));

        u.create(HTML.BR, null, div);
        buttons = u.create({className:"buttons"}, div);
        renderButtons(buttons);

        u.create(HTML.H2, "Positions", div);

        tableLocations = u.table({
            id: "admin:locations",
            className: "user-locations",
            caption: {
                items: [
                    { label: "Timestamp" },
                    { label: "Provider", selectable: true },
                    { label: "Latitude" },
                    { label: "Longitude" },
                    { label: "Accuracy" },
                    { label: "Bearing" },
                    { label: "Speed" }
                ]
            },
            placeholder: "Loading..."
        }, div);

        if(divMap.offsetHeight) {
            if(!map && !(window.google && google.maps)) {
                window.initMap = initMap;
                u.require("https://maps.googleapis.com/maps/api/js?key="+data.firebase_config.apiKey+"&callback=initMap&libraries=geometry,places").then(function(){});
            } else {
                initMap();
            }
        } else {
            updateAll();
        }
        updateAll();
        return div;

    }

    function updateSummary() {
        var refRoot = database.ref();
        var refGroups = refRoot.child(DATABASE.SECTION_GROUPS);
        tableSummary.placeholder.show();

        refGroups.child(groupId).child(DATABASE.USERS).child(DATABASE.PRIVATE).off();
        refGroups.child(groupId).child(DATABASE.USERS).child(DATABASE.PRIVATE).child(userNumber).once("value").then(function(snapshot){
            if(!snapshot || !snapshot.val()) return;

            var uid = snapshot.val()[REQUEST.UID];
            tableSummary.userUidNode.lastChild.innerHTML = uid || "[invalid]";
            if(uid) {
                refRoot.child(DATABASE.SECTION_USERS).child(uid).child(DATABASE.PRIVATE).once("value")
                .then(function(snapshot){
                    if(snapshot.val()) {
                        tableSummary.userOsNode.lastChild.innerHTML = u.clear(snapshot.val()[REQUEST.OS] || "&#150;");
                        tableSummary.userDeviceNode.lastChild.innerHTML = u.clear(snapshot.val()[REQUEST.MODEL] || "&#150;");
                        tableSummary.userSignProviderNode.lastChild.innerHTML = u.clear(snapshot.val()[REQUEST.SIGN_PROVIDER] || "anonymous");
                    }
                });
            }
        }).catch(function(error){
            console.warn("Resign because of",error.message);
            window.location = window.location.href;
        });

        refGroups.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(userNumber).off();
        refGroups.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(userNumber).on("value",function(snapshot) {
            if(!snapshot || !snapshot.val()) return;

            tableSummary.placeholder.hide();

            tableSummary.userNameNode.lastChild.innerHTML = u.clear(snapshot.val()[DATABASE.NAME] || "&lt;Friend "+userNumber+"&gt;");
            tableSummary.userActiveNode.lastChild.innerHTML = snapshot.val()[DATABASE.ACTIVE] ? "Yes" : "No";
            tableSummary.userColorNode.lastChild.style.backgroundColor = utils.getHexColor(snapshot.val()[DATABASE.COLOR]);
            tableSummary.userCreatedNode.lastChild.innerHTML = snapshot.val()[DATABASE.CREATED] ? new Date(snapshot.val()[DATABASE.CREATED]).toLocaleString() : "&#150;";
            tableSummary.userUpdatedNode.lastChild.innerHTML = snapshot.val()[DATABASE.CHANGED] ? new Date(snapshot.val()[DATABASE.CHANGED]).toLocaleString() : "&#150;";

        },function(error){
            console.warn("Resign because of",error.message);
            window.location = window.location.href;
        });
    }

    function updateAll() {
        updateSummary();
        updateData();
    }

    function updateData(){
        var refRoot = database.ref();
        var refGroups = refRoot.child(DATABASE.SECTION_GROUPS);

        tableLocations.placeholder.show();
        u.clear(tableLocations.body);
        var reload = false;
        var initial = true;
        setTimeout(function(){initial = false;}, 3000);

        refGroups.child(groupId).child(DATABASE.PUBLIC).child("tracking").child(userNumber).off();
        refGroups.child(groupId).child(DATABASE.PUBLIC).child("tracking").child(userNumber).limitToLast(limit).on("child_added", function(snapshot) {

            if(!snapshot || !snapshot.val()){
                tableLocations.placeholder.show("No locations");
                return;
            }
            setTimeout(function(){
                var snapshot = this;

                reload = false;

                var lat = snapshot.val()[USER.LATITUDE];
                var lng = snapshot.val()[USER.LONGITUDE];

                var row = tableLocations.add({
                    className: "highlight",
                    tabindex: -1,
                    cells: [
                        { innerHTML: new Date(snapshot.val()[REQUEST.TIMESTAMP]).toLocaleString(), sort: snapshot.val()[REQUEST.TIMESTAMP] },
                        { innerHTML: u.clear(snapshot.val()[USER.PROVIDER]) },
                        { innerHTML: lat },
                        { innerHTML: lng },
                        { innerHTML: u.clear(snapshot.val()[USER.ACCURACY] || "&#150;") },
                        { innerHTML: u.clear(snapshot.val()[USER.BEARING] || "&#150;") },
                        { innerHTML: u.clear(snapshot.val()[USER.SPEED] || "&#150;") }
                    ],
                    onclick: function() {
                        if(!map) return;

                        var removeMarker = false;
                        var setMarker = false;

                        if(marker && marker.origin == this) {
                            removeMarker = true;
                        } else if(marker) {
                            removeMarker = true;
                            setMarker = true;
                        } else {
                            setMarker = true;
                        }
                        if(removeMarker) {
                            marker.setMap(null);
                            marker = null;
                        }
                        if(setMarker) {
                            var position = utils.latLng({coords:{latitude: this.snapshot[USER.LATITUDE], longitude:this.snapshot[USER.LONGITUDE]}});
                            marker = new google.maps.Marker({
                                position: position,
                                map: map
                            });
                            marker.origin = this;
                            map.setCenter(position);
                            map.setZoom(15);
                        }
                    }
                });
                row.snapshot = snapshot.val();

                tableSummary.userLocations.lastChild.innerHTML = +tableSummary.userLocations.lastChild.innerHTML + 1;
                if((+tableSummary.userLocations.lastChild.innerHTML) == limit) { tableSummary.userLocations.lastChild.innerHTML += " (restricted to " + limit + ")" }

                if(map) {
                    var position = utils.latLng({coords:{latitude:lat, longitude:lng}});
                    positions.push(position);
                    bounds.extend(position);
                    clearTimeout(drawTrackTask);
                    drawTrackTask = setTimeout(function(){
                        map.fitBounds(bounds);
                        map.fitBounds(bounds);
                        track = track || new google.maps.Polyline({
                            geodesic: true,
                            strokeColor: "blue",
                            strokeWeight: 2,
                            map: map
                        });
                        track.setPath(positions);
                    }, 100);
                }
            }.bind(snapshot), 0);


        }, function(error){
            console.warn("Resign because of",error);
            window.location = window.location.href;
        });
    }

    function renderButtons(div) {
        u.clear(div);
        u.create(HTML.BUTTON, { innerHTML:"Switch activity", onclick: switchActivity}, div);
        u.create(HTML.BUTTON, { innerHTML:"Remove", onclick: removeUser}, div);
    }

    function switchActivity(){
        u.clear(buttons);
        u.create(HTML.BUTTON,{innerHTML:"Active", onclick: function(){
            switchActive(userNumber, true);
            renderButtons(buttons);
        }}, buttons);
        u.create(HTML.BUTTON,{innerHTML:"Inactive", onclick: function(){
            switchActive(userNumber, false);
        }}, buttons);
        u.create(HTML.BUTTON,{innerHTML:"Cancel", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
    }

    function switchActive(number, active) {
        u.progress.show("Switching...");
        u.post("/admin/rest/user/switch", JSON.stringify({group_id:groupId, user_number:userNumber,property:DATABASE.ACTIVE,value:active}))
            .then(function(){
                u.progress.hide();
                if(!active) {
                    u.toast.show("User #"+userNumber+" is offline.");
                    main.turn("group", groupId);
                } else {
                    u.toast.show("User #"+userNumber+" is online.");
                }
            }).catch(function(code,xhr){
            u.progress.hide();
            console.warn("Resign because of",code,xhr);
            updateSummary();
            var res = JSON.parse(xhr.responseText) || {};
            u.toast.show(res.message || xhr.statusText);
            renderButtons(buttons);
        });
    }

    function removeUser() {
        u.clear(buttons);
        u.create({className:"question", innerHTML: "Are you sure you want to remove user "+userNumber+" from group "+groupId+"? Note that all user information will be removed from group."}, buttons);
        u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: function() {
            u.progress.show("Removing...");
            u.post("/admin/rest/user/remove", JSON.stringify({group_id:groupId, user_number:userNumber}))
                .then(function(){
                    u.progress.hide();
                    u.toast.show("User #"+userNumber+" was removed.");
                    main.turn("group", groupId);
                }).catch(function(code,xhr){
                u.progress.hide();
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
        map = new google.maps.Map(divMap, {
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
        positions = [];
        track = null;
        bounds = new google.maps.LatLngBounds();

        u.create(HTML.BUTTON, {
            className: "map-place-switch icon notranslate",
            innerHTML: "flip_to_front",
            onclick: function() {
                if(divMap.parentNode.classList.contains("modal")) {
                    divMap.parentNode.classList.remove("modal");
                } else {
                    divMap.parentNode.classList.add("modal");
                }

            }
        }, divMap);
        updateAll();
    }
}
