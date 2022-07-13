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

package com.huawei.registry.service.impl;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.config.grace.GraceContext;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.context.RegisterContext.ClientInfo;
import com.huawei.registry.service.cache.AddressCache;
import com.huawei.registry.service.utils.HttpClientUtils;
import com.huawei.registry.services.GraceService;
import com.huawei.registry.services.RegisterCenterService;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 下线通知服务
 *
 * @author provenceee
 * @since 2022-05-26
 */
public class GraceServiceImpl implements GraceService {
    private static final Executor EXECUTOR = Executors.newFixedThreadPool(10);

    private static final AtomicBoolean SHUTDOWN = new AtomicBoolean();

    private static final String REGISTRATION_DEREGISTER_METHOD_NAME = "stop";

    private static final String GRACE_HTTP_SERVER_PROTOCOL = "http://";

    private static final String REQUEST_BODY = JSONObject.toJSONString(new Object());

    /**
     * 下线通知
     */
    @Override
    public void shutdown() {
        if (SHUTDOWN.compareAndSet(false, true)) {
            Object registration = GraceContext.INSTANCE.getGraceShutDownManager().getRegistration();
            ReflectUtils.invokeMethodWithNoneParameter(registration, REGISTRATION_DEREGISTER_METHOD_NAME);
            checkAndCloseSc();
            GraceContext.INSTANCE.getGraceShutDownManager().setShutDown(true);
            ClientInfo clientInfo = RegisterContext.INSTANCE.getClientInfo();
            Map<String, String> header = new HashMap<>();
            header.put(GraceConstants.MARK_SHUTDOWN_SERVICE_NAME, clientInfo.getServiceName());
            header.put(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT, clientInfo.getIp() + ":"
                    + clientInfo.getPort());
            AddressCache.INSTANCE.getAddressSet().forEach(address -> notifyToGraceHttpServer(address, header));
        }
    }

    private void notifyToGraceHttpServer(String address, Map<String, String> header) {
        EXECUTOR.execute(() -> execute(address, header));
    }

    private void execute(String address, Map<String, String> header) {
        HttpClientUtils.INSTANCE.doPost(GRACE_HTTP_SERVER_PROTOCOL + address + GraceConstants.GRACE_NOTIFY_URL_PATH,
                REQUEST_BODY, header);
    }

    /**
     * 双注册场景同时注销原SC实例
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
