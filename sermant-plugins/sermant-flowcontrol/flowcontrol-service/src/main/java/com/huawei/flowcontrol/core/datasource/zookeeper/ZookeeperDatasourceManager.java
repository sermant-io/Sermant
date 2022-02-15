/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.core.datasource.zookeeper;

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.common.config.ConfigConst;
import com.huawei.flowcontrol.common.util.PluginConfigUtil;
import com.huawei.flowcontrol.core.datasource.DataSourceManager;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.heartbeat.ExtInfoProvider;
import com.huawei.sermant.core.service.heartbeat.HeartbeatService;

import com.alibaba.csp.sentinel.log.RecordLog;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * zookeeper流控规则加载
 * <h3>|保留用于适配原生的zookeeper路径|</h3>
 *
 * @author zhouss
 * @since 2022-02-11
 */
public class ZookeeperDatasourceManager implements DataSourceManager {
    /**
     * 默认的sentinel端口
     */
    private static final String DEFAULT_PORT = "8719";

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
            addExtraHeartBeatInfo();
        } catch (SentinelRpcException e) {
            RecordLog.error("[BootInterceptor] initRules loading failed=" + e);
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
            new ZookeeperCoreDataSource<FlowRule>(CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_FLOW, group,
                FlowRule.class);
        FlowRuleManager.register2Property(flowRuleZookeeperCoreDataSource.getProperty());
        dataSources.add(flowRuleZookeeperCoreDataSource);
    }

    private void initDegradeRule(String group) {
        // 熔断
        final ZookeeperCoreDataSource<DegradeRule> degradeRuleZookeeperCoreDataSource =
            new ZookeeperCoreDataSource<DegradeRule>(CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_DEGRADE, group,
                DegradeRule.class);
        DegradeRuleManager.register2Property(degradeRuleZookeeperCoreDataSource.getProperty());
        dataSources.add(degradeRuleZookeeperCoreDataSource);
    }

    private void initSystemRule(String group) {
        // 系统
        final ZookeeperCoreDataSource<SystemRule> systemRuleZookeeperCoreDataSource =
            new ZookeeperCoreDataSource<SystemRule>(CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_SYSTEM, group,
                SystemRule.class);
        SystemRuleManager.register2Property(systemRuleZookeeperCoreDataSource.getProperty());
        dataSources.add(systemRuleZookeeperCoreDataSource);
    }

    private void initAuthorityRule(String group) {
        // 授权
        final ZookeeperCoreDataSource<AuthorityRule> authorityRuleZookeeperCoreDataSource =
            new ZookeeperCoreDataSource<AuthorityRule>(CommonConst.SLASH_SIGN + CommonConst.SENTINEL_RULE_AUTHORITY,
                group, AuthorityRule.class);
        AuthorityRuleManager.register2Property(authorityRuleZookeeperCoreDataSource.getProperty());
        dataSources.add(authorityRuleZookeeperCoreDataSource);
    }

    private void addExtraHeartBeatInfo() {
        final HeartbeatService service = ServiceManager.getService(HeartbeatService.class);
        service.setExtInfo(new ExtInfoProvider() {
            @Override
            public Map<String, String> getExtInfo() {
                return Collections.singletonMap("port", DEFAULT_PORT);
            }
        });
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
}
