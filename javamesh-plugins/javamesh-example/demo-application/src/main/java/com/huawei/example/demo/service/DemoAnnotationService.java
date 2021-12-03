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

package com.huawei.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 被注解修饰的示例被拦截点
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
@DemoAnnotation
public class DemoAnnotationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoAnnotationService.class);
    /**
     * 被拦截的构造函数
     */
    public DemoAnnotationService() {
        LOGGER.info("DemoAnnotationEntity: constructor");
    }

    /**
     * 被拦截的实例方法
     */
    public void instFunc() {
        LOGGER.info("DemoAnnotationEntity: instFunc");
    }

    /**
     * 被拦截的静态方法
     */
    public static void staticFunc() {
        LOGGER.info("DemoAnnotationEntity: staticFunc");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
