/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config.source;

import com.huawei.dynamic.config.ConfigHolder;
import com.huawei.dynamic.config.closer.ConfigCenterCloser;
import com.huawei.dynamic.config.entity.DynamicConstants;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

/**
 * 监听禁用原配置中心的配置开关, 通过动态方式关闭
 *
 * @author zhouss
 * @since 2022-07-12
 */
@Component
@AutoConfigureBefore(SpringEventPublisher.class)
public class OriginConfigCenterDisableListener implements BeanFactoryAware {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final AtomicBoolean isShutdown = new AtomicBoolean();

    private final List<ConfigCenterCloser> configCenterClosers =
            new ArrayList<>(DynamicConstants.CONFIG_CENTER_CLOSER_INIT_NUM);

    @Autowired
    private Environment environment;

    private BeanFactory beanFactory;

    /**
     * 添加配置监听器
     */
    @PostConstruct
    public void addListener() {
        loadClosers();
        ConfigHolder.INSTANCE.addListener(event -> {
            if (!check()) {
                return;
            }
            disableConfigCenter();
            for (ConfigCenterCloser closer : configCenterClosers) {
                if (!closer.isSupport(beanFactory)) {
                    continue;
                }
                if (closer.close(beanFactory, environment)) {
                    LOGGER.warning(String.format(Locale.ENGLISH, "Origin Config Center [%s] has been unSubscribed!",
                            closer.type()));
                }
            }
        });
    }

    private void disableConfigCenter() {
        ConfigHolder.INSTANCE.getConfigSources().add(new OriginConfigDisableSource(
                DynamicConstants.DISABLE_CONFIG_SOURCE_NAME));
    }

    private boolean check() {
        final boolean isNeedClose =
                Boolean.parseBoolean(environment.getProperty(DynamicConstants.ORIGIN_CONFIG_CENTER_CLOSE_KEY));
        return isNeedClose && isShutdown.compareAndSet(false, true);
    }

    private void loadClosers() {
        for (ConfigCenterCloser closer : ServiceLoader.load(ConfigCenterCloser.class)) {
            configCenterClosers.add(closer);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
