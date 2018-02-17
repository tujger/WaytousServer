/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 1/19/17.
 */
function HomeHolder() {

    this.category = DRAWER.SECTION_PRIMARY;
    this.type = "home";
    this.title = "Home";
    this.menu = "Home";
    this.icon = "home";

    this.start = function() {
        div = document.getElementsByClassName("layout")[0];
    };

    this.resume = function() {
        u.clear(div);

        u.create(HTML.H2, "Waytous admin", div);

        u.create(HTML.DIV, "Here you may view and change groups and users in Waytous service.", div);

    }

}
