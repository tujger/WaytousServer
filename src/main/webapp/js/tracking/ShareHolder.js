/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 3/10/17.
 */
EVENTS.SHARE_LINK = "share_link";

function ShareHolder(main) {

    var type = "social";

    var shareBlockedDialog;
    var shareDialog;

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
    var whatsapp_svg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"0 0 90 90",
        version:"1.1",
        className: "menu-item",

    };
    var whatsapp_path = {
        xmlns:"http://www.w3.org/2000/svg",
        fill: "white",
        d: "M90,43.841c0,24.213-19.779,43.841-44.182,43.841c-7.747,0-15.025-1.98-21.357-5.455L0,90l7.975-23.522 c-4.023-6.606-6.34-14.354-6.34-22.637C1.635,19.628,21.416,0,45.818,0C70.223,0,90,19.628,90,43.841z M45.818,6.982 c-20.484,0-37.146,16.535-37.146,36.859c0,8.065,2.629,15.534,7.076,21.61L11.107,79.14l14.275-4.537 c5.865,3.851,12.891,6.097,20.437,6.097c20.481,0,37.146-16.533,37.146-36.857S66.301,6.982,45.818,6.982z M68.129,53.938 c-0.273-0.447-0.994-0.717-2.076-1.254c-1.084-0.537-6.41-3.138-7.4-3.495c-0.993-0.358-1.717-0.538-2.438,0.537 c-0.721,1.076-2.797,3.495-3.43,4.212c-0.632,0.719-1.263,0.809-2.347,0.271c-1.082-0.537-4.571-1.673-8.708-5.333 c-3.219-2.848-5.393-6.364-6.025-7.441c-0.631-1.075-0.066-1.656,0.475-2.191c0.488-0.482,1.084-1.255,1.625-1.882 c0.543-0.628,0.723-1.075,1.082-1.793c0.363-0.717,0.182-1.344-0.09-1.883c-0.27-0.537-2.438-5.825-3.34-7.977 c-0.902-2.15-1.803-1.792-2.436-1.792c-0.631,0-1.354-0.09-2.076-0.09c-0.722,0-1.896,0.269-2.889,1.344 c-0.992,1.076-3.789,3.676-3.789,8.963c0,5.288,3.879,10.397,4.422,11.113c0.541,0.716,7.49,11.92,18.5,16.223 C58.2,65.771,58.2,64.336,60.186,64.156c1.984-0.179,6.406-2.599,7.312-5.107C68.398,56.537,68.398,54.386,68.129,53.938z"
    };
    var telegram_svg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"0 0 190 190",
        version:"1.1",
        className: "menu-item",

    };
    var telegram_path = {
        xmlns:"http://www.w3.org/2000/svg",
        fill: "white",
        d: "M152.531,179.476c-1.48,0-2.95-0.438-4.211-1.293l-47.641-32.316l-25.552,18.386c-2.004,1.441-4.587,1.804-6.914,0.972 c-2.324-0.834-4.089-2.759-4.719-5.146l-12.83-48.622L4.821,93.928c-2.886-1.104-4.8-3.865-4.821-6.955 c-0.021-3.09,1.855-5.877,4.727-7.02l174.312-69.36c0.791-0.336,1.628-0.53,2.472-0.582c0.302-0.018,0.605-0.018,0.906-0.001 c1.748,0.104,3.465,0.816,4.805,2.13c0.139,0.136,0.271,0.275,0.396,0.42c1.11,1.268,1.72,2.814,1.835,4.389 c0.028,0.396,0.026,0.797-0.009,1.198c-0.024,0.286-0.065,0.571-0.123,0.854L159.898,173.38c-0.473,2.48-2.161,4.556-4.493,5.523 C154.48,179.287,153.503,179.476,152.531,179.476z M104.862,130.579l42.437,28.785L170.193,39.24l-82.687,79.566l17.156,11.638 C104.731,130.487,104.797,130.533,104.862,130.579z M69.535,124.178l5.682,21.53l12.242-8.809l-16.03-10.874 C70.684,125.521,70.046,124.893,69.535,124.178z M28.136,86.782l31.478,12.035c2.255,0.862,3.957,2.758,4.573,5.092l3.992,15.129 c0.183-1.745,0.974-3.387,2.259-4.624L149.227,38.6L28.136,86.782z"
    };

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
                    items: [
                        {type:HTML.DIV, className: "share-dialog-item-message", innerHTML: u.lang.share_link_dialog },
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
                itemLink = shareDialog.items[1];

                if(shareDialog && shareDialog.items && !shareDialog.itemFacebook) {
                    shareDialog.itemFacebook = shareDialog.addItem({
                        type: HTML.DIV,
                        className: "share-dialog-item"
                    });
                    var icon  = u.create(HTML.PATH, facebook_path, u.create(HTML.SVG, facebook_svg)).parentNode;
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
                        className: "dialog-item-icon",
                        content: icon
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
                    var icon = u.create(HTML.PATH, twitter_path, u.create(HTML.SVG, twitter_svg)).parentNode;
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
                        className: "dialog-item-icon",
                        content: icon
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
                    var icon = u.create(HTML.PATH, whatsapp_path, u.create(HTML.SVG, whatsapp_svg)).parentNode;
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
                        className: "dialog-item-icon",
                        content: icon
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
                    var icon = u.create(HTML.PATH, telegram_path, u.create(HTML.SVG, telegram_svg)).parentNode;
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
                        className: "dialog-item-icon",
                        content: icon
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
                        className: "dialog-item-icon",
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
        onEvent:onEvent,
    }
}