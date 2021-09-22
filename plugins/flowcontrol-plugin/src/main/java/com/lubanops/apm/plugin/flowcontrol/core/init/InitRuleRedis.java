/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.init;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.lubanops.apm.plugin.flowcontrol.core.config.CommonConst;
import com.lubanops.apm.plugin.flowcontrol.core.config.ConfigConst;
import com.lubanops.apm.plugin.flowcontrol.core.util.LettuceUtils;
import com.lubanops.apm.plugin.flowcontrol.core.util.PluginConfigUtil;
import com.lubanops.apm.plugin.flowcontrol.core.util.ZookeeperConnectionEnum;
import org.apache.curator.framework.CuratorFramework;

/**
 * 从注册中心获取sentinel规则存储到本地redis
 *
 * @author liyi
 * @since 2020-08-26
 */
public class InitRuleRedis {
    private InitRuleRedis() {
    }

    /**
     * 开启线程执行初始化任务
     */
    public static void doInit() {
        new RedisTask().run();
    }

    /**
     * redis任务，用于备份流控规则到redis
     *
     * @author liyi
     * @since 2020-08-26
     */
    private static class RedisTask {
        public void run() {
            CuratorFramework client = ZookeeperConnectionEnum.INSTANCE.getZookeeperConnection();
            LettuceUtils redisUtils = new LettuceUtils();
            try {
                String path;
                try {
                    // 加载流控规则
                    path = getCommonPath() + CommonConst.SENTINEL_RULE_FLOW;
                    String flowValue = new String(client.getData().forPath(path));
                    String flowKey = getCommonKey() + CommonConst.SENTINEL_RULE_FLOW;
                    redisUtils.set(flowKey, flowValue);
                } catch (Exception e) {
                    RecordLog.error("[InitRuleRedis] failed to load flowRule backup to redis=" + e);
                }
                try {
                    // 加载降级规则
                    path = getCommonPath() + CommonConst.SENTINEL_RULE_DEGRADE;
                    String degradeValue = new String(client.getData().forPath(path));
                    String degradeKey = getCommonKey() + CommonConst.SENTINEL_RULE_DEGRADE;
                    redisUtils.set(degradeKey, degradeValue);
                } catch (Exception e) {
                    RecordLog.error("[InitRuleRedis] failed to load degradeRule backup to redis=" + e);
                }
                try {
                    // 加载系统规则
                    path = getCommonPath() + CommonConst.SENTINEL_RULE_SYSTEM;
                    String systemRuleValue = new String(client.getData().forPath(path));
                    String systemRuleKey = getCommonKey() + CommonConst.SENTINEL_RULE_SYSTEM;
                    redisUtils.set(systemRuleKey, systemRuleValue);
                } catch (Exception e) {
                    RecordLog.error("[InitRuleRedis] failed to load systemRule backup to redis=" + e);
                }
                try {
                    // 记载授权规则
                    path = getCommonPath() + CommonConst.SENTINEL_RULE_AUTHORITY;
                    String authorityRuleValue = new String(client.getData().forPath(path));
                    String authorityRuleKey = getCommonKey() + CommonConst.SENTINEL_RULE_AUTHORITY;
                    redisUtils.set(authorityRuleKey, authorityRuleValue);
                } catch (Exception e) {
                    RecordLog.error("[InitRuleRedis] failed to load authorityRule backup to redis=" + e);
                }
            } finally {
                redisUtils.close();
            }
        }

        private String getCommonPath() {
            String rootPath = PluginConfigUtil.getValueByKey(ConfigConst.SENTINEL_ZOOKEEPER_PATH);
            String appName = AppNameUtil.getAppName();
            return rootPath + CommonConst.SLASH_SIGN
                + appName + CommonConst.SLASH_SIGN;
        }

        private String getCommonKey() {
            String appName = AppNameUtil.getAppName();
            return CommonConst.SENTINEL + CommonConst.COLON_SIGN
                + appName + CommonConst.COLON_SIGN;
        }
    }
}
