/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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
