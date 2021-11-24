/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.session;

import lombok.Data;

import java.util.Objects;

/**
 * 远程执行时所需要的服务器信息
 *
 * @author y30010171
 * @since 2021-10-20
 **/
@Data
public class ServerInfo {
    private static final int DEFAULT_PORT = 22;
    private String serverIp;
    private String serverUser;
    private String serverPassword;
    private int serverPort;

    public ServerInfo(String serverIp, String serverUser) {
        this(serverIp, serverUser, "", DEFAULT_PORT);
    }

    public ServerInfo(String serverIp, String serverUser, int serverPort) {
        this(serverIp, serverUser, "", serverPort);
    }

    public ServerInfo(String serverIp, String serverUser, String serverPassword, int serverPort) {
        this.serverIp = serverIp;
        this.serverUser = serverUser;
        this.serverPassword = serverPassword;
        this.serverPort = serverPort;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ServerInfo that = (ServerInfo) object;
        return serverPort == that.serverPort && serverIp.equals(that.serverIp) && serverUser.equals(that.serverUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverIp, serverUser, serverPort);
    }
}
