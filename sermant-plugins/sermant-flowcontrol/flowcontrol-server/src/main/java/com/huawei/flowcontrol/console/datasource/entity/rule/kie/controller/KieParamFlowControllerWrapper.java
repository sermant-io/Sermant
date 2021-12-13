/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.controller;

import com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator.RuleKieProvider;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator.RuleKiePublisher;
import com.huawei.flowcontrol.console.datasource.entity.rule.wrapper.ParamFlowControllerWrapper;
import com.huawei.flowcontrol.console.entity.ParamFlowRuleVo;
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
 * Parameter flow rule controller.
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@Slf4j
@Component("servicecomb-kie-paramFlowRule")
public class KieParamFlowControllerWrapper extends KieRuleCommonController<ParamFlowRuleVo>
    implements ParamFlowControllerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(KieParamFlowControllerWrapper.class);

    @Autowired
    @Qualifier("paramFlowRuleKieProvider")
    private RuleKieProvider<List<ParamFlowRuleVo>> ruleProvider;
    @Autowired
    @Qualifier("paramFlowRuleKiePublisher")
    private RuleKiePublisher<List<ParamFlowRuleVo>> rulePublisher;

    public KieParamFlowControllerWrapper(
        @Autowired
        @Qualifier("paramFlowRuleKiePublisher")
        RuleKiePublisher<List<ParamFlowRuleVo>> rulePublisher) {
        super(rulePublisher);
    }

    @Override
    public Result<List<ParamFlowRuleVo>> apiQueryRules(String app) {
        try {
            List<ParamFlowRuleVo> rules = ruleProvider.getRules(app);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            LOGGER.error("Error when querying paramFlow rules", throwable);
            return Result.ofThrowable(ERROR_CODE, throwable);
        }
    }

    @Override
    public Result<ParamFlowRuleVo> apiAddRule(HttpServletRequest httpServletRequest, ParamFlowRuleVo entity) {
        String app = entity.getApp();
        try {
            rulePublisher.add(app, Collections.singletonList(entity));
        } catch (Throwable throwable) {
            LOGGER.error("Error when add paramFlow rules", throwable);
            return Result.ofThrowable(ERROR_CODE, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @Override
    public Result<ParamFlowRuleVo> apiUpdateRule(HttpServletRequest httpServletRequest,
        ParamFlowRuleVo entity) {
        String app = entity.getApp();
        try {
            rulePublisher.update(app, Collections.singletonList(entity));
            return Result.ofSuccess(entity);
        } catch (Throwable throwable) {
            LOGGER.error("Error when update paramFlow rules", throwable);
            return Result.ofThrowable(ERROR_CODE, throwable);
        }
    }

    @Override
    public Result<String> apiDeleteRule(HttpServletRequest httpServletRequest, long id, String extInfo, String app) {
        return super.apiDeleteRule(httpServletRequest, id, extInfo, app);
    }
}
