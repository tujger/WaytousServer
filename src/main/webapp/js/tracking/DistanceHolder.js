/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 3/9/17.
 */

EVENTS.SHOW_DISTANCE = "show_distance";
EVENTS.HIDE_DISTANCE = "hide_distance";
EVENTS.MOVING_CLOSE_TO = "moving_close_to";
EVENTS.MOVING_AWAY_FROM = "moving_away_from";

function DistanceHolder(main) {

    var DISTANCE_MOVING_CLOSE = 50;
    var DISTANCE_MOVING_AWAY = 100;
    var MIN_INTERVAL_BETWEEN_DISTANCE_NOTIFICATIONS = 300;

    var type = "distance";
    var view;
    var drawerItemShow;
    var drawerItemHide;
    var lastCloseNotifyTime = 0;
    var lastAwayNotifyTime = 0;
    var sounds;
    var closeSound;
    var closeSoundNode;
    var awaySound;
    var awaySoundNode;
    var defaultCloseSound = "oringz-w426.mp3";
    var defaultAwaySound = "office-1.mp3";

    function start() {
        view = {};

        closeSound = u.load("distance:close") || defaultCloseSound;
        closeSoundNode = u.create(HTML.AUDIO, {className:"hidden", preload:"", src:"/sounds/"+closeSound, last:0, playButLast:function(){
            var current = new Date().getTime();
            if(current - this.last > 10) {
                this.last = current;
                this.play();
            }
        }}, main.right);

        awaySound = u.load("distance:away") || defaultAwaySound;
        awaySoundNode = u.create(HTML.AUDIO, {className:"hidden", preload:"", src:"/sounds/"+awaySound, last:0, playButLast:function(){
            var current = new Date().getTime();
            if(current - this.last > 10) {
                this.last = current;
                this.play();
            }
        }}, main.right);

    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerItemShow = object.add({section: DRAWER.SECTION_VIEWS, id: EVENTS.SHOW_DISTANCE, name: u.lang.show_distances, icon: "settings_ethernet", callback: function(){
                    main.users.forAllActiveUsers(function (number, user) {
                        if(!user.location) return;
                        user.fire(EVENTS.SHOW_DISTANCE);
                        drawerPopulate();
                    });
                }});
                drawerItemHide = object.add({section: DRAWER.SECTION_VIEWS, id: EVENTS.HIDE_DISTANCE, name: u.lang.hide_distances, icon: "code", callback: function(){
                    main.users.forAllActiveUsers(function (number, user) {
                        user.fire(EVENTS.HIDE_DISTANCE);
                        drawerPopulate();
                    });
                }});
                drawerItemShow.hide();
                drawerItemHide.hide();
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user && user !== main.me && user.location && !user.views.distance.show) {
                    object.add(MENU.SECTION_VIEWS, EVENTS.SHOW_DISTANCE, u.lang.show_distance, "settings_ethernet", function(){
                        user.fire(EVENTS.SHOW_DISTANCE);
                        drawerPopulate();
                    });
                } else if(user && user !== main.me && user.views.distance.show) {
                    object.add(MENU.SECTION_VIEWS, EVENTS.HIDE_DISTANCE, u.lang.hide_distance, "code", function(){
                        user.fire(EVENTS.HIDE_DISTANCE);
                        drawerPopulate();
                    });
                }
                break;
            case EVENTS.SHOW_DISTANCE:
                if(this !== main.me && this.properties && this.properties.active) {
                    this.views.distance.show = true;
                    u.saveForContext("distance:show:" + this.number, true);
                    show.call(this);
                }
                break;
            case EVENTS.HIDE_DISTANCE:
                this.views.distance.show = false;
                u.saveForContext("distance:show:" + this.number);
                this.fire(EVENTS.UPDATE_MENU_SUFFIX, "");
                if(this.views && this.views.distance && this.views.distance.distance) {
                    this.views.distance.distance.setMap(null);
                    this.views.distance.distance = null;
                    this.views.distance.marker.setMap(null);
                    this.views.distance.marker = null;
                    this.views.distance.label.setMap(null);
                    this.views.distance.label = null;
                }
                break;
            case EVENTS.MAP_MOVED:
                main.users.forAllActiveUsers(function (number, user) {
                    if(user.views && user.views.distance && user.views.distance.distance) {
                        show.call(user);
                    }
                });
                break;
            case EVENTS.MOVING_CLOSE_TO:
                var currentTime = new Date().getTime();
                if(currentTime - lastCloseNotifyTime > MIN_INTERVAL_BETWEEN_DISTANCE_NOTIFICATIONS * 1000) {
                    u.notification({
                        title: u.lang.close_to_s.format(this.properties.getDisplayName()).innerText,
                        body: u.lang.you_are_closer_than_d_to_s.format(utils.formatLengthToLocale(DISTANCE_MOVING_CLOSE), this.properties.getDisplayName()).innerText,
                        icon: "/icons/favicon-256x256.png",
                        duration: 10000,
                        onclick: function(e){
                            console.log(this,e)
                        }
                    });
                    u.toast.show(u.lang.close_to_s.format(this.properties.getDisplayName()));
                    closeSoundNode.playButLast();
                    lastCloseNotifyTime = currentTime;
                }
                break;
            case EVENTS.MOVING_AWAY_FROM:
                currentTime = new Date().getTime();
                if(currentTime - lastAwayNotifyTime > MIN_INTERVAL_BETWEEN_DISTANCE_NOTIFICATIONS * 1000) {
                    u.notification({
                        title: u.lang.away_from_s.format(this.properties.getDisplayName()).innerText,
                        body: u.lang.you_are_away_than_d_from_s.format(utils.formatLengthToLocale(DISTANCE_MOVING_AWAY), this.properties.getDisplayName()).innerText,
                        icon: "/icons/favicon-256x256.png",
                        duration: 10000,
                        onclick: function(e){
                            console.log(this,e)
                        }
                    });
                    u.toast.show(u.lang.away_from_s.format(this.properties.getDisplayName()));
                    awaySoundNode.playButLast();
                    lastAwayNotifyTime = currentTime;
                }
                break;
            default:
                break;
        }
        return true;
    }

    function createView(myUser){
        var view = {};
        view.user = myUser;
        view.notifiedThatClose = 0;

        view.show = u.loadForContext("distance:show:" + myUser.number);

        if(view.show) {
            show.call(myUser);
        }
        drawerPopulate();
        return view;
    }

    function removeView(user) {
        if(user && user.views && user.views.distance & user.views.distance.distance) {
            user.views.distance.distance.setMap(null);
            user.views.distance.distance = null;
            user.views.distance.marker.setMap(null);
            user.views.distance.marker = null;
            user.views.distance.label.setMap(null);
            user.views.distance.label = null;
        }
    }

    function drawerPopulate() {
        setTimeout(function(){
            drawerItemHide.hide();
            drawerItemShow.hide();
            main.users.forAllActiveUsersExceptMe(function (number, user) {
                if(user.properties.active && user.views.distance && user.location) {
                    if (user.views.distance.show) {
                        drawerItemHide.show();
                    } else {
                        drawerItemShow.show();
                    }
                }
            })
        },0);
    }

    function show() {
        if (!this || !this.views || !this.views.distance || !this.views.distance.show) return;
        if (this.location && main.me.location && google) {
            if (!this.views.distance.distance) {
                this.views.distance.distance = new google.maps.Polyline({
                    geodesic: true,
                    strokeColor: "rgb(100,100,100)",
                    strokeOpacity: 0.6,
                    strokeWeight: 2,
                    map: main.map
                });

                this.views.distance.marker = new google.maps.Marker({
                    map: main.map,
                    visible: false
                });
                this.views.distance.label = new utils.label({map:main.map, className:"distance-label"});
                this.views.distance.label.bindTo("position", this.views.distance.marker, "position");
            }

            var points = [
                utils.latLng(main.me.location),
                utils.latLng(this.location)
            ];
            this.views.distance.distance.setPath(points);

            var markerPosition = utils.labelPosition(main.map, points, utils.latLng(main.me.location), utils.latLng(this.location));

            this.views.distance.marker.setPosition(markerPosition);
            var title = utils.formatLengthToLocale(google.maps.geometry.spherical.computeDistanceBetween(points[0], points[1]));
            this.fire(EVENTS.UPDATE_MENU_SUFFIX, title);

            title = this.properties.getDisplayName() + "\n" + title;
            this.views.distance.label.set("text", title);
        }
    }

    function onChangeLocation(location) {
        if(this.number == main.me.number) {
            main.users.forAllActiveUsersExceptMe(function(number,user){
                show.call(user);
                checkDistance(main.me,user);
            })
        } else {
            show.call(this);
            checkDistance(main.me,this);
        }
        drawerPopulate();
    }

    function checkDistance(me, user) {
        if(!me || !me.location || !user || !user.location) return;
        var points = [
            utils.latLng(me.location),
            utils.latLng(user.location)
        ];
        var distance = google.maps.geometry.spherical.computeDistanceBetween(points[0], points[1]);

        if(distance <= DISTANCE_MOVING_CLOSE && user.views.distance.previous > DISTANCE_MOVING_CLOSE) {
            user.fire(EVENTS.MOVING_CLOSE_TO, distance);
        } else if(distance > DISTANCE_MOVING_AWAY && user.views.distance.previous && user.views.distance.previous < DISTANCE_MOVING_AWAY) {
            user.fire(EVENTS.MOVING_AWAY_FROM, distance);
        }
        user.views.distance.previous = distance;

    }

    function Label(opt_options, node) {
        // Initialization
        this.setValues(opt_options);

        // Label specific
        if(!node) {
            node = u.create(HTML.DIV, {className:"distance-label"});
        }
        this.span_ = node;
        var div = this.div_ = u.create(HTML.DIV, {style: "position: absolute; display: none"});
        div.appendChild(node);
    }

    function help(){
        return {
            title: u.lang.distance_help_title,
            1: {
                title: u.lang.distance_article_1_title,
                body: u.lang.distance_article_1_body
            }
        }
    }

    function options(){
        return {
            id: "general",
            title: u.lang.general,
            categories: [
                {
                    id: "general:notifications",
                    title: u.lang.notifications,
                    items: [
                        {
                            id:"distance:close",
                            type: HTML.SELECT,
                            label: "Close to user",
                            default: u.load("distance:close") || defaultCloseSound,
                            onaccept: function(e, event) {
                                u.save("distance:close", this.value);
                                closeSoundNode.src = "/sounds/" + this.value;
                            },
                            onchange: function(e, event) {
                                var sample = u.create(HTML.AUDIO, {className:"hidden", preload:true, src:"/sounds/"+this.value}, main.right);
                                sample.addEventListener("load", function() {
                                    sample.play();
                                }, {passive: true});
                                sample.play();
                            },
                            onshow: function(e) {
                                if(sounds) {
                                } else {
                                    u.getJSON("/rest/sounds").then(function(json){
                                        sounds = {};
                                        u.clear(e);
                                        var selected = 0;
                                        for(var i in json.message) {
                                            var file = json.message[i];
                                            var name = (file.replace(/\..*$/,"").replace(/[\-_]/g," ")).toUpperCaseFirst();
                                            sounds[file] = name;
                                            u.create(HTML.OPTION, {value:file, innerHTML:name}, e);
                                            if((closeSound || defaultCloseSound) === file) selected = i;
                                        }
                                        e.selectedIndex = selected;
                                    });
                                }
                            },
                            values: {"": u.lang.loading.innerText}
                        },
                        {
                            id:"distance:away",
                            type: HTML.SELECT,
                            label: "Away from user",
                            default: u.load("distance:away") || defaultAwaySound,
                            onaccept: function(e, event) {
                                u.save("distance:away", this.value);
                                awaySoundNode.src = "/sounds/" + this.value;
                            },
                            onchange: function(e, event) {
                                var sample = u.create(HTML.AUDIO, {className:"hidden", preload:true, src:"/sounds/"+this.value}, main.right);
                                sample.addEventListener("load", function() {
                                    sample.play();
                                }, {passive: true});
                                sample.play();
                            },
                            onshow: function(e) {
                                if(sounds) {
                                } else {
                                    u.getJSON("/rest/sounds").then(function(json){
                                        sounds = {};
                                        u.clear(e);
                                        var selected = 0;
                                        for(var i in json.message) {
                                            var file = json.message[i];
                                            var name = (file.replace(/\..*$/,"").replace(/[\-_]/g," ")).toUpperCaseFirst();
                                            sounds[file] = name;
                                            u.create(HTML.OPTION, {value:file, innerHTML:name}, e);
                                            if((awaySound || defaultAwaySound) === file) selected = i;
                                        }
                                        e.selectedIndex = selected;
                                    });
                                }
                            },
                            values: {"": u.lang.loading.innerText}
                        }
                    ]
                }
            ]
        }
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        createView:createView,
        removeView:removeView,
        onChangeLocation:onChangeLocation,
        help:help,
        options:options
    }
}