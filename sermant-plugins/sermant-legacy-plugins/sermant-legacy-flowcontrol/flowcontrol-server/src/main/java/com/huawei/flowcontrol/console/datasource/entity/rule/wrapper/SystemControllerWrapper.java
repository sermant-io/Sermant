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
