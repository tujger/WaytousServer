/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/19/17.
 */

function TrackingFB(main) {

    var link;
    var trackingListener;
    var json;
    var newTracking;
    var token;
    var status;
    var serverUri;
    var ref;
    var updateTask;
    var refs = [];
    var updateFocusTask;

    function start() {
        status = EVENTS.TRACKING_DISABLED;
        var uri;
        if(this.link) {
            uri = new URL(this.link);
        } else {
            newTracking = true;
            uri = new URL(window.location.href);
        }

        var path = uri.pathname.replace("/group/","/track/");
        serverUri = "wss://" + uri.hostname + ":"+ data.WSS_FB_PORT + "/v1" + path;
//         serverUri = "ws://" + uri.hostname + ":" + data.WS_FB_PORT + "/v1" + path;

        if(newTracking) {
            setStatus(EVENTS.TRACKING_CONNECTING);
            trackingListener.onCreating();
        } else {
            setStatus(EVENTS.TRACKING_RECONNECTING);
            trackingListener.onJoining()
        }
        webSocketListener = WebSocketListener(serverUri);

    }

    function stop(){
        status = EVENTS.TRACKING_DISABLED;

        var updates = {};
        clearInterval(updateTask);
        updates[DATABASE.USER_ACTIVE] = false;
        updates[DATABASE.USER_CHANGED] = firebase.database.ServerValue.TIMESTAMP;

//console.log("UPDATE",DATABASE.SECTION_USERS_DATA + "/" + main.me.number,updates);
        for(var i in refs) {
            ref.database.ref().child(refs[i]).off();
        }

        if(ref) {
            ref.child(DATABASE.SECTION_USERS_DATA).child(main.me.number).update(updates)
                .then(function() {
                    ref.database.goOffline();
                })
                .catch(function (error) {
                    console.error(error);
                    ref.database.goOffline();
                });
        }
        //firebase.auth().signOut();
        window.removeEventListener("focus", updateActive);
        document.removeEventListener("visibilitychange", updateActive);
        trackingListener.onStop();

        var uri = new URL(serverUri);
        window.location.href = "/group/";
//        window.location.href = "https://" + uri.hostname + (data.HTTPS_PORT == 443 ? "" : ":"+ data.HTTPS_PORT) + "/track/";
    }

    function WebSocketListener(link, reconnect) {

        var sendOriginal = send;
        var onopen =  function(event) {
            opened = true;
            if(newTracking) {
                put(REQUEST.REQUEST, REQUEST.NEW_GROUP);
                put(REQUEST.DEVICE_ID, utils.getUuid());
            } else if(reconnect) {
                var parts = link.split("/");
                var groupId = parts[parts.length-1];
                setToken(groupId);

                put(REQUEST.REQUEST, REQUEST.JOIN_GROUP);
                put(REQUEST.TOKEN, groupId);
            } else {
                var parts = link.split("/");
                var groupId = parts[parts.length-1];
                setToken(groupId);

                put(REQUEST.REQUEST, REQUEST.JOIN_GROUP);
                put(REQUEST.TOKEN, groupId);
                put(REQUEST.DEVICE_ID, utils.getUuid());
            }
            put(REQUEST.MODEL, navigator.appCodeName );
            put(REQUEST.MANUFACTURER, navigator.appCodeName);
            put(REQUEST.OS, navigator.platform);
            // put("aaa", navigator.appVersion);
            var name = u.load(USER.NAME);
            if(name) put(USER.NAME, name);

            send();
        };

        var onmessage = function(event) {
            var o = JSON.parse(event.data);
            if(!o[RESPONSE.STATUS]) return;
            switch (o[RESPONSE.STATUS]) {
                case RESPONSE.STATUS_CHECK:
                    if(RESPONSE.CONTROL) {
                        var control = o[RESPONSE.CONTROL];
                        var deviceId = utils.getUuid();
                        var hash = utils.getEncryptedHash(control +":"+ deviceId);
                        put(REQUEST.REQUEST, REQUEST.CHECK_USER);
                        put(REQUEST.HASH, hash);
                        send();
                    }
                    break;
                case RESPONSE.STATUS_ACCEPTED:
                    var groupCreated = newTracking;
                    newTracking = false;
                    send = sendOriginal;
                    if(o[RESPONSE.SIGN]) {
                        var authToken = o[RESPONSE.SIGN];
                        delete o[RESPONSE.SIGN];

                        try {
                            firebase.auth().signInWithCustomToken(authToken).then(function (e) {

                                // setStatus(EVENTS.TRACKING_ACTIVE);
                                if (o[RESPONSE.TOKEN]) {
                                    setToken(o[RESPONSE.TOKEN]);
                                    if(!serverUri.match(o[RESPONSE.TOKEN])) {
                                        serverUri = link + "/" + o[RESPONSE.TOKEN];
                                    }
                                }
                                if (o[RESPONSE.NUMBER] != undefined) {
                                    console.warn("Joined with number",o[RESPONSE.NUMBER]);
                                    main.users.setMyNumber(o[RESPONSE.NUMBER]);
                                }
                                o[RESPONSE.INITIAL] = true;

                                ref = database.ref().child(getToken());

                                if(main.me && main.me.number != undefined) {
                                    ref.child(DATABASE.SECTION_USERS_DATA).child(main.me.number).child(DATABASE.USER_ACTIVE).set(true);
                                }

                                updateTask = setInterval(updateActive, 60000);
                                window.addEventListener("focus", updateActive);
                                document.addEventListener("visibilitychange", updateActive);

                                registerValueListener(ref.child(DATABASE.SECTION_OPTIONS).child(DATABASE.OPTION_DATE_CREATED), groupListener, groupErrorListener);
                                registerValueListener(ref.child(DATABASE.SECTION_USERS_DATA).child(main.me.number).child(DATABASE.USER_ACTIVE), userActiveListener);
                                registerChildListener(ref.child(DATABASE.SECTION_USERS_DATA), usersDataListener, -1);
                                main.eventBus.chain(function(holder){
                                    if(holder.saveable) {
                                        registerChildListener(ref.child(DATABASE.SECTION_PRIVATE).child(holder.type).child(main.me.number), userPrivateDataListener, -1);
                                    }
                                });
                                try {
                                    trackingListener.onAccept(o);
                                } catch (e) {
                                    console.error(e.message);
                                }
                            }).catch(function (error) {
                                setStatus(EVENTS.TRACKING_DISABLED);
                                trackingListener.onReject(error.message);
                            });
                        } catch(e) {
                            console.error(e);
                            debugger;
                            main.initialize();
                        }
                    } else {
                        setStatus(EVENTS.TRACKING_DISABLED);
                        console.log("REJECTED");
                        trackingListener.onReject("Old version of server");
                    }
                    break;
                case RESPONSE.STATUS_ERROR:
                    setStatus(EVENTS.TRACKING_DISABLED);
                    trackingListener.onReject(o[RESPONSE.MESSAGE] ? o[RESPONSE.MESSAGE] : "");
                    break;
                default:
                    trackingListener.onMessage(o);
                    break;
            }
        };

        var onclose = function(event) {
//            console.log("CLOSE",opened,event.code,event.reason,event.wasClean);
            if(!opened) {
                console.error("Websocket processing closed unexpectedly, will try to use XHR instead of " + link + " (error " + event.code + (event.reason?": "+event.reason:")")+".");
                xhrModeStart(link);
            }
        };

        var onerror = function(event) {
                console.error("Websocket processing failed, will try to use XHR instead of " + link + ".");
            if(status == EVENTS.TRACKING_DISABLED) return;
            xhrModeStart(link);
        };

        var xhrModeStart = function(link) {
            var uri = new URL(link);
            link = "/rest/v1/join"/* + uri.pathname*/;
//            link = "https://" + uri.hostname + (data.HTTPS_PORT == 443 ? "" : ":" + data.HTTPS_PORT) + "/rest/v1/join"/* + uri.pathname*/;

            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() { //
                if (xhr.readyState != 4) return;
                xhrModeCheck(link,xhr.response);
            };
            send = function(jsonMessage){
                if(!jsonMessage) {
                    send(json);
                    json = {};
                    return;
                }
                put(REQUEST.TIMESTAMP, new Date().getTime());
                xhr.send(JSON.stringify(json));
                json = {};
            };
            xhr.open("POST", link, true);
            onopen();
        };

        var xhrModeCheck = function(link, check) {
            // var check = JSON.parse(check);

            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() { //
                if (xhr.readyState != 4) return;
                onmessage({data:xhr.response});
            };
            send = function(jsonMessage){
                if(!jsonMessage) {
                    send(json);
                    json = {};
                    return;
                }
                put(REQUEST.TIMESTAMP, new Date().getTime());
                try {
                    xhr.send(JSON.stringify(json));
                } catch(e) {
                    console.error(e,xhr);
                }
                json = {};
            };
            xhr.open("POST", link, true);
            onmessage({data:check});
        };

        var webSocket = {};
        try {
            link = link.replace(/#.*/,"");
            webSocket = new WebSocket(link);

            webSocket.onopen = onopen;
            webSocket.onmessage = onmessage;
            webSocket.onclose = onclose;
            webSocket.onerror = onerror;

            setTimeout(function(){
                if(webSocket instanceof WebSocket && webSocket.readyState != WebSocket.OPEN) {
                    webSocket.close();
                }
            }, data.isStandAlone ? 15000 : 100);
        } catch(e){
            console.warn(link,e);
            xhrModeStart(link);
        }
        var opened = false;

        return webSocket;
    }


    function put(name, value){
        if(!json) json = {};
        json[name] = value;
    }

    function send(jsonMessage) {
        var updates;

        if(!jsonMessage) {
//            put(REQUEST.TIMESTAMP, new Date().getTime());
            send(json);
            json = {};
            return;
        }

        if(jsonMessage.constructor === String) {
            put(REQUEST.REQUEST, jsonMessage);
            send();
            return;
        }

        jsonMessage[REQUEST.TIMESTAMP] = new Date().getTime();
        var type = jsonMessage[REQUEST.REQUEST];
        if(type == REQUEST.NEW_GROUP || type == REQUEST.JOIN_GROUP || type == REQUEST.CHECK_USER) {
            // console.error("WRONG WAY");
            // switch (webSocketListener.status) {
            webSocketListener.send(JSON.stringify(json));
            //
            // }
        } else if(ref) {
            if(type == REQUEST.CHANGE_NAME) {
                updates = {};
                updates[USER.NAME] = jsonMessage[USER.NAME];
                updates[DATABASE.USER_CHANGED] = firebase.database.ServerValue.TIMESTAMP;

//console.log("UPDATE1",DATABASE.SECTION_USERS_DATA + "/" + main.me.number,updates);
                ref.child(DATABASE.SECTION_USERS_DATA).child(main.me.number).update(updates).catch(function(error) {
                    console.error(error);
                });

                return;
            } else if(type == REQUEST.WELCOME_MESSAGE) {
                console.error("WELCOMEMESSAGE");

                return;
            }

            var holder = main.eventBus.holders[type];
            if(!holder || !holder.saveable) return;

            delete jsonMessage[REQUEST.REQUEST];
            delete jsonMessage[REQUEST.PUSH];
            delete jsonMessage[REQUEST.DELIVERY_CONFIRMATION];

            var path,refPath;
            if(jsonMessage.to) {
                var to = jsonMessage.to;
                delete jsonMessage.to;
                jsonMessage.from = main.me.number;
                path = DATABASE.SECTION_PRIVATE + "/" + type + "/" + to;
            } else {
                path = DATABASE.SECTION_PUBLIC + "/" + type + "/" + main.me.number;
            }
            var key = ref.push().key;

            updates = {};
            updates[path + "/" + key] = jsonMessage;
            updates[DATABASE.SECTION_USERS_DATA + "/" + main.me.number + "/" + DATABASE.USER_CHANGED] = firebase.database.ServerValue.TIMESTAMP;

//console.log("UPDATE2",updates);
            ref.update(updates).catch(function(error) {
               console.error(error);
           });

        }
    }

    function sendMessage(type, jsonMessage) {
        json = json || {};
        for(var x in jsonMessage) {
            json[x] = jsonMessage[x];
        }
        json[REQUEST.REQUEST] = type;
        sendUpdate();
    }

    function sendUpdate() {
        json = json || {};
        if(!json[REQUEST.REQUEST]) json[REQUEST.REQUEST] = REQUEST.UPDATE;
        send();
    }

    function setLink(link) {
        this.link = link;
    }

    function setTrackingListener(callback) {
        trackingListener = callback;
    }

    function setToken(id){
        token = id;
    }

    function getToken(){
        return token;
    }

    function setStatus(currentStatus){
        status = currentStatus;
    }

    function getStatus(){
        return status;
    }

    function getTrackingUri(){
        var uri = window.location.href;
        uri = uri.replace("/group/", "/track/");
        return uri;
//        var uri = new URL(serverUri);
//        return "http://" + uri.hostname + (data.HTTP_PORT == 80 ? "" : ":"+data.HTTP_PORT) + "/track/" + token;
    }

    function registerChildListener(ref, listener, limit) {
        if(limit > 0){
            ref.limitToLast(limit).on("child_added", listener);
        } else {
            ref.on("child_added", listener);
        }
        refs.push(ref.path.toString());
    }

    function registerValueListener(ref, listener, errorListener) {
        ref.on("value", listener, errorListener);
        refs.push(ref.path.toString());
    }

    function userActiveListener(data) {
        if(data.val() == undefined) {
            stop();
        } else if(!data.val()) {
            switch(webSocketListener.readyState) {
            case WebSocket.CLOSED:
                setStatus(EVENTS.TRACKING_RECONNECTING);
                trackingListener.onReconnecting();
                try {
                    webSocketListener.close();
                } catch(e) {
                    console.warn(e);
                }
                webSocketListener = WebSocketListener(serverUri, true);
                break;
            }
        }
    }

    function groupListener(data, data2) {
    }

    function groupErrorListener(error) {
        switch (error.code) {
            case "PERMISSION_DENIED":
                setStatus(EVENTS.TRACKING_DISABLED);
                var reason = u.lang.group_has_been_removed;
                trackingListener.onReject(reason);
                break
        }
    }

    function usersDataListener(data){
//        if(main.me.number != parseInt(data.key)) {
            try{
                var o = data.val();
                o[RESPONSE.NUMBER] = parseInt(data.key);
                o[RESPONSE.INITIAL] = true;
                delete o.active;
                var user = main.users.addUser(o);

                user.type = "user";

                //registers
                registerValueListener(ref.child(DATABASE.SECTION_USERS_DATA).child(user.number).child(DATABASE.USER_NAME), usersDataNameListener);
                registerValueListener(ref.child(DATABASE.SECTION_USERS_DATA).child(user.number).child(DATABASE.USER_ACTIVE), usersDataActiveListener);
                registerValueListener(ref.child(DATABASE.SECTION_USERS_DATA).child(user.number).child(DATABASE.USER_CHANGED), usersDataChangedListener);

                //usersDataNameListener(data.child(DATABASE.USER_NAME));
                //usersDataActiveListener(data.child(DATABASE.USER_ACTIVE));
                //usersDataChangedListener(data.child(DATABASE.USER_CHANGED));

                main.eventBus.chain(function(holder){
                    if(holder.saveable) {
                        var loadSaved = holder.loadsaved || 1;
                        registerChildListener(ref.child(DATABASE.SECTION_PUBLIC).child(holder.type).child(user.number), userPublicDataListener, loadSaved);
                    }
                });

                trackingListener.onAccept(o);
            } catch(e) {
                console.error(e.message);
            }
//        }
        // console.log(data);
    }

    function userPublicDataListener(data) {
        try {
            var o = data.val();
            o[RESPONSE.NUMBER] = parseInt(data.ref.parent.key);
            o[RESPONSE.STATUS] = data.ref.parent.parent.key;
            o["key"] = data.key;

            trackingListener.onMessage(o);
        } catch(e) {
            console.error(e.message);
        }
    }

    function userPrivateDataListener(data) {
        try{
            var o = data.val();
            var from = parseInt(o["from"]);
            delete o["from"];

            o[RESPONSE.NUMBER] = from;
            o[RESPONSE.STATUS] = data.ref.parent.parent.key;
            o["key"] = data.key;
            o[EVENTS.PRIVATE_MESSAGE] = true;

            trackingListener.onMessage(o);
            // data.ref.remove();

        } catch(e) {
            console.error(e.message);
        }
    }

    function usersDataNameListener(data) {
        try {
            var number = parseInt(data.ref.parent.key);
            var name = data.val();
            main.users.forUser(number, function(number, user, name){
                if(user.number != main.me.number && user.properties && name != user.properties.name) {
                    user.fire(EVENTS.CHANGE_NAME, name);
                }
            }, name);
        } catch(e) {
            console.error(e.message);
        }
    }

    function usersDataActiveListener(data) {
        try {
            var number = parseInt(data.ref.parent.key);
            var active = data.val();
            var user = main.users.users[number];
            if(user && user.properties && active != user.properties.active) {
                var o = {};
                o[RESPONSE.STATUS] = RESPONSE.STATUS_UPDATED;
                o[RESPONSE.NUMBER] = number;
                o[active ? USER.JOINED : USER.DISMISSED] = number;
                trackingListener.onMessage(o);
            }
        } catch(e) {
            console.error(e.message);
        }
    }

    function usersDataChangedListener(data) {
        try {
            var number = parseInt(data.ref.parent.key);
            //var user = main.users.users[number];
            //if(user && user.properties && active != user.properties.active) {
                var o = {};
                o[RESPONSE.STATUS] = RESPONSE.STATUS_UPDATED;
                o[RESPONSE.NUMBER] = number;
                o[REQUEST.TIMESTAMP] = data.val();
                trackingListener.onMessage(o);
            //}
        } catch(e) {
            console.error(e.message);
        }
    }

    function updateActive() {
        try {
            if(main.me && main.me.number != undefined) {
                ref.child(DATABASE.SECTION_USERS_DATA).child(main.me.number).child(DATABASE.USER_ACTIVE).set(true);
                ref.child(DATABASE.SECTION_USERS_DATA).child(main.me.number).child(DATABASE.USER_CHANGED).set(firebase.database.ServerValue.TIMESTAMP);
            }
        } catch(e) {
            console.error(e.message);
        }
    }

    return {
        start: start,
        stop:stop,
        title: "Group",
        menu: true,
        setLink:setLink,
        setTrackingListener:setTrackingListener,
        getTrackingUri:getTrackingUri,
        getStatus:getStatus,
        setStatus:setStatus,
        getToken:getToken,
        sendMessage:sendMessage,
        put:put,
        sendUpdate:sendUpdate,
        send:send,
    }
}