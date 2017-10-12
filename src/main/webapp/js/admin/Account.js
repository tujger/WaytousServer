/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 10/12/17.
 */
function Account() {

    var title = "Account";

    var positions;
    var div;
    var groupId;
    var userId;
    var userNumber;
    var tableSummary;
    var tableLocations;
    var divMap;
    var map;
    var bounds;
    var drawTrackTask;
    var track;
    var limit = 1000;

    var renderInterface = function() {

        var ref = database.ref();

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
                WTU.switchTo("/admin/group/"+groupId);
                return false;
            },
            cells: [
                { className: "th", innerHTML: "Group" },
                { className: "option", innerHTML: groupId }
            ]
        });

        tableSummary.userUidNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "UID" },
            { className: "option", innerHTML: "..." }
        ] });

        tableSummary.userColorNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Color" },
            { style: { /*backgroundColor: utils.getHexColor(snapshot.val().color), */opacity: 0.5 } }
        ]});

        tableSummary.userCreatedNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Created" },
            { className: "option", innerHTML: "..." }
        ]});

        tableSummary.userUpdatedNode = tableSummary.add({
//            style: { display: (snapshot.val().persistent ? "none" : "")},
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
            caption: {
                items: [
                    { label: "Timestamp", className: "media-hidden" },
                    { label: "Provider" },
                    { label: "Latitude" },
                    { label: "Longitude" },
                    { label: "Accuracy", className: "media-hidden" },
                    { label: "Bearing", className: "media-hidden" },
                    { label: "Speed", className: "media-hidden" }
                ]
            },
            placeholder: "Loading..."
        }, div);

        updateAll();
        return div;

    };

    function updateSummary() {
        var ref = database.ref();
        tableSummary.placeholder.show();

    }

    function updateAll() {
        updateSummary();
        updateData();
    }

    function updateData(){

        var ref = database.ref();
        tableLocations.placeholder.show();
        u.clear(tableLocations.body);
        var reload = false;
        var initial = true;
        setTimeout(function(){initial = false;}, 3000);

        ref.child(DATABASE.SECTION_USERS).off();
        ref.child(DATABASE.SECTION_USERS).on("child_added", function(snapshot) {

            if(!snapshot || !snapshot.val()){
                tableLocations.placeholder.show("No locations");
                return;
            }
            reload = false;

            var lat = snapshot.val()[USER.LATITUDE];
            var lng = snapshot.val()[USER.LONGITUDE];

            var row = tableLocations.add({
                className: "locations-row highlight"/* + (snapshot.val()[DATABASE.ACTIVE] ? "" : " inactive")*/,
                tabindex: -1,
                cells: [
                    { className: "media-hidden", innerHTML: new Date(snapshot.val()[REQUEST.TIMESTAMP]).toLocaleString(), sort: snapshot.val()[REQUEST.TIMESTAMP] },
                    { innerHTML: snapshot.val()[USER.PROVIDER] },
                    { innerHTML: lat },
                    { innerHTML: lng },
                    { className: "media-hidden", innerHTML: snapshot.val()[USER.ACCURACY] },
                    { className: "media-hidden", innerHTML: snapshot.val()[USER.BEARING] },
                    { className: "media-hidden", innerHTML: snapshot.val()[USER.SPEED] }
                ]
            });

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

        }, function(error){
            console.warn("Resign because of",error);
            WTU.resign(updateAll);
        });

    }

    function renderButtons(div) {
        u.clear(div);
        u.create(HTML.BUTTON, { innerHTML:"Switch activity", onclick: switchActivity}, div);
        u.create(HTML.BUTTON, { innerHTML:"Remove", onclick: removeUser}, div);
    }

    function switchActivity(e){
        var ref = database.ref();
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
        var ref = database.ref();
        u.post("/admin/rest/v1/user/switch", JSON.stringify({group_id:groupId, user_number:userNumber,property:DATABASE.ACTIVE,value:active}))
            .then(function(){
                u.progress.hide();
                if(!active) {
                    u.toast.show("User #"+userNumber+" is offline.");
                    WTU.switchTo("/admin/group/" + groupId);
                } else {
                    u.toast.show("User #"+userNumber+" is online.");
                }
            }).catch(function(code,xhr){
            u.progress.hide();
            console.warn("Resign because of",code,xhr);
            WTU.resign(updateSummary);
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
            u.post("/admin/rest/v1/user/remove", JSON.stringify({group_id:groupId, user_number:userNumber}))
                .then(function(){
                    u.progress.hide();
                    u.toast.show("User #"+userNumber+" was removed.");
                    WTU.switchTo("/admin/group/" + groupId);
                }).catch(function(code,xhr){
                u.progress.hide();
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

    return {
        start: function(request) {
            if(request) {
                this.page = request[2] + "/" + request[3];
                userId = request[3];
            } else {
                var parts = window.location.pathname.split("/");
                this.page = parts[2] + "/" + parts[3];
                userId = parts[3];
            }
            div = document.getElementsByClassName("layout")[0];
            u.clear(div);

            renderInterface();
        },
        page: "account",
        icon: "person",
        title: title,
        move:true
    }
}
