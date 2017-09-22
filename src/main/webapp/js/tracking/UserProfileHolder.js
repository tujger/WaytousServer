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
    var user;
    var userBackup;
    var resign;
    var placeholder;

    function start() {
        console.log("USERPROFILEHOLDER", this);

        placeholder = u.create(HTML.DIV, {className:"dialog-dim hidden"}, document.body);
        u.dialog({
            queue: true,
            className: "progress-dialog",
            items: [
                { type: HTML.DIV, className: "progress-dialog-circle" },
                { type: HTML.DIV, className: "progress-dialog-title", innerHTML: "Waiting for signing in..." }
            ]
        }, placeholder).show();
    }

    function onEvent(EVENT,object){
        // console.log("SAMPLEEVENT",EVENT,object)
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                var menuItem = object.add(DRAWER.SECTION_MISCELLANEOUS, EVENT.SAMPLE_EVENT, u.lang.user_profile, "person", function(){
                    main.fire(EVENTS.SHOW_USER_PROFILE);
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
                //if(main.tracking && main.tracking.getStatus() != EVENTS.TRACKING_DISABLED) {
                //    profileDialog.setHeader({
                //        type: HTML.DIV,
                //        innerHTML: "You must exit group to be able sign in with other login."
                //    });
                //    profileDialog.addItem({
                //        content: u.create(HTML.BUTTON, {
                //            className:"dialog-button dialog-item-button",
                //            onclick: function() {
                //                main.fire(EVENTS.TRACKING_STOP);
                //            }
                //        }).place(HTML.DIV, {
                //            className: "dialog-item-icon",
                //            innerHTML: "exit_to_app"
                //        }).place(HTML.DIV, {
                //            innerHTML: u.lang.exit_group
                //        })
                //    });
                //    break;
                //}
                profileDialog.addItem({
                    content: u.create(HTML.BUTTON, {
                        className:"dialog-button dialog-item-button",
                        onclick: function() {

                            signOtherLogin(function() {
                                console.log("LOGIN FACEBOOK", this);
                                var provider = new firebase.auth.FacebookAuthProvider();
                                provider.setCustomParameters({
                                    display: "popup"
                                });
                                firebase.auth().signInWithPopup(provider).then(onAuthStateChanged).catch(onAuthStateError);
                            });

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

                            signOtherLogin(function() {
                                console.log("LOGIN GOOGLE", this);

                                var provider = new firebase.auth.GoogleAuthProvider();
                                firebase.auth().signInWithPopup(provider).then(onAuthStateChanged).catch(onAuthStateError);
                            });
                            /*.then(function(result) {
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
                            signOtherLogin(function() {
                                console.log("LOGIN TWITTER", this);

                                var provider = new firebase.auth.TwitterAuthProvider();
                                firebase.auth().signInWithPopup(provider).then(onAuthStateChanged).catch(onAuthStateError);
                            });
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
                            signOtherLogin(function() {
                                console.log("LOGIN EMAIL", this);

                                firebase.auth().signInWithEmailAndPassword("test1@gmail.com", "testpassword").then(onAuthStateChanged).catch(onAuthStateError);
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

    function signOtherLogin(signProcedureCallback) {
        placeholder.show(HIDING.OPACITY);
        if(main.tracking && main.tracking.getStatus() != EVENTS.TRACKING_DISABLED) {
            resign = true;
            main.tracking.stop(function(e){
                console.log("STOPPED");
            });
        }
        signProcedureCallback();
    }

    function onAuthStateChanged(result) {
        placeholder.hide(HIDING.OPACITY);
        if (result) {
            try {
                var user = result.user.toJSON();
                // User is signed in.
                var displayName = user.displayName;
                var email = user.email;
                var emailVerified = user.emailVerified;
                var photoURL = user.photoURL;
                var isAnonymous = user.isAnonymous;
                var providerData = user.providerData;
                console.log("AUTH:", user);

                u.save("uuid", utils.getEncryptedHash(user.uid));
                u.save("uid", user.uid);
                //initProfileDialog("email");

                user.providerData.forEach(function (profile) {
                    u.save(REQUEST.SIGN_PROVIDER, profile.providerId);
                    console.log("Sign-in provider: " + profile.providerId);
                    console.log("  Provider-specific UID: " + profile.uid);
                    console.log("  Name: " + profile.displayName);
                    console.log("  Email: " + profile.email);
                    console.log("  Photo URL: " + profile.photoURL);
                });
// {uuid: "e30a815be353be517c2f07498c2193aa", uid: null}
            }catch(e) {
                console.error(e);
            }
        } else {
            console.log("OUT:");
            // User is signed out.
        }
        if(resign) {
            window.location = window.location.href;
            resign = false;
        }
    }

    function onAuthStateError(error) {
        placeholder.hide(HIDING.OPACITY);
        // Handle Errors here.
        console.log("ERROR",error);
        var errorCode = error.code;
        var errorMessage = error.message;
        // The email of the user's account used.
        var email = error.email;
        // The firebase.auth.AuthCredential type that was used.
        var credential = error.credential;
        if(resign) {
            window.location = window.location.href;
            resign = false;
        }
    }

    function saveUid() {
        userBackup = {
            uuid: u.load("uuid"),
            uid: u.load("uid")
        };
        console.log(userBackup);
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