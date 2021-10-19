/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.controller;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.huawei.flowcontrol.console.datasource.entity.rule.wrapper.FlowControllerWrapper;
import com.huawei.flowcontrol.console.entity.FlowRuleVo;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.huawei.flowcontrol.console.util.SystemUtils.ERROR_CODE;

/**
 * 流控规则controller center
 */
@RestController
@RequestMapping(value = "/flowRule")
public class FlowControllerCenter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowControllerCenter.class);

    private static final Logger OPERATE_LOGGER = LoggerFactory.getLogger("OPERATE_LOGGER");

    private static final int BEHAVIOR_WARM_UP = 1;

    private static final int BEHAVIOR_RATE_LIMITER = 2;

    @Autowired
    private Map<String, FlowControllerWrapper> flowControllerMap;

    @Value("${sentinel.configCenter.type}")
    private String configCenterType;

    @PostConstruct
    public void init() {
        configCenterType = configCenterType + DataType.FLOW_RULE_SUFFIX.getDataType();
    }

    @GetMapping("/rules")
    public Result<List<FlowRuleVo>> apiQueryRules(@RequestParam String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(ERROR_CODE, "app can't be null or empty");
        }
        FlowControllerWrapper flowControllerWrapper = flowControllerMap.get(configCenterType);
        Result<List<FlowRuleVo>> rules = flowControllerWrapper.apiQueryRules(app);
        if (rules == null) {
            LOGGER.error("Error when querying flow rules");
            return Result.ofFail(ERROR_CODE, "Error when querying flow rules");
        }
        return rules;
    }

    @PostMapping("/add")
    public Result<FlowRuleVo> apiAddFlowRule(HttpServletRequest httpServletRequest,
        @RequestBody FlowRuleVo entity) {
        Result<FlowRuleVo> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        FlowControllerWrapper flowControllerWrapper = flowControllerMap.get(configCenterType);
        Result<FlowRuleVo> rules = flowControllerWrapper.apiAddRule(httpServletRequest, entity);
        if (rules == null) {
            OPERATE_LOGGER.info("{} add flow rules failed, app: {}!", userName, entity.getApp());
            return Result.ofFail(ERROR_CODE, "Failed to add flow rule");
        }
        OPERATE_LOGGER.info("{} add flow rules success, app: {}!", userName, entity.getApp());
        return rules;
    }

    @PostMapping(value = "/update")
    public Result<FlowRuleVo> apiUpdateFlowRule(HttpServletRequest httpServletRequest,
        @RequestBody FlowRuleVo entity) {
        Long id = entity.getId();
        String extInfo = entity.getExtInfo();
        if ((id == null) && StringUtil.isEmpty(extInfo)) {
            return Result.ofFail(ERROR_CODE, "Invalid id and extInfo");
        }
        Result<FlowRuleVo> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        Date date = new Date();
        entity.setGmtModified(date);

        FlowControllerWrapper flowControllerWrapper = flowControllerMap.get(configCenterType);
        Result<FlowRuleVo> rules = flowControllerWrapper.apiUpdateRule(httpServletRequest, entity);
        if (rules == null) {
            OPERATE_LOGGER.info("{} update flow rules failed, app: {}!", userName, entity.getApp());
            return Result.ofFail(ERROR_CODE, "Failed to update flow rule");
        }
        OPERATE_LOGGER.info("{} update flow rules success, app: {}!", userName, entity.getApp());
        return rules;
    }

    @PostMapping("/delete")
    public Result<String> apiDeleteRule(HttpServletRequest httpServletRequest,
        @RequestBody FlowRuleVo flowRuleVo) {
        Long id = flowRuleVo.getId();
        String extInfo = flowRuleVo.getExtInfo();
        String app = flowRuleVo.getApp();
        if ((id == null) && (StringUtil.isEmpty(extInfo))) {
            return Result.ofFail(ERROR_CODE, "Invalid id and extInfo");
        }
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(ERROR_CODE, "appId can't be null or empty");
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        FlowControllerWrapper flowControllerWrapper = flowControllerMap.get(configCenterType);
        Result<String> rules = flowControllerWrapper.apiDeleteRule(httpServletRequest, id, extInfo, app);
        if (rules == null) {
            OPERATE_LOGGER.info("{} delete flow rules failed, app: {}!", userName, app);
            return Result.ofFail(ERROR_CODE, "Failed to delete flow rule");
        }
        OPERATE_LOGGER.info("{} delete flow rules success, app: {}!", userName, app);
        return rules;
    }

    /**
     * 检查必要参数
     *
     * @param entity 流控规则实体
     * @param <R> 响应结果实体
     * @return 返回提示信息
     */
    private <R> Result<R> checkEntityInternal(FlowRuleVo entity) {
        String message = null;
        if (entity == null) {
            message = "invalid body";
        } else if (StringUtil.isBlank(entity.getApp())) {
            message = "app can't be null or empty";
        } else if (StringUtil.isBlank(entity.getLimitApp())) {
            message = "limitApp can't be null or empty";
        } else if (StringUtil.isBlank(entity.getResource())) {
            message = "resource can't be null or empty";
        } else if (entity.getGrade() == null) {
            message = "grade can't be null";
        } else if (entity.getGrade() != 0 && entity.getGrade() != 1) {
            message = "grade must be 0 or 1, but " + entity.getGrade() + " got";
        } else if (entity.getCount() == null || entity.getCount() < 0) {
            message = "count should be at lease zero";
        } else if (entity.getStrategy() == null) {
            message = "strategy can't be null";
        } else if (entity.getStrategy() != 0 && StringUtil.isBlank(entity.getRefResource())) {
            message = "refResource can't be null or empty when strategy!=0";
        } else if (entity.getControlBehavior() == null) {
            message = "controlBehavior can't be null";
        } else {
            message = checkControlBehavior(entity);
        }
        if (message == null) {
            return null;
        } else {
            return Result.ofFail(ERROR_CODE, message);
        }
    }

    private String checkControlBehavior(FlowRuleVo entity) {
        String message = null;
        int controlBehavior = entity.getControlBehavior();
        if (controlBehavior == BEHAVIOR_WARM_UP && entity.getWarmUpPeriodSec() == null) {
            message = "warmUpPeriodSec can't be null when controlBehavior==1";
        }
        if (message == null && controlBehavior == BEHAVIOR_RATE_LIMITER && entity.getMaxQueueingTimeMs() == null) {
            message = "maxQueueingTimeMs can't be null when controlBehavior==2";
        }
        if (message == null && entity.isClusterMode() && entity.getClusterConfig() == null) {
            message = "cluster config should be valid";
        }
        return message;
    }
}
