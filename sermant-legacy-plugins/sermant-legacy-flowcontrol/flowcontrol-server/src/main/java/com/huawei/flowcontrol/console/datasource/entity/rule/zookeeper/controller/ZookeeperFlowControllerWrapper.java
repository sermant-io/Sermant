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
import com.huawei.flowcontrol.console.datasource.entity.rule.wrapper.FlowControllerWrapper;
import com.huawei.flowcontrol.console.entity.FlowRuleVo;
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
 * 流控controller
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
@Component("zookeeper-flowRule")
public class ZookeeperFlowControllerWrapper implements FlowControllerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperFlowControllerWrapper.class);

    private static final Logger OPERATE_LOGGER = LoggerFactory.getLogger("OPERATE_LOGGER");

    /**
     * 直接限流
     */
    private static final int DIRECT_CURRENT_LIMITING = 0;

    /**
     * 关联限流
     */
    private static final int ASSOCIATED_CURRENT_LIMITING = 1;

    /**
     * 链路限流
     */
    private static final int LINK_CURRENT_LIMITING = 2;

    /**
     * 流控效果 default
     */
    private static final int FLOW_CONTROL_EFFECT_DEFAULT = 0;

    /**
     * 流控效果 warm up
     */
    private static final int WARM_UP = 1;

    /**
     * 流控效果 rate limiter
     */
    private static final int RATE_LIMITER = 2;

    @Autowired
    private DynamicRuleProviderExt ruleProvider;
    @Autowired
    private DynamicRulePublisherExt rulePublisher;

    /**
     * 查询流控规则
     *
     * @param app 应用名
     * @return 返回流控规则集合
     */
    @Override
    public Result<List<FlowRuleVo>> apiQueryRules(String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(SystemUtils.ERROR_CODE, "app can't be null or empty");
        }
        try {
            List<FlowRuleVo> rules = ruleProvider.getFlowRules(app);
            if (CollectionUtils.isEmpty(rules)) {
                return Result.ofSuccess(null);
            }

            // 循环遍历数据
            for (FlowRuleVo entity : rules) {
                entity.setId(getId(entity));
            }
            return Result.ofSuccess(rules);
        } catch (Exception e) {
            LOGGER.error("Error when querying flow rules", e);
            return Result.ofFail(SystemUtils.ERROR_CODE, "Error when querying flow rules!");
        }
    }

    /**
     * 新增流控规则
     *
     * @param entity 流控规则实体
     * @return 返回新增流控规则
     */
    @Override
    public Result<FlowRuleVo> apiAddRule(HttpServletRequest httpServletRequest, FlowRuleVo entity) {
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        entity.setLimitApp(entity.getLimitApp().trim());
        entity.setResource(entity.getResource().trim());
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} add flow rules, app: {}!", userName, entity.getApp());

        // 设置id
        entity.setId(ruleEntityId(entity));

        // 查询已有规则，没有规则新建list
        List<FlowRuleVo> ruleEntityList = null;
        try {
            ruleEntityList = ruleProvider.getFlowRules(entity.getApp());
        } catch (Exception e) {
            LOGGER.error("Failed to add flow rule", e);
            return Result.ofFail(SystemUtils.ERROR_CODE, "Failed to add flow rule!");
        }
        if (CollectionUtils.isEmpty(ruleEntityList)) {
            ruleEntityList = new ArrayList<>();
        }
        for (FlowRuleVo newEntity : ruleEntityList) {
            // 判断是否有该规则
            if (flowRuleVoId(entity).equals(flowRuleVoId(newEntity))) {
                return Result.ofFail(SystemUtils.ERROR_CODE, "Rule already exists!");
            }
        }

        // 将规则添加到list中，并推送zookeeper
        ruleEntityList.add(entity);
        if (!publishRules(entity.getApp(), ruleEntityList)) {
            OPERATE_LOGGER.info("{} publish flow rules fail after rule add, app: {}, state：{}!",
                userName, entity.getApp(), DataType.OPERATION_FAIL.getDataType());
            return Result.ofFail(SystemUtils.ERROR_CODE, "publish flow rules fail after rule add!");
        }
        OPERATE_LOGGER.info("{} add flow rules, rule detail: [{}], state: {}!",
            userName, ruleToString(entity), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(entity);
    }

    /**
     * 更新流控规则
     *
     * @param entity 流控规则实体
     * @return 返回修改流控规则
     */
    @Override
    public Result<FlowRuleVo> apiUpdateRule(HttpServletRequest httpServletRequest, FlowRuleVo entity) {
        String app = entity.getApp();
        long id = entity.getId();
        List<FlowRuleVo> ruleEntityList = null;

        // 获取规则
        try {
            ruleEntityList = ruleProvider.getFlowRules(app);
        } catch (Exception e) {
            LOGGER.error("Failed to update flow rule", e);
            return Result.ofFail(SystemUtils.ERROR_CODE, "Failed to update flow rule!");
        }

        Result<FlowRuleVo> flowRuleVoResult = getDate(ruleEntityList, id, entity.getGrade(), entity);
        if (!flowRuleVoResult.isSuccess()) {
            return flowRuleVoResult;
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} modify flow rules,app:{}!", userName, flowRuleVoResult.getMsg());

        // 检查数据是否在合理范围
        Result<FlowRuleVo> result = checkDate(entity);
        if (!result.isSuccess()) {
            return result;
        }

        Date date = new Date();
        entity.setGmtModified(date);

        // 计算id
        entity.setId(ruleEntityId(entity));
        ruleEntityList.add(entity);
        if (!publishRules(entity.getApp(), ruleEntityList)) {
            OPERATE_LOGGER.info("{} publish flow rules fail after rule update, app: {}, state: {}!",
                userName, entity.getApp(), DataType.OPERATION_FAIL.getDataType());
            return Result.ofFail(SystemUtils.ERROR_CODE, "Publish flow rules fail after rule update");
        }
        OPERATE_LOGGER.info("{} modify flow rules, rule detail: [{}], state: {}!",
            userName, ruleToString(entity), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(entity);
    }

    /**
     * 从list获取流控规则
     *
     * @param ruleEntityList 流控规则list
     * @param id             流控规则id
     * @param grade          阈值类型
     * @param entity         流控规则
     * @return 错误信息
     */
    private <R> Result<R> getDate(List<FlowRuleVo> ruleEntityList, long id, int grade, FlowRuleVo entity) {
        FlowRuleVo oldEntity = null;

        // 判断是否有规则
        if (CollectionUtils.isEmpty(ruleEntityList)) {
            return Result.ofFail(SystemUtils.ERROR_CODE,
                "The rule does not exist or has been modified. Please refresh and try again!");
        }
        Iterator<FlowRuleVo> iterator = ruleEntityList.iterator();
        while (iterator.hasNext()) {
            FlowRuleVo newEntity = iterator.next();
            long newEntityId = getId(newEntity);

            // 判断是否有该规则，有则删除
            if (newEntityId == id) {
                oldEntity = newEntity;
                ruleEntityList.remove(newEntity);
                break;
            }
        }
        if (oldEntity == null) {
            return Result.ofFail(SystemUtils.ERROR_CODE,
                "The rule does not exist or has been modified. Please refresh and try again!");
        }

        // 修改，可能修改阈值类型
        if (entity != null && grade != oldEntity.getGrade()) {
            for (FlowRuleVo flowRuleVo : ruleEntityList) {
                if (id != flowRuleVo.getId()
                    && flowRuleVoId(entity).equals(flowRuleVoId(flowRuleVo))) {
                    return Result.ofFail(SystemUtils.ERROR_CODE, "Rule already exists!");
                }
            }
        }
        return Result.ofSuccessMsg(ruleToString(oldEntity));
    }

    /**
     * 删除流控规则
     *
     * @param id     流控规则id
     * @param ruleId kie专用，不考虑
     * @param app    应用名称
     * @return 返回流控规则id
     */
    @Override
    public Result<String> apiDeleteRule(HttpServletRequest httpServletRequest, Long id, String ruleId, String app) {
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} delete flow rules, id: {}, app: {}!", userName, id, app);
        List<FlowRuleVo> ruleEntityList = null;
        String errorMessage = "Publish flow rules failed after rule delete";

        // 获取规则
        try {
            ruleEntityList = ruleProvider.getFlowRules(app);
        } catch (Exception e) {
            LOGGER.error(errorMessage);
            return Result.ofFail(SystemUtils.ERROR_CODE, errorMessage);
        }

        Result<String> flowRuleResult = getDate(ruleEntityList, id, 0, null);
        if (!flowRuleResult.isSuccess()) {
            return flowRuleResult;
        }

        if (!publishRules(app, ruleEntityList)) {
            OPERATE_LOGGER.info("{} publish flow rules fail after rule delete, id: {}, app: {}, state: {}!",
                userName, id, app, DataType.OPERATION_FAIL.getDataType());
            return Result.ofFail(SystemUtils.ERROR_CODE, errorMessage);
        }
        OPERATE_LOGGER.info("{} delete flow rules, id: {}, rule detail: [{}], state: {}!",
            userName, id, flowRuleResult.getMsg(), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(String.valueOf(id));
    }

    /**
     * 检查数据是否在合理范围
     *
     * @param entity 流控规则实体
     * @return 返回错误信息，没有错误返回null
     */
    private <R> Result<R> checkDate(FlowRuleVo entity) {
        int grade = entity.getGrade();
        int strategy = entity.getStrategy();
        String refResource = entity.getRefResource();
        int controlBehavior = entity.getControlBehavior();
        String message = null;
        String got = " got";
        if (grade != 0 && grade != 1) {
            message = "grade must be 0 or 1, but " + grade + got;
        }
        if (message == null && strategy != 0) {
            message = checkStrategy(strategy, refResource, got);
        }
        if (message == null && controlBehavior != FLOW_CONTROL_EFFECT_DEFAULT) {
            message = checkControlBehavior(controlBehavior, entity, got);
        }
        if (message == null) {
            return Result.ofSuccessMsg("success");
        } else {
            return Result.ofFail(SystemUtils.ERROR_CODE, message);
        }
    }

    /**
     * 流控效果检查
     *
     * @param controlBehavior 流控效果
     * @param entity          流控实体
     * @param got             got信息
     * @return 错误信息
     */
    private String checkControlBehavior(int controlBehavior, FlowRuleVo entity, String got) {
        String message = null;
        if (controlBehavior != WARM_UP && controlBehavior != RATE_LIMITER) {
            message = "controlBehavior must be in [0, 1, 2], but " + controlBehavior + got;
        }
        if (message == null && controlBehavior == WARM_UP && entity.getWarmUpPeriodSec() == null) {
            message = "warmUpPeriodSec can't be null when controlBehavior==1";
        }
        if (message == null && controlBehavior == RATE_LIMITER && entity.getMaxQueueingTimeMs() == null) {
            message = "maxQueueingTimeMs can't be null when controlBehavior==2";
        }
        return message;
    }

    /**
     * 检查限流策略
     *
     * @param strategy    限流策略
     * @param refResource 关联资源
     * @param got         got信息
     * @return 错误信息
     */
    private String checkStrategy(int strategy, String refResource, String got) {
        String message = null;
        if (strategy != DIRECT_CURRENT_LIMITING
            && strategy != ASSOCIATED_CURRENT_LIMITING
            && strategy != LINK_CURRENT_LIMITING) {
            message = "strategy must be in [0, 1, 2], but " + strategy + got;
        }
        if (message == null && strategy != DIRECT_CURRENT_LIMITING) {
            if (StringUtil.isBlank(refResource)) {
                message = "refResource can't be null or empty when strategy!=0";
            }
        }
        return message;
    }

    /**
     * 推送规则到zookeeper
     *
     * @param app   应用名
     * @param rules 流控规则集合
     * @return 成功返回true，失败返回false
     */
    private boolean publishRules(String app, List<FlowRuleVo> rules) {
        try {
            rulePublisher.publish(app, DataType.FLOW.getDataType(), rules);
        } catch (Exception e) {
            LOGGER.error("Failed to publish flow rules !", e);
            return false;
        }
        return true;
    }

    /**
     * 拼接字符串，判断同一规则
     *
     * @param entity 流控规则实体
     * @return 返回拼接流控规则id
     */
    private String flowRuleVoId(FlowRuleVo entity) {
        return entity.getApp() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getIp() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getPort() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getResource() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getLimitApp() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getRefResource() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getGrade() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getStrategy() + DataType.SEPARATOR_COLON.getDataType()
            + entity.getControlBehavior() + DataType.SEPARATOR_COLON.getDataType()
            + entity.isClusterMode();
    }

    /**
     * 规则转字符串
     *
     * @param entity 规则
     * @return 规则字符串
     */
    private String ruleToString(FlowRuleVo entity) {
        return "app:" + entity.getApp()
            + " ip:" + entity.getIp()
            + " port:" + entity.getPort()
            + " resource:" + entity.getResource()
            + " limit app:" + entity.getLimitApp()
            + " ref resource:" + entity.getRefResource()
            + " grade:" + entity.getGrade()
            + " strategy:" + entity.getStrategy()
            + " control behavior:" + entity.getControlBehavior();
    }

    /**
     * 计算id
     *
     * @param entity 流控规则实体
     * @return 返回流控规则id（long）
     */
    private long ruleEntityId(FlowRuleVo entity) {
        return Md5Util.stringToMd5(flowRuleVoId(entity) + entity.getCount()).hashCode();
    }

    /**
     * 获取流控规则id
     *
     * @param entity 流控规则实体
     * @return 流控规则id
     */
    private long getId(FlowRuleVo entity) {
        if (entity.getId() == null || entity.getId() == SystemUtils.LONG_INITIALIZE) {
            return ruleEntityId(entity);
        }
        return entity.getId();
    }
}
