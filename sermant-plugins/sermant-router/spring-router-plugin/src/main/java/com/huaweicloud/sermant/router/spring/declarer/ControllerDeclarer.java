/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.declarer;

import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 增强Controller的RequestMapping方法
 *
 * @author provenceee
 * @since 2022-10-29
 */
public class ControllerDeclarer extends AbstractDeclarer {
    private static final String CONTROLLER_ANNOTATION = "org.springframework.stereotype.Controller";

    private static final String REST_CONTROLLER_ANNOTATION = "org.springframework.web.bind.annotation.RestController";

    private static final String INTERCEPT_CLASS
            = "com.huaweicloud.sermant.router.spring.interceptor.ControllerInterceptor";

    private static final String REQUEST_MAPPING_NAME = "org.springframework.web.bind.annotation.RequestMapping";

    private static final String POST_MAPPING_NAME = "org.springframework.web.bind.annotation.PostMapping";

    private static final String GET_MAPPING_NAME = "org.springframework.web.bind.annotation.GetMapping";

    private static final String DELETE_MAPPING_NAME = "org.springframework.web.bind.annotation.DeleteMapping";

    private static final String PATCH_MAPPING_NAME = "org.springframework.web.bind.annotation.PatchMapping";

    private static final String PUT_MAPPING_NAME = "org.springframework.web.bind.annotation.PutMapping";

    /**
     * 构造方法
     */
    public ControllerDeclarer() {
        super(null, INTERCEPT_CLASS, null);
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.isAnnotatedWith(CONTROLLER_ANNOTATION)
                .or(ClassMatcher.isAnnotatedWith(REST_CONTROLLER_ANNOTATION));
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return MethodMatcher.isAnnotatedWith(REQUEST_MAPPING_NAME)
                .or(MethodMatcher.isAnnotatedWith(POST_MAPPING_NAME))
                .or(MethodMatcher.isAnnotatedWith(GET_MAPPING_NAME))
                .or(MethodMatcher.isAnnotatedWith(DELETE_MAPPING_NAME))
                .or(MethodMatcher.isAnnotatedWith(PATCH_MAPPING_NAME))
                .or(MethodMatcher.isAnnotatedWith(PUT_MAPPING_NAME));
    }
}