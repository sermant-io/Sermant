/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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
import com.huawei.flowcontrol.console.datasource.entity.rule.wrapper.ParamFlowControllerWrapper;
import com.huawei.flowcontrol.console.entity.ParamFlowRuleVo;
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
 * 热点controller
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
@Component("zookeeper-paramFlowRule")
public class ZookeeperParamFlowRuleControllerWrapper implements ParamFlowControllerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperParamFlowRuleControllerWrapper.class);

    private static final Logger OPERATE_LOGGER = LoggerFactory.getLogger("OPERATE_LOGGER");

    /**
     * 失败ERROR_CODE
     */
    private static final int ERROR_CODE = -1;

    /**
     * id分割符
     */
    private static final String ID_SEPARATOR = ":";

    @Autowired
    private DynamicRuleProviderExt ruleProvider;
    @Autowired
    private DynamicRulePublisherExt rulePublisher;

    /**
     * 查询热点规则
     *
     * @param app 应用名
     * @return 返回热点规则集合
     */
    @Override
    public Result<List<ParamFlowRuleVo>> apiQueryRules(String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(ERROR_CODE, "app cannot be null or empty");
        }

        List<ParamFlowRuleVo> rules = null;
        try {
            rules = ruleProvider.getParamFlowRules(app);
        } catch (Exception e) {
            LOGGER.error("Error when querying parameter flow rules", e);
            return Result.ofFail(ERROR_CODE, "Error when querying parameter flow rules!");
        }

        if (CollectionUtils.isEmpty(rules)) {
            for (ParamFlowRuleVo entity : rules) {
                entity.setId(getId(entity));
            }
        }
        return Result.ofSuccess(rules);
    }

    /**
     * 新增热点规则
     *
     * @param entity 热点规则实体
     * @return 返回新增热点规则
     */
    @Override
    public Result<ParamFlowRuleVo> apiAddRule(HttpServletRequest httpServletRequest, ParamFlowRuleVo entity) {
        entity.getRule().setResource(entity.getResource().trim());
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} add param flow rules, app: {}!", userName, entity.getApp());
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        entity.setId(ruleEntityId(entity));
        List<ParamFlowRuleVo> ruleEntityList = null;
        try {
            ruleEntityList = ruleProvider.getParamFlowRules(entity.getApp());
        } catch (Exception e) {
            LOGGER.error("Error when adding new parameter flow rules", e);
            return Result.ofFail(ERROR_CODE, "Error when adding new parameter flow rules!");
        }
        if (CollectionUtils.isEmpty(ruleEntityList)) {
            ruleEntityList = new ArrayList<>();
        }
        for (ParamFlowRuleVo newEntity : ruleEntityList) {
            // 判断是否有该规则
            if (flowRuleEntityId(entity).equals(flowRuleEntityId(newEntity))) {
                return Result.ofFail(ERROR_CODE, "Rule already exists!");
            }
        }
        ruleEntityList.add(entity);
        if (!publishRules(entity.getApp(), ruleEntityList)) {
            OPERATE_LOGGER.info("{} publish param flow rules fail after rule add, app: {}, state:{}!",
                userName, entity.getApp(), DataType.OPERATION_FAIL.getDataType());
            return Result.ofFail(ERROR_CODE, "Publish param flow rules failed after rule add");
        }
        OPERATE_LOGGER.info("{} add param flow rules, rule detail: [{}],state: {}!",
            userName, ruleToString(entity), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(entity);
    }

    /**
     * 更新热点规则
     *
     * @param entity 热点规则实体
     * @return 返回更新热点规则
     */
    @Override
    public Result<ParamFlowRuleVo> apiUpdateRule(HttpServletRequest httpServletRequest,
        ParamFlowRuleVo entity) {
        long id = entity.getId();
        ParamFlowRuleVo oldEntity = null;
        List<ParamFlowRuleVo> ruleEntityList = null;

        // 获取规则
        try {
            ruleEntityList = ruleProvider.getParamFlowRules(entity.getApp());
            if (!CollectionUtils.isEmpty(ruleEntityList)) {
                oldEntity = getDate(ruleEntityList, id);
            }

            if (oldEntity == null) {
                return Result.ofFail(ERROR_CODE,
                    "The rule does not exist or has been modified. Please refresh and try again!");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to modify rule!");
            return Result.ofFail(ERROR_CODE, "Failed to modify rule!");
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} modify param flow rules, rule detail: [{}]!", userName, ruleToString(oldEntity));
        Date date = new Date();
        entity.setGmtCreate(oldEntity.getGmtCreate());
        entity.setGmtModified(date);
        entity.setId(ruleEntityId(entity));

        // 修改，可能修改是否集群，判断是否有该规则
        if (entity.isClusterMode() != oldEntity.isClusterMode()) {
            for (ParamFlowRuleVo paramFlowRuleVo : ruleEntityList) {
                if (flowRuleEntityId(entity).equals(flowRuleEntityId(paramFlowRuleVo))) {
                    return Result.ofFail(ERROR_CODE, "Rule already exists!");
                }
            }
        }
        ruleEntityList.add(entity);
        if (!publishRules(entity.getApp(), ruleEntityList)) {
            OPERATE_LOGGER.info("{} publish param flow rules fail after rule update, app: {}, state: {}!",
                userName, entity.getApp(), DataType.OPERATION_FAIL.getDataType());
            return Result.ofFail(ERROR_CODE, "Publish param flow rules failed after rule update");
        }
        OPERATE_LOGGER.info("{} modify param flow rules, rule detail: [{}], state: {}!",
            userName, ruleToString(entity), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(entity);
    }

    /**
     * 从list获取热点规则
     *
     * @param ruleEntityList 热点规则list
     * @param id             热点规则id
     * @return 返回热点规则
     */
    private ParamFlowRuleVo getDate(List<ParamFlowRuleVo> ruleEntityList, long id) {
        ParamFlowRuleVo oldEntity = null;

        Iterator<ParamFlowRuleVo> iterator = ruleEntityList.iterator();
        while (iterator.hasNext()) {
            ParamFlowRuleVo newEntity = iterator.next();
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
     * 删除热点规则
     *
     * @param id     热点规则id
     * @param ruleId kie规则id
     * @param app    应用名
     * @return 返回热点规则id
     */
    @Override
    public Result<String> apiDeleteRule(HttpServletRequest httpServletRequest, long id, String ruleId, String app) {
        if (id == 0) {
            return Result.ofFail(ERROR_CODE, "id cannot be null");
        }
        String userName = SystemUtils.getUserName(httpServletRequest);
        OPERATE_LOGGER.info("{} delete param flow rules, id: {}, app: {}!", userName, id, app);
        List<ParamFlowRuleVo> ruleEntityList = null;
        String errorMessage = "Publish param flow rules failed after rule delete";

        // 获取规则
        try {
            ruleEntityList = ruleProvider.getParamFlowRules(app);
        } catch (Exception e) {
            LOGGER.info(errorMessage);
            return Result.ofFail(ERROR_CODE, errorMessage);
        }

        ParamFlowRuleVo oldEntity = null;
        if (!CollectionUtils.isEmpty(ruleEntityList)) {
            oldEntity = getDate(ruleEntityList, id);
        }

        if (oldEntity == null) {
            return Result.ofFail(ERROR_CODE,
                "The rule does not exist or has been modified. Please refresh and try again!");
        }

        if (!publishRules(app, ruleEntityList)) {
            OPERATE_LOGGER.info("{} publish param flow rules fail after rule delete,id:{},app:{},state:{}!",
                userName, id, app, DataType.OPERATION_FAIL.getDataType());
            return Result.ofFail(ERROR_CODE, errorMessage);
        }
        OPERATE_LOGGER.info("{} delete param flow rules, id: {}, rule detail: [{}], state: {}!",
            userName, id, ruleToString(oldEntity), DataType.OPERATION_SUCCESS.getDataType());
        return Result.ofSuccess(String.valueOf(id));
    }

    /**
     * 推送热点规则到zookeeper
     *
     * @param app   应用名称
     * @param rules 热点规则list
     * @return 成功返回true，失败返回false
     */
    private boolean publishRules(String app, List<ParamFlowRuleVo> rules) {
        try {
            rulePublisher.publish(app, DataType.PARAMFLOW.getDataType(), rules);
        } catch (Exception e) {
            LOGGER.error("Failed to publish parameter flow rules !", e);
            return false;
        }
        return true;
    }

    /**
     * 拼接字符串，判断同一规则
     *
     * @param entity 热点规则实体
     * @return 返回拼接热点规则id
     */
    private String flowRuleEntityId(ParamFlowRuleVo entity) {
        return entity.getApp() + ID_SEPARATOR + entity.getIp() + ID_SEPARATOR + entity.getPort() + ID_SEPARATOR
            + entity.getResource() + ID_SEPARATOR + entity.getLimitApp() + ID_SEPARATOR
            + entity.getGrade() + ID_SEPARATOR + entity.getControlBehavior() + ID_SEPARATOR + entity.isClusterMode();
    }

    /**
     * 规则转字符串
     *
     * @param entity 规则
     * @return 规则字符串
     */
    private String ruleToString(ParamFlowRuleVo entity) {
        return "app:" + entity.getApp()
            + " ip:" + entity.getIp()
            + " port:" + entity.getPort()
            + " resource:" + entity.getResource()
            + " limit app:" + entity.getLimitApp()
            + " grade:" + entity.getGrade()
            + " controlBehavior:" + entity.getControlBehavior()
            + " isClusterMode:" + entity.isClusterMode();
    }

    /**
     * 计算热点规则id
     *
     * @param entity 热点规则实体
     * @return 返回热点规则id（long）
     */
    private long ruleEntityId(ParamFlowRuleVo entity) {
        return Md5Util.stringToMd5(flowRuleEntityId(entity)
            + entity.getCount() + entity.getParamIdx() + entity.getDurationInSec()).hashCode();
    }

    /**
     * 获取热点规则id
     *
     * @param entity 热点规则实体
     * @return 返回热点规则id
     */
    private long getId(ParamFlowRuleVo entity) {
        long idLong = entity.getId();
        if (idLong == 0) {
            idLong = ruleEntityId(entity);
        }
        return idLong;
    }
}
