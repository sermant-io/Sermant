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
import com.huawei.flowcontrol.console.datasource.entity.rule.wrapper.DegradeControllerWrapper;
import com.huawei.flowcontrol.console.entity.DegradeRuleVo;
import com.huawei.flowcontrol.console.entity.Result;
import com.huawei.flowcontrol.console.rule.DynamicRuleProviderExt;
import com.huawei.flowcontrol.console.rule.DynamicRulePublisherExt;
import com.huawei.flowcontrol.console.util.DataType;
import com.huawei.flowcontrol.console.util.Md5Util;
import com.huawei.flowcontrol.console.util.SystemUtils;
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
 * 降级controller
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
@Component("zookeeper-degradeRule")
public class ZookeeperDegradeControllerWrapper implements DegradeControllerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperDegradeControllerWrapper.class);

    private static final Logger OPERATE_LOGGER = LoggerFactory.getLogger("OPERATE_LOGGER");

    private static final String RULE_EXISTS = "Rule already exists!";

    @Autowired
    private DynamicRuleProviderExt ruleProvider;
    @Autowired
    private DynamicRulePublisherExt rulePublisher;

    /**
     * 查询降级规则
     *
     * @param app 应用名
     * @return 降级规则list
     */
    @Override
    public Result<List<DegradeRuleVo>> apiQueryRules(String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(SystemUtils.ERROR_CODE, "app can't be null or empty");
        }
        try {
            List<DegradeRuleVo> rules = ruleProvider.getDegradeRules(app);
            if (CollectionUtils.isEmpty(rules)) {
                return Result.ofSuccess(null);
            }
            for (DegradeRuleVo entity : rules) {
                entity.setId(getId(entity));
            }
            return Result.ofSuccess(rules);
        } catch (Exception e) {
            LOGGER.error("Error when querying degrade rules!", e.getMessage());
            return Result.ofFail(SystemUtils.ERROR_CODE, "Error when querying degrade rules!");
        }
    }

    /**
     * 新增降级规则
     *
     * @param entity 降级实体
     * @return 返回该降级规则
     */
    @Override
    public Result<DegradeRuleVo> apiAddRule(HttpServletRequest httpServletRequest, DegradeRuleVo entity) {
        String app = entity.getApp();
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        long dataId = ruleEntityId(entity);
        entity.setId(dataId);
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} add degrade rules, app: {}!", userName, entity.getApp());

        // 查询已有规则，没有规则新建list
        List<DegradeRuleVo> ruleEntityList = null;
        String errorMessage = "Failed to add rule!";
        try {
            ruleEntityList = ruleProvider.getDegradeRules(app);
        } catch (Exception e) {
            LOGGER.error(errorMessage, e);
            return Result.ofFail(SystemUtils.ERROR_CODE, errorMessage);
        }
        if (CollectionUtils.isEmpty(ruleEntityList)) {
            ruleEntityList = new ArrayList<>();
        }
        for (DegradeRuleVo newEntity : ruleEntityList) {
            // 判断是否有该规则，有则提示用户
            if (flowRuleEntityId(entity).equals(flowRuleEntityId(newEntity))) {
                return Result.ofFail(SystemUtils.ERROR_CODE, RULE_EXISTS);
            }
        }

        // 将规则添加到list中，并推送zookeeper
        ruleEntityList.add(entity);
        if (!publishRules(app, ruleEntityList)) {
            OPERATE_LOGGER.info("{} publish degrade rules fail after rule add, app: {}, state: {}!",
                userName, entity.getApp(), DataType.OPERATION_FAIL.getDataType());
            return Result.ofFail(SystemUtils.ERROR_CODE, "publish degrade rules fail after rule add!");
        }
        OPERATE_LOGGER.info("{} add degrade rules, rule detail: [{}], state: {}!",
            userName, ruleToString(entity), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(entity);
    }

    /**
     * 修改降级规则
     *
     * @param entity 降级规则实体
     * @return 返回修改降级规则
     */
    @Override
    public Result<DegradeRuleVo> apiUpdateRule(HttpServletRequest httpServletRequest, DegradeRuleVo entity) {
        String app = entity.getApp();
        int grade = entity.getGrade();
        List<DegradeRuleVo> ruleEntityList;
        long id = entity.getId();
        Result<DegradeRuleVo> degradeRuleEntity = null;

        // 获取规则
        try {
            ruleEntityList = ruleProvider.getDegradeRules(app);
            degradeRuleEntity = getDate(ruleEntityList, id, grade, entity);
            if (!degradeRuleEntity.isSuccess()) {
                return degradeRuleEntity;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to modify rule!", e);
            return Result.ofFail(SystemUtils.ERROR_CODE, "Failed to modify rule!");
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} modify degrade rules,rule detail: [{}]!", userName, degradeRuleEntity.getMsg());
        Date date = new Date();
        entity.setGmtModified(date);
        entity.setId(ruleEntityId(entity));
        ruleEntityList.add(entity);
        if (!publishRules(entity.getApp(), ruleEntityList)) {
            OPERATE_LOGGER.info("{} publish degrade rules fail after rule update, app: {}, state: {}!",
                userName, entity.getApp(), DataType.OPERATION_FAIL.getDataType());
            return Result.ofFail(SystemUtils.ERROR_CODE, "Publish degrade rules fail after rule update!");
        }
        OPERATE_LOGGER.info("{} modify degrade rules, rule detail: [{}], state: {}!",
            userName, ruleToString(entity), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(entity);
    }

    /**
     * 从list获取降级规则
     *
     * @param ruleEntityList 降级规则集合
     * @param id             降级规则id
     * @param grade          降级策略
     * @param entity         降级规则实体
     * @param <R>            错误信息
     * @return 返回错误信息，没有错误返回null
     */
    private <R> Result<R> getDate(List<DegradeRuleVo> ruleEntityList,
        long id, int grade, DegradeRuleVo entity) {
        DegradeRuleVo oldEntity = null;
        if (CollectionUtils.isEmpty(ruleEntityList)) {
            return Result.ofFail(SystemUtils.ERROR_CODE,
                "The rule does not exist or has been modified. Please refresh and try again!");
        }
        Iterator<DegradeRuleVo> iterator = ruleEntityList.iterator();
        while (iterator.hasNext()) {
            DegradeRuleVo newEntity = iterator.next();
            long newEntityId = getId(newEntity);

            // 判断是否有该规则，有则删除
            if (id == newEntityId) {
                oldEntity = newEntity;
                iterator.remove();
                break;
            }
        }

        if (oldEntity == null) {
            return Result.ofFail(SystemUtils.ERROR_CODE,
                "The rule does not exist or has been modified. Please refresh and try again!");
        }

        // 修改，可能修改降级策略
        if (entity != null && grade != oldEntity.getGrade()) {
            for (DegradeRuleVo degradeRuleEntity : ruleEntityList) {
                if (id != degradeRuleEntity.getId()
                    && flowRuleEntityId(entity).equals(flowRuleEntityId(degradeRuleEntity))) {
                    return Result.ofFail(SystemUtils.ERROR_CODE, RULE_EXISTS);
                }
            }
        }
        return Result.ofSuccessMsg(ruleToString(oldEntity));
    }

    /**
     * 删除降级规则
     *
     * @param id     降级规则id
     * @param ruleId kie降级规则id
     * @param app    应用名
     * @return 返回降级规则id
     */
    @Override
    public Result<String> apiDeleteRule(HttpServletRequest httpServletRequest, long id, String ruleId, String app) {
        if (id == 0) {
            return Result.ofFail(SystemUtils.ERROR_CODE, "id can't be null");
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} delete degrade rules, id: {}, app: {}!", userName, id, app);
        Result<String> degradeRuleEntity = null;

        // 获取规则
        try {
            List<DegradeRuleVo> ruleEntityList = ruleProvider.getDegradeRules(app);
            degradeRuleEntity = getDate(ruleEntityList, id, 0, null);
            if (!degradeRuleEntity.isSuccess()) {
                return degradeRuleEntity;
            }

            if (!publishRules(app, ruleEntityList)) {
                OPERATE_LOGGER.info("{} publish degrade rules fail after rule delete, id: {}, app: {}, state: {}!",
                    userName, id, app, DataType.OPERATION_FAIL.getDataType());
                return Result.ofFail(SystemUtils.ERROR_CODE, "Publish degrade rules fail after rule delete!");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to delete rule!", e);
            return Result.ofFail(SystemUtils.ERROR_CODE, "Failed to delete rule!");
        }
        OPERATE_LOGGER.info("{} delete degrade rules, id: {}, rule detail: [{}], state: {}!",
            userName, id, degradeRuleEntity.getMsg(), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(String.valueOf(id));
    }

    /**
     * 推送降级规则到zookeeper
     *
     * @param app   应用名
     * @param rules 规则结合
     * @return 成功返回true，失败返回false
     */
    private boolean publishRules(String app, List<DegradeRuleVo> rules) {
        try {
            rulePublisher.publish(app, DataType.DEGRADE.getDataType(), rules);
        } catch (Exception e) {
            LOGGER.error("Failed to publish degrade rules !", e);
            return false;
        }
        return true;
    }

    /**
     * 拼接字符串，判断同一降级规则
     *
     * @param entity 降级规则实体
     * @return 返回降级规则拼接id
     */
    private String flowRuleEntityId(DegradeRuleVo entity) {
        return entity.getApp() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getIp() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getPort() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getResource() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getLimitApp() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getGrade();
    }

    /**
     * 规则转字符串
     *
     * @param entity 规则
     * @return 规则字符串
     */
    private String ruleToString(DegradeRuleVo entity) {
        return "app:" + entity.getApp()
            + " ip:" + entity.getIp()
            + " port:" + entity.getPort()
            + " resource:" + entity.getResource()
            + " limit app:" + entity.getLimitApp()
            + " grade:" + entity.getGrade();
    }

    /**
     * 计算降级id
     *
     * @param entity 降级规则实体
     * @return 返回降级规则id（long）
     */
    private long ruleEntityId(DegradeRuleVo entity) {
        return Md5Util.stringToMd5(flowRuleEntityId(entity) + entity.getCount() + entity.getTimeWindow()).hashCode();
    }

    /**
     * 获取id
     *
     * @param entity 降级规则实体
     * @return 返回降级规则id
     */
    private long getId(DegradeRuleVo entity) {
        if (entity.getId() == null || entity.getId() == SystemUtils.LONG_INITIALIZE) {
            return ruleEntityId(entity);
        }
        return entity.getId();
    }
}
