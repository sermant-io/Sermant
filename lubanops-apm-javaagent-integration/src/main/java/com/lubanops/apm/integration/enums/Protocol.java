package com.lubanops.apm.integration.enums;

/**
 * 协议
 */
public enum Protocol {
    /**
     * websocket
     */
    WS("ws", "wss"),
    /**
     * http
     */
    HTTP("http", "https"),
    ;

    private String value;

    private String secure;

    Protocol(String value, String secure) {
        this.value = value;
        this.secure = secure;
    }

    public String getValue() {
        return value;
    }

    public String getSecure() {
        return secure;
    }

}
