/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 10/12/17.
 */
function AccountHolder(main) {

     this.type = "account";
     this.title = "Account";

    var div;
    var buttons;
    var accountId;
    var tableSummary;
    var tableHistory;
    var utils = main.arguments.utils;

    var modes = {
        ra: "Add remote value",
        ru: "Update remote value",
        rr: "Remove remote value",
        or: "Override remote value",
        ro: "Override remote value",
        la: "Add local value",
        lo: "Override local value",
        lu: "Update local value",
        lr: "Remove local value"
    };
    modes[DATABASE.STAT_GROUPS_CREATED_PERSISTENT] = "Group created (persistent)";
    modes[DATABASE.STAT_GROUPS_CREATED_TEMPORARY] = "Group created (temporary)";
    modes[DATABASE.STAT_GROUPS_DELETED] = "Group deleted";
    modes[DATABASE.STAT_GROUPS_REJECTED] = "Group rejected";
    modes[DATABASE.STAT_USERS_JOINED] = "User joined";
    modes[DATABASE.STAT_USERS_RECONNECTED] = "User reconnected";
    modes[DATABASE.STAT_USERS_REJECTED] = "User rejected";

    var keys = {
        "p/ch": "Updated",
        "p/tos-confirmed": "Terms of service confirmed",
        "p/name": "Name",
        "group": "Group"
    };

    this.start = function() {
         database = firebase.database();
         div = document.getElementsByClassName("layout")[0];
     }

    this.resume = function(account_id) {
        accountId = account_id;
        window.history.pushState({}, null, "/admin/account/" + accountId);

         u.clear(div);

        u.create(HTML.H2, "Summary", div);
        u.create(HTML.H4, "No real-time updating", div);

        tableSummary = u.table({
            className: "option"
        }, div);

        tableSummary.accountNameItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Name" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountStatusItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Status" },
                { className:"option", innerHTML: "..." }
            ]
        });
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
        tableSummary.accountSyncedItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Last sync" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountModelItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Device model" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountOsItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "OS" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountSignProviderItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Sign provider" },
                { className:"option", innerHTML: "..." }
            ]
        });
        tableSummary.accountHistoryCountItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "History items" },
                { className:"option", innerHTML: "0" }
            ]
        });

        u.create("br", null, div);
        buttons = u.create("div", {className:"buttons"}, div);
        renderButtons(buttons);

        u.create(HTML.H2, "History", div);

        tableHistory = u.table({
            id: "admin:account:history",
            className: "account-history",
            caption: {
                items: [
                    { label: "Timestamp" },
                    { label: "Action", selectable: true },
                    { label: "Key", selectable: true },
                    { label: "Value" }
                ]
            },
            placeholder: "Loading..."
        }, div);

        updateAll();
        return div;

    };

    function updateSummary() {
        if(!accountId) {
            main.turn("accounts");
            return;
        }

        var ref = database.ref();

        ref.child(DATABASE.SECTION_USERS).child(accountId).child(DATABASE.PRIVATE).once("value").then(function(snapshot) {
            if(!snapshot || !snapshot.val()) return;

            var privateData = snapshot.val();

            tableSummary.accountNameItem.lastChild.innerHTML = u.clear(privateData[DATABASE.NAME] || "&#150;");
            tableSummary.accountUidItem.lastChild.innerHTML = accountId;

            var expired = false;
            var trusted = false;
            if(privateData[REQUEST.SIGN_PROVIDER] === "anonymous") {
                if (new Date().getTime() - privateData[DATABASE.CHANGED] > 30 * 24 * 60 * 60 * 1000) expired = true;
            } else if(privateData[REQUEST.SIGN_PROVIDER] === "password") {
                trusted = true;
            } else {
                trusted = true;
            }
            tableSummary.accountStatusItem.lastChild.innerHTML = expired ? "Expired" : (trusted ? "Trusted" : "Waiting");

            tableSummary.accountModelItem.lastChild.innerHTML = u.clear(privateData[REQUEST.MODEL] || "&#150;");
            tableSummary.accountSignProviderItem.lastChild.innerHTML = u.clear(privateData[REQUEST.SIGN_PROVIDER] || "&#150;");
            tableSummary.accountOsItem.lastChild.innerHTML = u.clear(privateData[REQUEST.OS] || "&#150;");
            tableSummary.accountCreatedItem.lastChild.innerHTML = new Date(privateData[DATABASE.CREATED]).toLocaleString();
            tableSummary.accountUpdatedItem.lastChild.innerHTML = privateData[DATABASE.CHANGED] ? new Date(privateData[DATABASE.CHANGED]).toLocaleString() + " (" + utils.toDateString(new Date().getTime() - new Date(privateData[DATABASE.CHANGED])) + " ago)" : "never";
            tableSummary.accountSyncedItem.lastChild.innerHTML = privateData[DATABASE.SYNCED] ? new Date(privateData[DATABASE.SYNCED]).toLocaleString() + " (" + utils.toDateString(new Date().getTime() - new Date(privateData[DATABASE.SYNCED])) + " ago)" : "never";

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

        setTimeout(function() {
            if(tableHistory.rows.length === 0){
                tableHistory.placeholder.show("No history");
            }
        }, 1000);

        ref.child(DATABASE.SECTION_USERS).child(accountId).child(DATABASE.PRIVATE).child(DATABASE.HISTORY).off();
        ref.child(DATABASE.SECTION_USERS).child(accountId).child(DATABASE.PRIVATE).child(DATABASE.HISTORY).on("child_added", function(snapshot) {

            setTimeout(function(){
                var snapshot = this;
                reload = false;
                tableHistory.add({
                    className: "highlight"/* + (snapshot.val()[DATABASE.ACTIVE] ? "" : " inactive")*/,
                    tabindex: -1,
                    cells: [
                        { innerHTML: new Date(snapshot.val()[DATABASE.TIMESTAMP]).toLocaleString(), sort: snapshot.val()[DATABASE.TIMESTAMP] },
                        { innerHTML: u.clear(modes[snapshot.val()[DATABASE.MODE]] || snapshot.val()[DATABASE.MODE]) },
                        { innerHTML: u.clear(keys[snapshot.val()[DATABASE.KEYS]] || snapshot.val()[DATABASE.KEYS]) },
                        { innerHTML: u.clear(snapshot.val()[DATABASE.VALUE] || "&#150;") }
                    ]
                });
                tableSummary.accountHistoryCountItem.lastChild.innerHTML = +tableSummary.accountHistoryCountItem.lastChild.innerHTML + 1;
            }.bind(snapshot), 0);

        }, function(error){
            console.warn("Resign because of",error);
            WTU.resign(updateAll);
        });

    }

    function renderButtons(div) {
        u.clear(div);
        u.create(HTML.BUTTON, { innerHTML:"Delete account", onclick: deleteAccount}, div);
    }

    function deleteAccount(e){
        u.clear(buttons);
        u.create({className:"question", innerHTML: "Are you sure you want to delete account "+accountId+"?"}, buttons);
        u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: function() {
            u.post("/admin/rest/account/delete", JSON.stringify({uid:accountId}))
                .then(function(){
                    main.turn("accounts");
                    u.toast.show("Account " + accountId + " was deleted.");
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

}
