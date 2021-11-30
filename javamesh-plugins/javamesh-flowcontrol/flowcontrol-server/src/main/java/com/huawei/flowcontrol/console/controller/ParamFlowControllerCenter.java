/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.controller;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.huawei.flowcontrol.console.datasource.entity.rule.wrapper.ParamFlowControllerWrapper;
import com.huawei.flowcontrol.console.entity.ParamFlowRuleVo;
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
 * 热点参数规则controller center
 *
 * @author Sherlockhan
 * @since 2020-12-21
 */
@RestController
@RequestMapping(value = "/paramFlowRule")
public class ParamFlowControllerCenter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamFlowControllerCenter.class);

    private static final Logger OPERATE_LOGGER = LoggerFactory.getLogger("OPERATE_LOGGER");

    private static final String APP_NULL_MSG = "app can't be null or empty";

    @Autowired
    private Map<String, ParamFlowControllerWrapper> paramFlowControllerMap;

    @Value("${sentinel.configCenter.type}")
    private String configCenterType;

    @PostConstruct
    public void init() {
        configCenterType = configCenterType + DataType.PARAMFLOW_RULE_SUFFIX.getDataType();
    }

    @GetMapping("/rules")
    public Result<List<ParamFlowRuleVo>> apiQueryRules(HttpServletRequest httpServletRequest,
        @RequestParam String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(ERROR_CODE, APP_NULL_MSG);
        }
        ParamFlowControllerWrapper paramFlowControllerWrapper = paramFlowControllerMap.get(configCenterType);
        Result<List<ParamFlowRuleVo>> rules = paramFlowControllerWrapper.apiQueryRules(app);
        if (rules == null) {
            LOGGER.error("Error when querying param flow rules");
            return Result.ofFail(ERROR_CODE, "Error when querying param flow rules");
        }
        return rules;
    }

    @PostMapping("/add")
    public Result<ParamFlowRuleVo> apiAddParamFlowRule(HttpServletRequest httpServletRequest,
        @RequestBody ParamFlowRuleVo entity) {
        Result<ParamFlowRuleVo> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        ParamFlowControllerWrapper paramFlowControllerWrapper = paramFlowControllerMap.get(configCenterType);
        Result<ParamFlowRuleVo> rules = paramFlowControllerWrapper.apiAddRule(httpServletRequest, entity);
        if (rules == null) {
            OPERATE_LOGGER.info("{} add param flow rules failed, app: {}!", userName, entity.getApp());
            return Result.ofFail(ERROR_CODE, "Failed to add param flow rule");
        }
        OPERATE_LOGGER.info("{} add param flow rules success, app: {}!", userName, entity.getApp());
        return rules;
    }

    @PostMapping(value = "/update")
    public Result<ParamFlowRuleVo> apiUpdateFlowRule(HttpServletRequest httpServletRequest,
        @RequestBody ParamFlowRuleVo entity) {
        Long id = entity.getId();
        String extInfo = entity.getExtInfo();
        if ((id == null) && StringUtil.isEmpty(extInfo)) {
            return Result.ofFail(ERROR_CODE, "Invalid id and extInfo");
        }
        Result<ParamFlowRuleVo> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        Date date = new Date();
        entity.setGmtModified(date);
        ParamFlowControllerWrapper paramFlowControllerWrapper = paramFlowControllerMap.get(configCenterType);
        Result<ParamFlowRuleVo> rules = paramFlowControllerWrapper.apiUpdateRule(httpServletRequest, entity);
        if (rules == null) {
            OPERATE_LOGGER.info("{} update param flow rules failed, app: {}!", userName, entity.getApp());
            return Result.ofFail(ERROR_CODE, "Failed to update param flow rule");
        }
        OPERATE_LOGGER.info("{} update param flow rules success, app: {}!", userName, entity.getApp());
        return rules;
    }

    @PostMapping("/delete")
    public Result<String> apiDeleteRule(HttpServletRequest httpServletRequest,
        @RequestBody ParamFlowRuleVo paramFlowRuleVo) {
        Long id = paramFlowRuleVo.getId();
        String extInfo = paramFlowRuleVo.getExtInfo();
        String app = paramFlowRuleVo.getApp();
        if ((id == null) && StringUtil.isEmpty(extInfo)) {
            return Result.ofFail(ERROR_CODE, "Invalid id and extInfo");
        }
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(ERROR_CODE, APP_NULL_MSG);
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        ParamFlowControllerWrapper paramFlowControllerWrapper = paramFlowControllerMap.get(configCenterType);
        Result<String> rules = paramFlowControllerWrapper.apiDeleteRule(httpServletRequest, id, extInfo, app);
        if (rules == null) {
            OPERATE_LOGGER.info("{} delete param flow rules failed, app: {}!", userName, app);
            return Result.ofFail(ERROR_CODE, "Failed to delete param flow rule");
        }
        OPERATE_LOGGER.info("{} delete param flow rules success, app: {}!", userName, app);
        return rules;
    }

    /**
     * 检查必要参数
     *
     * @param entity 规则实体
     * @return 返回提示信息
     */
    private <R> Result<R> checkEntityInternal(ParamFlowRuleVo entity) {
        String message = null;
        if (entity == null) {
            message = "bad rule body";
        } else if (StringUtil.isBlank(entity.getApp())) {
            message = APP_NULL_MSG;
        } else if (entity.getRule() == null) {
            message = "rule can't be null";
        } else if (StringUtil.isBlank(entity.getResource())) {
            message = "resource name cannot be null or empty";
        } else if (entity.getCount() < 0) {
            message = "count should be valid";
        } else if (entity.getGrade() != RuleConstant.FLOW_GRADE_QPS) {
            message = "Unknown mode (blockGrade) for parameter flow control";
        } else if (entity.getParamIdx() == null || entity.getParamIdx() < 0) {
            message = "paramIdx should be valid";
        } else if (entity.getDurationInSec() <= 0) {
            message = "durationInSec should be valid";
        } else if (entity.getControlBehavior() < 0) {
            message = "controlBehavior should be valid";
        }
        if (message == null) {
            return null;
        } else {
            return Result.ofFail(ERROR_CODE, message);
        }
    }
}
