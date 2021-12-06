/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.core.lubanops.integration.transport.websocket;

import com.huawei.javamesh.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.lubanops.integration.exception.JavaagentRuntimeException;
import com.huawei.javamesh.core.lubanops.integration.utils.UriUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 发送线程获取socket的工厂，当配置变化的时候获取的socket配置也会有变化 有安全和非安全的地址两个列表，每个列表可以包含内部和外部两个地址
 * 系统连接的时候根据用户配置的是安全的还是非安全的链接情况选择哪个地址去连接，连接的时候按顺序链接， 设置地址列表的时候将内部地址放在前面
 * @author
 * @since 2020/5/14
 **/
public class WebSocketFactory {
    private final static Logger LOG = LogFactory.getLogger();

    private static WebSocketFactory factory = new WebSocketFactory();

    /**
     * 连接参数
     */
    private ConnectConfig connectConfig;

    /**
     * 当前的连接
     */
    private LubanWebSocketClient lubanWebSocketClient;

    /**
     * 是否需要重连，如果需要重连，一般是配置发生变化了才要求重连
     */
    private boolean needReconnect;

    /*
     * 上一次创建连接的时间
     */
    private long lastConnectTime;

    private WebSocketFactory() {

    }

    /**
     * 获取工厂
     * @return
     */
    public static WebSocketFactory getInstance() {
        return factory;
    }

    /**
     * 获取连接的地址
     * @return
     */
    public LubanWebSocketClient getWebSocketClient() {
        if (connectConfig == null) {
            throw new JavaagentRuntimeException("factory not initialized");
        }

        if (useCurrent()) {
            return this.lubanWebSocketClient;
        }

        return createWebSocketClient();
    }

    /**
     * 是否使用当前链接
     * @return
     */
    private boolean useCurrent() {
        if (!needReconnect && lubanWebSocketClient != null && lubanWebSocketClient.isOpen()) {
            return true;
        }
        return false;

    }

    /**
     * 配置变化，设置是否需要断开重新连接
     */
    public synchronized void setConfig(ConnectConfig newConfig) {
        if (this.connectConfig == null) {
            this.connectConfig = newConfig;
            return;
        }

        if (this.connectConfig.equals(newConfig)) {
            return;
        }

        this.connectConfig = newConfig;
        this.needReconnect = true;

    }

    /**
     * 创建物理连接
     * @return
     */
    private synchronized LubanWebSocketClient createWebSocketClient() {
        if (useCurrent()) {
            return this.lubanWebSocketClient;
        }

        if (this.lubanWebSocketClient != null) {
            this.lubanWebSocketClient.close();
            this.lubanWebSocketClient = null;
        }

        LubanWebSocketClient client = null;

        List<String> addressList = this.connectConfig.getAddressList();

        for (String address : addressList) {
            String uri = UriUtil.buildUri(address, AgentConfigManager.getMasterAuthAk(),
                AgentConfigManager.getMasterAuthSk(), this.connectConfig.getInstanceId());
            URI serverUri = buildServerUri(uri);
            LubanWebSocketClient cc;
            cc = this.connectConfig.isSecure()
                ? new SecureWebSocketClient(serverUri, uri)
                : new UnSecureWebSocketClient(serverUri, uri);
            LOG.log(Level.SEVERE, "[TRANSFER INVOKER]make client from  uri:" + uri + " to WebSocketClient:" + cc);
            try {
                cc.connectBlocking(this.connectConfig.getConnectTimeout(), TimeUnit.MILLISECONDS);
                if (cc.isOpen() && cc.isHasOpenResult(this.connectConfig.getConnectTimeout())) {
                    client = cc;
                    break;
                }
            } catch (InterruptedException e) {
                throw new JavaagentRuntimeException("connect InterruptedException:", e);
            }
        }
        if (client != null) {
            this.needReconnect = false;
            this.lubanWebSocketClient = client;
            this.setLastConnectTime(System.currentTimeMillis());
            return client;
        }
        return null;
    }

    private URI buildServerUri(String s) {
        try {
            return new URI(s);
        } catch (URISyntaxException e) {
            throw new JavaagentRuntimeException("wrong uri:" + s, e);
        }
    }

    public long getLastConnectTime() {
        return lastConnectTime;
    }

    public void setLastConnectTime(long lastConnectTime) {
        this.lastConnectTime = lastConnectTime;
    }

}
