/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/19/17.
 */
function Create() {

    var title = "Create group";
    var dialog;

    var inputId,inputRequiresPassword,inputPassword,inputWelcomeMessage,inputPersistent,inputTtl,inputDismissInactive,inputDelay;

    var start = function() {

        div = document.getElementsByClassName("layout")[0];
        dialog = dialog || u.dialog({
                title: "Create group",
                className: "create-dialog",
                items: [
                    {
                        type: HTML.INPUT,
                        label: u.create(HTML.DIV, "ID ").place(HTML.BUTTON, {
                            className: "create-dialog-button-generate",
                            innerHTML:"Generate ID",
                            onclick: function(){
                                dialog.items[0].value = Math.random().toString(32).toUpperCase().replace(/0\./,"");
                            }
                        }),
                        oninput: validate_id
                    },
                    {
                        type: HTML.CHECKBOX, label: "Requires password", onchange: function() {
                        dialog.items[2].disabled = !this.checked;
                        dialog.items[2].parentNode.classList[this.checked ? "remove" : "add"]("disabled");
                        dialog.items[2].focus();
                    } },
                    { type: HTML.PASSWORD, itemClassName: "disabled", disabled: true, label: "&#150; password" },
                    { type: HTML.INPUT, label: "Welcome message" },
                    {
                        type: HTML.CHECKBOX, label: "Persistent group",
                        onchange: function() {
                            dialog.items[5].disabled = this.checked;
                            dialog.items[5].parentNode.classList[this.checked ? "add" : "remove"]("disabled");
                            dialog.items[5].focus();
                        }
                    },
                    { type: HTML.NUMBER, label: "&#150; time to live, min", oninput: validate_ttl, value: 24 * 60 },
                    { type: HTML.CHECKBOX, label: "Dismiss inactive users", onchange: function() {
                        dialog.items[7].disabled = !!this.checked;
                        dialog.items[7].parentNode.classList[this.checked ? "add" : "remove"]("disabled");
                        dialog.items[7].focus();
                    }, checked: true },
                    { type: HTML.NUMBER, itemClassName: "", label: "&#150; delay to dismiss, sec", title:"Minimal value 300", onchange: validate_delay, oninput: validate_delay, value: 3600 },
                ],
                positive: {
                    label: u.create(HTML.SPAN, "OK"),
                    onclick: validate_submit
                },
                negative: {
                    label: u.create(HTML.SPAN, "Cancel")
                },
                help: function() {
                    console.log("HELP");
                }
            });
        dialog.open();
        inputId = dialog.items[0];
        inputRequiresPassword = dialog.items[1];
        inputPassword = dialog.items[2];
        inputWelcomeMessage = dialog.items[3];
        inputPersistent = dialog.items[4];
        inputTtl = dialog.items[5];
        inputDismissInactive = dialog.items[6];
        inputDelay = dialog.items[7];

    };

    var validate_id = function(e) {
        this.value = this.value.toUpperCase().replace(/[^\w]/g, "");
    };

    var validate_ttl = function(e) {
        this.value = this.value.replace(/[^\d]/g, "");
    };

    var validate_delay = function(e) {
        this.value = this.value.replace(/[^\d]/g, "");
        if(this.value < 300) this.value = 300;
    };

    var validate_submit = function(e) {

        validate_id.call(inputId);
        validate_ttl.call(inputTtl);
        validate_delay.call(inputDelay);

        if(!inputId.value) return;

        var options = {
            "group_id": inputId.value,
            "requires-password": inputRequiresPassword.checked,
            "password": inputPassword.value ? inputPassword.value : null,
            "welcome-message": inputWelcomeMessage.value,
            "persistent": inputPersistent.checked,
            "time-to-live-if-empty": inputTtl.value,
            "dismiss-inactive": inputDismissInactive.checked,
            "delay-to-dismiss": inputDelay.value
        };
        u.post("/admin/rest/v1/group/create", JSON.stringify(options))
            .then(function(xhr){
                u.toast.show("Group "+inputId.value+" has created.");
                WTU.switchTo("/admin/groups");
            }).catch(function(code,xhr){
            console.error(code,xhr);
            var res = JSON.parse(xhr.responseText) || {};
            u.toast.show(res.message || xhr.statusText);
        });

//        window.location.href = "/admin/groups";

//        if(window.name == "content") {
//            window.parent.history.pushState({}, null, "/admin/create");
//        }

        return false;
    };


    return {
        start: start,
        page: "create",
        icon: "group_add",
        title: title,
        menu: title,
    }
}
