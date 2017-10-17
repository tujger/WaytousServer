/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 7/23/17.
 */

EVENTS.START = "start";

function StartHolder(main) {

    this.type = "start";
    this.category = "main";
    this.title = u.lang.start;
    this.menu = u.lang.start;

    this.icon = u.create(HTML.IMG, {
        src: "/images/navigation_twinks.svg",
        className: "icon drawer-menu-item-icon"
    });

    this.start = function() {
        console.log("INDEX START");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.START:
                console.log("INDEX TRACK");
                window.location.href = "/group/new";
                break;
        }
        return true;
    }

}