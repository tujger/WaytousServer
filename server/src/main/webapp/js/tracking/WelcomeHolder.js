/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 3/18/17.
 */

function WelcomeHolder(main) {

    var type = "welcome";

    function start() {
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.MAP_READY:
                if(!main.tracking) {
                    main.fire(EVENTS.SHOW_HELP, {
                        module:main.eventBus.holders[type],
                        article:1
                    })
                }
                break;
            default:
                break;
        }
        return true;
    }

    function help(){
        return {
            1: {
                title: u.lang.welcome_help_title_1,
                body: u.lang.welcome_help_body_1
            },
            2: {
                title: u.lang.welcome_help_title_2,
                body: u.lang.welcome_help_body_2
            }
        }
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        help:help,
    }
}