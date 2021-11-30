/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.controller;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.huawei.flowcontrol.console.datasource.entity.rule.wrapper.SystemControllerWrapper;
import com.huawei.flowcontrol.console.entity.Result;
import com.huawei.flowcontrol.console.entity.SystemRuleVo;
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
@RequestMapping(value = "/systemRule")
public class SystemControllerCenter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemControllerCenter.class);

    private static final Logger OPERATE_LOGGER = LoggerFactory.getLogger("OPERATE_LOGGER");

    private static final Double DOUBLE_DEFAULT_VALUE = -1D;

    private static final Long LONG_DEFAULT_VALUE = -1L;

    private static final String APP_NULL_MSG = "app can't be null or empty";

    @Autowired
    private Map<String, SystemControllerWrapper> systemControllerMap;

    @Value("${sentinel.configCenter.type}")
    private String configCenterType;

    @PostConstruct
    public void init() {
        configCenterType = configCenterType + DataType.SYSTEM_RULE_SUFFIX.getDataType();
    }

    @GetMapping("/rules")
    public Result<List<SystemRuleVo>> apiQueryRules(String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(ERROR_CODE, APP_NULL_MSG);
        }
        SystemControllerWrapper systemControllerWrapper = systemControllerMap.get(configCenterType);
        Result<List<SystemRuleVo>> rules = systemControllerWrapper.apiQueryRules(app);
        if (rules == null) {
            LOGGER.error("Error when querying system rules");
            return Result.ofFail(ERROR_CODE, "Error when querying system rules");
        }
        return rules;
    }

    @PostMapping("/add")
    public Result<SystemRuleVo> apiAddSystemRule(HttpServletRequest httpServletRequest,
        @RequestBody SystemRuleVo entity) {
        Result<SystemRuleVo> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        SystemRuleVo resEntity = modifyNullValue(entity);
        String userName = SystemUtils.getUserName(httpServletRequest);
        Date date = new Date();
        resEntity.setGmtCreate(date);
        resEntity.setGmtModified(date);
        SystemControllerWrapper systemControllerWrapper = systemControllerMap.get(configCenterType);
        Result<SystemRuleVo> rules = systemControllerWrapper.apiAddRule(httpServletRequest, resEntity);
        if (rules == null) {
            OPERATE_LOGGER.info("{} add system rules failed, app: {}!", userName, entity.getApp());
            return Result.ofFail(ERROR_CODE, "Failed to add system rule");
        }
        OPERATE_LOGGER.info("{} add system rules success, app: {}!", userName, entity.getApp());
        return rules;
    }

    @PostMapping(value = "/update")
    public Result<SystemRuleVo> apiUpdateFlowRule(HttpServletRequest httpServletRequest,
        @RequestBody SystemRuleVo entity) {
        Result<SystemRuleVo> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        if ((entity.getId() == null) && StringUtil.isEmpty(entity.getExtInfo())) {
            return Result.ofFail(ERROR_CODE, "Invalid id and extInfo");
        }
        SystemRuleVo resEntity = modifyNullValue(entity);
        String userName = SystemUtils.getUserName(httpServletRequest);
        Date date = new Date();
        resEntity.setGmtModified(date);
        SystemControllerWrapper systemControllerWrapper = systemControllerMap.get(configCenterType);
        Result<SystemRuleVo> rules = systemControllerWrapper.apiUpdateRule(httpServletRequest, resEntity);
        if (rules == null) {
            OPERATE_LOGGER.info("{} update system rules failed, app: {}!", userName, entity.getApp());
            return Result.ofFail(ERROR_CODE, "Failed to update system rule");
        }
        OPERATE_LOGGER.info("{} update system rules success, app: {}!", userName, entity.getApp());
        return rules;
    }

    @PostMapping("/delete")
    public Result<String> apiDeleteRule(HttpServletRequest httpServletRequest,
        @RequestBody SystemRuleVo systemRuleVo) {
        Long id = systemRuleVo.getId();
        String extInfo = systemRuleVo.getExtInfo();
        String app = systemRuleVo.getApp();
        if ((id == null) && StringUtil.isEmpty(extInfo)) {
            return Result.ofFail(ERROR_CODE, "Invalid id and extInfo");
        }
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(ERROR_CODE, APP_NULL_MSG);
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        SystemControllerWrapper systemControllerWrapper = systemControllerMap.get(configCenterType);
        Result<String> rules = systemControllerWrapper.apiDeleteRule(httpServletRequest, id, extInfo, app);
        if (rules == null) {
            OPERATE_LOGGER.info("{} delete system rules failed, app: {}!", userName, app);
            return Result.ofFail(ERROR_CODE, "Failed to delete system rule");
        }
        OPERATE_LOGGER.info("{} delete system rules success, app: {}!", userName, app);
        return rules;
    }

    /**
     * 检查必要参数
     *
     * @param entity 系统规则实体
     * @param <R>
     * @return 返回提示信息
     */
    private <R> Result<R> checkEntityInternal(SystemRuleVo entity) {
        if (entity == null) {
            return Result.ofFail(ERROR_CODE, "invalid body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(ERROR_CODE, APP_NULL_MSG);
        }
        Double highestSystemLoad = entity.getHighestSystemLoad();
        Double highestCpuUsage = entity.getHighestCpuUsage();
        Long avgRt = entity.getAvgRt();
        Long maxThread = entity.getMaxThread();
        Double qps = entity.getQps();
        int notNullCount = countNotNullAndNotNegative(highestSystemLoad, avgRt, maxThread, qps, highestCpuUsage);
        if (notNullCount != 1) {
            return Result.ofFail(ERROR_CODE, "only one of [highestSystemLoad, avgRt, maxThread, qps,"
                + "highestCpuUsage] " + "value must be set > 0, but " + notNullCount + " values get");
        }
        if (highestCpuUsage != null && highestCpuUsage > 1) {
            return Result.ofFail(ERROR_CODE, "highestCpuUsage must between [0.0, 1.0]");
        }
        return null;
    }

    private int countNotNullAndNotNegative(Number... values) {
        int notNullCount = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null && values[i].doubleValue() >= 0) {
                notNullCount++;
            }
        }
        return notNullCount;
    }

    public SystemRuleVo modifyNullValue(SystemRuleVo entity) {
        if (entity.getHighestSystemLoad() == null) {
            entity.setHighestSystemLoad(DOUBLE_DEFAULT_VALUE);
        }

        if (entity.getHighestCpuUsage() == null) {
            entity.setHighestCpuUsage(DOUBLE_DEFAULT_VALUE);
        }

        if (entity.getAvgRt() == null) {
            entity.setAvgRt(LONG_DEFAULT_VALUE);
        }

        if (entity.getMaxThread() == null) {
            entity.setMaxThread(LONG_DEFAULT_VALUE);
        }

        if (entity.getQps() == null) {
            entity.setQps(DOUBLE_DEFAULT_VALUE);
        }
        return entity;
    }
}
