/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.registry.service.impl;

import com.alibaba.fastjson.JSONObject;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.registry.config.RegisterConfig;
import io.sermant.registry.config.grace.GraceConstants;
import io.sermant.registry.config.grace.GraceContext;
import io.sermant.registry.context.RegisterContext;
import io.sermant.registry.context.RegisterContext.ClientInfo;
import io.sermant.registry.service.cache.AddressCache;
import io.sermant.registry.service.utils.HttpClientResult;
import io.sermant.registry.service.utils.HttpClientUtils;
import io.sermant.registry.services.GraceService;
import io.sermant.registry.services.RegisterCenterService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Offline notification service
 *
 * @author provenceee
 * @since 2022-05-26
 */
public class GraceServiceImpl implements GraceService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(10);

    private static final AtomicBoolean SHUTDOWN = new AtomicBoolean();

    private static final String REGISTRATION_DEREGISTER_METHOD_NAME = "stop";

    private static final String GRACE_HTTP_SERVER_PROTOCOL = "http://";

    private static final String REQUEST_BODY = JSONObject.toJSONString(new Object());

    private static final int RETRY_TIME = 3;

    /**
     * Offline notifications
     */
    @Override
    public void shutdown() {
        if (SHUTDOWN.compareAndSet(false, true)) {
            Object registration = GraceContext.INSTANCE.getGraceShutDownManager().getRegistration();
            ReflectUtils.invokeMethodWithNoneParameter(registration, REGISTRATION_DEREGISTER_METHOD_NAME);
            checkAndCloseSc();
            GraceContext.INSTANCE.getGraceShutDownManager().setShutDown(true);
            ClientInfo clientInfo = RegisterContext.INSTANCE.getClientInfo();
            Map<String, Collection<String>> header = new HashMap<>();
            header.put(GraceConstants.MARK_SHUTDOWN_SERVICE_NAME,
                    Collections.singletonList(clientInfo.getServiceName()));
            header.put(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT,
                    Arrays.asList(clientInfo.getIp() + ":" + clientInfo.getPort(),
                            clientInfo.getHost() + ":" + clientInfo.getPort()));
            AddressCache.INSTANCE.getAddressSet().forEach(address -> notifyToGraceHttpServer(address, header));
        }
    }

    private void notifyToGraceHttpServer(String address, Map<String, Collection<String>> header) {
        EXECUTOR.execute(() -> execute(address, header));
    }

    private void execute(String address, Map<String, Collection<String>> header) {
        for (int i = 0; i < RETRY_TIME; i++) {
            HttpClientResult result = HttpClientUtils.INSTANCE.doPost(
                    GRACE_HTTP_SERVER_PROTOCOL + address + GraceConstants.GRACE_NOTIFY_URL_PATH,
                    REQUEST_BODY, header);
            if (result.getCode() == GraceConstants.GRACE_HTTP_SUCCESS_CODE) {
                break;
            }
            LOGGER.log(Level.WARNING, "Failed to notify before shutdown, address: {0}", address);
        }
    }

    /**
     * In the dual-registration scenario, the original SC instance is deregistered at the same time
     */
    private void checkAndCloseSc() {
        final RegisterConfig registerConfig = PluginConfigManager.getPluginConfig(RegisterConfig.class);
        if (registerConfig.isEnableSpringRegister() && registerConfig.isOpenMigration()) {
            PluginServiceManager.getPluginService(RegisterCenterService.class).unRegister();
        }
    }

    @Override
    public void addAddress(String address) {
        AddressCache.INSTANCE.addAddress(address);
    }
}
