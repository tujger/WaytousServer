/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 4/20/17.
 */
function ChatHolder(main) {
    this.category = DRAWER.SECTION_EXPLORE;
    this.type = "chat";
    this.title = "Chat";
    this.menu = "Chat";
    this.icon = "chat";
    this.preventState = true;

    var dialogChat;
    var ref;
    var ons = [];
    var bound;
    var maximumMessagesLoad = 1000;
    var database;

    this.start = function() {
        database = firebase.database();
    };

    this.resume = function() {
        dialogChat = dialogChat || u.dialog({
            title: {
                label: "Mixed chat",
                filter: true
            },
            className: "chat-dialog",
            resizeable: true,
            itemsClassName: "chat-dialog-messages",
            items: []
        });

        ref = database.ref();
        if(dialogChat.opened) {
            dialogChat.close();
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
                                dialogChat.add({
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
}


