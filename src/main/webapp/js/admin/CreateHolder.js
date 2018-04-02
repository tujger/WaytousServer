/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 1/19/17.
 */
function CreateHolder(main) {
    var dialog;
    var div;

    // this.category = DRAWER.SECTION_EXPLORE;
    this.type = "create";
    this.title = "Create group";
    // this.menu = "Create group";
    // this.icon = "group_add";
    this.preventState = true;

    var inputId,inputRequiresPassword,inputPassword,inputWelcomeMessage,inputPersistent,inputTtl,inputLimitUsers,inputDismissInactive,inputDelay;

    this.start = function() {
        div = main.content;
    };

    this.resume = function() {
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
                    // noinspection PointlessBooleanExpressionJS
                    dialog.items[7].disabled = !!this.checked;
                    dialog.items[7].parentNode.classList[this.checked ? "add" : "remove"]("disabled");
                    dialog.items[7].focus();
                }, checked: true },
                { type: HTML.NUMBER, itemClassName: "", label: "&#150; delay to dismiss, sec", title:"Minimal value 300", onchange: validate_delay, oninput: validate_delay, value: 3600 },
                { type: HTML.NUMBER, label: "Limit users" }
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
        inputLimitUsers = dialog.items[8];
    };

    var validate_id = function() {
        this.value = this.value.toUpperCase().replace(/[^\w]/g, "");
    };

    var validate_ttl = function() {
        this.value = this.value.replace(/[^\d]/g, "");
    };

    var validate_delay = function() {
        this.value = this.value.replace(/[^\d]/g, "");
        if(this.value < 300) this.value = 300;
    };

    var validate_submit = function() {
        validate_id.call(inputId);
        validate_ttl.call(inputTtl);
        validate_delay.call(inputDelay);

        if(!inputId.value) return;

        var options = {};
        options.group_id = inputId.value;
        options[DATABASE.REQUIRES_PASSWORD] = inputRequiresPassword.checked;
        options[DATABASE.PASSWORD] = inputPassword.value ? inputPassword.value : null;
        options[DATABASE.WELCOME_MESSAGE] = inputWelcomeMessage.value;
        options[DATABASE.PERSISTENT] = inputPersistent.checked;
        options[DATABASE.TIME_TO_LIVE_IF_EMPTY] = inputTtl.value;
        options[DATABASE.LIMIT_USERS] = inputLimitUsers.value;
        options[DATABASE.DISMISS_INACTIVE] = inputDismissInactive.checked;
        options[DATABASE.DELAY_TO_DISMISS] = inputDelay.value;

        u.post("/admin/rest/group/create", JSON.stringify(options))
            .then(function(){
                u.toast.show("Group "+inputId.value+" has created.");
                main.turn("group", inputId.value);
            }).catch(function(code,xhr){
            console.error(code,xhr);
            var res = JSON.parse(xhr.responseText) || {};
            u.toast.show(res.message || xhr.statusText);
        });
        return false;
    }
}
