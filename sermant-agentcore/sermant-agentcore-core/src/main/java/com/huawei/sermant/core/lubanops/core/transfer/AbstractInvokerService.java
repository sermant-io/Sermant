/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.lubanops.core.transfer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.huawei.sermant.core.lubanops.bootstrap.config.ConfigManager;
import com.huawei.sermant.core.lubanops.bootstrap.config.IdentityConfigManager;
import com.huawei.sermant.core.lubanops.bootstrap.exception.ApmRuntimeException;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.lubanops.core.api.AgentService;
import com.huawei.sermant.core.lubanops.core.common.ConnectionException;
import com.huawei.sermant.core.lubanops.core.container.Priority;
import com.huawei.sermant.core.lubanops.core.executor.ExecuteRepository;
import com.huawei.sermant.core.lubanops.core.executor.timer.Timeout;
import com.huawei.sermant.core.lubanops.core.executor.timer.TimerTask;

import com.huawei.sermant.core.lubanops.integration.access.Address;
import com.huawei.sermant.core.lubanops.integration.access.MessageWrapper;
import com.huawei.sermant.core.lubanops.integration.transport.ClientManager;
import com.huawei.sermant.core.lubanops.integration.transport.websocket.ConnectConfig;
import com.huawei.sermant.core.lubanops.integration.transport.websocket.LubanWebSocketClient;
import com.huawei.sermant.core.lubanops.integration.transport.websocket.WebSocketFactory;

/**
 * Abstract invoker service.
 * @author
 */
public abstract class AbstractInvokerService implements InvokerService, AgentService {

    public static final int DEFAULT_ENFORCE_TICKS = 4;

    public final static int DEFAULT_CONNECT_INTERVAL = 2;

    private final static Logger LOGGER = LogFactory.getLogger();

    public static final long MAX_CONNECT_TIMEOUT = 10000L;

    public LubanWebSocketClient client = null;

    private final ReentrantLock connectLock = new ReentrantLock();

    @Inject
    ExecuteRepository executeRepository;

    private volatile Timeout failBackConnectTimeout = null;

    private volatile boolean inFailBack = Boolean.FALSE;

    private volatile boolean closed = false;

    protected LubanWebSocketClient connect(List<Address> addressList, boolean isSecure) {
        ConnectConfig newConfig = new ConnectConfig();
        newConfig.setInstanceId(IdentityConfigManager.getInstanceId());
        // TODO random
        newConfig.setRandomConnect(false);
        newConfig.setSecure(isSecure);
        newConfig.setConnectTimeout(MAX_CONNECT_TIMEOUT);
        List<String> secureList = new ArrayList<String>();
        List<String> unSecureList = new ArrayList<String>();
        for (Address address : addressList) {
            StringBuilder unSecureUrlBuilder = new StringBuilder(address.getProtocol().getValue());
            unSecureUrlBuilder.append("://").append(address.getHost()).append(":").append(address.getPort());
            unSecureList.add(unSecureUrlBuilder.toString());

            StringBuilder secureUrlBuilder = new StringBuilder(address.getProtocol().getSecure());
            secureUrlBuilder.append("://").append(address.getHost()).append(":").append(address.getSport());
            secureList.add(secureUrlBuilder.toString());
        }
        newConfig.setSecureAddressList(secureList);
        newConfig.setUnSecureAddressList(unSecureList);
        WebSocketFactory factory = ClientManager.getWebSocketFactory();
        factory.setConfig(newConfig);
        return factory.getWebSocketClient();
    }

    protected LubanWebSocketClient connectProxy(List<Address> addressList, boolean isSecure, String proxyAddr) {
        InetSocketAddress socksaddr = new InetSocketAddress(proxyAddr, 38335);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
        LubanWebSocketClient lubanWebSocketClient = this.connect(addressList, isSecure);
        lubanWebSocketClient.setProxy(proxy);
        return lubanWebSocketClient;
    }

    @Override
    public boolean isSendEnable() {
        return failBackConnectTimeout == null;
    }

