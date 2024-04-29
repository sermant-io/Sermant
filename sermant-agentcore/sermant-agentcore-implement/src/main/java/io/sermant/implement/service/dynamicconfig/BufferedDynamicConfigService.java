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

package io.sermant.implement.service.dynamicconfig;

import io.sermant.core.service.dynamicconfig.DynamicConfigService;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import io.sermant.implement.service.dynamicconfig.kie.KieDynamicConfigService;
import io.sermant.implement.service.dynamicconfig.nacos.NacosDynamicConfigService;
import io.sermant.implement.service.dynamicconfig.zookeeper.ZooKeeperDynamicConfigService;

import java.util.List;
import java.util.Optional;

/**
 * DynamicConfigService wrapper. Determine which implementation should be used based on the static configuration
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-14
 */
public class BufferedDynamicConfigService extends DynamicConfigService {
    /**
     * DynamicConfigService object
     */
    private final DynamicConfigService service;

    /**
     * Constructor
     */
    public BufferedDynamicConfigService() {
        // Different implementations are initialized depending on the configuration
        switch (CONFIG.getServiceType()) {
            case KIE:
                service = new KieDynamicConfigService();
                break;
            case NACOS:
                service = new NacosDynamicConfigService();
                break;
            default:
                service = new ZooKeeperDynamicConfigService();
        }
    }

    @Override
    public void start() {
        service.start();
    }

    @Override
    public void stop() {
        service.stop();
    }

    @Override
    public String getConfig(String key) {
        return service.getConfig(key);
    }

    @Override
    public String getConfig(String key, String group) {
        return service.getConfig(key, group);
    }

    @Override
    public boolean publishConfig(String key, String content) {
        return service.publishConfig(key, content);
    }

    @Override
    public boolean publishConfig(String key, String group, String content) {
        return service.publishConfig(key, group, content);
    }

    @Override
    public boolean removeConfig(String key) {
        return service.removeConfig(key);
    }

    @Override
    public boolean removeConfig(String key, String group) {
        return service.removeConfig(key, group);
    }

    @Override
    public boolean addConfigListener(String key, DynamicConfigListener listener) {
        return service.addConfigListener(key, listener);
    }

    @Override
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener) {
        return service.addConfigListener(key, group, listener);
    }

    @Override
    public boolean addConfigListener(String key, DynamicConfigListener listener, boolean ifNotify) {
        return service.addConfigListener(key, listener, ifNotify);
    }

    @Override
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener, boolean ifNotify) {
        return service.addConfigListener(key, group, listener, ifNotify);
    }

    @Override
    public boolean removeConfigListener(String key) {
        return service.removeConfigListener(key);
    }

    @Override
    public boolean removeConfigListener(String key, String group) {
        return service.removeConfigListener(key, group);
    }

    @Override
    public List<String> listKeysFromGroup(String group) {
        return service.listKeysFromGroup(group);
    }

    @Override
    public boolean addGroupListener(String group, DynamicConfigListener listener) {
        return service.addGroupListener(group, listener);
    }

    @Override
    public boolean addGroupListener(String group, DynamicConfigListener listener, boolean ifNotify) {
        return service.addGroupListener(group, listener, ifNotify);
    }

    @Override
    public boolean removeGroupListener(String group) {
        return service.removeGroupListener(group);
    }

    @Override
    public Optional<String> doGetConfig(String key, String group) {
        return service.doGetConfig(key, group);
    }

    @Override
    public boolean doPublishConfig(String key, String group, String content) {
        return service.doPublishConfig(key, group, content);
    }

    @Override
    public boolean doRemoveConfig(String key, String group) {
        return service.doRemoveConfig(key, group);
    }

    @Override
    public boolean doAddConfigListener(String key, String group, DynamicConfigListener listener) {
        return service.doAddConfigListener(key, group, listener);
    }

    @Override
    public boolean doRemoveConfigListener(String key, String group) {
        return service.doRemoveConfigListener(key, group);
    }

    @Override
    public List<String> doListKeysFromGroup(String group) {
        return service.doListKeysFromGroup(group);
    }

    @Override
    public boolean doAddGroupListener(String group, DynamicConfigListener listener) {
        return service.doAddGroupListener(group, listener);
    }

    @Override
    public boolean doRemoveGroupListener(String group) {
        return service.doRemoveGroupListener(group);
    }
}
