/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.wrapper;

import com.huawei.flowcontrol.console.entity.FlowRuleVo;
import com.huawei.flowcontrol.console.entity.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 流控规则接口
 *
 * @author wxl
 * @since 2020-12-26
 */
public interface FlowControllerWrapper {
    /**
     * 查询规则
     *
     * @param app 应用名
     * @return Result
     */
    Result<List<FlowRuleVo>> apiQueryRules(String app);

    /**
     * 添加规则
     *
     * @param httpServletRequest 请求对象
     * @param entity             规则实例
     * @return Result
     */
    Result<FlowRuleVo> apiAddRule(HttpServletRequest httpServletRequest, FlowRuleVo entity);

    /**
     * 更新规则
     *
     * @param httpServletRequest 请求对象
     * @param entity             规则实例
     * @return Result
     */
    Result<FlowRuleVo> apiUpdateRule(HttpServletRequest httpServletRequest, FlowRuleVo entity);

    /**
     * 删除规则
     *
     * @param httpServletRequest 请求对象
     * @param id                 ID
     * @param extInfo            扩展信息
     * @param app                应用名
     * @return Result
     */
    Result<String> apiDeleteRule(HttpServletRequest httpServletRequest, Long id, String extInfo, String app);
}
