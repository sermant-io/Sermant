/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.controller;

import com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator.RuleKieProvider;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator.RuleKiePublisher;
import com.huawei.flowcontrol.console.datasource.entity.rule.wrapper.DegradeControllerWrapper;
import com.huawei.flowcontrol.console.entity.DegradeRuleVo;
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
 * Degrade rule controller wrapper.
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Slf4j
@Component("servicecomb-kie-degradeRule")
public class KieDegradeControllerWrapper extends KieRuleCommonController<DegradeRuleVo>
    implements DegradeControllerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(KieDegradeControllerWrapper.class);

    @Autowired
    @Qualifier("degradeRuleKieProvider")
    private RuleKieProvider<List<DegradeRuleVo>> ruleProvider;
    @Autowired
    @Qualifier("degradeRuleKiePublisher")
    private RuleKiePublisher<List<DegradeRuleVo>> rulePublisher;

    public KieDegradeControllerWrapper(
        @Autowired @Qualifier("degradeRuleKiePublisher")
        RuleKiePublisher<List<DegradeRuleVo>> rulePublisher) {
        super(rulePublisher);
    }

    @Override
    public Result<List<DegradeRuleVo>> apiQueryRules(String app) {
        try {
            List<DegradeRuleVo> rules = ruleProvider.getRules(app);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            LOGGER.error("Error when querying degrade rules", throwable);
            return Result.ofThrowable(ERROR_CODE, throwable);
        }
    }

    @Override
    public Result<DegradeRuleVo> apiAddRule(HttpServletRequest httpServletRequest, DegradeRuleVo entity) {
        String app = entity.getApp();
        try {
            rulePublisher.add(app, Collections.singletonList(entity));
        } catch (Throwable throwable) {
            LOGGER.error("Error when add degrade rules", throwable);
            return Result.ofThrowable(ERROR_CODE, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @Override
    public Result<DegradeRuleVo> apiUpdateRule(HttpServletRequest httpServletRequest, DegradeRuleVo entity) {
        String app = entity.getApp();
        try {
            rulePublisher.update(app, Collections.singletonList(entity));
            return Result.ofSuccess(entity);
        } catch (Throwable throwable) {
            LOGGER.error("Error when update degrade rules", throwable);
            return Result.ofThrowable(ERROR_CODE, throwable);
        }
    }

    @Override
    public Result<String> apiDeleteRule(HttpServletRequest httpServletRequest, long id, String extInfo, String app) {
        return super.apiDeleteRule(httpServletRequest, id, extInfo, app);
    }
}
