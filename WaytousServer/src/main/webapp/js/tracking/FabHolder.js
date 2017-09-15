/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/9/17.
 */
function FabHolder(main) {

    var type = "fab";

    var fab;

    function start() {
        // console.log("FABHOLDER",main);
        FabHolder.fab = u.create(HTML.DIV, {className:"fab", innerHTML:"gps_off", onclick:onFabClick}, main.layout);
    }

    function onFabClick(){
        console.log("FABCLICK")
    }

    function onEvent(EVENT,object){
        switch (EVENT){

            case EVENTS.ACTIVITY_RESUME:
                if(main.tracking_active) {
                    FabHolder.fab.innerHTML = "add";
                    // fab.setImageResource(R.drawable.ic_add_white_24dp);

                } else if(main.tracking_connecting || main.tracking_reconnecting) {
                    FabHolder.fab.innerHTML = "clear";
                    // fab.setImageResource(R.drawable.ic_clear_white_24dp);

                } else {
                    FabHolder.fab.innerHTML = "check";
                    // fab.setImageResource(R.drawable.ic_navigation_twinks_white_24dp);

                }
                // fab.setOnClickListener(onMainClickListener);
                break;
            case EVENTS.TRACKING_CONNECTING:
            case EVENTS.TRACKING_RECONNECTING:
                FabHolder.fab.innerHTML = "clear";
                // fab.setImageResource(R.drawable.ic_clear_white_24dp);
                break;
            case EVENTS.TRACKING_ACTIVE:
                FabHolder.fab.innerHTML = "add";
                // fab.setImageResource(R.drawable.ic_add_white_24dp);
                break;
            case EVENTS.TRACKING_DISABLED:
            case EVENTS.TRACKING_ERROR:
            case EVENTS.TRACKING_EXPIRED:
                FabHolder.fab.innerHTML = "warning";
                // fab.setImageResource(R.drawable.ic_navigation_twinks_white_24dp);
                break;

            default:
                break;
        }
        return true;
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
    }
}