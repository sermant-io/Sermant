package com.huawei.flowcontrol.service;

import com.huawei.apm.bootstrap.boot.PluginService;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.flowcontrol.core.FlowControlThreadFactory;
import com.huawei.flowcontrol.core.config.CommonConst;
import com.huawei.flowcontrol.core.init.InitExecutor;
import com.huawei.flowcontrol.core.init.InitRuleRedis;
import com.huawei.flowcontrol.core.util.InitRulesUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 流控插件初始化
 */
public class FlowControlService implements PluginService {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
        new FlowControlThreadFactory("FLOW_CONTROL_INIT_THREAD"));

    @Override
    public void init() {
        executorService.execute(new FlowControlInitTask());
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }

    static class FlowControlInitTask implements Runnable {
        @Override
        public void run() {
            try {
                // 开启定时任务（发送心跳和监控数据）
                InitExecutor.doInit();
                // 获取启动参数，判断是否备份流控规则
                String flag = System.getProperty(CommonConst.REDIS_RULE_STORE);
                if ("true".equals(flag)) {
                    InitRuleRedis.doInit();
                }
            } catch (Exception e) {
                LogFactory.getLogger().warning(String.format("Init Flow control plugin failed, {%s}", e));
            }
            InitRulesUtils.initRules();
        }
    }
}