    @Override
    public void dispose() throws ApmRuntimeException {
        if (closed) {
            return;
        }
        closed = true;
        if (this.client != null) {
            client.close();
        }
        if (null != failBackConnectTimeout) {
            failBackConnectTimeout.cancel();
        }
    }

    @Override
    public int getPriority() {
        return Priority.AGENT_INTERNAL_INFRASTRUCTURE_SERVICE;
    }

    private LubanWebSocketClient getConnect() throws ConnectionException {
        connectLock.lock();
        try {
            if (failBackConnectTimeout != null) {
                if (inFailBack == true) {
                    if (this.client != null && this.client.isOpen()) {
                        this.client.close();
                    }
                    this.client = createConnectConfig();
                    cancelTimeout();
                }
            } else if (needReconnect()) {
                if (this.client != null && this.client.isOpen()) {
                    this.client.close();
                }
                this.client = createConnectConfig();
                cancelTimeout();
            } else {
                if (this.client == null || (this.client.isClosed() && this.client.isHasOpenResult())) {
                    this.client = createConnectConfig();
                    cancelTimeout();
                }
            }
        } catch (ConnectionException e) {
            if (failBackConnectTimeout == null) {
                failBackConnectTimeout = AbstractInvokerService.this.executeRepository.getSharedTimer()
                        .newTimeout(new FailBackConnectTimeTask(), DEFAULT_CONNECT_INTERVAL, TimeUnit.SECONDS);
            }
            throw e;
        } finally {
            connectLock.unlock();
        }
        return this.client;
    }

    /**
     * 是否需要重连，由子类实现
     * @return
     */
    protected abstract boolean needReconnect();

    /**
     * 创建物理物理连接
     * @return
     * @throws IOException
     */
    protected abstract LubanWebSocketClient createConnectConfig() throws ConnectionException;

    /**
     * 这里加syn的目的是，如果多线程使用的时候会有问题。add by liyuejian
     * @return
     * @throws ConnectionException
     * @throws IOException
     */
    protected synchronized void sendRequest(MessageWrapper request) throws ConnectionException, IOException {
        if (ConfigManager.isValidated()) {
            LubanWebSocketClient lubanWebSocketClient = getConnect();
            if (lubanWebSocketClient.getOpenResult()) {
                lubanWebSocketClient.sendAsync(request.generatorMessage());
            }
        }
    }

    protected final class FailBackConnectTimeTask implements TimerTask {

        private final static String TASK_NAME = "FailBackConnectAccessTimeTask";

        private int retryCount = 0;

        public FailBackConnectTimeTask reset() {
            FailBackConnectTimeTask newTask = new FailBackConnectTimeTask();
            newTask.setRetryCount(retryCount);
            return newTask;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            connectLock.lock();
            try {
                inFailBack = true;
                ++retryCount;
                LubanWebSocketClient lubanWebSocketClient = getConnect();
                if (lubanWebSocketClient != null) {
                    AbstractInvokerService.this.failBackConnectTimeout = null;
                } else {
                    reput();
                }
                inFailBack = false;
            } catch (Exception e) {
                reput();
                LOGGER.log(Level.SEVERE, String.format("[APM TRANSFER]reconnect task[%s] has exception.", this), e);
            } finally {
                connectLock.unlock();
            }
        }

        private void reput() {
            long tick = DEFAULT_CONNECT_INTERVAL;
            int circle = 0;
            if (retryCount < 11) {
                circle = retryCount;
            } else {
                circle = 11;
            }
            if (circle > DEFAULT_ENFORCE_TICKS) {
                tick = (long) Math.pow(tick, circle - 3);
            }
            AbstractInvokerService.this.failBackConnectTimeout = AbstractInvokerService.this.executeRepository
                    .getSharedTimer()
                    .newTimeout(reset(), tick, TimeUnit.SECONDS);
        }

        @Override
        public String getName() {
            return TASK_NAME;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }

    }

    private void cancelTimeout() {
        if (null != AbstractInvokerService.this.failBackConnectTimeout) {
            AbstractInvokerService.this.failBackConnectTimeout.cancel();
            AbstractInvokerService.this.failBackConnectTimeout = null;
        }
    }

}
