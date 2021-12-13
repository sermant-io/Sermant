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

package com.huawei.flowcontrol.console.datasource.entity.rule.zookeeper.controller;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.huawei.flowcontrol.console.datasource.entity.rule.wrapper.SystemControllerWrapper;
import com.huawei.flowcontrol.console.entity.Result;
import com.huawei.flowcontrol.console.entity.SystemRuleVo;
import com.huawei.flowcontrol.console.rule.DynamicRuleProviderExt;
import com.huawei.flowcontrol.console.rule.DynamicRulePublisherExt;
import com.huawei.flowcontrol.console.util.DataType;
import com.huawei.flowcontrol.console.util.Md5Util;
import com.huawei.flowcontrol.console.util.SystemUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 系统controller
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */

@Component("zookeeper-systemRule")
public class ZookeeperSystemControllerWrapper implements SystemControllerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperSystemControllerWrapper.class);

    private static final Logger OPERATE_LOGGER = LoggerFactory.getLogger("OPERATE_LOGGER");

    /**
     * long型初始值
     */
    private static final long LONG_INITIALIZE = -1L;

    /**
     * double型初始值
     */
    private static final double DOUBLE_INITIALIZE = -1.0;

    /**
     * 失败ERROR_CODE
     */
    private static final int ERROR_CODE = -1;
    @Autowired
    private DynamicRuleProviderExt ruleProvider;
    @Autowired
    private DynamicRulePublisherExt rulePublisher;

    /**
     * 查询系统规则
     *
     * @param app 应用名
     * @return 返回系统规则集合
     */
    @Override
    public Result<List<SystemRuleVo>> apiQueryRules(String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(ERROR_CODE, "app can't be null or empty");
        }
        try {
            List<SystemRuleVo> rules = ruleProvider.getSystemRules(app);

            if (CollectionUtils.isEmpty(rules)) {
                return Result.ofSuccess(null);
            }
            for (SystemRuleVo entity : rules) {
                long idLong = getId(entity);
                entity.setId(idLong);
            }
            return Result.ofSuccess(rules);
        } catch (Exception e) {
            LOGGER.error("Error when querying system rules", e);
            return Result.ofFail(ERROR_CODE, "Error when querying system rules");
        }
    }

    /**
     * 新增系统规则
     *
     * @param systemRuleVo 系统规则实体
     * @return 返回新增系统规则
     */
    @Override
    public Result<SystemRuleVo> apiAddRule(HttpServletRequest httpServletRequest,
        SystemRuleVo systemRuleVo) {
        String app = systemRuleVo.getApp();
        SystemRuleVo entity = setDate(app, systemRuleVo);
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} add system rules, app: {}!", userName, entity.getApp());
        long dataId = ruleEntityId(entity);
        entity.setId(dataId);
        List<SystemRuleVo> ruleEntityList = null;
        try {
            ruleEntityList = ruleProvider.getSystemRules(app);
        } catch (Exception e) {
            LOGGER.error("Add system rules error", e);
            return Result.ofFail(ERROR_CODE, "Add system rules error");
        }

        if (CollectionUtils.isEmpty(ruleEntityList)) {
            ruleEntityList = new ArrayList<>();
        }

        for (SystemRuleVo newEntity : ruleEntityList) {
            // 判断是否有该规则
            if (flowRuleEntityId(entity).equals(flowRuleEntityId(newEntity))) {
                return Result.ofFail(ERROR_CODE, "System rules already exists!");
            }
        }
        ruleEntityList.add(entity);
        if (!publishRules(app, ruleEntityList)) {
            OPERATE_LOGGER.info("{} publish system rules fail after rule add, rule: {}, state: {}!",
                userName, entity.getApp(), DataType.OPERATION_FAIL.getDataType());
            return Result.ofFail(ERROR_CODE, "Publish system rules fail after rule add");
        }
        OPERATE_LOGGER.info("{} add system rules, rule detail: [{}], state: {}!",
            userName, ruleToString(entity), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(entity);
    }

    /**
     * 设置数据
     *
     * @param app          应用名称
     * @param systemRuleVo 系统规则实体
     * @return 返回系统规则实体
     */

    private SystemRuleVo setDate(String app, SystemRuleVo systemRuleVo) {
        String ip = systemRuleVo.getIp();
        SystemRuleVo entity = new SystemRuleVo();
        entity.setApp(app.trim());
        if (StringUtils.isNotEmpty(ip)) {
            entity.setIp(ip.trim());
        }
        entity.setPort(systemRuleVo.getPort());
        double highestSystemLoad = systemRuleVo.getHighestSystemLoad();

        // -1 is a fake value
        if (highestSystemLoad != DOUBLE_INITIALIZE) {
            entity.setHighestSystemLoad(highestSystemLoad);
        } else {
            entity.setHighestSystemLoad(DOUBLE_INITIALIZE);
        }
        double highestCpuUsage = systemRuleVo.getHighestCpuUsage();
        if (highestCpuUsage != DOUBLE_INITIALIZE) {
            entity.setHighestCpuUsage(highestCpuUsage);
        } else {
            entity.setHighestCpuUsage(DOUBLE_INITIALIZE);
        }
        long avgRt = systemRuleVo.getAvgRt();
        if (avgRt != LONG_INITIALIZE) {
            entity.setAvgRt(avgRt);
        } else {
            entity.setAvgRt(LONG_INITIALIZE);
        }
        long maxThread = systemRuleVo.getMaxThread();
        if (maxThread != LONG_INITIALIZE) {
            entity.setMaxThread(maxThread);
        } else {
            entity.setMaxThread(LONG_INITIALIZE);
        }
        double qps = systemRuleVo.getQps();
        if (qps != DOUBLE_INITIALIZE) {
            entity.setQps(qps);
        } else {
            entity.setQps(DOUBLE_INITIALIZE);
        }
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        return entity;
    }

    /**
     * 修改系统规则
     *
     * @param systemRuleVo 系统规则实体
     * @return 返回修改的系统规则
     */
    @Override
    public Result<SystemRuleVo> apiUpdateRule(HttpServletRequest httpServletRequest,
        SystemRuleVo systemRuleVo) {
        String app = systemRuleVo.getApp();
        long id = systemRuleVo.getId();
        List<SystemRuleVo> ruleEntityList = null;
        SystemRuleVo entity = null;

        // 获取规则
        try {
            ruleEntityList = ruleProvider.getSystemRules(app);
        } catch (Exception e) {
            LOGGER.error("Failed to modify system rules!", e);
            return Result.ofFail(ERROR_CODE, "Failed to modify system rules!");
        }

        if (!CollectionUtils.isEmpty(ruleEntityList)) {
            entity = getDate(ruleEntityList, id);
        }
        if (entity == null) {
            return Result.ofFail(ERROR_CODE,
                "The rule does not exist or has been modified. Please refresh and try again!");
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} modify system rules, rule detail: [{}]!", userName, ruleToString(entity));
        Result<SystemRuleVo> checkResult = setUpdateDate(entity, app, systemRuleVo);

        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        Date date = new Date();
        entity.setGmtModified(date);
        entity.setId(ruleEntityId(entity));
        ruleEntityList.add(entity);
        if (!publishRules(entity.getApp(), ruleEntityList)) {
            OPERATE_LOGGER.info("{} publish system rules fail after rule update, app: {}, state: {}!",
                userName, entity.getApp(), DataType.OPERATION_FAIL.getDataType());
            return Result.ofFail(ERROR_CODE, "Publish system rules fail after rule update");
        }
        OPERATE_LOGGER.info("{} modify system rules, rule detail: [{}], state: {}!",
            userName, ruleToString(entity), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(entity);
    }

    /**
     * 从list获取系统规则
     *
     * @param ruleEntityList 系统规则list
     * @param id             系统规则id
     * @return 返回系统规则实体
     */
    private SystemRuleVo getDate(List<SystemRuleVo> ruleEntityList, long id) {
        SystemRuleVo oldEntity = null;
        Iterator<SystemRuleVo> iterator = ruleEntityList.iterator();
        while (iterator.hasNext()) {
            SystemRuleVo newEntity = iterator.next();
            long newEntityId = getId(newEntity);

            // 判断是否有该规则，有则删除
            if (id == newEntityId) {
                oldEntity = newEntity;
                ruleEntityList.remove(newEntity);
                break;
            }
        }
        return oldEntity;
    }

    /**
     * 设置更新数据
     *
     * @param entity       系统规则实体
     * @param app          应用名称
     * @param systemRuleVo 系统规则实体
     * @return 返回错误信息，没有错误返回null
     */
    private <R> Result<R> setUpdateDate(SystemRuleVo entity, String app, SystemRuleVo systemRuleVo) {
        String message = setHighestSystemLoad(entity, systemRuleVo);
        if (StringUtil.isNotBlank(app)) {
            entity.setApp(app.trim());
        }
        double highestCpuUsage = systemRuleVo.getHighestCpuUsage();
        if (message == null && highestCpuUsage != DOUBLE_INITIALIZE) {
            message = setHighestCpuUsage(highestCpuUsage, entity);
        }
        long avgRt = systemRuleVo.getAvgRt();
        if (message == null && (avgRt != DOUBLE_INITIALIZE)) {
            if (avgRt < 0) {
                message = "avgRt must >= 0";
            } else {
                entity.setAvgRt(avgRt);
            }
        }
        long maxThread = systemRuleVo.getMaxThread();
        if (message == null && maxThread != LONG_INITIALIZE) {
            if (maxThread < 0) {
                message = "maxThread must >= 0";
            } else {
                entity.setMaxThread(maxThread);
            }
        }
        double qps = systemRuleVo.getQps();
        if (message == null && qps != DOUBLE_INITIALIZE) {
            if (qps < 0) {
                message = "qps must >= 0";
            } else {
                entity.setQps(qps);
            }
        }
        if (message == null) {
            return Result.ofSuccessMsg("success");
        } else {
            return Result.ofFail(ERROR_CODE, message);
        }
    }

    /**
     * 设置 cpu使用率
     *
     * @param highestCpuUsage cpu使用率
     * @param entity          系统规则实体
     * @return 错误信息
     */
    private String setHighestCpuUsage(double highestCpuUsage, SystemRuleVo entity) {
        String message = null;
        if (highestCpuUsage < 0) {
            message = "highestCpuUsage must >= 0";
        } else if (highestCpuUsage > 1) {
            message = "highestCpuUsage must <= 1";
        } else {
            entity.setHighestCpuUsage(highestCpuUsage);
        }
        return message;
    }

    private String setHighestSystemLoad(SystemRuleVo entity, SystemRuleVo systemRuleVo) {
        double highestSystemLoad = systemRuleVo.getHighestSystemLoad();
        String message = null;
        if (highestSystemLoad != DOUBLE_INITIALIZE) {
            if (highestSystemLoad < 0) {
                message = "highestSystemLoad must >= 0";
            } else {
                entity.setHighestSystemLoad(highestSystemLoad);
            }
        }
        return message;
    }

    /**
     * 删除系统规则
     *
     * @param id     系统规则id
     * @param ruleId kie规则id
     * @param app    应用名
     * @return 返回删除系统规则id
     */

    @Override
    public Result<String> apiDeleteRule(HttpServletRequest httpServletRequest, long id, String ruleId, String app) {
        if (id == 0) {
            return Result.ofFail(ERROR_CODE, "id can't be null");
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} delete system rules,id:{},app:{}!", userName, id, app);
        List<SystemRuleVo> ruleEntityList;

        // 获取规则
        try {
            ruleEntityList = ruleProvider.getSystemRules(app);
        } catch (Exception e) {
            LOGGER.error("Failed to delete system rules!", e);
            return Result.ofFail(ERROR_CODE, "Failed to delete system rules!");
        }
        SystemRuleVo oldEntity = null;
        if (!CollectionUtils.isEmpty(ruleEntityList)) {
            oldEntity = getDate(ruleEntityList, id);
        }

        if (oldEntity == null) {
            return Result.ofFail(ERROR_CODE,
                "The rule does not exist or has been modified. Please refresh and try again!");
        }
        if (!publishRules(app, ruleEntityList)) {
            OPERATE_LOGGER.info("{} publish system rules fail after rule delete,id:{},app:{},state:{}!",
                userName, id, app, DataType.OPERATION_FAIL.getDataType());
            return Result.ofFail(ERROR_CODE, "Publish system rules fail after rule delete");
        }
        OPERATE_LOGGER.info("{} delete system rules,id:{},rule detail:[{}],state:{}!",
            userName, id, ruleToString(oldEntity), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(String.valueOf(id));
    }

    /**
     * 发送系统规则
     *
     * @param app   应用名
     * @param rules 系统规则集合
     * @return 成功返回true，失败返回false
     */
    private boolean publishRules(String app, List<SystemRuleVo> rules) {
        try {
            rulePublisher.publish(app, DataType.SYSTEM.getDataType(), rules);
        } catch (Exception e) {
            LOGGER.error("Failed to publish system rules !", e);
            return false;
        }
        return true;
    }

    /**
     * 拼接字符串，判断同一规则
     *
     * @param entity 系统规则实体
     * @return 返回拼接id
     */
    private String flowRuleEntityId(SystemRuleVo entity) {
        StringBuilder ruleId = new StringBuilder(entity.getApp()
            + DataType.SEPARATOR_COLON.getDataType() + entity.getIp()
            + DataType.SEPARATOR_COLON.getDataType() + entity.getPort());
        return ruleId.append(getThresholdType(entity)).toString();
    }

    /**
     * 获取阈值类型
     *
     * @param entity 规则实体
     * @return 返回阈值类型
     */
    private String getThresholdType(SystemRuleVo entity) {
        String thresholdType = null;
        if (LONG_INITIALIZE == entity.getAvgRt()) {
            thresholdType = ":AvgRt";
        }
        if (DOUBLE_INITIALIZE == entity.getHighestCpuUsage()) {
            thresholdType = ":HighestCpuUsage";
        }
        if (DOUBLE_INITIALIZE == entity.getHighestSystemLoad()) {
            thresholdType = ":HighestSystemLoad";
        }
        if (LONG_INITIALIZE == entity.getMaxThread()) {
            thresholdType = ":MaxThread";
        }
        if (DOUBLE_INITIALIZE == entity.getQps()) {
            thresholdType = ":Qps";
        }
        return thresholdType;
    }

    /**
     * 规则转字符串
     *
     * @param entity 规则
     * @return 规则字符串
     */
    private String ruleToString(SystemRuleVo entity) {
        return "app:" + entity.getApp()
            + " ip:" + entity.getIp()
            + " port:" + entity.getPort()
            + " thresholdType" + getThresholdType(entity);
    }

    /**
     * 计算系统规则id
     *
     * @param entity 系统规则实体
     * @return 返回系统规则id
     */
    private long ruleEntityId(SystemRuleVo entity) {
        StringBuilder id = new StringBuilder(flowRuleEntityId(entity));
        if (!entity.getAvgRt().equals(LONG_INITIALIZE)) {
            id.append(entity.getAvgRt());
        }
        if (!entity.getHighestCpuUsage().equals(DOUBLE_INITIALIZE)) {
            id.append(entity.getHighestCpuUsage());
        }
        if (!entity.getHighestSystemLoad().equals(DOUBLE_INITIALIZE)) {
            id.append(entity.getHighestSystemLoad());
        }
        if (!entity.getMaxThread().equals(LONG_INITIALIZE)) {
            id.append(entity.getMaxThread());
        }
        if (!entity.getQps().equals(DOUBLE_INITIALIZE)) {
            id.append(entity.getQps());
        }
        return Md5Util.stringToMd5(id.toString()).hashCode();
    }

    /**
     * 获取系统规则id
     *
     * @param entity 系统规则实体
     * @return 返回系统规则id
     */
    private long getId(SystemRuleVo entity) {
        if (entity.getId() == null || entity.getId() == SystemUtils.LONG_INITIALIZE) {
            return ruleEntityId(entity);
        }
        return entity.getId();
    }
}
