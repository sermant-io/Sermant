/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.datasource.entity.rule.wrapper;

import com.huawei.flowcontrol.console.entity.Result;
import com.huawei.flowcontrol.console.entity.SystemRuleVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * SystemController的包装类
 *
 * @author wxl
 * @since 2020-12-26
 */
public interface SystemControllerWrapper {
    /**
     * 查询规则
     *
     * @param app 应用名
     * @return Result
     */
    Result<List<SystemRuleVo>> apiQueryRules(String app);

    /**
     * 添加规则
     *
     * @param httpServletRequest 请求对象
     * @param entity             规则实例
     * @return Result
     */
    Result<SystemRuleVo> apiAddRule(HttpServletRequest httpServletRequest, SystemRuleVo entity);

    /**
     * 更新规则
     *
     * @param httpServletRequest 请求对象
     * @param entity             规则实例
     * @return Result
     */
    Result<SystemRuleVo> apiUpdateRule(HttpServletRequest httpServletRequest, SystemRuleVo entity);

    /**
     * 删除规则
     *
     * @param httpServletRequest 请求对象
     * @param id                 ID
     * @param extInfo            扩展信息
     * @param app                应用名
     * @return Result
     */
    Result<String> apiDeleteRule(HttpServletRequest httpServletRequest, long id, String extInfo, String app);
}
