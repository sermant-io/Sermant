/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.resolver;

import com.huawei.flowcontrol.adapte.cse.constants.CseConstants;
import com.huawei.flowcontrol.adapte.cse.converter.Converter;
import com.huawei.flowcontrol.adapte.cse.converter.YamlConverter;
import com.huawei.flowcontrol.adapte.cse.entity.CseServiceMeta;
import com.huawei.flowcontrol.adapte.cse.rule.Configurable;
import com.huawei.flowcontrol.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 抽象解析类
 *
 * @author zhouss
 * @since 2021-11-16
 */
public abstract class AbstractResolver<T extends Configurable> {
    /**
     * 各类规则配置前缀
     */
    private String configKey;

    /**
     * 规则数据
     * map<业务场景名, 规则数据>
     */
    private Map<String, T> rules;

    /**
     * 转换器
     */
    private Converter<String, T> converter;

    public AbstractResolver() {

    }

    public AbstractResolver(String configKey) {
        this.configKey = configKey;
        // 线上SC目前治理策略仅支持yaml格式配置
        this.converter = new YamlConverter<T>(getRuleClass());
        rules = new HashMap<String, T>();
    }

    /**
     * 转换规则
     *
     * @param rulesMap 更新的规则列表
     */
    public void parseRules(Map<String, String> rulesMap) {
        final String configKeyPrefix = getConfigKeyPrefix();
        for (Map.Entry<String, String> ruleEntity : rulesMap.entrySet()) {
            String key = ruleEntity.getKey();
            if (StringUtils.isEmpty(key) || !key.startsWith(configKeyPrefix)) {
                continue;
            }
            String businessKey = key.substring(configKeyPrefix.length());
            final T rule = parseRule(businessKey, ruleEntity.getValue(), false);
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
     * @return 转换后的规则
     */
    public T parseRule(String businessKey, String value, boolean override) {
        if (StringUtils.isEmpty(businessKey) || StringUtils.isEmpty(value)) {
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
        if (!servicesMatch(rule.getServices())) {
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

    public String getConfigKeyPrefix() {
        return configKey + ".";
    }

    /**
     * 匹配服务名与版本
     * 此处版本需拦截sdk获取
     *
     * @param services 服务，多个服务逗号隔开
     * @return 是否匹配版本
     */
    private boolean servicesMatch(String services) {
        if (StringUtils.isEmpty(services)) {
            return true;
        }
        for (String service : services.split(CseConstants.SERVICE_SEPARATOR)) {
            String[] serviceAndVersion = service.split(CseConstants.SERVICE_VERSION_SEPARATOR);
            // 服务名匹配
            if (serviceAndVersion.length == 1 && serviceAndVersion[0].equals(CseServiceMeta.getInstance().getService())) {
                return true;
            }
            // 服务加版本匹配
            if (serviceAndVersion.length == 2 && serviceAndVersion[0].equals(CseServiceMeta.getInstance().getService())
                    && serviceAndVersion[1].equals(CseServiceMeta.getInstance().getVersion())) {
                return true;
            }
        }
        return false;
    }

    public Map<String, T> getRules() {
        return rules;
    }
}
