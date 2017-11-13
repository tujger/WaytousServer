/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/10/17.
 */
function PropertiesHolder(main) {

    this.type = "properties";
    this.saveable = true;

    var setNameDialog;

    this.start = function() {
        setNameDialog = u.dialog({
            title: u.lang.set_my_name,
            queue: true,
            priority: 9,
            items: [
                { type: HTML.INPUT, label: u.lang.name }
            ],
            positive: {
                label: u.lang.ok,
                onclick: function(args) {
                    if(args[0].value) {
                        var name = u.clear(args[0].value);
                        u.save("properties:name", name);
                        u.save("properties:name_asked", true);
                        main.me.fire(EVENTS.CHANGE_NAME, name);
                        synchronizeName(true);
                    }
                }
            },
            negative: {
                label: u.lang.cancel
            }
        }, main.right);
    };

    this.onEvent = function(EVENT,object){
        var self = this;
        switch (EVENT){
            case EVENTS.TRACKING_ACTIVE:
                if(!u.load("properties:name") && !u.load("properties:name_asked")) {
                    var askIfNameNotDefinedDialog = u.dialog({
                        queue: true,
                        priority: 1,
                        items: [
                            { type: HTML.DIV, label: u.lang.your_name_is_not_defined }
                        ],
                        positive: {
                            label: u.lang.yes,
                            onclick: function(args) {
                                setMyName.call(main.me);
                            }
                        },
                        negative: {
                            label: u.lang.no,
                            onclick: function(){
                                u.save("properties:name_asked", true);
                            }
                        },
                        neutral: {
                            label: u.lang.maybe_later
                        },
                        timeout: 10000
                    }, main.right);
                    setTimeout(function(){askIfNameNotDefinedDialog.open();}, 0);
                }
                var name = u.clear(main.me.name);
                if(!name && main.me.properties) name = main.me.properties.name;
                if(name) {
                    main.me.fire(EVENTS.CHANGE_NAME, name);
                }
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user.number == main.me.number) {
                    object.add(MENU.SECTION_PRIMARY, self.type + "_set_my_name", u.lang.set_my_name, "face", function () {
                        setMyName.call(user);
                    });
                }
                break;
            case EVENTS.SELECT_USER:
                this.properties.selected = true;
                break;
            case EVENTS.UNSELECT_USER:
                this.properties.selected = false;
                break;
            case EVENTS.SELECT_SINGLE_USER:
                var myUser = this;
                main.users.forAllUsers(function(number,user){
                    if(user == myUser) {
                        user.properties.selected = true;
                        myUser.fire(EVENTS.SELECT_USER);
                    } else if(user.properties && user.properties.selected) {
                        user.properties.selected = false;
                        user.fire(EVENTS.UNSELECT_USER);
                    }
                });

//                main.users.forAllUsers(function(number,user){
//                    if(myUser != user) {
//                    }
//                });
                break;
            case EVENTS.CHANGE_NAME:
                if(this.properties)this.properties.name = object;
                if(main.tracking && this.number == main.me.number) {
                    main.tracking.put(USER.NAME, object);
                    main.tracking.send(REQUEST.CHANGE_NAME);
                    u.save("properties:name", object);
                }
                break;
            case EVENTS.CHANGE_NUMBER:
                if(this.properties)this.properties.number = object;
                break;
            case EVENTS.CHANGE_COLOR:
                if(this.properties) this.properties.color = object;
                if(this.number == main.me.number) this.properties.color = "#0000FF";
                break;
            case EVENTS.MAKE_ACTIVE:
                if(this.properties) {
                    this.properties.active = true;
                }
                break;
            case EVENTS.MAKE_INACTIVE:
                if(this.properties) {
                    this.properties.changed = object;
                    this.properties.active = false;
                }
                break;
                //if(this.properties) this.properties.active = false;
                //break;
            case EVENTS.MAKE_ENABLED:
            case EVENTS.MAKE_DISABLED:
                if(this.properties) {
                    this.properties.changed = object;
                    var delta = parseInt((new Date().getTime() - object) / 1000);
                    this.properties.enabled = delta <= 120;
                }
                break;
            case EVENTS.MAP_READY:
                main.me.createViews();
                main.me.fire(EVENTS.SELECT_SINGLE_USER);
                main.me.fire(EVENTS.CHANGE_COLOR, "#0000FF");
                main.me.fire(EVENTS.MAKE_ACTIVE);
                break;
            case EVENTS.SYNC_PROFILE:
                synchronizeName();
            default:
                break;
        }
        //if(this && this.properties && !this.properties.active) return false;
        return true;
    };

    this.createView = function(myUser) {
        var delta = parseInt((new Date().getTime() - myUser.changed) / 1000);
        var view = {
            user: myUser,
            color: myUser.color,
            name: myUser.name,
            number: myUser.number,
            active: false,
            selected: myUser.selected,
            changed: myUser.changed,
            enabled: delta <= 120,
            getDisplayName: getDisplayName.bind(myUser),
        };

        delete myUser.color;
        delete myUser.name;
        delete myUser.active;
        delete myUser.selected;
        myUser.properties = view;

        var disableIfOffline = function() {
            //console.log(this.properties.name, this.properties.changed);
            if(this.type === "user") {
                var delta = parseInt((new Date().getTime() - this.properties.changed) / 1000);
                if (this.properties && this.properties.active && delta > 3600) {
                    this.fire(EVENTS.MAKE_INACTIVE);
                } else if (this.properties && this.properties.enabled && delta > 120) {
                    this.fire(EVENTS.MAKE_DISABLED);
                }
            }
        };

        disableIfOffline.call(myUser);
        view.taskChanged = setInterval(disableIfOffline.bind(myUser), 10000);

        return view;
    };

    this.removeView = function(myUser) {
        clearInterval(myUser.properties.taskChanged);
    };

    function getDisplayName(){
        var name = this.properties.name;
        if(!name){
            if(this.number == main.me.number) {
                name = "Me";
            } else {
                name = "Friend "+this.number;
            }
        }
        return name;
    }

    function setMyName(name){
        if(setNameDialog) setNameDialog.close();
        setNameDialog.items[0].value = main.me.properties.name || "";
        setNameDialog.options.priority = 9;
        setNameDialog.open();
    }

    function synchronizeName(forceToServer) {
        var sync = new utils.sync({
            type: utils.sync.Type.ACCOUNT_PRIVATE,
            log: true,
            key: DATABASE.NAME,
            onupdatevalue: function(key, newName, oldName) {
                main.me.fire(EVENTS.CHANGE_NAME, u.clear(newName));
            }
        });
        if(forceToServer) {
            sync.overrideRemoteValue(main.me.properties.name);
        } else if(main.me && main.me.properties) {
            sync.syncValue(main.me.properties.name || null);
        }
    }

    this.options = function(){
        return {
            id: "general",
            title: u.lang.general,
            categories: [
                {
                    id: "general:properties",
                    title: u.lang.properties,
                    items: [
                        {
                            id:"properties:name",
                            type: HTML.INPUT,
                            label: u.lang.name,
                            default: u.load("properties:name") || "",
                            onaccept: function(e, event) {
                                u.save("properties:name", this.value);
                                u.save("properties:name_asked", true);
                                main.me.fire(EVENTS.CHANGE_NAME, u.clear(this.value));
                            }
                        }
                    ]
                }
            ]
        }
    }

}