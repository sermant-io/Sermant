/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse;


import com.huawei.flowcontrol.adapte.cse.resolver.AbstractResolver;

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
     * @param forDelete 是否是为了移除场景
     */
    public void resolve(Map<String, String> rulesMap, boolean forDelete) {
        final Set<Map.Entry<String, AbstractResolver<?>>> resolvers = resolversMap.entrySet();
        for (Map.Entry<String, String> ruleEntity : rulesMap.entrySet()) {
            final String key = ruleEntity.getKey();
            for (Map.Entry<String, AbstractResolver<?>> resolverEntry : resolvers) {
                if (!key.startsWith(resolverEntry.getKey())) {
                    continue;
                }
                String businessKey = key.substring(resolverEntry.getKey().length());
                // 匹配以该配置打头的解析器，更新解析器内容
                resolverEntry.getValue().parseRule(businessKey, ruleEntity.getValue(), true, forDelete);
            }
        }
    }

    /**
     * 解析配置
     *
     * @param rulesMap 配置中心获取的规则数据
     */
    public void resolve(Map<String, String> rulesMap) {
        resolve(rulesMap, false);
    }

    public Map<String, AbstractResolver<?>> getResolversMap() {
        return resolversMap;
    }

    public AbstractResolver getResolver(String configKey) {
        return resolversMap.get(configKey);
    }

    private void loadSpiResolvers() {
        for (AbstractResolver<?> resolver : ServiceLoader.load(AbstractResolver.class)) {
            final String configKeyPrefix = resolver.getConfigKeyPrefix();
            if (".".equals(configKeyPrefix)) {
                // 空配置跳过
                continue;
            }
            resolversMap.put(configKeyPrefix, resolver);
        }
    }

}
