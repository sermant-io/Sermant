/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.controller;

import com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator.RuleKieProvider;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator.RuleKiePublisher;
import com.huawei.flowcontrol.console.datasource.entity.rule.wrapper.FlowControllerWrapper;
import com.huawei.flowcontrol.console.entity.FlowRuleVo;
import com.huawei.flowcontrol.console.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static com.huawei.flowcontrol.console.util.SystemUtils.ERROR_CODE;

/**
 * Flow rule controller (v2).
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Slf4j
@Component("servicecomb-kie-flowRule")
public class KieFlowControllerWrapper extends KieRuleCommonController<FlowRuleVo> implements FlowControllerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(KieFlowControllerWrapper.class);

    @Autowired
    @Qualifier("flowRuleKieProvider")
    private RuleKieProvider<List<FlowRuleVo>> ruleProvider;
    @Autowired
    @Qualifier("flowRuleKiePublisher")
    private RuleKiePublisher<List<FlowRuleVo>> rulePublisher;

    public KieFlowControllerWrapper(
        @Autowired
        @Qualifier("flowRuleKiePublisher")
        RuleKiePublisher<List<FlowRuleVo>> rulePublisher) {
        super(rulePublisher);
    }

    @Override
    public Result<List<FlowRuleVo>> apiQueryRules(String app) {
        try {
            List<FlowRuleVo> rules = ruleProvider.getRules(app);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            LOGGER.error("Error when querying flow rules", throwable);
            return Result.ofThrowable(ERROR_CODE, throwable);
        }
    }

    @Override
    public Result<FlowRuleVo> apiUpdateRule(HttpServletRequest httpServletRequest, FlowRuleVo entity) {
        String app = entity.getApp();
        try {
            rulePublisher.update(app, Collections.singletonList(entity));
            return Result.ofSuccess(entity);
        } catch (Throwable throwable) {
            LOGGER.error("Error when update flow rules", throwable);
            return Result.ofThrowable(ERROR_CODE, throwable);
        }
    }

    @Override
    public Result<FlowRuleVo> apiAddRule(HttpServletRequest httpServletRequest, FlowRuleVo entity) {
        String app = entity.getApp();
        try {
            rulePublisher.add(app, Collections.singletonList(entity));
        } catch (Throwable throwable) {
            LOGGER.error("Error when add flow rules", throwable);
            return Result.ofThrowable(ERROR_CODE, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @Override
    public Result<String> apiDeleteRule(HttpServletRequest httpServletRequest, Long id, String extInfo, String app) {
        return super.apiDeleteRule(httpServletRequest, id, extInfo, app);
    }
}
