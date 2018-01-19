/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 2/12/17.
 */
function MyUsers(main) {

    var users = {};
    var myNumber = 0;

    function addUser (json) {
        var user;// = new MyUser();
        if (!users[json[RESPONSE.NUMBER]]) {
            user = new MyUser(main);
            user.number = json[RESPONSE.NUMBER];
            if (json[USER.COLOR]){
                user.color = utils.getHexColor(json[USER.COLOR]);
            }
            if (json[USER.NAME]){
                user.name = json[USER.NAME];
            }
            if (json[USER.PROVIDER]) {
                var location = utils.jsonToLocation(json);
                user.addLocation(location);
            }
            users[json[RESPONSE.NUMBER]] = user;
            user.type = json.type;
            user.changed = json.changed;
            user.origin = json;
            if(user.number != json[RESPONSE.NUMBER]) {
                user.fire(EVENTS.CHANGE_NUMBER, json[RESPONSE.NUMBER]);
            }
            user.createViews();

        } else {
            user = users[json[RESPONSE.NUMBER]];
            if (json[USER.COLOR]) user.fire(EVENTS.CHANGE_COLOR, json[USER.COLOR]);
        }
        return user;
    }

    function setMe() {
        delete users[myNumber];
        main.me.number = myNumber;
        main.me.type = "user";
        main.me.fire(EVENTS.CHANGE_NUMBER, myNumber);
        var name = u.load("properties:name");
        if(name) {
            main.me.name = name;
            main.me.fire(EVENTS.CHANGE_NAME, u.clear(name));
        }
        users[myNumber] = main.me;
        return main.me;
    }

    function setMyNumber(number) {
        number = parseInt(number);
        if(number != myNumber) {
            main.me.number = number;
            main.users.users[number] = main.me;
            if (myNumber != undefined) delete main.users.users[myNumber];
            myNumber = number;
            main.me.fire(EVENTS.CHANGE_NUMBER, number);
        }
    }

    function forAllUsers(callback){
        forMe(callback);
        forAllUsersExceptMe(callback);
    }

    function forSelectedUsers(callback){
        for(var i in users){
            if(users[i] && users[i].selected) forUser(i, callback);
        }
    }

    function forMe(callback) {
        forUser(myNumber, callback);
    }

    function forAllUsersExceptMe(callback){
        for(var i in users){
            if(users[i] && users[i] != main.me) forUser(i, callback);
        }
    }

    function forAllActiveUsersExceptMe(callback){
        for(var i in users){
            if(users[i] && users[i] != main.me && users[i].properties && users[i].properties.active) forUser(i, callback);
        }
    }

    function forAllActiveUsers(callback){
        forMe(callback);
        forAllActiveUsersExceptMe(callback);
    }

    function forUser(number,callback, args){
        if(users[number] && users[number].properties) callback(number, users[number], args);
    }

    function getCountSelected(){
        var count = 0;
        for(var i in users) {
            if(users[i] && i == users[i].number && users[i].properties && users[i].properties.active && users[i].properties.selected) {
                count ++;
            }
        }
        return count;
    }

    function getCountActive(){
        var count = 0;
        for(var i in users) {
            if(users[i] && i == users[i].number && users[i].properties && users[i].properties.active) {
                count ++;
            }
        }
        return count;
    }

    return {
        addUser:addUser,
        setMe:setMe,
        forAllUsers:forAllUsers,
        forAllActiveUsers:forAllActiveUsers,
        forAllActiveUsersExceptMe:forAllActiveUsersExceptMe,
        forSelectedUsers:forSelectedUsers,
        forMe:forMe,
        forAllUsersExceptMe:forAllUsersExceptMe,
        forUser:forUser,
        users:users,
        getCountSelected:getCountSelected,
        getCountActive:getCountActive,
        setMyNumber:setMyNumber,
    }
}
