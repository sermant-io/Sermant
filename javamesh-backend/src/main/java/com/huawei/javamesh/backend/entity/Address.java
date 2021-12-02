package com.huawei.javamesh.backend.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {
    /**
     * 主机名，可以是ip也可以是域名
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * 安全端口
     */
    private int sport;

    /**
     * inner或者outer，代表是内网还是外网，地址优先链接内网的
     */
    private AddressType type;

    /**
     * 内外
     */
    private AddressScope scope;

    /*
     * 协议,当前只支持ws
     */
    private Protocol protocol;
}
