/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/19/17.
 */
function Home() {

    var title = "Home";

    var start = function() {
        div = document.getElementsByClassName("layout")[0];
        u.clear(div);

        u.create(HTML.H2, "Waytous admin", div);

        u.create(HTML.DIV, "Here you may view and change groups and users in Waytous service.", div);



    };

    return {
        start: start,
        page: "home",
        icon: "home",
        title: title,
        menu: title,
        move:true
    }
}
