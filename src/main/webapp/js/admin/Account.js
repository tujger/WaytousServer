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
    var accountId;
    var userNumber;
    var tableSummary;
    var tableHistory;
    var divMap;
    var map;
    var bounds;
    var drawTrackTask;
    var track;
    var limit = 1000;

    var renderInterface = function() {

        var ref = database.ref();

        u.create(HTML.H2, "Summary", div);

        tableSummary = u.table({
            className: "option",
        }, div);

        tableSummary.accountUidItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "UID" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountCreatedItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Created" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountUpdatedItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Updated" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountTrustedItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Trusted" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountNameItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Name" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountModelItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Model" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountSignProviderItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Sign provider" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountOsItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "OS" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountSyncedItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Last sync" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountHistoryCountItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "History items" },
                { className:"option", innerHTML: "0" }
            ]
        });

        var accountsTitleNode = u.create(HTML.H2, "History", div);
//        buttons = u.create("div", {className:"buttons"}, accountsTitleNode);
//        renderButtons(buttons);

        tableHistory = u.table({
            id: "admin:account:history",
            caption: {
                items: [
                    { label: "Timestamp" },
                    { label: "Action" },
                    { label: "Key" },
                    { label: "Value" },
                ]
            },
            placeholder: "Loading..."
        }, div);

        updateAll();
        return div;

    };

    function updateSummary() {
        if(!accountId) {
            WTU.switchTo("/admin/accounts/");
            return;
        }

        var ref = database.ref();

        ref.child(DATABASE.SECTION_USERS).child(accountId).child(DATABASE.PRIVATE).once("value").then(function(snapshot) {
            if(!snapshot || !snapshot.val()) return;


            tableSummary.accountNameItem.lastChild.innerHTML = snapshot.val()[DATABASE.NAME] || "";
            tableSummary.accountUidItem.lastChild.innerHTML = accountId;
    //        tableSummary.accountTrustedItem.lastChild.innerHTML = snapshot.val()[DATABASE.WELCOME_MESSAGE] || "";
            tableSummary.accountModelItem.lastChild.innerHTML = snapshot.val()[REQUEST.MODEL] || "";
            tableSummary.accountSignProviderItem.lastChild.innerHTML = snapshot.val()[REQUEST.SIGN_PROVIDER] || "";
            tableSummary.accountOsItem.lastChild.innerHTML = snapshot.val()[REQUEST.OS] || "";
            tableSummary.accountCreatedItem.lastChild.innerHTML = new Date(snapshot.val()[DATABASE.CREATED]).toLocaleString();
            tableSummary.accountUpdatedItem.lastChild.innerHTML = new Date(snapshot.val()[DATABASE.CHANGED]).toLocaleString() + " (" + utils.toDateString(new Date().getTime() - new Date(snapshot.val()[DATABASE.CHANGED])) + " ago)";
            tableSummary.accountSyncedItem.lastChild.innerHTML = new Date(snapshot.val()[DATABASE.SYNCED]).toLocaleString() + " (" + utils.toDateString(new Date().getTime() - new Date(snapshot.val()[DATABASE.SYNCED])) + " ago)";


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
        tableHistory.placeholder.show();
        u.clear(tableHistory.body);
        var reload = false;
        var initial = true;
        setTimeout(function(){initial = false;}, 3000);

        var modes = {
            or: "Override remote",
            ol: "Override local",
        };
        var keys = {
            "p/ch": "Updated",
        };


        ref.child(DATABASE.SECTION_USERS).child(accountId).child(DATABASE.PRIVATE).child(DATABASE.HISTORY).off();
        ref.child(DATABASE.SECTION_USERS).child(accountId).child(DATABASE.PRIVATE).child(DATABASE.HISTORY).on("child_added", function(snapshot) {

            if(!snapshot || !snapshot.val()){
                tableHistory.placeholder.show("No locations");
                return;
            }
            reload = false;

            var lat = snapshot.val()[USER.LATITUDE];
            var lng = snapshot.val()[USER.LONGITUDE];

            var row = tableHistory.add({
                className: "locations-row highlight"/* + (snapshot.val()[DATABASE.ACTIVE] ? "" : " inactive")*/,
                tabindex: -1,
                cells: [
                    { innerHTML: new Date(snapshot.val()[DATABASE.TIMESTAMP]).toLocaleString(), sort: snapshot.val()[DATABASE.TIMESTAMP] },
                    { innerHTML: modes[snapshot.val()[DATABASE.MODE]] },
                    { innerHTML: keys[snapshot.val()[DATABASE.KEYS]] },
                    { innerHTML: snapshot.val()[DATABASE.VALUE] },
                ]
            });

            tableSummary.accountHistoryCountItem.lastChild.innerHTML = +tableSummary.accountHistoryCountItem.lastChild.innerHTML + 1;

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
            WTU.resign(updateAll);
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
                WTU.resign(updateAll);
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
                accountId = request[3];
            } else {
                var parts = window.location.pathname.split("/");
                this.page = parts[2] + "/" + parts[3];
                accountId = parts[3];
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
