/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.datasource.kie;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.huawei.flowcontrol.core.datasource.DataSourceManager;
import com.huawei.flowcontrol.core.datasource.kie.rule.RuleCenter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流控规则初始化
 *
 * @author hanpeng
 * @since 2020-11-12
 */
public class KieDataSourceManager implements DataSourceManager {
    /**
     * 默认规则文件名
     */
    private static final String DEFAULT_RULE_FILE = "default-rules.json";

    /**
     * 规则map
     */
    private final Map<String, String> rulesMap = new ConcurrentHashMap<String, String>();

    /**
     * 数据源map
     */
    private final Map<String, KieDataSource<?>> sourceMap = new ConcurrentHashMap<String, KieDataSource<?>>();

    /**
     * 规则中心
     */
    private final RuleCenter ruleCenter = new RuleCenter();

    @Override
    public void initRules() {
        // 初始化默认规则
        initDefaultRules();

        // 初始化数据源
        initDataSources();

        // 注册规则管理器
        registerRuleManager();
    }

    private void initDefaultRules() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_RULE_FILE);
        if (inputStream != null) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                int buf;
                while ((buf = inputStream.read()) != -1) {
                    stringBuilder.append((char) buf);
                }
            } catch (IOException e) {
                RecordLog.error(String.format("Get default rule file (%s) failed.", DEFAULT_RULE_FILE), e);
                return;
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    RecordLog.error("Input stream close failed when init kie default rules", e);
                }
            }

            JSONObject jsonObject = JSON.parseObject(stringBuilder.toString());
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                rulesMap.put(entry.getKey(), entry.getValue().toString());
            }
        } else {
            RecordLog.warn(String.format("Default rule file (%s) is not exist.", DEFAULT_RULE_FILE));
        }
    }

    private void initDataSources() {
        for (String ruleType : ruleCenter.getRuleTypes()) {
            String defaultRule = null;
            for (Map.Entry<String, String> entry : rulesMap.entrySet()) {
                if (ruleType.equals(entry.getKey())) {
                    defaultRule = entry.getValue();
                    break;
                }
            }

            Class<?> ruleClass = ruleCenter.getRuleClass(ruleType);
            KieDataSource<?> kieDataSource = getKieDataSource(ruleType, defaultRule, ruleClass);
            sourceMap.put(ruleType, kieDataSource);
        }
    }

    private <T> KieDataSource<List<T>> getKieDataSource(String ruleKey, String ruleValue, Class<T> ruleClass) {
        final TypeReference<List<T>> typeReference = new KieTypeReference<List<T>>(ruleClass);
        Converter<String, List<T>> parser = new KieDataSourceConverter<List<T>>(typeReference);
        return new KieDataSource<List<T>>(parser, ruleKey, ruleValue);
    }

    private void registerRuleManager() {
        for (Map.Entry<String, KieDataSource<?>> entry : sourceMap.entrySet()) {
            ruleCenter.registerRuleManager(entry.getKey(), entry.getValue());
        }
    }

    static class KieDataSourceConverter<T> implements Converter<String, T> {
        private final TypeReference<T> typeReference;

        KieDataSourceConverter(TypeReference<T> typeReference) {
            this.typeReference = typeReference;
        }

        @Override
        public T convert(String source) {
            return JSON.parseObject(source, typeReference);
        }
    }

    static class KieTypeReference<T> extends TypeReference<T> {

        public KieTypeReference(Type... actualTypeArguments) {
            super(actualTypeArguments);
        }
    }
}
