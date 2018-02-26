/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 9/19/17.
 */
EVENTS.SHOW_USER_PROFILE = "show_user_profile";

function UserProfileHolder(main) {

    var type = "user";
    var profileDialog;
    var resign;
    var nameUpdateDialog;
    var menu;
    var waitingDialog;
    var shareBlockedDialog;
    var accountCreatedDialog;
    var globalSync;

    function start() {
        waitingDialog = u.dialog({
            queue: true,
            className: "progress-dialog",
            modal: true,
            items: [
                { type: HTML.DIV, className: "progress-dialog-circle" },
                { type: HTML.DIV, className: "progress-dialog-title", innerHTML: u.lang.waiting_for_sign_in }
            ]
        }, document.body);
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                object.add({section: DRAWER.SECTION_MISCELLANEOUS, id: EVENTS.SHOW_USER_PROFILE, name: u.lang.user_profile, icon: "person", callback: function(){
                    main.fire(EVENTS.SHOW_USER_PROFILE);
                }});
                break;
            case EVENTS.SHOW_USER_PROFILE:
                initProfileDialog();
                profileDialog.open();
                break;
            case EVENTS.FIREBASE_READY:
                var redirectResult = firebase.auth().getRedirectResult();
                if(redirectResult) {
                    redirectResult.then(onAuthStateChanged).catch(onAuthStateError);
                } else {
                    doGlobalSync();
                }
                break;
            default:
                break;
        }
        return true;
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
                        innerHTML: u.lang.update_your_name,
                        onclick: function() {
                            nameUpdateDialog = nameUpdateDialog || u.dialog({
                                positive: {
                                    label: u.lang.yes,
                                    onclick: function() {
                                        main.me.fire(EVENTS.CHANGE_NAME, u.clear(u.create.variables.userNameNode.innerHTML));
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
                                    innerHTML: u.lang.you_want_to_change_your_name_to_s.format(u.create.variables.userNameNode.innerHTML)
                                }
                            ]);
                            nameUpdateDialog.open();
                        }
                    },
                    {
                        type: HTML.DIV,
                        innerHTML: u.lang.synchronize,
                        onclick: function() {
                            main.fire(EVENTS.SYNC_PROFILE);
                        }
                    },
                    {
                        type: HTML.DIV,
                        innerHTML: u.lang.sign_out,
                        onclick: function() {
                            u.save("uuid");
                            u.save(REQUEST.SIGN_PROVIDER);
                            signOtherLogin(function(result) {
                                firebase.auth().signOut().then(function(signout) {
                                    main.toast.show(u.lang.signed_out);
                                    initProfileDialog();
                                    utils.getUuid(function(){
                                        waitingDialog.close();
                                        onAuthStateChanged(result);
                                    });
                                }).catch(function (error) {
                                    console.error(error);
                                    main.toast.show(u.lang.signed_out);

                                    initProfileDialog();
                                    waitingDialog.close();
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
                        u.create(HTML.SPAN, {innerHTML: u.lang.signed_with_facebook}),
                        u.create(HTML.IMG, {src: "/images/facebook.svg", className: "icon menu-item-icon"})
                    ]
                });
                break;
            case "google.com":
                menu.setHeader({
                    type: HTML.DIV, children: [
                        u.create(HTML.SPAN, {innerHTML: u.lang.signed_with_google}),
                        u.create(HTML.IMG, {src: "/images/google.svg", className: "icon menu-item-icon"})
                    ]
                });
                break;
            case "twitter.com":
                menu.setHeader({
                    type: HTML.DIV, children: [
                        u.create(HTML.SPAN, {innerHTML: u.lang.signed_with_twitter}),
                        u.create(HTML.IMG, {src: "/images/twitter.svg", className: "icon menu-item-icon"})
                    ]
                });
                break;
            case "email_signin":
                profileDialog.setHeader({
                    type: HTML.DIV,
                    className: "user-profile-dialog-summary",
                    innerHTML: u.lang.signing_in_to_account
                });
                var email = profileDialog.loginNode ? profileDialog.loginNode.value : "";
                profileDialog.loginNode = profileDialog.addItem({ type: HTML.INPUT, label: u.lang.email, value: email });
                profileDialog.passwordNode = profileDialog.addItem({ type: HTML.PASSWORD, label: u.lang.password });
                profileDialog.addItem({
                    type: HTML.DIV,
                    className: "user-profile-forgot-password",
                    innerHTML: u.lang.forgot_password,
                    onclick: function () {
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
                    innerHTML: u.lang.creating_new_account
                });
                profileDialog.loginNode = profileDialog.addItem({ type: HTML.INPUT, label: u.lang.email, value: profileDialog.loginNode.value });
                profileDialog.passwordNode = profileDialog.addItem({ type: HTML.PASSWORD, label: u.lang.password });
                profileDialog.confirmPasswordNode = profileDialog.addItem({ type: HTML.PASSWORD, label: u.lang.confirm_password });
                profileDialog.setPositive({
                    label: u.lang.sign_up,
                    dismiss: false,
                    onclick: function() {
                        if(profileDialog.passwordNode.value !== profileDialog.confirmPasswordNode.value) {
                            profileDialog.errorNode.innerHTML = u.lang.confirming_password_is_not_equals_to_password;
                            profileDialog.errorNode.show();
                            profileDialog.passwordNode.focus();
                            return;
                        }
                        signOtherLogin(function () {
                            console.log("LOGIN GOOGLE", this);
                            firebase.auth().createUserWithEmailAndPassword(profileDialog.loginNode.value, profileDialog.passwordNode.value)
                                .then(function(result) {
                                    result.sendEmailVerification().then(function() {
                                        profileDialog.setFooter(null);
                                        profileDialog.clearItems();
                                        profileDialog.setPositive(null);
                                        profileDialog.setNeutral(null);

                                        accountCreatedDialog = accountCreatedDialog || u.dialog({
                                            title: u.lang.account_has_created,
                                            items: [
                                                {type:HTML.DIV, innerHTML: u.lang.your_account_is_created }
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
                    innerHTML: u.lang.resetting_password
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
                    innerHTML: u.lang.email_has_sent
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
                                // firebase.auth().signInWithRedirect(provider).catch(onAuthStateError);
                                firebase.auth().signInWithPopup(provider).then(onAuthStateChanged).catch(onAuthStateError);
                            });
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
                                // firebase.auth().signInWithRedirect(provider).catch(onAuthStateError);
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
                                // firebase.auth().signInWithRedirect(provider).catch(onAuthStateError);
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
                            .place(HTML.DIV, {
                                innerHTML: user.email ? user.email : "",
                                className: "user-profile-dialog-email"
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
        waitingDialog.open();
        if(main.tracking && main.tracking.getStatus() !== EVENTS.TRACKING_DISABLED) {
            resign = true;
            if(globalSync) globalSync.watchChanges(null);
            main.tracking.stop(function(){
                signProcedureCallback();
            });
            return;
        }
        signProcedureCallback();
    }

    function onAuthStateChanged(result) {
        waitingDialog.close();
        if (result && result.user) {
            try {
                result = result.user ? result.user.toJSON() : result.toJSON();
                u.save("uuid", result.uid);
                result.providerData.forEach(function (profile) {
                    u.save(REQUEST.SIGN_PROVIDER, profile.providerId);
                    main.toast.show(u.lang.signed_as_s_using_s.format(profile.displayName || profile.email, profile.providerId));
                    doGlobalSync();
                });
                initProfileDialog();
            }catch(e) {
                console.error(e);
            }
        } else {
            // console.log("OUT:");
        }
        if(resign) {
            main.tracking.setLink(window.location.href);
            main.tracking.start(function(e){console.log(e)});
            resign = false;
        }
    }

    function onAuthStateError(error) {
        waitingDialog.close();
        doGlobalSync();
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
                        {type:HTML.DIV, enclosed:true, innerHTML: u.lang.popup_blocked_dialog_2 }
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

        doGlobalSync();
        if(resign) {
            main.tracking.setLink(window.location.href);
            main.tracking.start(function(e){console.log(e)});
            resign = false;
        }
    }

    function fetchAccount() {
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

    function doGlobalSync() {
        if(fetchAccount()) {
            main.fire(EVENTS.SYNC_PROFILE);
        }
    }

    return {
        onEvent:onEvent,
        start:start,
        type:type
    }
}


