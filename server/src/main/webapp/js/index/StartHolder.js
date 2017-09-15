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

    var drawerItemNewIconSvg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"0 0 24 24",
        version:"1.1",
        className: "menu-item"
    };
    var drawerItemNewIconPath = {
        xmlns:"http://www.w3.org/2000/svg",
        fill:"darkslategray",
        d: "M10,2l-6.5,15 0.5,0.5L9,15L12.29,7.45z M14,5.5l-6.5,15 0.5,0.5 6,-3l6,3 0.5,-0.5z"
    };
    this.icon =  u.create(HTML.PATH, drawerItemNewIconPath, u.create(HTML.SVG, drawerItemNewIconSvg)).parentNode;

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