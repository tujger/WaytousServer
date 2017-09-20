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
    var user;

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
            case EVENTS.MAP_READY:
                firebase.auth().onAuthStateChanged(function(userOptions) {
                    if (userOptions) {
                        user = userOptions;
                        // User is signed in.
                        var displayName = user.displayName;
                        var email = user.email;
                        var emailVerified = user.emailVerified;
                        var photoURL = user.photoURL;
                        var isAnonymous = user.isAnonymous;
                        userId = user.uid;
                        var providerData = user.providerData;
                        console.log("AUTH:",user.toJSON());

                        //u.save("uuid", userId);
                        //initProfileDialog("email");

                        user.providerData.forEach(function (profile) {
                            console.log("Sign-in provider: "+profile.providerId);
                            console.log("  Provider-specific UID: "+profile.uid);
                            console.log("  Name: "+profile.displayName);
                            console.log("  Email: "+profile.email);
                            console.log("  Photo URL: "+profile.photoURL);
                        });

                    } else {
                        console.log("OUT:");
                        // User is signed out.
                    }
                });
                break;
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
                //autoclose: true,
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
                profileDialog.clearItems();
                break;
            case "twitter":
                profileDialog.clearItems();
                break;
            case "email":
                profileDialog.clearItems();
                break;
            default:

                profileDialog.clearItems();
                if(main.tracking && main.tracking.getStatus() != EVENTS.TRACKING_DISABLED) {
                    profileDialog.setHeader({
                        type: HTML.DIV,
                        innerHTML: "You must exit group to be able sign in with other login."
                    });
                    profileDialog.addItem({
                        content: u.create(HTML.BUTTON, {
                            className:"dialog-button dialog-item-button",
                            onclick: function() {
                                main.fire(EVENTS.TRACKING_STOP);
                            }
                        }).place(HTML.DIV, {
                            className: "dialog-item-icon",
                            innerHTML: "exit_to_app"
                        }).place(HTML.DIV, {
                            innerHTML: u.lang.exit_group
                        })
                    });
                    break;
                }
                profileDialog.addItem({
                    content: u.create(HTML.BUTTON, {
                        className:"dialog-button dialog-item-button",
                        onclick: function() {
                            console.log("LOGIN FACEBOOK", this);

                            var provider = new firebase.auth.FacebookAuthProvider();
                            provider.setCustomParameters({
                                'display': 'popup'
                            });
                            firebase.auth().signInWithPopup(provider);

                            //initProfileDialog("facebook");
                        }
                    }).place(HTML.DIV, {
                        className: "dialog-item-icon",
                        content: u.create(HTML.IMG, {src:"/images/facebook.svg"})
                    }).place(HTML.DIV, {
                        innerHTML: u.lang.sign_in_with_facebook
                    })
                });
                profileDialog.addItem({
                    content: u.create(HTML.BUTTON, {
                        className:"dialog-button dialog-item-button",
                        onclick: function() {
                            console.log("LOGIN GOOGLE", this);

                            var provider = new firebase.auth.GoogleAuthProvider();
                            firebase.auth().signInWithPopup(provider)/*.then(function(result) {
                                // This gives you a Google Access Token. You can use it to access the Google API.
                                var token = result.credential.accessToken;
                                // The signed-in user info.
                                var user = result.user;
                                // ...
                            }).catch(function(error) {
                                // Handle Errors here.
                                var errorCode = error.code;
                                var errorMessage = error.message;
                                // The email of the user's account used.
                                var email = error.email;
                                // The firebase.auth.AuthCredential type that was used.
                                var credential = error.credential;
                                // ...
                            })*/;

                        }
                    }).place(HTML.DIV, {
                        className: "dialog-item-icon",
                        content: u.create(HTML.IMG, {src: "/images/google.svg"})
                    }).place(HTML.DIV, {
                        innerHTML: u.lang.sign_in_with_google
                    })
                });
                profileDialog.addItem({
                    content: u.create(HTML.BUTTON, {
                        className:"dialog-button dialog-item-button",
                        onclick: function() {
                            console.log("LOGIN TWITTER", this);

                            var provider = new firebase.auth.TwitterAuthProvider();
                            firebase.auth().signInWithPopup(provider);
                        }
                    }).place(HTML.DIV, {
                        className: "dialog-item-icon",
                        content: u.create(HTML.IMG, {src: "/images/twitter.svg"})
                    }).place(HTML.DIV, {
                        innerHTML: u.lang.sign_in_with_twitter
                    })
                });
                profileDialog.addItem({
                    content: u.create(HTML.BUTTON, {
                        className:"dialog-button dialog-item-button",
                        onclick: function() {
                            console.log("LOGIN EMAIL", this);
                            firebase.auth().createUserWithEmailAndPassword("test1@gmail.com", "testpassword").catch(function(error) {
                                // Handle Errors here.
                                var errorCode = error.code;
                                var errorMessage = error.message;
                                // ...
                                console.log("ERROR:"+error);
                            });
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
                        className:"dialog-button dialog-item-button",
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
        type:type
    }
}