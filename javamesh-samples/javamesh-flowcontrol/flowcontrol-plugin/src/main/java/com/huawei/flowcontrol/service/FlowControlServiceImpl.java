package com.huawei.flowcontrol.service;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.plugin.service.PluginService;
import com.huawei.apm.core.service.dynamicconfig.Config;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigType;
import com.huawei.flowcontrol.adapte.cse.KieConfigSyncer;
import com.huawei.flowcontrol.core.FlowControlThreadFactory;
import com.huawei.flowcontrol.core.config.CommonConst;
import com.huawei.flowcontrol.core.config.ConfigConst;
import com.huawei.flowcontrol.core.init.InitExecutor;
import com.huawei.flowcontrol.core.init.InitRuleRedis;
import com.huawei.flowcontrol.core.util.DataSourceInitUtils;
import com.huawei.flowcontrol.core.util.PluginConfigUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 流控插件初始化
 *
 * @author zhouss
 * @since 2021-11-20
 */
public class FlowControlServiceImpl implements PluginService {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
        new FlowControlThreadFactory("FLOW_CONTROL_INIT_THREAD"));

    private static KieConfigSyncer configSyncer;

    @Override
    public void start() {
        executorService.execute(new FlowControlInitTask());
    }

    @Override
    public void stop() {
        executorService.shutdown();
        if (configSyncer != null) {
            configSyncer.stop();
        }
        DataSourceInitUtils.stop();
    }

    static class FlowControlInitTask implements Runnable {
        @Override
        public void run() {
            try {
                // 开启定时任务（发送心跳和监控数据）
                InitExecutor.doInit();
                // 获取启动参数，判断是否备份流控规则
                String flag = System.getProperty(CommonConst.REDIS_RULE_STORE);
                if (Boolean.TRUE.toString().equals(flag)) {
                    InitRuleRedis.doInit();
                }
            } catch (Exception e) {
                LogFactory.getLogger().warning(String.format("Init Flow control plugin failed, {%s}", e));
            }
            final String configCenterType = PluginConfigUtil.getValueByKey(ConfigConst.CONFIG_CENTER_TYPE);
            DataSourceInitUtils.initRules(configCenterType);
            tryStartCseKieConfigSyncer(configCenterType);

        }

        private void tryStartCseKieConfigSyncer(String configCenterType) {
            if (CommonConst.CONFIG_TYPE_CSE_KIE.equals(configCenterType)
                    && Config.getDynamic_config_type() == DynamicConfigType.KIE) {
                configSyncer = new KieConfigSyncer();
                configSyncer.start();
            }
        }
    }
}
