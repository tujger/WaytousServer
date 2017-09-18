/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/9/17.
 */
EVENTS.NEW_MESSAGE = "new_message";
EVENTS.SEND_MESSAGE = "send_message";
EVENTS.PRIVATE_MESSAGE = "private";
EVENTS.USER_MESSAGE = "user_message";
EVENTS.SHOW_MESSAGES = "show_messages";
EVENTS.WELCOME_MESSAGE = "welcome_message";

function MessageHolder(main) {

    var MESSAGE_MAX_LENGTH = 1024;

    var type = "message";
    var chat;
    var messages;
    var reply;
    var replyTo;
    var replyInput;
    var replyButton;
    var lastReadTimestamp;
    var lastGotTimestamp;
    var drawerItemChat;
    var incomingMessageSounds;
    var incomingMessageSound;
    var defaultIncomingMessageSound = "youve-been-informed.mp3";
    var sound;

    function start() {
        // console.log("MESSAGEHOLDER",main);

        chat = u.dialog({
            title: {
                label: u.lang.chat,
                filter: true
            },
            className: "chat-dialog",
            itemsClassName: "chat-dialog-message",
            tabindex: 3,
            resizeable: true,
            items: [
            ],
            negative: {
                onclick: function(){
                    u.saveForContext("message:chat");
                }
            },
            onopen: function() {
                lastReadTimestamp = new Date().getTime();
                u.saveForContext("message:lastread", lastReadTimestamp);
            },
            footer: {
                type: HTML.DIV,
                className: "chat-dialog-reply hidden"
            }
        }, main.right);

//        messages = chat.items[0];
        reply = chat.footer;
        replyTo = u.create(HTML.INPUT, {type:HTML.HIDDEN, value:""}, reply);
        replyInput = u.create(HTML.INPUT, {
            className: "chat-dialog-reply-input",
            tabindex:5,
            maxlength: MESSAGE_MAX_LENGTH,
            onkeyup:function(e){
                if(e.keyCode == 13) {
                    replyButton.click();
                }
            },
            onclick: function(){
                this.focus();
            }
        }, reply);
        replyButton = u.create(HTML.BUTTON, {className: "chat-dialog-reply-button", innerHTML:"send", onclick:sendUserMessage}, reply);

        incomingMessageSound = u.load("message:incoming") || defaultIncomingMessageSound;
        sound = u.create(HTML.AUDIO, {className:"hidden", preload:"", src:"/sounds/"+incomingMessageSound, last:0, playButLast:function(){
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
                drawerItemChat = object.add(DRAWER.SECTION_COMMUNICATION, type+"_1", u.lang.chat, "chat", function(){
                    if(chat.classList.contains("hidden")) {
                        main.fire(EVENTS.SHOW_MESSAGES);
                    } else {
                        u.saveForContext("message:chat");
                        chat.close();
                    }
                });
                break;
            case EVENTS.TRACKING_ACTIVE:
                if(u.loadForContext("message:chat")) chat.open();
                lastReadTimestamp = u.loadForContext("message:lastread");

                chat.clearItems();
                chat.footer.show(HIDING.SCALE_Y_TOP);
                break;
            case EVENTS.TRACKING_RECONNECTING:
            case EVENTS.TRACKING_DISABLED:
                chat.footer.hide(HIDING.SCALE_Y_TOP);
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user.type == "user" && user != main.me) {
                    object.add(MENU.SECTION_COMMUNICATION, type + "_1", u.lang.private_message, "chat", function () {
                        chat.open();
                        replyTo.value = user.properties.number;
                        replyInput.focus();
                    });
                }
                break;
            case EVENTS.SHOW_MESSAGES:
                u.saveForContext("message:chat", true);
                chat.open();
                chat.focus();
                replyInput.focus();
                main.users.forAllUsers(function(number,user){
                    user.fire(EVENTS.HIDE_BADGE);
                    drawerItemChat && drawerItemChat.hideBadge();
                });
                break;
            case EVENTS.USER_MESSAGE:
                var div = chat.addItem({
                    type:HTML.DIV,
                    className:"chat-dialog-message" + (object.private ? " chat-dialog-message-private" : ""),
                    order: object.timestamp
                });
                u.create(HTML.DIV, {className:"chat-dialog-message-timestamp", innerHTML: new Date(object.timestamp).toLocaleString()}, div);

                var toUser = null;
                if(object.private) {
                    toUser = main.users.users[object.to] || main.me;
                }

                var divName = u.create(HTML.DIV, {
                    className:"chat-dialog-message-name",
                    style: {textShadow: this.properties.color + " 0px 0px 1px"},
                    innerHTML:this.properties.getDisplayName() + (object.private ? " &rarr; " + toUser.properties.getDisplayName() : "") + ":"}, div);
                u.create(HTML.DIV, {className:"chat-dialog-message-body", innerHTML: object.body}, div);

                div.scrollIntoView();

                if(object.timestamp > lastReadTimestamp) {
                    sound.playButLast();

                    u.notification({
                        title: divName.innerHTML,
                        body: object.body,
                        icon: "/images/waytous-transparent-256.png",
                        duration: 5000,
                        onclick: function(e){
                            main.fire(EVENTS.SHOW_MESSAGES);
                        },
                        silent: true
                    });

                    if(chat.classList.contains("hidden")) {
                        this.fire(EVENTS.SHOW_BADGE, EVENTS.INCREASE_BADGE);
                        drawerItemChat && drawerItemChat.increaseBadge();
                    } else {
                        u.saveForContext("message:lastread", object.timestamp);
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    function createView(user){
        return {
            user:user,
            messages:[],
        }
    }

    function sendUserMessage(){
        try {
            var text = replyInput.value;
            if(!text) return;
            if(text.length > MESSAGE_MAX_LENGTH) {
                main.toast.show(u.lang.too_long_message);
                return;
            }
            replyInput.value = "";

            main.tracking.put(USER.MESSAGE, text);
            if(replyTo.value) {
                main.tracking.put(RESPONSE.PRIVATE, parseInt(replyTo.value));
                main.me.fire(EVENTS.USER_MESSAGE, {body: text, timestamp: new Date().getTime(), private: true, to: parseInt(replyTo.value)});
                replyTo.value = "";
            } else {
//                main.me.fire(EVENTS.USER_MESSAGE, {body: text, timestamp: new Date().getTime()});
            }
            main.tracking.put(REQUEST.DELIVERY_CONFIRMATION, true);
            main.tracking.put(USER.MESSAGE, text);
            main.tracking.send(REQUEST.MESSAGE);

        } catch(e) {
            console.error(e);
        }
    }

    function perform(json) {
        var number = json[USER.NUMBER];
        var text = json[USER.MESSAGE];
        var time = json[REQUEST.TIMESTAMP];
        var key = json["key"];
        var privateMessage = json[EVENTS.PRIVATE_MESSAGE] || false;

        main.users.forUser(number, function(number,user){
            user.fire(EVENTS.USER_MESSAGE, {body: text, timestamp: time, key: key, private: privateMessage});
        });
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
                            id:"message:incoming",
                            type: HTML.SELECT,
                            label: u.lang.incoming_message,
                            default: u.load("message:incoming") || defaultIncomingMessageSound,
                            onaccept: function(e, event) {
                                u.save("message:incoming", this.value);
                                sound.src = "/sounds/" + this.value;
                            },
                            onchange: function(e, event) {
                                var sample = u.create(HTML.AUDIO, {className:"hidden", preload:true, src:"/sounds/"+this.value}, main.right);
                                sample.addEventListener("load", function() {
                                    sample.play();
                                }, true);
                                sample.play();
                            },
                            onshow: function(e) {
                                if(incomingMessageSounds) {
                                } else {
                                    u.getJSON("/rest/v1/getSounds").then(function(json){
                                        incomingMessageSounds = {};
                                        u.clear(e);
                                        var selected = 0;
                                        for(var i in json.files) {
                                            var file = json.files[i];
                                            var name = (file.replace(/\..*$/,"").replace(/[\-_]/g," ")).toUpperCaseFirst();
                                            incomingMessageSounds[file] = name;
                                            u.create(HTML.OPTION, {value:file, innerHTML:name}, e);
                                            if((incomingMessageSound || defaultIncomingMessageSound) == file) selected = i;
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
        perform:perform,
        saveable:true,
        loadsaved:-1,
        options:options,
    }
}