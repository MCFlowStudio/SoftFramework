package com.softhub.softframework.database.redis.message;

public enum ProtocolName {

    SYSTEM_CONFIG_UPDATE,
    ONLINE_BACKEND_UPDATE,
    ONLINE_USER_UPDATE,

    // 이벤트
    USER_CONNECT,
    USER_DISCONNECT,
    USER_SERVER_SWITCH,

    // 신호
    SERVER_BROADCAST_MESSAGE,
    USER_MESSAGE,
    USER_ACTIONBAR,
    USER_COMMAND,
    USER_CONNECT_SERVER,
    USER_TELEPORT_POSITION,
    USER_GET_POSITION;

}