/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.adapte.cse;

import com.huawei.flowcontrol.common.adapte.cse.match.MatchGroupResolver;
import com.huawei.flowcontrol.common.adapte.cse.resolver.AbstractResolver;
import com.huawei.flowcontrol.common.adapte.cse.resolver.listener.ConfigUpdateListener;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * 解析器管理
 *
 * @author zhouss
 * @since 2021-11-16
 */
public enum ResolverManager {
    /**
     * 单例
     */
    INSTANCE;

    /**
     * 基于SPI加载所有Resolver
     */
    private final Map<String, AbstractResolver<?>> resolversMap = new HashMap<String, AbstractResolver<?>>();

    ResolverManager() {
        loadSpiResolvers();
    }

    /**
     * 解析配置
     *
     * @param rulesMap 配置中心获取的规则数据
     */
    public void resolve(Map<String, String> rulesMap) {
        resolve(rulesMap, false);
    }

    /**
     * 解析配置
     *
     * @param rulesMap 配置中心获取的规则数据
     * @param isForDelete 是否是为了移除场景
     */
    public void resolve(Map<String, String> rulesMap, boolean isForDelete) {
        for (Map.Entry<String, String> ruleEntity : rulesMap.entrySet()) {
            final String key = ruleEntity.getKey();
            resolve(key, ruleEntity.getValue(), isForDelete);
        }
    }

    /**
     * 单个kv解析
     *
     * @param key 键
     * @param value 值
     * @param isForDelete 是否是删除键
     */
    public void resolve(String key, String value, boolean isForDelete) {
        final Set<Map.Entry<String, AbstractResolver<?>>> resolvers = resolversMap.entrySet();
        for (Map.Entry<String, AbstractResolver<?>> resolverEntry : resolvers) {
            if (!key.startsWith(resolverEntry.getKey())) {
                continue;
            }
            String businessKey = key.substring(resolverEntry.getKey().length());

            // 匹配以该配置打头的解析器，更新解析器内容
            resolverEntry.getValue().parseRule(businessKey, value, true, isForDelete);
            resolverEntry.getValue().notifyListeners(businessKey);
        }
    }

    /**
     * 判断是否由对应的业务场景规则
     *
     * @param businessKey 业务场景名
     * @return boolean
     */
    public boolean hasMatchedRule(String businessKey) {
        final String matchGroupKey = AbstractResolver.getConfigKeyPrefix(MatchGroupResolver.CONFIG_KEY);
        for (Map.Entry<String, AbstractResolver<?>> entry : resolversMap.entrySet()) {
            if (entry.getKey().equals(matchGroupKey)) {
                continue;
            }
            if (entry.getValue().getRules().containsKey(businessKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 注册监听器
     *
     * @param configKey 监听的规则类型
     * @param listener 监听器
     */
    public void registerListener(String configKey, ConfigUpdateListener listener) {
        String configKeyPrefix = AbstractResolver.getConfigKeyPrefix(configKey);
        final AbstractResolver<?> abstractResolver = resolversMap.get(configKeyPrefix);
        if (abstractResolver != null) {
            abstractResolver.registerListener(listener);
        }
    }

    public Map<String, AbstractResolver<?>> getResolversMap() {
        return resolversMap;
    }

    public <R extends AbstractResolver<?>> R getResolver(String configKey) {
        return (R) resolversMap.get(AbstractResolver.getConfigKeyPrefix(configKey));
    }

    private void loadSpiResolvers() {
        for (AbstractResolver<?> resolver : ServiceLoader.load(AbstractResolver.class,
                ResolverManager.class.getClassLoader())) {
            final String configKeyPrefix = AbstractResolver.getConfigKeyPrefix(resolver.getConfigKey());
            if (".".equals(configKeyPrefix)) {
                // 空配置跳过
                continue;
            }
            resolversMap.put(configKeyPrefix, resolver);
        }
    }
}
