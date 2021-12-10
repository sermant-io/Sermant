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

package com.huawei.flowcontrol.adapte.cse.resolver;

import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.flowcontrol.adapte.cse.constants.CseConstants;
import com.huawei.flowcontrol.adapte.cse.converter.Converter;
import com.huawei.flowcontrol.adapte.cse.converter.YamlConverter;
import com.huawei.flowcontrol.adapte.cse.entity.CseServiceMeta;
import com.huawei.flowcontrol.adapte.cse.resolver.listener.ConfigUpdateListener;
import com.huawei.flowcontrol.adapte.cse.rule.Configurable;
import com.huawei.flowcontrol.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 抽象解析类
 *
 * @author zhouss
 * @since 2021-11-16
 */
public abstract class AbstractResolver<T extends Configurable> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 各类规则配置前缀
     */
    private final String configKey;

    /**
     * 规则数据
     * map<业务场景名, 规则数据>
     */
    private final Map<String, T> rules;

    /**
     * 配置更新监听
     * 进行解析后再通知
     */
    private final List<ConfigUpdateListener<T>> listeners = new ArrayList<ConfigUpdateListener<T>>();

    /**
     * 转换器
     */
    private final Converter<String, T> converter;

    public AbstractResolver(String configKey) {
        this.configKey = configKey;
        // 线上SC目前治理策略仅支持yaml格式配置
        this.converter = new YamlConverter<T>(getRuleClass());
        rules = new HashMap<String, T>();
    }

    /**
     * 注册监听器
     *
     * @param listener 监听器
     */
    public synchronized void registerListener(ConfigUpdateListener<T> listener) {
        listeners.add(listener);
    }

    /**
     * 配置更新通知
     */
    public void notifyListeners() {
        for (ConfigUpdateListener<T> listener : listeners) {
            try {
                listener.notify(rules);
            } catch (Exception ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Notified listener failed when updating rule! %s",
                        ex.getMessage()));
            }
        }
    }

    /**
     * 转换规则
     *
     * @param rulesMap 更新的规则列表
     */
    public void parseRules(Map<String, String> rulesMap) {
        final String configKeyPrefix = getConfigKeyPrefix(configKey);
        for (Map.Entry<String, String> ruleEntity : rulesMap.entrySet()) {
            String key = ruleEntity.getKey();
            if (StringUtils.isEmpty(key) || !key.startsWith(configKeyPrefix)) {
                continue;
            }
            String businessKey = key.substring(configKeyPrefix.length());
            final T rule = parseRule(businessKey, ruleEntity.getValue(), false, false);
            if (rule != null) {
                rules.put(businessKey, rule);
            } else {
                rules.remove(businessKey);
            }
        }
    }

    /**
     * 格式化规则
     *
     * @param businessKey 业务场景名
     * @param value       业务规则
     * @param override 是否覆盖规则， 用于单个业务场景更新时
     * @param forDelete 为了删除的场景，则直接移除该业务配置
     * @return 转换后的规则
     */
    public T parseRule(String businessKey, String value, boolean override, boolean forDelete) {
        if (StringUtils.isEmpty(businessKey)) {
            return null;
        }
        if (forDelete) {
            rules.remove(businessKey);
            return null;
        }
        // 值为空场景，用户删除了该业务场景名
        if (StringUtils.isEmpty(value) && override) {
            rules.remove(businessKey);
            return null;
        }
        // 1、移除旧的配置
        rules.remove(businessKey);
        // 2、转换配置
        final T rule = converter.convert(value);
        if (rule == null) {
            return null;
        }
        // 3、设置名称以及服务名
        rule.setName(businessKey);
        // 4、判断规则是否合法
        if (rule.isValid()) {
            return null;
        }
        if (!isServicesMatch(rule.getServices())) {
            return null;
        }
        if (override) {
            rules.put(businessKey, rule);
        }
        return rule;
    }

    /**
     * 获取规则实体类型
     *
     * @return 类型
     */
    protected abstract Class<T> getRuleClass();

    public static String getConfigKeyPrefix(String configKey) {
        return configKey + ".";
    }

    /**
     * 匹配服务名与版本
     * 此处版本需拦截sdk获取
     *
     * @param services 服务，多个服务逗号隔开
     * @return 是否匹配版本
     */
    private boolean isServicesMatch(String services) {
        if (StringUtils.isEmpty(services)) {
            return true;
        }
        for (String service : services.split(CseConstants.SERVICE_SEPARATOR)) {
            String[] serviceAndVersion = service.split(CseConstants.SERVICE_VERSION_SEPARATOR);
            // 服务名匹配
            if (serviceAndVersion.length == 1 && serviceAndVersion[0].equals(CseServiceMeta.getInstance().getServiceName())) {
                return true;
            }
            // 服务加版本匹配
            if (serviceAndVersion.length == 2 && serviceAndVersion[0].equals(CseServiceMeta.getInstance().getServiceName())
                    && serviceAndVersion[1].equals(CseServiceMeta.getInstance().getVersion())) {
                return true;
            }
        }
        return false;
    }

    public String getConfigKey() {
        return configKey;
    }

    public Map<String, T> getRules() {
        return rules;
    }
}
