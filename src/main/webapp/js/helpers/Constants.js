/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/9/17.
 */
function Constants() {

    window.EVENTS = {
        SELECT_USER: "select",
        SELECT_SINGLE_USER: "select_single",
        UNSELECT_USER: "unselect",
        MAKE_ACTIVE: "make_active",
        MAKE_INACTIVE: "make_inactive",
        MAKE_ENABLED: "make_enabled",
        MAKE_DISABLED: "make_disabled",
        CHANGE_NAME: "change_name",
        CHANGE_NUMBER: "change_number",
        CHANGE_COLOR: "change_color",
        SYSTEM_MESSAGE: "system_message",
        MAP_MY_LOCATION_BUTTON_CLICKED: "map_my_location_button_clicked",

        CREATE_CONTEXT_MENU: "create_context_menu",
        CREATE_OPTIONS_MENU: "create_options_menu",
        PREPARE_OPTIONS_MENU: "prepare_options_menu",
        PREPARE_FAB: "prepare_fab",
        CREATE_DRAWER: "create_drawer",
        PREPARE_DRAWER: "prepare_drawer",
        DROPPED_TO_USER: "dropped_to_user",

        ACTIVITY_CREATE: "activity_create",
        ACTIVITY_PAUSE: "activity_pause",
        ACTIVITY_RESUME: "activity_resume",
        ACTIVITY_DESTROY: "activity_destroy",
        ACTIVITY_RESULT: "activity_result",

        TRACKING_NEW: "tracking_new",
        TRACKING_JOIN: "tracking_join",
        TRACKING_STOP: "tracking_stop",
        TRACKING_DISABLED: "tracking_disabled",
        TRACKING_CONNECTING: "tracking_connecting",
        TRACKING_ACTIVE: "tracking_active",
        TRACKING_RECONNECTING: "tracking_reconnecting",
        TRACKING_EXPIRED: "tracking_expired",
        TRACKING_ERROR: "tracking_error",
        TOKEN_CREATED: "token_created",

        MAP_READY: "map_ready",
        FIREBASE_READY: "firebase_ready",

        MOVING_CLOSE_TO: "moving_close_to",
        MOVING_AWAY_FROM: "moving_away_from",

        MOUSE_OVER: "mouse_over",
        MOUSE_OUT: "mouse_out",

        SHOW_BADGE: "show_badge",
        HIDE_BADGE: "hide_badge",
        INCREASE_BADGE: "increase_badge",

        SYNC_PROFILE: "sync_profile"
    };
    window.REQUEST = {
        REQUEST: "client",
        TIMESTAMP : "timestamp",
        UPDATE : "update",
        JOIN_GROUP : "join",
        NEW_GROUP : "create",
        CHECK_USER : "check",
        TOKEN : "token",
        HASH : "hash",
        PUSH : "push",
        ADMIN : "admin",

        UID : "uid",
        MODEL : "model",
        MANUFACTURER : "manufacturer",
        OS : "os",
        KEY : "key",
        SIGN_PROVIDER : "sign-provider",

        TRACKING : "tracking",
        MESSAGE : "message",
        CHANGE_NAME : "change_name",
        WELCOME_MESSAGE : "welcome_message",
        LEAVE : "leave",
        SAVED_LOCATION : "saved_location",

        DELIVERY_CONFIRMATION : "delivery"
    };
    window.RESPONSE = {
        STATUS : "server",
        STATUS_ACCEPTED : "accepted",
        STATUS_UPDATED : "updated",
        STATUS_CHECK : "check",
        STATUS_ERROR : "error",

        MESSAGE : "message",
        TOKEN : "token",
        CONTROL : "control",
        NUMBER : "number",
        INITIAL : "initial",
        PRIVATE : "to",
        SIGN : "sign"
    };
    window.USER = {
        JOINED : "joined",
        DISMISSED : "dismissed",
        LEFT : "left",

        PROVIDER : "pr",
        LATITUDE : "la",
        LONGITUDE : "lo",
        ALTITUDE : "al",
        ACCURACY : "ac",
        BEARING : "be",
        TILT : "ti",
        SPEED : "sp",

        NUMBER : "number",
        COLOR : "color",
        NAME : "name",
        MESSAGE : "user_message",
        ADDRESS : "address",
        DESCRIPTION : "description"
    };
    window.DATABASE = {
        SECTION_GROUPS : "_groups",
        PUBLIC : "b", // public
        PRIVATE : "p", // private
        USERS : "u", // users
        NAME : "name", // name
        ACTIVE : "active", // active
        COLOR : "color", // color
        KEYS: "k",
        ORDER: "n",
        OPTIONS : "o", // options
        PERSISTENT : "persistent",
        TIME_TO_LIVE_IF_EMPTY : "time-to-live-if-empty",
        DELAY_TO_DISMISS : "delay-to-dismiss",
        DISMISS_INACTIVE : "dismiss-inactive",
        REQUIRES_PASSWORD : "requires-password",
        WELCOME_MESSAGE : "welcome-message",
        CREATED : "cr",
        CHANGED : "ch",
        SYNCED : "sy",

        SECTION_STAT: "_stat",
        STAT_TOTAL: "total",
        STAT_BY_DATE: "by-date",
        STAT_GROUPS_CREATED_TEMPORARY: "gt",
        STAT_GROUPS_CREATED_PERSISTENT: "gp",
        STAT_GROUPS_DELETED: "gd",
        STAT_GROUPS_REJECTED: "gr",
        STAT_USERS_JOINED: "ju",
        STAT_USERS_RECONNECTED: "uc",
        STAT_USERS_REJECTED: "ur",
        STAT_MESSAGES: "messages",
        STAT_MISC: "misc",
        STAT_MISC_GROUPS_CLEANED: "groups-cleaned",

        SECTION_USERS: "_users",
        HISTORY: "h"
   };

    this.type = "constants";
    this.start = function(){}
}