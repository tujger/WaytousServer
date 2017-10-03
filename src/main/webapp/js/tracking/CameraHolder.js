/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/9/17.
 */
EVENTS.CAMERA_UPDATE = "camera_update";
EVENTS.CAMERA_UPDATED = "camera_updated";
EVENTS.CAMERA_ZOOM_IN = "camera_zoom_in";
EVENTS.CAMERA_ZOOM_OUT = "camera_zoom_out";
EVENTS.CAMERA_ZOOM = "camera_zoom";
EVENTS.CAMERA_NEXT_ORIENTATION = "camera_next_orientation";

function CameraHolder(main) {

    var type = "camera";


    var CAMERA_ORIENTATION_NORTH = 0;
    var CAMERA_ORIENTATION_DIRECTION = 1;
    var CAMERA_ORIENTATION_PERSPECTIVE = 2;
    var CAMERA_ORIENTATION_STAY = 3;
    var CAMERA_ORIENTATION_USER = 4;
    var CAMERA_DEFAULT_ZOOM = 15.;
    var CAMERA_DEFAULT_TILT = 0.;
    var CAMERA_DEFAULT_BEARING = 0.;
    var CAMERA_ORIENTATION_LAST = 2;
    var CAMERA_ORIENTATION_PERSPECTIVE_NORTH = true;

    var menuFitToScreen;
    var task;
    var orientation;
    var unselect_icon;
    var maxZoomService;

    var unselect_svg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"0 0 24 24",
        version:"1.1",
        className: "menu-item"
    };
    var unselect_path = {
        xmlns:"http://www.w3.org/2000/svg",
        strokeWidth:"0",
        fill:"darkslategray",
        d: "M3,5h2L5,3c-1.1,0 -2,0.9 -2,2zM3,13h2v-2L3,11v2zM7,21h2v-2L7,19v2zM3,9h2L5,7L3,7v2zM13,3h-2v2h2L13,3zM19,3v2h2c0,-1.1 -0.9,-2 -2,-2zM5,21v-2L3,19c0,1.1 0.9,2 2,2zM3,17h2v-2L3,15v2zM9,3L7,3v2h2L9,3zM11,21h2v-2h-2v2zM19,13h2v-2h-2v2zM19,21c1.1,0 2,-0.9 2,-2h-2v2zM19,9h2L21,7h-2v2zM19,17h2v-2h-2v2zM15,21h2v-2h-2v2zM15,5h2L17,3h-2v2z"
    };



    function start() {
        orientation = CAMERA_ORIENTATION_NORTH;
    }

    function onEvent(EVENT,object){
        // if(!this || !this.views || !this.views.camera) return true;
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                menuFitToScreen = object.add(DRAWER.SECTION_VIEWS,type+"_1", u.lang.fit_to_screen, "fullscreen", function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.SELECT_USER);
                    });
                });
                menuFitToScreen.disable();
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(main.users.getCountSelected() == 1 && user.properties.selected) break;

                var select, unselect;
                select = object.add(MENU.SECTION_PRIMARY, EVENTS.SELECT_USER, u.lang.select, "select_all", function () {
                    select.hide();
                    unselect.show();
                    user.fire(EVENTS.SELECT_USER);
                });
                unselect_icon = unselect_icon || u.create(HTML.PATH, unselect_path, u.create(HTML.SVG, unselect_svg)).parentNode;
                unselect = object.add(MENU.SECTION_PRIMARY, EVENTS.UNSELECT_USER, u.lang.unselect, unselect_icon, function () {
                    select.show();
                    unselect.hide();
                    user.fire(EVENTS.UNSELECT_USER);
                });
                select.hide();
                unselect.hide();
                setTimeout(function(){
                    if(user.properties.selected) {
                        if(main.users.getCountSelected() > 1) {
                            unselect.show();
                        }
                    } else {
                        select.show();
                    }
                }, 0);
                break;
            case EVENTS.TRACKING_ACTIVE:
                menuFitToScreen.enable();
                break;
            case EVENTS.TRACKING_DISABLED:
                menuFitToScreen.disable();
                break;
            case EVENTS.MAP_READY:
                main.map.addListener("dragend", function(e){
                    orientation = CAMERA_ORIENTATION_USER;
                });
                maxZoomService = new google.maps.MaxZoomService();
                break;
            case EVENTS.MARKER_CLICK:
                onEvent.call(this,EVENTS.CAMERA_NEXT_ORIENTATION);
                break;
