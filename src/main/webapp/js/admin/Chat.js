/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/20/17.
 */
function Chat() {

    var title = "Chat";
    var dialogChat;
    var ref;
    var ons = [];
    var bound;
    var maximumMessagesLoad = 1000;

    function start() {
        dialogChat = dialogChat || u.dialog({
            title: {
                label: "Mixed chat",
                filter: true
            },
            className: "chat-dialog",
            resizeable: true,
            itemsClassName: "chat-dialog-messages",
            items: [],
//            negative: {
//                label: "Close"
//            }
        });

        ref = database.ref();
        if(dialogChat.opened) {
            dialogChat.close();

//            for(var i in ons) {
//                ons[i].off();
//            }
//            ons = [];

//            u.clear(dialogChat.itemsLayout);
//            dialogChat.items = [];
        } else {
            dialogChat.open();

            if(!bound) {
                var on = ref.child(DATABASE.SECTION_GROUPS);
                ons.push(on);
                on.on("child_added", function(group) {
                    var groupId = group.key;
                    var on = ref.child(groupId).child(DATABASE.PUBLIC).child("message");
                    ons.push(on);
                    on.on("child_added", function(user) {
                        var userNumber = user.key;

                        ref.child(groupId).child(DATABASE.PUBLIC).child("message").child(userNumber);

                        ref.child(groupId).child(DATABASE.USERS).child(DATABASE.PUBLIC).child(userNumber).once("value").then(function(snapshot){
                            var userName = snapshot.val() ? snapshot.val().name : userNumber;

                            var on = ref.child(groupId).child(DATABASE.PUBLIC).child("message").child(userNumber);
                            ons.push(on);
                            on.limitToLast(maximumMessagesLoad).on("child_added", function(message) {
                                if(dialogChat.items.length > maximumMessagesLoad) {
                                    dialogChat.itemsLayout.removeChild(dialogChat.itemsLayout.firstChild);
                                    dialogChat.items.shift();
                                }

                                var post = message.val();
                                dialogChat.addItem({
                                    type: HTML.DIV,
                                    className:"chat-dialog-message",
                                    order: post[REQUEST.TIMESTAMP],
                                    content: u.create(HTML.DIV, {className:"chat-dialog-message-content"}).place(HTML.DIV, {
                                        className:"chat-dialog-message-group",
                                        content: u.create(HTML.DIV, {innerHTML: groupId, onclick: function(){
                                            WTU.switchTo("/admin/group/"+groupId);
                                        }})
                                    }).place(HTML.DIV, {
                                        className:"chat-dialog-message-name",
                                        content: u.create(HTML.DIV, {innerHTML: userName ? userName + " (#"+userNumber+")" : "#"+userNumber, onclick: function(){
                                            WTU.switchTo("/admin/user/"+groupId+"/"+userNumber)
                                        }})
                                    }).place(HTML.DIV, {
                                        className:"chat-dialog-message-timestamp",
                                        innerHTML: new Date(post[REQUEST.TIMESTAMP]).toLocaleString()
                                    }).place(HTML.DIV, {
                                        className:"chat-dialog-message-body",
                                        innerHTML: post[USER.MESSAGE]
                                    })
                                }).scrollIntoView();
                            });
                        });

                    });
                });
                bound = true;
            }
        }
    }

    return {
        start: start,
        page: "chat",
        icon: "chat",
        title: title,
        menu: title
    }
}


