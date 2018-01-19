/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 1/19/17.
 */
function Settings() {

    var title = "Settings";
    var dialog;

    var start = function() {
        var div = document.getElementsByClassName("layout")[0];

        dialog = dialog || u.dialog({
            title: "Settings",
            className: "settings-dialog",
            items: [
                { type: HTML.DIV, label: "To be implemented soon..." }
            ],
            positive: {
                label: "OK"
            }
        });
        dialog.open();
    };

    return {
        start: start,
        page: "settings",
        icon: "settings",
        title: title,
        menu: title
    }
}
