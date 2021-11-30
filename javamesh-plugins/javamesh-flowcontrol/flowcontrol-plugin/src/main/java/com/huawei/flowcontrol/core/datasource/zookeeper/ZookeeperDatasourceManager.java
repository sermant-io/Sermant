/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.datasource.zookeeper;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.huawei.javamesh.core.plugin.config.PluginConfigManager;
import com.huawei.javamesh.core.service.dynamicconfig.Config;
import com.huawei.flowcontrol.core.config.CommonConst;
import com.huawei.flowcontrol.core.config.ConfigConst;
import com.huawei.flowcontrol.core.config.FlowControlConfig;
import com.huawei.flowcontrol.core.datasource.DataSourceManager;
import com.huawei.flowcontrol.core.util.PluginConfigUtil;
import com.huawei.flowcontrol.core.util.RedisRuleUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * zookeeper流控规则加载
 * <h3>|保留用于适配原生的zookeeper路径|</h3>
 *
 * @author liyi
 * @since 2020-08-26
 */
public class ZookeeperDatasourceManager implements DataSourceManager {
    private final List<ZookeeperCoreDataSource<?>> dataSources = new ArrayList<ZookeeperCoreDataSource<?>>();

    /**
     * 初始化规则
     */
    @Override
    public void start() {
        try {
            RecordLog.info("initRules begin....");
            String rootPath = fixRootPath(PluginConfigUtil.getValueByKey(ConfigConst.ZOOKEEPER_PATH));
            String appName = AppNameUtil.getAppName();
            String group = getGroupId(rootPath, appName);
            initFlowRule(group);
            initDegradeRule(group);
            initAuthorityRule(group);
            initSystemRule(group);
            RecordLog.info("initRules end");
        } catch (SentinelRpcException e) {
            RecordLog.error("[BootInterceptor] initRules loading failed=" + e);
        } finally {
            if (FlowRuleManager.getRules().size() == 0) {
                // 判断后执行加载redis配置文件
                RedisRuleUtil.loadRules();
            }
        }
    }

    @Override
    public void stop() {
        for (ZookeeperCoreDataSource<?> dataSource : dataSources) {
            dataSource.removeListener();
        }
    }

    private void initFlowRule(String group) {
        // 流控
        final ZookeeperCoreDataSource<FlowRule> flowRuleZookeeperCoreDataSource =
                new ZookeeperCoreDataSource<FlowRule>(CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_FLOW, group, FlowRule.class);
        FlowRuleManager.register2Property(flowRuleZookeeperCoreDataSource.getProperty());
        dataSources.add(flowRuleZookeeperCoreDataSource);
    }

    private void initDegradeRule(String group) {
        // 熔断
        final ZookeeperCoreDataSource<DegradeRule> degradeRuleZookeeperCoreDataSource =
                new ZookeeperCoreDataSource<DegradeRule>(CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_DEGRADE, group, DegradeRule.class);
        DegradeRuleManager.register2Property(degradeRuleZookeeperCoreDataSource.getProperty());
        dataSources.add(degradeRuleZookeeperCoreDataSource);
    }

    private void initSystemRule(String group) {
        // 系统
        final ZookeeperCoreDataSource<SystemRule> systemRuleZookeeperCoreDataSource =
                new ZookeeperCoreDataSource<SystemRule>(CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_SYSTEM, group, SystemRule.class);
        SystemRuleManager.register2Property(systemRuleZookeeperCoreDataSource.getProperty());
        dataSources.add(systemRuleZookeeperCoreDataSource);
    }

    private void initAuthorityRule(String group) {
        // 授权
        final ZookeeperCoreDataSource<AuthorityRule> authorityRuleZookeeperCoreDataSource =
                new ZookeeperCoreDataSource<AuthorityRule>(CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_AUTHORITY, group, AuthorityRule.class);
        AuthorityRuleManager.register2Property(authorityRuleZookeeperCoreDataSource.getProperty());
        dataSources.add(authorityRuleZookeeperCoreDataSource);
    }

    private String getZookeeperAddress() {
        final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        if (pluginConfig.isUseSelfUrl()) {
            return PluginConfigUtil.getValueByKey(ConfigConst.ZOOKEEPER_ADDRESS);
        } else {
            return convertAddress(Config.getZookeeperUri());
        }
    }

