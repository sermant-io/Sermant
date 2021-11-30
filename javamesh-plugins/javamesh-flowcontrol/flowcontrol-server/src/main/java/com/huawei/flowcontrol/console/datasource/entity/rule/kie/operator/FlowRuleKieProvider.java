/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.KieConfigClient;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigItem;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.util.KieConfig;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.util.KieConfigUtil;
import com.huawei.flowcontrol.console.entity.FlowRuleVo;
import org.springframework.stereotype.Component;

/**
 * kie配置中心限流规则查询类
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Component("flowRuleKieProvider")
public class FlowRuleKieProvider extends CommonKieRuleProvider<FlowRule, FlowRuleVo> {

    public FlowRuleKieProvider(KieConfigClient kieConfigClient, KieConfig kieConfig) {
        super(kieConfigClient, kieConfig, "FlowRule");
    }

    @Override
    protected FlowRuleVo getRuleVo(KieConfigItem item, String app) {
        FlowRule flowRule = KieConfigUtil.parseKieConfig(FlowRule.class, item);
        return FlowRuleVo.fromFlowRule(app, null, null, flowRule);
    }

}
