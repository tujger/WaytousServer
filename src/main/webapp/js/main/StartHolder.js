/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 7/23/17.
 */

function StartHolder(main) {

    this.category = DRAWER.SECTION_PRIMARY;
    this.type = "start";
    this.title = u.lang.start;
    this.menu = u.lang.start;

    this.icon = u.create(HTML.IMG, {
        src: "/images/navigation_twinks.svg",
        className: "icon drawer-menu-item-icon"
    });

    this.start = function() {
        console.log("Starting StartHolder");
    }

    this.resume = function() {
        console.log("Resuming StartHolder");
        window.location.href = "/group/new";
        this.title = u.lang.start;
        this.menu = u.lang.start;
    }
}