/*
            case EVENTS.CAMERA_NEXT_ORIENTATION:
                console.log("NEXTORIENTATION",this);
                var camera = this.views.camera;
                if(this.views.camera.orientation > CAMERA_ORIENTATION_LAST) {
                    camera.orientation = camera.previousOrientation;
                } else if(camera.orientation == CAMERA_ORIENTATION_LAST){
                    camera.orientation = CAMERA_ORIENTATION_NORTH;
                } else {
                    camera.orientation++;
                }
                if(camera.orientation == CAMERA_ORIENTATION_DIRECTION && this.location.coords.heading == 0) {
                    camera.orientation++;
                }
                camera.orientationChanged = true;
                camera.previousOrientation = camera.orientation;

                if(orientation == CAMERA_ORIENTATION_USER) orientation = CAMERA_ORIENTATION_NORTH;
                onChangeLocation.call(this,this.location);
                break;
*/
            case EVENTS.SELECT_USER:
                orientation = this && this.views && this.views.camera ? this.views.camera.orientation : CAMERA_ORIENTATION_NORTH;
                onChangeLocation.call(this, this.location);
                menuFitToScreen.show();
                break;
            case EVENTS.UNSELECT_USER:
                update();
//                orientation = this && this.views && this.views.camera ? this.views.camera.orientation : CAMERA_ORIENTATION_NORTH;
//                onChangeLocation.call(this, this.location);
//                menuFitToScreen.classList.remove("disabled");
                break;
            case EVENTS.CAMERA_ZOOM:
                if(main.users.getCountSelected()==1) {
                    main.users.forAllUsers(function (number, user) {
                        if(user.properties.selected) {
                            if(!object) {
                                var z = user.views.camera.zoom;
                                var zooms = [CAMERA_DEFAULT_ZOOM, CAMERA_DEFAULT_ZOOM + 2, CAMERA_DEFAULT_ZOOM + 4, CAMERA_DEFAULT_ZOOM - 2, CAMERA_DEFAULT_ZOOM - 4, CAMERA_DEFAULT_ZOOM];
                                var index = zooms.indexOf(z);
                                object = zooms[index+1];
                            }
                            user.views.camera.zoom = object;
                        }
                    });
                }
                if(this && this.properties && this.properties.selected) {
                    setTimeout(function(){
                        maxZoomService.getMaxZoomAtLatLng(main.map.getCenter(), function(response) {
                            if (response.status !== google.maps.MaxZoomStatus.OK) {
    //                            console.error('Error in MaxZoomService',response);
                            } else {
                                if(object && object > response.zoom) object = response.zoom;
                                if(main.map.getZoom() != object) {
                                    main.map.setZoom(Math.round(object));
                                }
                            }
                        });
                    },0);
                }
                break;
            case EVENTS.CAMERA_UPDATE:
                if(window.google) google.maps.event.trigger(map, "resize");
                update();
                break;
            default:
                break;
        }
        return true;
    }

    function onChangeLocation(location){
        if(!this || !this.views || !this.views.camera) return;
        var camera = this.views.camera;
        this.location = location;
        if(this.properties.selected) {
            update();
        }
        switch (camera.orientation){
            /*case CAMERA_ORIENTATION_NORTH:
//                    if(orientationChanged) {
//                    }
                position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                position.bearing(0);
                position.tilt(0);
                break;
            case CAMERA_ORIENTATION_DIRECTION:
//                    if(orientationChanged) {
//                    }
                position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                position.bearing(location.getBearing());
                position.tilt(0);
                break;
            case CAMERA_ORIENTATION_PERSPECTIVE:
                if(orientationChanged) {
                    position.tilt(60);
                }
                position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                position.bearing(location.getBearing());

                /!*DisplayMetrics metrics = new DisplayMetrics();
                 context.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                 Projection projection = map.getProjection();

                 Point cameraCenter = projection.toScreenLocation(Utils.latLng(location));

                 float tiltFactor = (90 - map.getCameraPosition().tilt) / 90;

                 System.out.println("METRICS:"+metrics);
                 System.out.println("VISIBLE:"+projection.getVisibleRegion());
                 System.out.println("POINT:"+cameraCenter);

                 cameraCenter.x -= metrics.widthPixels / 2;// - cameraCenter.x;
                 cameraCenter.y -= metrics.heightPixels *.2;// / 2 * tiltFactor;

                 System.out.println("POINT2:"+cameraCenter);

                 LatLng fixLatLng = projection.fromScreenLocation(cameraCenter);
                 position.target(fixLatLng);*!/

                break;
            case CAMERA_ORIENTATION_STAY:
                position.target(map.getCameraPosition().target);
                break;*/
        }

    }

    function update() {
        if(orientation == CAMERA_ORIENTATION_USER) return;
        if(main.users.getCountSelected() > 1) {
            var finalBounds = new google.maps.LatLngBounds();
            for(var i in main.users.users) {
                var user = main.users.users[i];
                if(user.properties && user.properties.selected && user.properties.active && user.location && user.location.coords) {
                    finalBounds.extend(utils.latLng(user.location));
                }
            }
            main.map.fitBounds(finalBounds);
            main.fire(EVENTS.CAMERA_UPDATED);

/*            var startBounds = main.map.getBounds();
            var startNE = startBounds.getNorthEast();
            var startSW = startBounds.getSouthWest();
            var finalNE = finalBounds.getNorthEast();
            var finalSW = finalBounds.getSouthWest();
            clearInterval(task);
            task = utils.smoothInterpolated(1000, function(time,value) {

                var currentNE = new google.maps.LatLng(
                    startNE.lat()*(1-time) + finalNE.lat()*time,
                    startNE.lng()*(1-time) + finalNE.lng()*time
                );
                var currentSW = new google.maps.LatLng(
                    startSW.lat()*(1-time) + finalSW.lat()*time,
                    startSW.lng()*(1-time) + finalSW.lng()*time
                );
                var currentBounds = new google.maps.LatLngBounds(currentNE, currentSW);

                main.map.fitBounds(currentBounds);
            }, function(){
                main.map.fitBounds(finalBounds);
            });*/
        } else if(main.users.getCountSelected() == 1) {
            main.users.forAllUsers(function(number,user){
                if(user.properties && user.properties.selected && user.properties.active) {
                    var finalCenter = utils.latLng(user.location);
                    if (finalCenter) {
                        var startCenter = main.map.getCenter();

                        var startZoom = main.map.getZoom();
                        var finalZoom = user.views.camera.zoom || CAMERA_DEFAULT_ZOOM;

                        if(startCenter.lat() == finalCenter.lat()
                            && startCenter.lng() == finalCenter.lng()
                            && startZoom == finalZoom) return;

                        clearInterval(task);
                        task = utils.smoothInterpolated(1000, function(time,value) {

                            var currentCenter = new google.maps.LatLng(
                                startCenter.lat()*(1-time) + finalCenter.lat()*time,
                                startCenter.lng()*(1-time) + finalCenter.lng()*time
                            );
                            var currentZoom = startZoom*(1-time) + finalZoom*time;

                            main.map.setZoom(currentZoom);
                            main.map.panTo(currentCenter);

                        }, function(){
                            main.map.setZoom(finalZoom);
                            main.map.panTo(finalCenter);
                            main.fire(EVENTS.CAMERA_UPDATED);
                        });
                    }

                    // main.map.setZoom(finalZoom);
                    // main.map.panTo(finalCenter);

                }
            });
        } else {
            //main.me.fire(EVENTS.SELECT_USER);
        }
    }

    function createView(user){
        if(!user) return;

        var b = {
            bearing: CAMERA_DEFAULT_BEARING,
            zoom: CAMERA_DEFAULT_ZOOM,
            orientation: CAMERA_ORIENTATION_NORTH,
            previousOrientation: CAMERA_ORIENTATION_NORTH,
            perspectiveNorth: true,
            location:user.location,
            // latitude: user.getLocation().getLatitude();
            // longitude: myUser.getLocation().getLongitude();
        };
        return b;
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        createView:createView,
        onChangeLocation:onChangeLocation,
    }
}

