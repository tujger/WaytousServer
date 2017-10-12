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

        var divSummaryMap = u.create(HTML.DIV, {className: "two-divs"}, div);
        var divSummary = u.create(HTML.DIV, {className: "summary-place"}, divSummaryMap);

        tableSummary = u.table({
            className: "option",
            placeholder: " "
        }, divSummary);

        tableSummary.userNumberNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Accounts" },
        ] });
        tableSummary.accountsTotalItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; total" },
                { className:"option", innerHTML: "0" }
            ],
            onclick: function(e){
                tableAccounts.filter.remove(filterActive);
                tableAccounts.filter.remove(filterExpired);
            }
        });
        tableSummary.accountsActiveItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; active" },
                { className:"option", innerHTML: "0" }
            ],
            onclick: function(e){
                tableAccounts.filter.add(filterActive);
            }
        });
        tableSummary.accountsExpiredItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; expired" },
                { className:"option", innerHTML: "0" }
            ],
            onclick: function(e){
                tableAccounts.filter.add(filterExpired);
            }
        });
        tableSummary.groupsTemporaryItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Last registered" },
                { className:"option", innerHTML: "..." }
            ],
            onclick: function(e){
                tableAccounts.filter.add(filterExpired);
            }
        });


        var accountsTitleNode = u.create(HTML.H2, "Accounts", div);
        buttons = u.create("div", {className:"buttons"}, accountsTitleNode);
        renderButtons(buttons);

        tableAccounts = u.table({
            id: "admin:accounts",
            caption: {
                items: [
                    { label: "Name" },
                    { label: "Created", className: "media-hidden" },
                    { label: "Updated" },
                    { label: "Sign Provider" },
                    { label: "OS", className: "media-hidden" },
                    { label: "Model", className: "media-hidden" },
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
        tableAccounts.placeholder.show();
        u.clear(tableAccounts.body);
        var reload = false;
        var initial = true;
        setTimeout(function(){initial = false;}, 3000);

        ref.child(DATABASE.SECTION_USERS).off();
        ref.child(DATABASE.SECTION_USERS).on("child_added", function(snapshot) {

            if(!snapshot || !snapshot.val()){
                tableAccounts.placeholder.show("No accounts");
                return;
            }
            reload = false;
            var privateData = snapshot.val()[DATABASE.PRIVATE];
            tableSummary.accountsTotalItem.lastChild.innerHTML = +tableSummary.accountsTotalItem.lastChild.innerHTML + 1;

            var row = tableAccounts.add({
                className: "accounts-row highlight",
                onclick: function(){
                    WTU.switchTo("/admin/account/"+snapshot.key);
                    return false;
                },
                cells: [
                    { innerHTML: privateData[DATABASE.NAME] },
                    { className: "media-hidden", innerHTML: new Date(privateData[DATABASE.CREATED]).toLocaleString(), sort: privateData[DATABASE.CREATED] },
                    { innerHTML: new Date(privateData[DATABASE.CHANGED]).toLocaleString(), sort: privateData[DATABASE.CHANGED] },
                    { innerHTML: privateData[REQUEST.SIGN_PROVIDER] },
                    { className: "media-hidden", innerHTML: privateData[REQUEST.OS] },
                    { className: "media-hidden", innerHTML: privateData[REQUEST.MODEL] },
                ]
            });

        }, function(error){
            console.warn("Resign because of",error);
            WTU.resign(updateAll);
        });

    }

    function renderButtons(div) {
        u.clear(div);
        u.create(HTML.BUTTON, { className:"button-clean", innerHTML: "clear_all", title:"Clean groups", onclick: cleanAccountsQuestion}, div);
    }

    function cleanAccountsQuestion(e){
        u.clear(buttons);
        u.create({className:"question", innerHTML: "This will immediately check for expired or invalid accounts and remove them. Continue?"}, buttons);
        u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: function() {
            renderButtons(buttons);
            u.toast.show("Accounts clean is performing.");
            u.get("/admin/rest/v1/accounts/clean")
                .then(function(xhr){
                }).catch(function(code,xhr){
                var res = JSON.parse(xhr.responseText) || {};
                u.toast.show(res.message || xhr.statusText);
            });
        }}, buttons);
        u.create(HTML.BUTTON,{ innerHTML:"No", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
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
