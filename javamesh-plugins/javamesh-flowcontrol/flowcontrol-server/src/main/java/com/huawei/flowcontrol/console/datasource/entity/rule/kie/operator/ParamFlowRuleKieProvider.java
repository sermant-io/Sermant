/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator;

import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.KieConfigClient;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigItem;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.util.KieConfig;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.util.KieConfigUtil;
import com.huawei.flowcontrol.console.entity.ParamFlowRuleVo;
import org.springframework.stereotype.Component;

/**
 * kie配置中心热点参数规则查询类
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Component("paramFlowRuleKieProvider")
public class ParamFlowRuleKieProvider extends CommonKieRuleProvider<ParamFlowRule, ParamFlowRuleVo> {

    public ParamFlowRuleKieProvider(KieConfigClient kieConfigClient, KieConfig kieConfig) {
        super(kieConfigClient, kieConfig, "ParamFlowRule");
    }

    @Override
    protected ParamFlowRuleVo getRuleVo(KieConfigItem item, String app) {
        ParamFlowRule paramFlowRule = KieConfigUtil.parseKieConfig(ParamFlowRule.class, item);
        return new ParamFlowRuleVo(paramFlowRule);
    }
}
