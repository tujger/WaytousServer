/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 1/19/17.
 */

function TrackingFB(main) {

    var trackingListener;
    var json;
    var newTracking;
    var token;
    var status;
    var serverUri;
    var refRoot;
    var refGroup;
    var refAccounts;
    var refStat;
    var updateTask;
    var webSocketListener;

    var refs = [];

    function start() {
        status = EVENTS.TRACKING_DISABLED;
        var uri;
        if(this.link) {
            uri = new URL(this.link);
        } else {
            newTracking = true;
            uri = new URL(window.location.href);
        }

        var path = uri.path.replace("/group/","/track/");
        serverUri = "wss://" + uri.hostname + ":"+ window.data.WSS_FB_PORT + "/v1" + path;
//         serverUri = "ws://" + uri.hostname + ":" + window.data.WS_FB_PORT + "/v1" + path;

        if(newTracking) {
            setStatus(EVENTS.TRACKING_CONNECTING);
            trackingListener.onCreating();
        } else {
            setStatus(EVENTS.TRACKING_RECONNECTING);
            trackingListener.onJoining()
        }
        webSocketListener = WebSocketListener(serverUri);

    }

    function stop(callback){
        status = EVENTS.TRACKING_DISABLED;

        var updates = {};
        clearInterval(updateTask);
        window.removeEventListener("focus", updateActive);
        document.removeEventListener("visibilitychange", updateActive);

        updates[DATABASE.ACTIVE] = false;
        updates[DATABASE.CHANGED] = firebase.database.ServerValue.TIMESTAMP;

        for(var i in refs) {
            try {
                refs[i].off();
            } catch(e) {
                console.log("OFF",refs[i].toString(),e);
            }
        }
        refs = [];

        if(refRoot) {
            refGroup.child(DATABASE.USERS).child(DATABASE.PUBLIC).child(main.me.number).update(updates).then(function () {
                refRoot.database.goOffline();

                trackingListener.onStop();

                if (callback) {
                    callback(this);
                } else {
                    //var uri = new URL(serverUri);
                    window.location.href = "/group/";
                }
            })
            .catch(function (error) {
                console.error(error);
                refRoot.database.goOffline();
                //firebase.auth().signOut();
                trackingListener.onStop();

                if (callback) {
                    callback(this);
                } else {
                    //var uri = new URL(serverUri);
                    window.location.href = "/group/";
                }
            });
        }
        //firebase.auth().signOut();
        /*window.removeEventListener("focus", updateActive);
        document.removeEventListener("visibilitychange", updateActive);
        trackingListener.onStop();

        if(callback) {
            callback(this);
        } else {
            //var uri = new URL(serverUri);
            window.location.href = "/group/";
        }*/
//        window.location.href = "https://" + uri.hostname + (window.data.HTTPS_PORT == 443 ? "" : ":"+ window.data.HTTPS_PORT) + "/track/";
    }

    function WebSocketListener(link, reconnect) {

        var sendOriginal = send;
        var onopen =  function() {
            opened = true;
            if(newTracking) { // create group
                put(REQUEST.REQUEST, REQUEST.NEW_GROUP);
                put(REQUEST.UID, utils.getUuid());
            } else if(reconnect) { // reconnect to group
                var parts = link.split("/");
                var groupId = parts[parts.length-1];
                setToken(groupId);

                put(REQUEST.REQUEST, REQUEST.JOIN_GROUP);
                put(REQUEST.TOKEN, groupId);
            } else { // join to group
                parts = link.split("/");
                groupId = parts[parts.length-1];
                setToken(groupId);

                put(REQUEST.REQUEST, REQUEST.JOIN_GROUP);
                put(REQUEST.TOKEN, groupId);
                put(REQUEST.UID, utils.getUuid());
            }
            put(REQUEST.MODEL, navigator.appCodeName );
            put(REQUEST.MANUFACTURER, navigator.appCodeName);
            put(REQUEST.OS, navigator.platform);

            var signProvider = u.load(REQUEST.SIGN_PROVIDER) || "anonymous";
            if(signProvider) {
                put(REQUEST.SIGN_PROVIDER, signProvider);
            }

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
                        var uid = utils.getUuid();
                        var hash = utils.getEncryptedHash(control +":"+ uid);
                        put(REQUEST.REQUEST, REQUEST.CHECK_USER);
                        put(REQUEST.HASH, hash);
                        send();
                    }
                    break;
                case RESPONSE.STATUS_ACCEPTED:
                    newTracking = false;
                    send = sendOriginal;
                    try {
                        webSocketListener && webSocketListener.close();
                    } catch(e) {
                        console.error(e);
                    }
                    if(o[RESPONSE.SIGN]) {
                        var authToken = o[RESPONSE.SIGN];
                        delete o[RESPONSE.SIGN];

                        try {
                            firebase.auth().signInWithCustomToken(authToken).then(function () {

                                // setStatus(EVENTS.TRACKING_ACTIVE);
                                if (o[RESPONSE.TOKEN]) {
                                    setToken(o[RESPONSE.TOKEN]);
                                    if(!serverUri.match(o[RESPONSE.TOKEN])) {
                                        serverUri = link + "/" + o[RESPONSE.TOKEN];
                                    }
                                }
                                if (o[RESPONSE.NUMBER] !== undefined) {
                                    console.warn("Joined with number",o[RESPONSE.NUMBER]);
                                    main.users.setMyNumber(o[RESPONSE.NUMBER]);
                                }
                                o[RESPONSE.INITIAL] = true;

                                refRoot = database.ref();
                                refGroup = refRoot.child(DATABASE.SECTION_GROUPS).child(getToken());
                                refAccounts = refRoot.child(DATABASE.SECTION_USERS);
                                refStat = refRoot.child(DATABASE.SECTION_STAT);
                                refRoot.database.goOnline();

                                try {
                                    trackingListener.onAccept(o);
                                } catch (e) {
                                    console.error(e.message);
                                }

                                if(main.me && main.me.number != undefined) {
                                    refGroup.child(DATABASE.USERS).child(DATABASE.PUBLIC).child(main.me.number).child(DATABASE.ACTIVE).set(true);
                                }

                                updateTask = setInterval(updateActive, 60000);
                                window.addEventListener("focus", updateActive);
                                document.addEventListener("visibilitychange", updateActive);

                                registerValueListener(refGroup.child(DATABASE.OPTIONS).child(DATABASE.CREATED), groupListener, groupErrorListener);
                                registerValueListener(refGroup.child(DATABASE.USERS).child(DATABASE.PUBLIC).child(main.me.number).child(DATABASE.ACTIVE), userActiveListener);
                                registerChildListener(refGroup.child(DATABASE.USERS).child(DATABASE.PUBLIC), usersDataListener, -1);
                                main.eventBus.fire(function(holder){
                                    if(holder.saveable) {
                                        registerChildListener(refGroup.child(DATABASE.PRIVATE).child(holder.type).child(main.me.number), userPrivateDataListener, -1);
                                    }
                                });

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
           // console.log("CLOSE",opened,event.code,event.reason,event.wasClean);
            if(!opened) {
                console.error("Websocket processing closed unexpectedly, will try to use XHR instead of " + link + " (error " + event.code + (event.extra?": "+event.extra:")")+".");
                xhrModeStart(link);
            }
        };

        var onerror = function(event) {
                console.error("Websocket processing failed, will try to use XHR instead of " + link + ".", event);
            if(status === EVENTS.TRACKING_DISABLED) return;
            xhrModeStart(link);
        };

        var xhrModeStart = function(link) {
            link = "/rest/join";

            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() { //
                if (xhr.readyState !== 4) return;
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
                if (xhr.readyState !== 4) return;
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
                if(webSocket instanceof WebSocket && webSocket.readyState !== WebSocket.OPEN) {
                    webSocket.close();
                }
            }, window.data.is_stand_alone ? 15000 : 100);
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
        if(type === REQUEST.NEW_GROUP || type === REQUEST.JOIN_GROUP || type === REQUEST.CHECK_USER) {
            // console.error("WRONG WAY");
            // switch (webSocketListener.status) {
            webSocketListener.send(JSON.stringify(json));
            //
            // }
        } else if(refGroup) {
            if(type === REQUEST.CHANGE_NAME) {
                updates = {};
                updates[USER.NAME] = jsonMessage[USER.NAME];
                updates[DATABASE.CHANGED] = firebase.database.ServerValue.TIMESTAMP;

                refGroup.child(DATABASE.USERS).child(DATABASE.PUBLIC).child(main.me.number).update(updates).catch(function(error) {
                    console.error(error);
                });

                return;
            } else if(type === REQUEST.WELCOME_MESSAGE) {
                console.error("WELCOMEMESSAGE");

                return;
            }

            var holder = main.eventBus.holders[type];
            if(!holder || !holder.saveable) return;

            delete jsonMessage[REQUEST.REQUEST];
            delete jsonMessage[REQUEST.PUSH];
            delete jsonMessage[REQUEST.DELIVERY_CONFIRMATION];

            var path;
            if(jsonMessage.to) {
                var to = jsonMessage.to;
                delete jsonMessage.to;
                jsonMessage.from = main.me.number;
                path = DATABASE.PRIVATE + "/" + type + "/" + to;
            } else {
                path = DATABASE.PUBLIC + "/" + type + "/" + main.me.number;
            }
            var key = refGroup.push().key;

            updates = {};
            updates[path + "/" + key] = jsonMessage;
            updates[DATABASE.USERS + "/" + DATABASE.PUBLIC + "/" + main.me.number + "/" + DATABASE.CHANGED] = firebase.database.ServerValue.TIMESTAMP;

//console.log("UPDATE2",updates);
            refGroup.update(updates).catch(function(error) {
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
//        return "http://" + uri.hostname + (window.data.HTTP_PORT == 80 ? "" : ":"+window.data.HTTP_PORT) + "/track/" + token;
    }

    function registerChildListener(ref, listener, limit) {
        if(limit > 0){
            ref.limitToLast(limit).on("child_added", listener);
        } else {
            ref.on("child_added", listener);
        }
        refs.push(ref);
        return ref;
    }

    function registerValueListener(ref, listener, errorListener) {
        ref.on("value", listener, errorListener);
        refs.push(ref);
        return ref;
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
        console.error("groupErrorListener", error);
        switch (error.code) {
            case "PERMISSION_DENIED":
                setStatus(EVENTS.TRACKING_DISABLED);
                var reason = u.lang.group_has_been_removed;
                trackingListener.onReject(reason);
                break
        }
    }

    function usersDataListener(data){
        try{
            var o = data.val();
            o[RESPONSE.NUMBER] = parseInt(data.key);
            o[RESPONSE.INITIAL] = true;
            delete o.active;
            var user = main.users.addUser(o);

            user.type = "user";

            //registers
            registerValueListener(refGroup.child(DATABASE.USERS).child(DATABASE.PUBLIC).child(user.number).child(DATABASE.NAME), usersDataNameListener);
            registerValueListener(refGroup.child(DATABASE.USERS).child(DATABASE.PUBLIC).child(user.number).child(DATABASE.ACTIVE), usersDataActiveListener);
            registerValueListener(refGroup.child(DATABASE.USERS).child(DATABASE.PUBLIC).child(user.number).child(DATABASE.CHANGED), usersDataChangedListener);

            main.eventBus.fire(function(holder){
                if(holder.saveable) {
                    var loadSaved = holder.loadsaved || 1;
                    registerChildListener(refGroup.child(DATABASE.PUBLIC).child(holder.type).child(user.number), userPublicDataListener, loadSaved);
                }
            });

            trackingListener.onAccept(o);
        } catch(e) {
            console.error(e.message);
        }
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
            var name = u.clear(data.val());
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

            var o = {};
            o[RESPONSE.STATUS] = RESPONSE.STATUS_UPDATED;
            o[RESPONSE.NUMBER] = number;
            o[active ? USER.JOINED : USER.DISMISSED] = number;

            //if(!user) {
            //    o[RESPONSE.INITIAL] = true;
            //    delete o.active;
            //    var user = main.users.addUser(o);
            //
            //    user.type = "user";
            //
            //    user.refs = user.refs || [];
            //
            //    trackingListener.onAccept(o);
            //}
            //if(active && user) {
            //    user.refs.push(registerValueListener(ref.child(DATABASE.USERS).child(DATABASE.PUBLIC).child(number).child(DATABASE.NAME), usersDataNameListener));
            //    user.refs.push(registerValueListener(ref.child(DATABASE.USERS).child(DATABASE.PUBLIC).child(number).child(DATABASE.CHANGED), usersDataChangedListener));
            //    console.log("ACTIVE",active, number, user);
            //} else if(user) {
            //    for(var i in user.refs) {
            //        user.refs[i].off();
            //    }
            //    user.refs = [];
            //}
            trackingListener.onMessage(o);
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
                refGroup.child(DATABASE.USERS).child(DATABASE.PUBLIC).child(main.me.number).child(DATABASE.ACTIVE).set(true);
                refGroup.child(DATABASE.USERS).child(DATABASE.PUBLIC).child(main.me.number).child(DATABASE.CHANGED).set(firebase.database.ServerValue.TIMESTAMP);
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
        send:send
    }
}