    private String convertAddress(String uri) {
        String prefix = "zookeeper://";
        if (uri == null || !uri.startsWith(prefix)) {
            return CommonConst.DEFAULT_ZOOKEEPER_ADDRESS;
        } else {
            return uri.substring(prefix.length() + 1);
        }
    }

    private String fixRootPath(String rootPath) {
        if (rootPath == null || "".equals(rootPath.trim())) {
            return "";
        }
        return rootPath.startsWith(CommonConst.SLASH_SIGN) ? rootPath.substring(1) : rootPath;
    }

    private String getGroupId(String rootPath, String appName) {
        if ("".equals(rootPath)) {
            return appName;
        }
        return rootPath + CommonConst.SLASH_SIGN + appName;
    }

    private void loadAuthorityRule(String remoteAddress, String rootPath, String appName) {
        // 加载授权规则
        String group = getGroupId(rootPath, appName);
        // 加载授权规则
        String authorityRulePath =
            CommonConst.SLASH_SIGN + group + CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_AUTHORITY;
        RecordLog.info("the authorityRule path in zookeeper =" + authorityRulePath);
        ReadableDataSource<String, List<AuthorityRule>> authorityRuleDataSource =
            new ZookeeperDataSource<List<AuthorityRule>>(
                remoteAddress,
                authorityRulePath,
                new ZookeeperDataSourceConverter<List<AuthorityRule>>(
                    new FlowControlTypeReference<AuthorityRule>(AuthorityRule.class)));
        AuthorityRuleManager.register2Property(authorityRuleDataSource.getProperty());
        printlnRules("AuthorityRule", AuthorityRuleManager.getRules());
    }

    private void loadSystemRule(String remoteAddress, String rootPath, String appName) {
        // 加载系统规则
        String group = getGroupId(rootPath, appName);
        String systemRulePath =
            CommonConst.SLASH_SIGN + group + CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_SYSTEM;
        RecordLog.info("The systemRule path in zookeeper =" + systemRulePath);
        ReadableDataSource<String, List<SystemRule>> systemRuleDataSource =
            new ZookeeperDataSource<List<SystemRule>>(
                remoteAddress,
                systemRulePath,
                new ZookeeperDataSourceConverter<List<SystemRule>>(
                    new FlowControlTypeReference<SystemRule>(SystemRule.class)));
        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
        printlnRules("SystemRule", SystemRuleManager.getRules());
    }

    private void loadDegradeRule(String remoteAddress, String rootPath, String appName) {
        // 加载降级规则
        String group = getGroupId(rootPath, appName);
        String degradeRulePath =
            CommonConst.SLASH_SIGN + group + CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_DEGRADE;
        RecordLog.info("The degradeRule path in zookeeper =" + degradeRulePath);
        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource =
            new ZookeeperDataSource<List<DegradeRule>>(
                remoteAddress,
                degradeRulePath,
                new ZookeeperDataSourceConverter<List<DegradeRule>>(
                    new FlowControlTypeReference<DegradeRule>(DegradeRule.class)));
        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
        printlnRules("DegradeRule", DegradeRuleManager.getRules());
    }

    private void loadFlowRule(String remoteAddress, String rootPath, String appName) {
        // 加载流控规则
        String group = getGroupId(rootPath, appName);
        String flowRulePath =
            CommonConst.SLASH_SIGN + group + CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_FLOW;
        RecordLog.info("The flowRule path in zookeeper =" + flowRulePath);
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource =
            new ZookeeperDataSource<List<FlowRule>>(
                remoteAddress,
                flowRulePath,
                new ZookeeperDataSourceConverter<List<FlowRule>>(
                    new FlowControlTypeReference<FlowRule>(FlowRule.class)));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
        printlnRules("FlowRule", FlowRuleManager.getRules());
    }

    private <T extends AbstractRule> void printlnRules(String ruleTypeName, List<T> rules) {
        for (AbstractRule rule : rules) {
            RecordLog.info(String.format("%s has : %s", ruleTypeName, rule.getResource()));
        }
    }

    static class ZookeeperDataSourceConverter<T> implements Converter<String, T> {
        private final TypeReference<T> typeReference;

        ZookeeperDataSourceConverter(TypeReference<T> typeReference) {
            this.typeReference = typeReference;
        }

        @Override
        public T convert(String source) {
            return JSON.parseObject(source, typeReference);
        }
    }

    static class FlowControlTypeReference<T> extends TypeReference<List<T>> {
        public FlowControlTypeReference(Type... actualTypeArguments) {
            super(actualTypeArguments);
        }
    }
}
