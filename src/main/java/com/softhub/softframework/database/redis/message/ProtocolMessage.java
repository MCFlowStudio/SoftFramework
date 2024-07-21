package com.softhub.softframework.database.redis.message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProtocolMessage {
    private ProtocolName name;
    private ProtocolType type;
    private ProtocolTarget target;
    private int sendPort;
    private int receivePort;
    private String requestId;
    private Map<String, Object> data;

    public ProtocolMessage(ProtocolName name, ProtocolType type, int sendPort, int receivePort, ProtocolTarget target, String requestId, Map<String, Object> data) {
        this.name = name;
        this.type = type;
        this.sendPort = sendPort;
        this.receivePort = receivePort;
        this.target = target;
        this.requestId = requestId;
        this.data = data;
    }

    public static ProtocolMessage createMessage(ProtocolName name, ProtocolType type, int sendPort, int receivePort, ProtocolTarget target, Map<String, Object> data) {
        return new ProtocolMessage(
                name,
                type,
                sendPort,
                receivePort,
                target,
                UUID.randomUUID().toString(),
                data
        );
    }

    public static ProtocolMessage createMessage(ProtocolName name, ProtocolType type, int sendPort, int receivePort, ProtocolTarget target) {
        return new ProtocolMessage(
                name,
                type,
                sendPort,
                receivePort,
                target,
                UUID.randomUUID().toString(),
                new HashMap<>()
        );
    }

    public ProtocolName getName() {
        return name;
    }

    public ProtocolType getType() {
        return type;
    }

    public int getSendPort() {
        return sendPort;
    }

    public int getReceivePort() {
        return receivePort;
    }

    public ProtocolTarget getTarget() {
        return target;
    }

    public String getRequestId() {
        return requestId;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
