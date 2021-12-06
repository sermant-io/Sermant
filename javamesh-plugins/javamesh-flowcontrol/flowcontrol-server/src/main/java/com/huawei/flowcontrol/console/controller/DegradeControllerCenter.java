/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Based on com/alibaba/csp/sentinel/dashboard/controller/DegradeController.java
 * from the Alibaba Sentinel project.
 */

package com.huawei.flowcontrol.console.controller;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.huawei.flowcontrol.console.datasource.entity.rule.wrapper.DegradeControllerWrapper;
import com.huawei.flowcontrol.console.entity.DegradeRuleVo;
import com.huawei.flowcontrol.console.entity.Result;
import com.huawei.flowcontrol.console.util.DataType;
import com.huawei.flowcontrol.console.util.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.huawei.flowcontrol.console.util.SystemUtils.ERROR_CODE;

/**
 * 降级规则controller center
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@RestController
@RequestMapping(value = "/degradeRule")
public class DegradeControllerCenter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DegradeControllerCenter.class);

    private static final Logger OPERATE_LOGGER = LoggerFactory.getLogger("OPERATE_LOGGER");

    @Autowired
    private Map<String, DegradeControllerWrapper> degradeControllerMap;

    @Value("${sentinel.configCenter.type}")
    private String configCenterType;

    @PostConstruct
    public void init() {
        configCenterType = configCenterType + DataType.DEGRADE_RULE_SUFFIX.getDataType();
    }

    @GetMapping("/rules")
    public Result<List<DegradeRuleVo>> apiQueryRules(String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(ERROR_CODE, "app can't be null or empty");
        }

        DegradeControllerWrapper degradeControllerWrapper = degradeControllerMap.get(configCenterType);
        Result<List<DegradeRuleVo>> rules = degradeControllerWrapper.apiQueryRules(app);
        if (rules == null) {
            LOGGER.error("Error when querying degrade rules");
            return Result.ofFail(ERROR_CODE, "Error when querying degrade rules");
        }
        return rules;
    }

    @PostMapping("/add")
    public Result<DegradeRuleVo> apiAddDegradeRule(HttpServletRequest httpServletRequest,
        @RequestBody DegradeRuleVo entity) {
        Result<DegradeRuleVo> checkResult = checkEntityInternal(entity);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);

        DegradeControllerWrapper degradeControllerWrapper = degradeControllerMap.get(configCenterType);
        Result<DegradeRuleVo> rules = degradeControllerWrapper.apiAddRule(httpServletRequest, entity);
        if (rules == null) {
            OPERATE_LOGGER.info("{} add degrade rules failed, app: {}!", userName, entity.getApp());
            return Result.ofFail(ERROR_CODE, "Failed to add degrade rule");
        }
        OPERATE_LOGGER.info("{} add degrade rules success, app: {}!", userName, entity.getApp());
        return rules;
    }

    @PostMapping(value = "/update")
    public Result<DegradeRuleVo> apiUpdateFlowRule(HttpServletRequest httpServletRequest,
        @RequestBody DegradeRuleVo entity) {
        if ((entity.getId() == null) && StringUtil.isEmpty(entity.getExtInfo())) {
            return Result.ofFail(ERROR_CODE, "Invalid id and extInfo");
        }
        Result<DegradeRuleVo> checkResult = checkEntityInternal(entity);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        Date date = new Date();
        entity.setGmtModified(date);

        DegradeControllerWrapper degradeControllerWrapper = degradeControllerMap.get(configCenterType);
        Result<DegradeRuleVo> rules = degradeControllerWrapper.apiUpdateRule(httpServletRequest, entity);
        if (rules == null) {
            OPERATE_LOGGER.info("{} update degrade rules failed, app: {}!", userName, entity.getApp());
            return Result.ofFail(ERROR_CODE, "Failed to update degrade rule");
        }
        OPERATE_LOGGER.info("{} update degrade rules success, app: {}!", userName, entity.getApp());
        return rules;
    }

    @PostMapping("/delete")
    public Result<String> apiDeleteRule(HttpServletRequest httpServletRequest,
        @RequestBody DegradeRuleVo degradeRuleEntity) {
        Long id = degradeRuleEntity.getId();
        String extInfo = degradeRuleEntity.getExtInfo();
        String app = degradeRuleEntity.getApp();
        if ((id == null) && (StringUtil.isEmpty(extInfo))) {
            return Result.ofFail(ERROR_CODE, "Invalid id or extInfo");
        }
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(ERROR_CODE, "app can't be null or empty");
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        DegradeControllerWrapper degradeControllerWrapper = degradeControllerMap.get(configCenterType);
        Result<String> rules = degradeControllerWrapper.apiDeleteRule(httpServletRequest, id, extInfo, app);
        if (rules == null) {
            OPERATE_LOGGER.info("{} delete degrade rules failed, app: {}!", userName, app);
            return Result.ofFail(ERROR_CODE, "Failed to delete degrade rule");
        }
        OPERATE_LOGGER.info("{} delete degrade rules success, app: {}!", userName, app);
        return rules;
    }

    /**
     * 检查必要参数
     *
     * @param entity 降级规则实体
     * @param <R>
     * @return 返回提示信息
     */
    private <R> Result<R> checkEntityInternal(DegradeRuleVo entity) {
        String message = null;
        if (StringUtil.isBlank(entity.getApp())) {
            message = "app can't be blank";
        } else if (StringUtil.isBlank(entity.getLimitApp())) {
            message = "limitApp can't be null or empty";
        } else if (StringUtil.isBlank(entity.getResource())) {
            message = "resource can't be null or empty";
        } else {
            message = checkEntityStrategy(entity);
        }
        if (message == null) {
            return Result.ofSuccessMsg("success");
        } else {
            return Result.ofFail(ERROR_CODE, message);
        }
    }

    private String checkEntityStrategy(DegradeRuleVo entity) {
        String message = null;
        Integer strategy = entity.getGrade();
        if (strategy == null) {
            message = "circuit breaker strategy cannot be null";
        }
        if (message == null) {
            if (strategy < RuleConstant.DEGRADE_GRADE_RT || strategy > RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {
                message = "Invalid grade: " + strategy;
            }
        }
        Double threshold = entity.getCount();
        if (message == null && (threshold == null || threshold < 0)) {
            message = "invalid threshold: " + threshold;
        }
        Integer recoveryTimeoutSec = entity.getTimeWindow();
        if (message == null && (recoveryTimeoutSec == null || recoveryTimeoutSec <= 0)) {
            message = "recoveryTimeout should be positive";
        }
        if (message == null && strategy == RuleConstant.DEGRADE_GRADE_RT) {
            if (entity.getSlowRatioThreshold() == null) {
                message = "SlowRatioThreshold is required for slow request ratio strategy";
            }
            if (entity.getSlowRatioThreshold() < 0 || entity.getSlowRatioThreshold() >= 1) {
                message = "SlowRatioThreshold should be in range: [0.0, 1.0)";
            }
        }
        if (message == null && strategy == RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO) {
            if (threshold >= 1) {
                message = "Ratio threshold should be in range: [0.0, 1.0)";
            }
        }
        return message;
    }
}
