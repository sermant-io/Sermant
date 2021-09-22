/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator;

import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.huawei.flowcontrol.console.entity.ParamFlowRuleVo;
import org.springframework.stereotype.Component;

/**
 * kie配置中心热点参数规则更新、新增、删除类
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Component("paramFlowRuleKiePublisher")
public class ParamFlowRuleCommonKiePublisher extends CommonKieRulePublisher<ParamFlowRule, ParamFlowRuleVo> {

}
