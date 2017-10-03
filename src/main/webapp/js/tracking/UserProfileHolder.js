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
    var waitingDialog;
    var shareBlockedDialog;
    var accountCreatedDialog;
    var globalSync;

    function start() {
//        placeholder = u.create(HTML.DIV, {className:"dialog-dim hidden"}, document.body);
        waitingDialog = u.dialog({
            queue: true,
            className: "progress-dialog",
            modal: true,
            items: [
                { type: HTML.DIV, className: "progress-dialog-circle" },
                { type: HTML.DIV, className: "progress-dialog-title", innerHTML: "Waiting for signing in..." }
            ]
        }, document.body);
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
            case EVENTS.FIREBASE_READY:
                setGlobalSync();
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
                    label: u.lang.close
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
                            main.fire(EVENTS.SYNC_PROFILE);
                            //synchronizeName();
                        }
                    },
                    {
                        type: HTML.DIV,
                        innerHTML: "Sign out",
                        onclick: function(evt) {
                            u.save("uuid");
                            u.save(REQUEST.SIGN_PROVIDER);
                            signOtherLogin(function(result) {
                                firebase.auth().signOut().then(function(signout) {
                                    main.toast.show("Signed out");
                                    initProfileDialog();
                                    utils.getUuid(function(uuid){
                                        //signOtherLogin()
//                                        placeholder.hide(HIDING.OPACITY);
                                        waitingDialog.close();
                                        onAuthStateChanged(result);
                                    });
                                }).catch(function (error) {
                                    console.error(error);
                                    main.toast.show("Signed out");

                                    initProfileDialog();
                                    waitingDialog.close();
//                                    placeholder.hide(HIDING.OPACITY);
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

        switch (mode) {
            case "facebook.com":
                menu.setHeader({
                    type: HTML.DIV, children: [
                        u.create(HTML.SPAN, {innerHTML: "Signed with Facebook"}),
                        u.create(HTML.IMG, {src: "/images/facebook.svg", className: "icon menu-item-icon"})
                    ]
                });
                break;
            case "google.com":
                menu.setHeader({
                    type: HTML.DIV, children: [
                        u.create(HTML.SPAN, {innerHTML: "Signed with Google"}),
                        u.create(HTML.IMG, {src: "/images/google.svg", className: "icon menu-item-icon"})
                    ]
                });
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
                var email = profileDialog.loginNode ? profileDialog.loginNode.value : "";
                profileDialog.loginNode = profileDialog.addItem({ type: HTML.INPUT, label: "E-mail", value: email });
                profileDialog.passwordNode = profileDialog.addItem({ type: HTML.PASSWORD, label: "Password" });
                profileDialog.addItem({
                    type: HTML.DIV,
                    className: "user-profile-forgot-password",
                    innerHTML: "Forgot password?",
                    onclick: function (evt) {
                        initProfileDialog("email_restore");
                        profileDialog.open();
                        return false;
                    }
                });
                profileDialog.setPositive({
                    label: u.lang.sign_in,
                    dismiss: false,
                    onclick: function() {
                        console.log("SIGN IN", profileDialog.items[0].value, profileDialog.items[1].value);
                        signOtherLogin(function () {
                            console.log("LOGIN PASSWORD", this);
                            var provider = new firebase.auth.GoogleAuthProvider();
                            firebase.auth().signInWithEmailAndPassword(profileDialog.items[0].value, profileDialog.items[1].value)
                                .then(onAuthStateChanged)
                                .catch(onAuthStateError);
                        });
                    }
                });
                profileDialog.setNeutral({
                    label: u.lang.sign_up,
                    dismiss: false,
                    onclick: function() {
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
                profileDialog.loginNode = profileDialog.addItem({ type: HTML.INPUT, label: "E-mail", value: profileDialog.loginNode.value });
                profileDialog.passwordNode = profileDialog.addItem({ type: HTML.PASSWORD, label: "Password" });
                profileDialog.confirmPasswordNode = profileDialog.addItem({ type: HTML.PASSWORD, label: "Confirm password" });
                profileDialog.setPositive({
                    label: u.lang.sign_up,
                    dismiss: false,
                    onclick: function() {
                        if(profileDialog.passwordNode.value != profileDialog.confirmPasswordNode.value) {
                            profileDialog.errorNode.innerHTML = "Confirm password not equals to password";
                            profileDialog.errorNode.show();
                            profileDialog.passwordNode.focus();
                            return;
                        }
                        signOtherLogin(function () {
                            console.log("LOGIN GOOGLE", this);
                            var provider = new firebase.auth.GoogleAuthProvider();
                            firebase.auth().createUserWithEmailAndPassword(profileDialog.loginNode.value, profileDialog.passwordNode.value)
                                .then(function(result) {
                                    result.sendEmailVerification().then(function() {
                                        profileDialog.setFooter(null);
                                        profileDialog.clearItems();
                                        profileDialog.setPositive(null);
                                        profileDialog.setNeutral(null);

                                        accountCreatedDialog = accountCreatedDialog || u.dialog({
                                            title: "Account created",
                                            items: [
                                                {type:HTML.DIV, innerHTML: "Your account is created. Remember your login and password to sign next time. Also, you will get a confirmation email. Please follow the link in this email, otherwise this account will be deleted in 30 days." }
                                            ],
                                            positive: {
                                                label: u.lang.ok
                                            }
                                        }, main.right);
                                        accountCreatedDialog.open();
                                        onAuthStateChanged(result);
                                    }).catch(onAuthStateError);
                                })
                                .catch(onAuthStateError);
                        });
                    }
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
                profileDialog.loginNode = profileDialog.addItem({ type: HTML.INPUT, label: "E-mail", value: profileDialog.loginNode.value});
                profileDialog.setPositive({
                    label: u.lang.reset,
                    dismiss: false,
                    onclick: function() {
                        firebase.auth().sendPasswordResetEmail(profileDialog.items[0].value)
                            .then(function() {
                                initProfileDialog("email_sent");
                            }).catch(onAuthStateError);
                    }
                });
                profileDialog.setNeutral({
                    label: u.lang.back,
                    dismiss: false,
                    onclick: function() {
                        console.log("BACK");
                        initProfileDialog("email_signin");
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
        profileDialog.errorNode = profileDialog.addItem({innerHTML: "", className: "user-profile-dialog-error hidden"});

    }

    function signOtherLogin(signProcedureCallback) {
//        placeholder.show(HIDING.OPACITY);
        waitingDialog.open();
        if(main.tracking && main.tracking.getStatus() != EVENTS.TRACKING_DISABLED) {
            resign = true;
            if(globalSync) globalSync.watchChanges(null);
            main.tracking.stop(function(e){
                console.log("STOPPED");
                signProcedureCallback();
            });
            return;
        }
        signProcedureCallback();
    }

    function onAuthStateChanged(result) {
//        placeholder.hide(HIDING.OPACITY);
        waitingDialog.close();
//        debugger;
        setGlobalSync();
        if (result) {
            try {
                result = result.user ? result.user.toJSON() : result.toJSON();

                u.save("uuid", result.uid);
                result.providerData.forEach(function (profile) {
                    u.save(REQUEST.SIGN_PROVIDER, profile.providerId);

                    main.toast.show("Signed as " + (profile.displayName || profile.email) + " using " + profile.providerId);
                });
                initProfileDialog();

                //main.fire(EVENTS.SYNC_PROFILE);
                // {uuid: "e30a815be353be517c2f07498c2193aa", uid: null}
            }catch(e) {
                console.error(e);
            }
        } else {
            console.log("OUT:");
            // User is signed out.
        }
        if(resign) {
            main.tracking.setLink(window.location.href);
            main.tracking.start(function(e){console.log(e)});
            //window.location = window.location.href;
            resign = false;
        }
    }

    function onAuthStateError(error) {
        waitingDialog.close();
        setGlobalSync();
        console.log("ERROR",error);

        switch(error.code) {
            case "auth/unauthorized-domain":
            case "auth/invalid-email":
            case "auth/user-not-found":
            case "auth/email-already-in-use":
            case "auth/popup-closed-by-user":
                profileDialog.errorNode.innerHTML = error.message;
                profileDialog.errorNode.show();
                if(profileDialog.loginNode) profileDialog.loginNode.focus();
                break;
            case "auth/weak-password":
                profileDialog.errorNode.innerHTML = error.message;
                profileDialog.errorNode.show();
                if(profileDialog.passwordNode) profileDialog.passwordNode.focus();
                break;
            case "auth/popup-blocked":
                profileDialog.errorNode.innerHTML = error.message;
                profileDialog.errorNode.show();

                shareBlockedDialog = shareBlockedDialog || u.dialog({
                    items: [
                        {type:HTML.DIV, innerHTML: u.lang.popup_blocked_dialog_1 },
                        {type:HTML.DIV, enclosed:true, innerHTML: u.lang.popup_blocked_dialog_2 },
                    ],
                    positive: {
                        label: u.lang.close
                    }
                }, main.right);
                shareBlockedDialog.open();
                break;
            default:
                profileDialog.errorNode.innerHTML = error.message;
                profileDialog.errorNode.show();
                if(profileDialog.loginNode) profileDialog.loginNode.focus();
                console.error("ERROR", error);
        }

        setGlobalSync();
        if(resign) {
            main.tracking.setLink(window.location.href);
            main.tracking.start(function(e){console.log(e)});
            resign = false;
        }
    }

    function saveUid() {
        userBackup = {
            uuid: u.load("uuid"),
        };
        console.log(userBackup);
    }

    function getUser() {
        var user = null;

        var data = firebase.auth().currentUser;
        if(data) {
            user = {};
            data.providerData.forEach(function(item){
                user = u.cloneAsObject(item);
                return false;
            });
            user.uid = data.uid;
        }
        return user;
    }

    function setGlobalSync() {
        if(getUser()) {
            //var sync = new utils.sync({type:utils.sync.Type.ACCOUNT_PRIVATE});
            //console.log(sync.ready());
            //sync.watch(REQUEST.SIGN_PROVIDER, function(key, newValue) {
            //    console.log("CHANGED", key, newValue);
            //    //                        main.fire(EVENTS.SYNC_PROFILE);
            //});
            globalSync = new utils.sync({type: utils.sync.Type.ACCOUNT_PRIVATE});
            if(globalSync.ready()) {
                globalSync.watchChanges(function (key, newValue) {
                    console.log("SYNC_PROFILE", key, newValue);
                    main.fire(EVENTS.SYNC_PROFILE);
                });
            }
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


