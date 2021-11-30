/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.KieConfigClient;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigItem;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.util.KieConfig;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.util.KieConfigUtil;
import com.huawei.flowcontrol.console.entity.DegradeRuleVo;
import org.springframework.stereotype.Component;

/**
 * kie配置中心降级规则查询类
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Component("degradeRuleKieProvider")
public class DegradeRuleKieProvider extends CommonKieRuleProvider<DegradeRule, DegradeRuleVo> {

    public DegradeRuleKieProvider(KieConfigClient kieConfigClient, KieConfig kieConfig) {
        super(kieConfigClient, kieConfig, "DegradeRule");
    }

    @Override
    protected DegradeRuleVo getRuleVo(KieConfigItem item, String app) {
        DegradeRule degradeRule = KieConfigUtil.parseKieConfig(DegradeRule.class, item);
        return DegradeRuleVo.fromDegradeRule(app, null, null, degradeRule);
    }
}
