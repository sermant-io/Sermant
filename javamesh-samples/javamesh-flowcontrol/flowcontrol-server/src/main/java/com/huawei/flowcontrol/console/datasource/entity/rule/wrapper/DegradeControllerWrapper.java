/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.wrapper;

import com.huawei.flowcontrol.console.entity.DegradeRuleVo;
import com.huawei.flowcontrol.console.entity.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 降级规则接口
 *
 * @author wxl
 * @since 2020-12-26
 */
public interface DegradeControllerWrapper {
    /**
     * 查询规则
     *
     * @param app 应用名
     * @return Result
     */
    Result<List<DegradeRuleVo>> apiQueryRules(String app);

    /**
     * 添加规则
     *
     * @param httpServletRequest 请求对象
     * @param entity             降级规则实例
     * @return Result
     */
    Result<DegradeRuleVo> apiAddRule(HttpServletRequest httpServletRequest, DegradeRuleVo entity);

    /**
     * 更新规则
     *
     * @param httpServletRequest 请求对象
     * @param entity             降级规则实例
     * @return Result
     */
    Result<DegradeRuleVo> apiUpdateRule(HttpServletRequest httpServletRequest, DegradeRuleVo entity);

    /**
     * 删除规则
     *
     * @param httpServletRequest 请求对象
     * @param id                 ID
     * @param extInfo            扩展信息
     * @param app                应用名
     * @return Result String
     */
    Result<String> apiDeleteRule(HttpServletRequest httpServletRequest, long id, String extInfo, String app);
}
