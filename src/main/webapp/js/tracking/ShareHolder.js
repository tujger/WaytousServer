/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 3/10/17.
 */
EVENTS.SHARE_LINK = "share_link";

function ShareHolder(main) {

    var type = "social";

    var shareBlockedDialog;
    var shareDialog;

    function start() {

        window.fbAsyncInit = function() {
            FB.init({
              appId      : '1226806074062997',
              xfbml      : false,
              version    : 'v2.9'
            });
//            FB.AppEvents.logPageView();
            /*FB.ui({
              method: 'share_open_graph',
              action_type: 'og.likes',
              action_properties: JSON.stringify({
                object:'https://developers.facebook.com/docs/',
              })
            }, function(response){
              // Debug response (optional)
              console.log(response);
            });*/
        };

// facebook
        (function(d, s, id){
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)) {return;}
            js = d.createElement(s); js.id = id;
            js.src = "//connect.facebook.net/en_US/sdk.js";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'facebook-tejssdk'));

// twitter
        !function(d,s,id){
            var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';
            if(!d.getElementById(id)){
                js=d.createElement(s);
                js.id=id;
                js.src=p+'://platform.twitter.com/widgets.js';
                fjs.parentNode.insertBefore(js,fjs);
            }
        }(document, 'script', 'twitter-wjs');

        shareBlockedDialog = u.dialog({
           items: [
               {type:HTML.DIV, innerHTML: u.lang.popup_blocked_dialog_1 },
               {type:HTML.DIV, enclosed:true, innerHTML: u.lang.popup_blocked_dialog_2 },
               {type:HTML.DIV, innerHTML: u.lang.popup_blocked_dialog_3 },
        //                   {type:HTML.DIV, innerHTML: main.tracking.getTrackingUri()}
           ],
           positive: {
               label: u.lang.close
           }
        }, main.right);
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.SHARE_LINK:
                object = object || main.tracking.getTrackingUri();
                if(shareDialog) shareDialog.close();
                shareDialog = shareDialog || u.dialog({
                    className: "share-dialog",
                    header: {type:HTML.DIV, className: "share-dialog-item-message", innerHTML: u.lang.share_link_dialog },
                    items: [
                        {type:HTML.INPUT, className: "dialog-item-input-link", readOnly:true }
                    ],
                    neutral: {
                        label: u.lang.copy,
                        dismiss: false,
                        onclick: function(items) {
                            if(u.copyToClipboard(itemLink)) {
                                main.toast.show(u.lang.link_was_copied_into_clipboard, 3000);
                            }
                            shareDialog.close();
                        }
                    },
                    negative: {
                        label: u.lang.cancel
                    },
                    timeout: 20000
                }, main.right);
                itemLink = shareDialog.items[0];

                if(shareDialog && shareDialog.items && !shareDialog.itemFacebook) {
                    shareDialog.itemFacebook = shareDialog.addItem({
                        type: HTML.DIV,
                        className: "share-dialog-item"
                    });
                    u.create(HTML.BUTTON, {
                        className: "dialog-button dialog-item-button",
                        onclick: function() {
                            shareDialog.close();
                            FB.ui({
                                method: "share",
                                display: "popup",
                                href: itemLink.value,
                                caption: "Follow me at ${WEB_PAGE}",
                            }, function(response){
                                console.log("A",response);
                            });
                         }
                    }, shareDialog.itemFacebook).place(HTML.DIV, {
                        className: "icon notranslate dialog-item-icon",
                        content: u.create(HTML.IMG, {src:"/images/facebook.svg"})
                    }).place(HTML.DIV, {
                        innerHTML: u.lang.share_to_facebook
                    });
                    shareDialog.itemsLayout.insertBefore(shareDialog.itemFacebook, shareDialog.itemFacebook.previousSibling);
                }
                if(shareDialog && shareDialog.items && !shareDialog.itemTwitter) {
                    shareDialog.itemTwitter = shareDialog.addItem({
                        type: HTML.DIV,
                        className: "share-dialog-item"
                    });
                    u.create(HTML.BUTTON, {
                        className: "dialog-button dialog-item-button",
                        onclick: function() {
                            shareDialog.close();
                            var popup = window.open("https://twitter.com/intent/tweet?text=Way%20to%20us&url="+encodeURIComponent(itemLink.value),"_blank");
                            utils.popupBlockerChecker.check(popup, function() {
                                shareBlockedDialog.open();
                            });
                         }
                    }, shareDialog.itemTwitter).place(HTML.DIV, {
                        className: "icon notranslate dialog-item-icon",
                        content: u.create(HTML.IMG, {src:"/images/twitter.svg"})
                    }).place(HTML.DIV, {
                        innerHTML: u.lang.share_to_twitter
                    });
                    shareDialog.itemsLayout.insertBefore(shareDialog.itemTwitter, shareDialog.itemTwitter.previousSibling);
                }
                if(shareDialog && shareDialog.items && !shareDialog.itemWhatsapp) {
                    shareDialog.itemWhatsapp = shareDialog.addItem({
                        type: HTML.DIV,
                        className: "share-dialog-item desktop-hidden"
                    });
                    u.create(HTML.BUTTON, {
                        className: "dialog-button dialog-item-button",
                        onclick: function() {
                            shareDialog.close();
                            var popup = window.open("whatsapp://send?text=Way%20to%20us&body="+encodeURIComponent(itemLink.value),"_blank");
                            utils.popupBlockerChecker.check(popup, function() {
                                shareBlockedDialog.open();
                            });
                         }
                    }, shareDialog.itemWhatsapp).place(HTML.DIV, {
                        className: "icon notranslate dialog-item-icon",
                        content: u.create(HTML.IMG, {src:"/images/whatsapp.svg"})
                    }).place(HTML.DIV, {
                        innerHTML: u.lang.share_to_whatsapp
                    });
                    shareDialog.itemsLayout.insertBefore(shareDialog.itemWhatsapp, shareDialog.itemWhatsapp.previousSibling);
                }
                if(shareDialog && shareDialog.items && !shareDialog.itemTelegram) {
                    shareDialog.itemTelegram = shareDialog.addItem({
                        type: HTML.DIV,
                        className: "share-dialog-item"
                    });
                    u.create(HTML.BUTTON, {
                        className: "dialog-button dialog-item-button",
                        onclick: function() {
                            shareDialog.close();
                            var popup = window.open("https://t.me/share/url?url="+encodeURIComponent(itemLink.value),"_blank");
                            utils.popupBlockerChecker.check(popup, function() {
                                shareBlockedDialog.open();
                            });
                         }
                    }, shareDialog.itemTelegram).place(HTML.DIV, {
                        className: "icon notranslate dialog-item-icon",
                        content: u.create(HTML.IMG, {src:"/images/telegram.svg"})
                    }).place(HTML.DIV, {
                        innerHTML: u.lang.share_to_telegram
                    });
                    shareDialog.itemsLayout.insertBefore(shareDialog.itemTelegram, shareDialog.itemTelegram.previousSibling);
                }
                if(shareDialog && shareDialog.items && !shareDialog.itemShare) {
                    shareDialog.itemShare = shareDialog.addItem({
                        type: HTML.DIV,
                        className: "share-dialog-item"
                    });
                    u.create(HTML.BUTTON, {
                        className: "dialog-button dialog-item-button",
                        onclick: function() {
                            shareDialog.close();
                            var popup = window.open("mailto:?subject=Way%20to%20us&body="+encodeURIComponent(itemLink.value),"_blank");
                            utils.popupBlockerChecker.check(popup, function() {
                                shareBlockedDialog = shareBlockedDialog || u.dialog({
                                    items: [
                                        {type:HTML.DIV, innerHTML: u.lang.popup_blocked_dialog_1 },
                                        {type:HTML.DIV, enclosed:true, innerHTML: u.lang.popup_blocked_dialog_2 },
                                        {type:HTML.DIV, innerHTML: u.lang.popup_blocked_dialog_3 },
                                        {type:HTML.DIV, innerHTML: "..."}
                                    ],
                                    positive: {
                                        label: u.lang.close
                                    }
                                }, main.right);
                                shareBlockedDialog.items[3].innerHTML = itemLink.value;
                                shareBlockedDialog.open();
                            });
                         }
                    }, shareDialog.itemShare).place(HTML.DIV, {
                        className: "icon notranslate dialog-item-icon",
                        innerHTML: "share"
                    }).place(HTML.DIV, {
                        innerHTML: u.lang.share_by_mail
                    });
                    shareDialog.itemsLayout.insertBefore(shareDialog.itemShare, shareDialog.itemShare.previousSibling);
                }
                itemLink.value = object;
                shareDialog.open();
                break;
            default:
                break;
        }
        return true;
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent
    }
}