/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 10/12/17.
 */
function Accounts() {

    var title = "Accounts";

    var positions;
    var div;
    var groupId;
    var userNumber;
    var tableSummary;
    var tableAccounts;
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

        function filterActive(row){
            return !row.classList.contains("inactive");
        }
        function filterExpired(row){
            return row.classList.contains("disabled");
        }
        function filterRecent(row){
            return (new Date().getTime() - row.cells[2].sort) < 24*60*60*1000;
        }
        function filterTrusted(row){
            return row.trusted;
        }

        tableSummary.accountsTotalItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Accounts" },
                { className:"option", innerHTML: "0" }
            ],
            onclick: function(e){
                tableAccounts.filter.remove(filterActive);
                tableAccounts.filter.remove(filterRecent);
                tableAccounts.filter.remove(filterExpired);
                tableAccounts.filter.remove(filterTrusted);
            }
        });
        tableSummary.accountsActiveItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; active" },
                { className:"option", innerHTML: "0" }
            ],
            onclick: function(e){
                tableAccounts.filter.remove(filterExpired);
                tableAccounts.filter.remove(filterRecent);
                tableAccounts.filter.remove(filterTrusted);
                tableAccounts.filter.add(filterActive);
            }
        });
        tableSummary.accountsExpiredItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; expired" },
                { className:"option", innerHTML: "0" }
            ],
            onclick: function(e){
                tableAccounts.filter.remove(filterActive);
                tableAccounts.filter.remove(filterRecent);
                tableAccounts.filter.remove(filterTrusted);
                tableAccounts.filter.add(filterExpired);
            }
        });
        tableSummary.accountsTrustedItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; trusted" },
                { className:"option", innerHTML: "0" }
            ],
            onclick: function(e){
                tableAccounts.filter.remove(filterActive);
                tableAccounts.filter.remove(filterRecent);
                tableAccounts.filter.remove(filterExpired);
                tableAccounts.filter.add(filterTrusted);
            }
        });
        tableSummary.accountsRecentItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; active today" },
                { className:"option", innerHTML: "0" }
            ],
            onclick: function(e){
                tableAccounts.filter.remove(filterExpired);
                tableAccounts.filter.remove(filterActive);
                tableAccounts.filter.remove(filterTrusted);
                tableAccounts.filter.add(filterRecent);
            }
        });
        tableSummary.lastRegisteredItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Last registered" },
                { className:"option", innerHTML: "..." }
            ],
            title: "Click to open last registered account",
            onclick: function(e){
                if(tableSummary.lastRegisteredItem.uid) {
                    WTU.switchTo("/admin/account/"+tableSummary.lastRegisteredItem.uid);
                }
            }
        });
        tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Maintenance" },
                { className:"option", innerHTML: "" }
            ]
        });
        tableSummary.lastValidationItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; accounts cleaned" },
                { className:"option", innerHTML: "..." }
            ]
        });

        var accountsTitleNode = u.create(HTML.H2, "Accounts", div);
        renderButtons(accountsTitleNode);

        tableAccounts = u.table({
            id: "admin:accounts",
            className: "accounts",
            caption: {
               items: [
                    { label: "Name" },
                    { label: "Created" },
                    { label: "Updated" },
                    { label: "Sign Provider", selectable: true },
                    { label: "OS", selectable: true },
                    { label: "Model", selectable: true },
                ]
            },
            placeholder: "Loading..."
        }, div);

        updateAll();
        return div;

    };

    function updateSummary() {
        var ref = database.ref();

    }

    function updateAll() {
        updateSummary();
        updateData();
    }

    function updateData(){

        var ref = database.ref();
        tableAccounts.placeholder.show();
        u.clear(tableAccounts.body);
        var reload = false;
        var initial = true;
        setTimeout(function(){initial = false;}, 3000);
        var lastRegistered = {};
        lastRegistered[DATABASE.CREATED] = 0;

        ref.child(DATABASE.SECTION_USERS).off();
        ref.child(DATABASE.SECTION_USERS).on("child_added", function(snapshot) {

            setTimeout(function(){
                var snapshot = this;

                if(!snapshot || !snapshot.val()){
                    tableAccounts.placeholder.show("No accounts");
                    return;
                }
                reload = false;
                var privateData = snapshot.val()[DATABASE.PRIVATE];
                tableSummary.accountsTotalItem.lastChild.innerHTML = +tableSummary.accountsTotalItem.lastChild.innerHTML + 1;

                var expired = false;
                var trusted = false;
                var activeToday = true;
                if(privateData[REQUEST.SIGN_PROVIDER] == "anonymous") {
                    if(new Date().getTime() - privateData[DATABASE.CHANGED] > 30*24*60*60*1000) expired = true;
                } else {
                    trusted = true;
                }
                if(new Date().getTime() - privateData[DATABASE.CHANGED] > 24*60*60*1000) activeToday = false;

                if(lastRegistered[DATABASE.CREATED] < privateData[DATABASE.CREATED]) {
                    lastRegistered = privateData;
                    lastRegistered[REQUEST.UID] = snapshot.key;
                    tableSummary.lastRegisteredItem.lastChild.innerHTML = new Date(lastRegistered[DATABASE.CREATED]).toLocaleString();
                    tableSummary.lastRegisteredItem.uid = u.clear(lastRegistered[REQUEST.UID]);
                }

                if(activeToday) tableSummary.accountsRecentItem.lastChild.innerHTML = +tableSummary.accountsRecentItem.lastChild.innerHTML + 1;
                if(expired) tableSummary.accountsExpiredItem.lastChild.innerHTML = +tableSummary.accountsExpiredItem.lastChild.innerHTML + 1;
                if(!expired) tableSummary.accountsActiveItem.lastChild.innerHTML = +tableSummary.accountsActiveItem.lastChild.innerHTML + 1;
                if(trusted) tableSummary.accountsTrustedItem.lastChild.innerHTML = +tableSummary.accountsTrustedItem.lastChild.innerHTML + 1;

                var row = tableAccounts.add({
                    className: "accounts-row highlight inactive",
                    onclick: function(){
                        WTU.switchTo("/admin/account/"+snapshot.key);
                        return false;
                    },
                    cells: [
                        { innerHTML: u.clear(privateData[DATABASE.NAME]) },
                        { innerHTML: new Date(privateData[DATABASE.CREATED]).toLocaleString(), sort: privateData[DATABASE.CREATED] },
                        { innerHTML: new Date(privateData[DATABASE.CHANGED]).toLocaleString(), sort: privateData[DATABASE.CHANGED] },
                        { innerHTML: u.clear(privateData[REQUEST.SIGN_PROVIDER]) },
                        { innerHTML: u.clear(privateData[REQUEST.OS]) },
                        { innerHTML: u.clear(privateData[REQUEST.MODEL]) },
                    ]
                });
                if(!expired) row.classList.remove("inactive");
                if(expired) row.classList.add("disabled");
                if(trusted) row.trusted = true;

            }.bind(snapshot), 0)

        }, function(error){
            console.warn("Resign because of",error);
            WTU.resign(updateAll);
        });

        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_MISC).child(DATABASE.STAT_MISC_ACCOUNTS_CLEANED).off();
        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_MISC).child(DATABASE.STAT_MISC_ACCOUNTS_CLEANED).on("value",function(data) {
            tableSummary.lastValidationItem.lastChild.innerHTML = new Date(data.val()).toLocaleString() + " (" + utils.toDateString(new Date().getTime() - new Date(data.val())) + " ago)";
        },function(error){
            console.error("REMOVED",error);
        });
    }

    function renderButtons(div) {
        var clear = u.create(HTML.BUTTON, { className:"icon button-inline", innerHTML: "clear_all", title:"Clean groups", onclick: function() {
            clear.hide();
            question.show();
            yes.show();
            no.show();
        }}, div);
        var question = u.create({className:"question hidden", innerHTML: "This will immediately check for expired or invalid accounts and remove them. Continue?"}, div);
        var yes = u.create(HTML.BUTTON,{ className:"question hidden", innerHTML:"Yes", onclick: function() {
            clear.show();
            question.hide();
            yes.hide();
            no.hide();
            u.toast.show("Accounts clean is performing.");
            u.get("/admin/rest/v1/accounts/clean")
                .then(function(xhr){
                }).catch(function(code,xhr){
                var res = JSON.parse(xhr.responseText) || {};
                u.toast.show(res.message || xhr.statusText);
            });
        }}, div);
        var no = u.create(HTML.BUTTON,{className:"hidden", innerHTML:"No", onclick: function(){
            clear.show();
            question.hide();
            yes.hide();
            no.hide();
        }}, div);
    }

    return {
        start: function(request) {
            if(request) {
                groupId = request[3];
                userNumber = request[4];
            } else {
                var parts = window.location.pathname.split("/");
                groupId = parts[3];
                userNumber = parts[4];
            }
            this.page = "accounts";
            div = document.getElementsByClassName("layout")[0];
            u.clear(div);

            renderInterface();
        },
        page: "accounts",
        icon: "person",
        title: title,
        menu: title,
        move:true
    }
}
