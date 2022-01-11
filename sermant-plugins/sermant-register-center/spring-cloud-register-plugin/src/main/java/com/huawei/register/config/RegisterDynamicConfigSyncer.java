/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.register.config;

import com.huawei.register.context.RegisterContext;
import com.huawei.register.handler.SingleStateCloseHandler;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.service.PluginService;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * 动态配置类
 *
 * @author zhouss
 * @since 2021-12-30
 */
public class RegisterDynamicConfigSyncer implements PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 注册中心关闭开关
     */
    private static final String REGISTER_KEY = "register-close-switch";

    /**
     * 注册中心配置组
     */
    private static final String REGISTER_GROUP = "register";

    @Override
    public void start() {
        final DynamicConfigService service = ServiceManager.getService(DynamicConfigService.class);
        service.addConfigListener(REGISTER_KEY, REGISTER_GROUP, new DynamicConfigListener() {
            @Override
            public void process(DynamicConfigEvent event) {
                if (event.getContent() == null || event.getEventType() == DynamicConfigEventType.DELETE) {
                    RegisterDynamicConfig.closeOriginRegisterCenter = false;
                } else {
                    RegisterDynamicConfig.closeOriginRegisterCenter =
                            Boolean.parseBoolean(event.getContent());
                }
                if (event.getEventType() != DynamicConfigEventType.INIT) {
                    // 初始化的参数不生效, 仅当后续修改生效, 防止用户因配置问题导致注册中心关闭
                    tryCloseOriginRegisterCenter();
                }
            }

            private void tryCloseOriginRegisterCenter() {
                if (RegisterDynamicConfig.closeOriginRegisterCenter) {
                    for (SingleStateCloseHandler handler : RegisterContext.INSTANCE.getCloseHandlers()) {
                        try {
                            handler.doClose();
                        } catch (Exception ex) {
                            LOGGER.warning(String.format(Locale.ENGLISH,
                                    "Origin register center closed failed! %s", ex.getMessage()));
                        }
                    }
                }
            }
        });
    }
}
