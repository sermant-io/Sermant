/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator;

import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.KieConfigClient;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigItem;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.util.KieConfig;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.util.KieConfigUtil;
import com.huawei.flowcontrol.console.entity.SystemRuleVo;
import org.springframework.stereotype.Component;

/**
 * kie配置中心系统规则查询类
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Component("systemRuleKieProvider")
public class SystemRuleKieProvider extends CommonKieRuleProvider<SystemRule, SystemRuleVo> {

    public SystemRuleKieProvider(KieConfigClient kieConfigClient, KieConfig kieConfig) {
        super(kieConfigClient, kieConfig, "SystemRule");
    }

    @Override
    protected SystemRuleVo getRuleVo(KieConfigItem item, String app) {
        SystemRule rule = KieConfigUtil.parseKieConfig(SystemRule.class, item);
        return SystemRuleVo.fromSystemRule(app, null, null, rule);
    }
}
