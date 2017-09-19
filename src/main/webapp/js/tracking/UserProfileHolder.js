/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 9/19/17.
 */
EVENTS.SHOW_USER_PROFILE = "show_user_profile";

function UserProfileHolder(main) {

    var type = "user";
    var profileDialog;
    var userId;

    var facebook_svg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"0 0 487 487",
        version:"1.1",
        className: "menu-item"
    };
    var facebook_path = {
        xmlns:"http://www.w3.org/2000/svg",
        strokeWidth:"2",
        fill:"white",
        stroke: "transparent",
        d: "M243.196,0C108.891,0,0,108.891,0,243.196s108.891,243.196,243.196,243.196 s243.196-108.891,243.196-243.196C486.392,108.861,377.501,0,243.196,0z M306.062,243.165l-39.854,0.03l-0.03,145.917h-54.689 V243.196H175.01v-50.281l36.479-0.03l-0.061-29.609c0-41.039,11.126-65.997,59.431-65.997h40.249v50.311h-25.171 c-18.817,0-19.729,7.022-19.729,20.124l-0.061,25.171h45.234L306.062,243.165z"
    };
    var twitter_svg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"0 0 612 612",
        version:"1.1",
        className: "menu-item",

    };
    var twitter_path = {
        xmlns:"http://www.w3.org/2000/svg",
        fill: "white",
        d: "M612,116.258c-22.525,9.981-46.694,16.75-72.088,19.772c25.929-15.527,45.777-40.155,55.184-69.411 c-24.322,14.379-51.169,24.82-79.775,30.48c-22.907-24.437-55.49-39.658-91.63-39.658c-69.334,0-125.551,56.217-125.551,125.513 c0,9.828,1.109,19.427,3.251,28.606C197.065,206.32,104.556,156.337,42.641,80.386c-10.823,18.51-16.98,40.078-16.98,63.101 c0,43.559,22.181,81.993,55.835,104.479c-20.575-0.688-39.926-6.348-56.867-15.756v1.568c0,60.806,43.291,111.554,100.693,123.104 c-10.517,2.83-21.607,4.398-33.08,4.398c-8.107,0-15.947-0.803-23.634-2.333c15.985,49.907,62.336,86.199,117.253,87.194 c-42.947,33.654-97.099,53.655-155.916,53.655c-10.134,0-20.116-0.612-29.944-1.721c55.567,35.681,121.536,56.485,192.438,56.485 c230.948,0,357.188-191.291,357.188-357.188l-0.421-16.253C573.872,163.526,595.211,141.422,612,116.258z"
    };
    var google_svg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"20 20 480 480",
        version:"1.1",
        className: "menu-item",
    };
    var google_path = {
        xmlns:"http://www.w3.org/2000/svg",
        fill: "white",
        d: "M457.732,216.625c2.628,14.041,4.063,28.743,4.063,44.098C461.796,380.688,381.481,466,260.204,466  c-116.023,0-210-93.977-210-210c0-116.023,93.977-210,210-210c56.704,0,104.077,20.867,140.44,54.73l-59.204,59.197v-0.135  c-22.046-21.002-50-31.762-81.236-31.762c-69.297,0-125.604,58.537-125.604,127.841c0,69.29,56.306,127.968,125.604,127.968  c62.87,0,105.653-35.965,114.46-85.312h-114.46v-81.902H457.732z"
    };


    function start() {
        console.log("USERPROFILEHOLDER", this);

        userId = u.load("userprofile:id");

    }

    function onEvent(EVENT,object){
        // console.log("SAMPLEEVENT",EVENT,object)
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                var menuItem = object.add(DRAWER.SECTION_MISCELLANEOUS, EVENT.SAMPLE_EVENT, u.lang.user_profile, "person", function(){
                         main.fire(EVENTS.SHOW_USER_PROFILE);
                   console.log("USERPROFILEEVENTDRAWERCALLBACK", EVENT);
                });
//                menuItem.classList.add("disabled");
                break;
            /*case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user) {
                    object.add(MENU.SECTION_PRIMARY, "USERPROFILE:AAAAAA", u.lang.sample_menu, "person", function () {
//                        u.save("sample:show:"+user.number, true);
                        console.log("USERPROFILEEVENTMENUCALLBACK", user);
                    });
                }
                break;*/
            case EVENTS.SHOW_USER_PROFILE:
                initProfileDialog();
                profileDialog.open();
                break;
            default:
                break;
        }
        return true;
    }

    function createView(user){
        var view = {
            user: user,
//            show: u.load("sample:user:" + user.number)
        };
        // console.log("SAMPLECREATEVIEW",user);
        return view;
    }

    function onChangeLocation(location) {
        // console.log("SAMPLEONCHANGELOCATION",this,location);
    }

    function options(){
        return {
            id: "general",
            title: u.lang.general,
            categories: [
                {
                    id: "general:user_profile",
                    title: "User",
                    items: [
                        {
                            id: "general:user_profile",
                            type: HTML.CHECKBOX,
                            label: "Sample option",
//                            checked: u.load("user:sample"),
                            onaccept: function(e, event) {
//                                u.save("general:sample", this.checked);
                            },
                            onchange: function(e, event) {
                                u.toast.show("general:sample " + this.checked);
                            },
                            onshow: function(e) {
                                u.toast.show("general:sample " + this.checked);
                            },
                        }
                    ]
                }
            ]
        }
    }

    function initProfileDialog(mode) {
        if(!profileDialog) {
          profileDialog = u.dialog({
                title: {
                    label: u.lang.user_profile,
                    filter: true
                },
                className: "user-profile-dialog",
                itemsClassName: "user-profile-dialog-message",
                tabindex: 3,
                resizeable: true,
                autoclose: true,
                items: [
                ],
                negative: {
                    onclick: function(){
                        console.log("NEGATIVE")

//                        u.saveForContext("message:chat");
                    }
                },
                onopen: function() {
                    console.log("OPEN")
//                    lastReadTimestamp = new Date().getTime();
//                    u.saveForContext("message:lastread", lastReadTimestamp);
                },
            }, main.right);
        }

        switch(mode) {
            case "facebook":
                break;
            case "twitter":
                break;
            case "email":
                break;
            default:
                profileDialog.clearItems();
                profileDialog.addItem({
                        content: u.create(HTML.BUTTON, {
                            className:"dialog-button dialog-item-button hidden",
                            onclick: function() {
                                console.log("LOGIN FACEBOOK", this)
                            }
                        }).place(HTML.DIV, {
                               className: "dialog-item-icon",
                               content: u.create(HTML.PATH, facebook_path, u.create(HTML.SVG, facebook_svg)).parentNode
                           }).place(HTML.DIV, {
                             innerHTML: u.lang.sign_in_with_facebook
                         })
                    });
                profileDialog.addItem({
                        content: u.create(HTML.BUTTON, {
                            className:"dialog-button dialog-item-button",
                            onclick: function() {
                                console.log("LOGIN GOOGLE", this)
                            }
                        }).place(HTML.DIV, {
                               className: "dialog-item-icon",
                               content: u.create(HTML.PATH, google_path, u.create(HTML.SVG, google_svg)).parentNode
                           }).place(HTML.DIV, {
                             innerHTML: u.lang.sign_in_with_google
                         })
                    });
                profileDialog.addItem({
                        content: u.create(HTML.BUTTON, {
                            className:"dialog-button dialog-item-button hidden",
                            onclick: function() {
                                console.log("LOGIN TWITTER", this)
                            }
                        }).place(HTML.DIV, {
                               className: "dialog-item-icon",
                               content: u.create(HTML.PATH, twitter_path, u.create(HTML.SVG, twitter_svg)).parentNode
                           }).place(HTML.DIV, {
                             innerHTML: u.lang.sign_in_with_twitter
                         })
                    });
                profileDialog.addItem({
                        content: u.create(HTML.BUTTON, {
                            className:"dialog-button dialog-item-button",
                            onclick: function() {
                                console.log("LOGIN EMAIL", this)
                            }
                        }).place(HTML.DIV, {
                               className: "dialog-item-icon",
                               innerHTML: "mail_outline"
                           }).place(HTML.DIV, {
                             innerHTML: u.lang.sign_in_with_email
                        })
                    });
                profileDialog.addItem({
                        content: u.create(HTML.BUTTON, {
                            className:"dialog-button dialog-item-button hidden",
                            onclick: function() {
                                console.log("LOGIN PHONE", this)
                            }
                        }).place(HTML.DIV, {
                               className: "dialog-item-icon",
                               innerHTML: "phone"
                           }).place(HTML.DIV, {
                             innerHTML: u.lang.sign_in_with_phone
                        })
                    });

                break;
        }


    }

    return {
        createView:createView,
        onChangeLocation:onChangeLocation,
        onEvent:onEvent,
        options:options,
        start:start,
        type:type,
    }
}