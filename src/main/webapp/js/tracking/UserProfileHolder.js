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
    var userBackup;
    var resign;
    var placeholder;
    var nameUpdateDialog;
    var menu;

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
                    filter: false
                },
                className: "user-profile-dialog",
                itemsClassName: "user-profile-dialog-message",
                tabindex: 3,
                negative: {
                    label: u.lang.close,
                    onclick: function(){
                        console.log("NEGATIVE")

//                        u.saveForContext("message:chat");
                    }
                },
                onopen: function() {
                    console.log("OPEN")
//                    lastReadTimestamp = new Date().getTime();
//                    u.saveForContext("message:lastread", lastReadTimestamp);
                }
            }, main.right);
            menu = u.menu({
                items: [
                    {
                        type: HTML.DIV,
                        innerHTML: "Update your name",
                        onclick: function() {
                            nameUpdateDialog = nameUpdateDialog || u.dialog({
                                positive: {
                                    label: u.lang.yes,
                                    onclick: function(items) {
                                        main.me.fire(EVENTS.CHANGE_NAME, u.create.variables.userNameNode.innerHTML);

                                    }
                                },
                                negative: {
                                    label: u.lang.no
                                },
                                timeout: 10000
                            }, main.right);
                            nameUpdateDialog.setItems([
                                {
                                    type: HTML.DIV,
                                    innerHTML: "You want to change your name in Waytous to " + u.create.variables.userNameNode.innerHTML + ". Continue?"
                                }
                            ]);
                            nameUpdateDialog.open();
                        }
                    },
                    {
                        type: HTML.DIV,
                        innerHTML: "Sync",
                        onclick: function() {
                            console.log("SYNC");
                        }
                    },
                    {
                        type: HTML.DIV,
                        innerHTML: "Sign out",
                        onclick: function(evt) {
                            u.save("uid");
                            u.save("uuid");
                            u.save(REQUEST.SIGN_PROVIDER);
                            signOtherLogin(function(result) {
                                firebase.auth().signOut().then(function(signout) {
                                    main.toast.show("Signed out");
                                    initProfileDialog();
                                    utils.getUuid(function(uuid){
                                        //signOtherLogin()
                                        placeholder.hide(HIDING.OPACITY);
                                        onAuthStateChanged(result);
                                    });
                                }).catch(function (error) {
                                    console.error(error);
                                    main.toast.show("Signed out");

                                    initProfileDialog();
                                    placeholder.hide(HIDING.OPACITY);
                                });
                            });
                        }
                    }
                ]
            });
        }

        var data = firebase.auth().currentUser;
        var user = null;
        if(data) {
            data.providerData.forEach(function(item){
                user = item;
                if(!mode) {
                    mode = item.providerId;
                }
                return false;
            });
        }
        mode = mode || "anonymous";


        profileDialog.setHeader(null);
        profileDialog.setFooter(null);
        profileDialog.clearItems();
        profileDialog.setPositive(null);
        profileDialog.setNeutral(null);
        //console.log(mode, firebase.auth().currentUser.toJSON());
        switch (mode) {
            case "facebook.com":
                menu.setHeader({
                    type: HTML.DIV, children: [
                        u.create(HTML.SPAN, {innerHTML: "Signed with Facebook"}),
                        u.create(HTML.IMG, {src: "/images/facebook.svg", className: "icon menu-item-icon"})
                    ] //innerHTML: "Signed with Twitter"
                });
                break;
            case "google.com":
                menu.setHeader({
                    type: HTML.DIV, children: [
                        u.create(HTML.SPAN, {innerHTML: "Signed with Google"}),
                        u.create(HTML.IMG, {src: "/images/google.svg", className: "icon menu-item-icon"})
                    ] //innerHTML: "Signed with Twitter"
                });
                //var node = u.create(HTML.DIV, {className:"test1"}, document.body);
                //node.content(u.create(HTML.DIV, {className:"test2"}));

                //profileDialog.addItem({
                //    content: u.create()
                //        .place({innerHTML:"TEST4"})
                //        .place({innerHTML:"TEST5"})
                //        .place({innerHTML:"TEST6"})
                //});
                //profileDialog.addItem({
                //    content: u.create()
                //        .place({innerHTML:"TEST7"})
                //        .place({innerHTML:"TEST8"})
                //        .place({innerHTML:"TEST9"})
                //});
                //profileDialog.addItem({
                //    content: u.create()
                //        .place({innerHTML:"TEST10"})
                //        .place({innerHTML:"TEST11"})
                //        .place({innerHTML:"TEST12"})
                //});


                break;
            case "twitter.com":
                menu.setHeader({
                    type: HTML.DIV, children: [
                        u.create(HTML.SPAN, {innerHTML: "Signed with Twitter"}),
                        u.create(HTML.IMG, {src: "/images/twitter.svg", className: "icon menu-item-icon"})
                    ]
                });
                break;
            case "email_signin":
                profileDialog.setHeader({
                    type: HTML.DIV,
                    className: "user-profile-dialog-summary",
                    innerHTML: "Signing in to account"
                });
                profileDialog.setItems([
                    { type: HTML.INPUT, label: "E-mail" },
                    { type: HTML.PASSWORD, label: "Password" },
                    {
                        type: HTML.DIV,
                        className: "user-profile-forgot-password",
                        innerHTML: "Forgot password?",
                        onclick: function (evt) {
                            initProfileDialog("email_restore");
                            profileDialog.open();
                            return false;
                        }
                    }
                ]);
                profileDialog.errorNode = profileDialog.addItem({
                    innerHTML: "",
                    className: "user-profile-dialog-error hidden"
                });
                profileDialog.setPositive({
                    label: u.lang.sign_in,
                    dismiss: false,
                    onclick: function() {
                        console.log("SIGN IN", profileDialog.items[0].value, profileDialog.items[1].value);
                        firebase.auth().signInWithEmailAndPassword(profileDialog.items[0].value, profileDialog.items[1].value).then(onAuthStateChanged).catch(function(error) {

                            profileDialog.errorNode.innerHTML = error.message;
                            profileDialog.errorNode.show();
                            console.log("ERROR",error)
                        });
                    }
                });
                profileDialog.setNeutral({
                    label: u.lang.sign_up,
                    dismiss: false,
                    onclick: function() {
                        console.log("SIGN UP");
                        initProfileDialog("email_signup");
                    }
                });
                profileDialog.items[0].focus();
                break;
            case "email_signup":
                profileDialog.setHeader({
                    type: HTML.DIV,
                    className: "user-profile-dialog-summary",
                    innerHTML: "Creating new account"
                });
                profileDialog.setItems([
                    { type: HTML.INPUT, label: "E-mail"},
                    { type: HTML.PASSWORD, label: "Password"},
                    { type: HTML.PASSWORD, label: "Confirm password"},
                ]);
                profileDialog.setPositive({
                    label: u.lang.sign_up,
                    dismiss: false,
                    onclick: function() {
                        console.log("SIGN UP", profileDialog.items[0].value, profileDialog.items[1].value);
                        /*firebase.auth().createUserWithEmailAndPassword(profileDialog.items[0].value, profileDialog.items[1].value).then(onAuthStateChanged).catch(function(error) {
                            console.log("ERROR",error)
                        });  */               }
                });
                profileDialog.setNeutral({
                    label: u.lang.back,
                    dismiss: false,
                    onclick: function() {
                        console.log("BACK");
                        initProfileDialog("email_signin");
                    }
                });
                profileDialog.items[0].focus();
                break;
            case "email_restore":
                profileDialog.setHeader({
                    type: HTML.DIV,
                    className: "user-profile-dialog-summary",
                    innerHTML: "Resetting password"
                });
                profileDialog.setItems([
                    { type: HTML.INPUT, label: "E-mail"},
                ]);
                profileDialog.setPositive({
                    label: u.lang.reset,
                    dismiss: false,
                    onclick: function() {
                        firebase.auth().sendPasswordResetEmail(profileDialog.items[0].value).then(function() {
                            initProfileDialog("email_sent");
                        }).catch(function(error) {
                            profileDialog.errorNode.innerHTML = error.message;
                            profileDialog.errorNode.show();
                            // An error happened.
                        });
                        /*firebase.auth().createUserWithEmailAndPassword(profileDialog.items[0].value, profileDialog.items[1].value).then(onAuthStateChanged).catch(function(error) {
                            console.log("ERROR",error)
                        });  */               }
                });
                profileDialog.setNeutral({
                    label: u.lang.back,
                    dismiss: false,
                    onclick: function() {
                        console.log("BACK");
                        initProfileDialog("email_signup");
                    }
                });
                break;
            case "email_sent":
                profileDialog.setHeader({
                    type: HTML.DIV,
                    className: "user-profile-dialog-summary",
                    innerHTML: "Email sent"
                });
                break;
            case "anonymous":
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
                        className: "dialog-button dialog-item-button",
                        onclick: function () {

                            signOtherLogin(function () {
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
                        content: u.create(HTML.IMG, {src: "/images/facebook.svg"})
                    }).place(HTML.DIV, {
                        innerHTML: u.lang.sign_in_with_facebook
                    })
                });
                profileDialog.addItem({
                    content: u.create(HTML.BUTTON, {
                        className: "dialog-button dialog-item-button",
                        onclick: function () {

                            signOtherLogin(function () {
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
                            })*/
                            ;

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
                        className: "dialog-button dialog-item-button",
                        onclick: function () {
                            signOtherLogin(function () {
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
                        className: "dialog-button dialog-item-button",
                        onclick: function () {
                            initProfileDialog("email_signin");
                        }
                    }).place(HTML.DIV, {
                        className: "dialog-item-icon",
                        innerHTML: "mail_outline"
                    }).place(HTML.DIV, {
                        innerHTML: u.lang.sign_in_with_email
                    })
                });
                break;
            default:
                break;
        }
        /*profileDialog.addItem({
            content: u.create(HTML.BUTTON, {
                className: "dialog-button dialog-item-button",
                onclick: function () {
                    console.log("LOGIN PHONE", this)
                }
            }).place(HTML.DIV, {
                className: "dialog-item-icon",
                innerHTML: "phone"
            }).place(HTML.DIV, {
                innerHTML: u.lang.sign_in_with_phone
            })
        });*/


        if(user) {
            profileDialog.setHeader({
                type: HTML.DIV,
                className: "user-profile-dialog-header"
            })
                .place(HTML.IMG, {className: "user-profile-dialog-avatar", src: user.photoURL})
                .appendChild(u.create({className: "user-profile-dialog-resume"})
                    .place({
                        children: u.create()
                            .place({
                                innerHTML: user.displayName,
                                className: "user-profile-dialog-name",
                                variable: "userNameNode"
                            })
                            .place(HTML.A, {
                                href: "mailto:" + user.email,
                                innerHTML: user.email ? "&lt;" + user.email + "&gt;" : "",
                                className: "user-profile-dialog-email",
                                title: "Compose e-mail",
                                target: "_blank"
                            })
                    })
                    .place({
                        children: [u.create(HTML.BUTTON, {
                            innerHTML: "more_vert",
                            className: "icon dialog-button user-profile-dialog-menu-button",
                            onclick: function () {
                                menu.open(this);
                            }
                        })]
                    })
                );
        }

    }

    function signOtherLogin(signProcedureCallback) {
        placeholder.show(HIDING.OPACITY);
        if(main.tracking && main.tracking.getStatus() != EVENTS.TRACKING_DISABLED) {
            resign = true;
            main.tracking.stop(function(e){
                console.log("STOPPED");
                signProcedureCallback();
            });
            return;
        }
        signProcedureCallback();
    }

    function onAuthStateChanged(result) {
        placeholder.hide(HIDING.OPACITY);
        if (result) {
            try {
                var user = result.user.toJSON();
                //// User is signed in.
                //var displayName = user.displayName;
                //var email = user.email;
                //var emailVerified = user.emailVerified;
                //var photoURL = user.photoURL;
                //var isAnonymous = user.isAnonymous;
                //var providerData = user.providerData;
                //console.log("AUTH:", user);

                u.save("uuid", user.uid);
                //u.save("uid", user.uid);

                user.providerData.forEach(function (profile) {
                    u.save(REQUEST.SIGN_PROVIDER, profile.providerId);

                    main.toast.show("Signed as " + user.displayName + " using " + profile.providerId);
                });
                initProfileDialog();

                // {uuid: "e30a815be353be517c2f07498c2193aa", uid: null}
            }catch(e) {
                console.error(e);
            }
        } else {
            console.log("OUT:");
            // User is signed out.
        }
        if(resign) {
            main.tracking.start(function(e){console.log(e)});
            //window.location = window.location.href;
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
            main.tracking.start(function(e){console.log(e)});
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