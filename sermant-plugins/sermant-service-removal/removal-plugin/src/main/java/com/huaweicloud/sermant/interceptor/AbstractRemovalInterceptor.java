/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.interceptor;

import com.huaweicloud.sermant.cache.InstanceCache;
import com.huaweicloud.sermant.cache.RuleCache;
import com.huaweicloud.sermant.config.RemovalConfig;
import com.huaweicloud.sermant.config.RemovalRule;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.entity.InstanceInfo;
import com.huaweicloud.sermant.entity.RemovalCountInfo;
import com.huaweicloud.sermant.service.RemovalEventService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 服务实例摘除抽象类
 *
 * @param <T> 实例信息
 * @author zhp
 * @since 2023-02-21
 */
public abstract class AbstractRemovalInterceptor<T> extends AbstractInterceptor {
    private static final RemovalConfig REMOVAL_CONFIG = PluginConfigManager.getConfig(RemovalConfig.class);

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final RemovalEventService removalEventService = ServiceManager.getService(RemovalEventService.class);

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (context.getResult() == null || !(context.getResult() instanceof List)) {
            return context;
        }
        List<T> instances = (List<T>) context.getResult();
        if (instances.size() == 0) {
            return context;
        }
        List<T> instanceList = removeInstance(instances);
        return context.changeResult(instanceList);
    }

    /**
     * 摘除已经处于摘除状态的实例
     *
     * @param servers 服务实例信息
     */
    protected void removeInstanceByStatus(List<T> servers) {
        if (servers == null || servers.size() == 0) {
            return;
        }
        RemovalCountInfo removalCountInfo = new RemovalCountInfo();
        for (Iterator<T> iterator = servers.iterator(); iterator.hasNext(); ) {
            InstanceInfo info = InstanceCache.INSTANCE_MAP.get(createKey(iterator.next()));
            if (info == null) {
                continue;
            }
            if (info.getRemovalStatus().get() && System.currentTimeMillis() > info.getRecoveryTime()
                    && info.getRemovalStatus().compareAndSet(true, false)) {
                LOGGER.info("The removal strength has reached the recovery time, and the removal is canceled");
                removalEventService.reportRecoveryEvent(info);
                continue;
            }
            if (info.getRemovalStatus().get()) {
                removalCountInfo.setRemovalCount(removalCountInfo.getRemovalCount() + 1);
                iterator.remove();
            }
        }
    }

    /**
     * 摘除实例
     *
     * @param instances 实例列表
     * @return 剩余的实例信息
     */
    protected List<T> removeInstance(List<T> instances) {
        List<T> instanceList = new ArrayList<>(instances);
        removeInstanceByStatus(instanceList);
        removeInstanceByRule(instances, instanceList);
        return instanceList;
    }

    /**
     * 摘除符合规则的实例信息
     *
     * @param instances 实例信息
     * @param instanceList 未处于摘除状态的实例
     */
    private void removeInstanceByRule(List<T> instances, List<T> instanceList) {
        Optional<RemovalRule> ruleOptional = RuleCache.getRule(getServiceKey(instances.get(0)));
        if (!ruleOptional.isPresent()) {
            return;
        }
        RemovalRule rule = ruleOptional.get();
        int removalCount = instances.size() - instanceList.size();
        float canRemovalNum = Math.min(instances.size() * rule.getScaleUpLimit() - removalCount,
                instances.size() - removalCount - rule.getMinInstanceNum());
        if (canRemovalNum <= 0) {
            return;
        }
        for (Iterator<T> iterator = instanceList.iterator(); iterator.hasNext(); ) {
            T instance = iterator.next();
            InstanceInfo info = InstanceCache.INSTANCE_MAP.get(createKey(instance));
            if (info == null || info.getCountDataList() == null || info.getCountDataList().size() == 0) {
                continue;
            }
            if (InstanceCache.isNeedRemoval(info, rule) && canRemovalNum >= 1
                    && info.getRemovalStatus().compareAndSet(false, true)) {
                LOGGER.info("The current instance is an abnormal instance, and the removal status is set to true");
                iterator.remove();
                info.setRemovalTime(System.currentTimeMillis());
                info.setRecoveryTime(System.currentTimeMillis() + REMOVAL_CONFIG.getRecoveryTimes());
                canRemovalNum--;
                removalEventService.reportRemovalEvent(info);
            }
        }
    }

    /**
     * 创建实例保存的key
     *
     * @param instance 服务实例信息
     * @return 实例key
     */
    protected abstract String createKey(T instance);

    /**
     * 获取实例key
     *
     * @param instance 服务实例信息
     * @return 实例key
     */
    protected abstract String getServiceKey(T instance);
}
