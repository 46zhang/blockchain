package com.gdut.fundraising.constant.raft;


/**
 * 消息类型枚举类
 */
public enum MessageType {
    /**
     * 投票
     */
    VOTE(0, "vote"),
    /**
     * 添加日志
     */
    APPEND_LOG(1, "appendlog"),
    /**
     * 确认日志
     */
    CONFIRM_LOG(2, "confirmlog"),
    /**
     * 心跳包
     */
    HEART_BEAT(3, "heartbeat");

    private int value;
    private String name;

    MessageType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static MessageType getMessageType(int value) {
        for (MessageType messageType : MessageType.values()) {
            if (value == messageType.value) {
                return messageType;
            }
        }
        return null;
    }
}
