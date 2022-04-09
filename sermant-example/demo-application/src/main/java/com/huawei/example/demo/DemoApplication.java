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

package com.huawei.example.demo;

import com.huawei.example.demo.service.DemoAnnotationService;
import com.huawei.example.demo.service.DemoNameService;
import com.huawei.example.demo.service.DemoSuperTypeService;
import com.huawei.example.demo.service.DemoTraceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 示例应用
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class DemoApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoApplication.class);

    /**
     * 主要方法
     *
     * @param args 入参
     */
    public static void main(String[] args) {
        annotationAndMethodTypeTest();
        superTypeAndMethodNameTest();
        classNameEqualsAndBaseFuncTest();
        classInfixAndAnnotationTest();
        classPrefixAndReturnTypeTest();
        classSuffixAndParamsTest();
        pluginServiceAndConfigTest();
        tracingFuncTest();
    }

    /**
     * 通过注解匹配类，通过方法类型匹配方法进行增强
     */
    private static void annotationAndMethodTypeTest() {
        LOGGER.info("annotationAndMethodTypeTest");
        DemoAnnotationService.staticFunc();
        new DemoAnnotationService().memberFunc();
    }

    /**
     * 通过超类匹配类，通过方法名匹配方法进行增强
     */
    private static void superTypeAndMethodNameTest() {
        LOGGER.info("superTypeAndMethodNameTest");
        DemoSuperTypeService demoSuperTypeService = new DemoSuperTypeService();
        demoSuperTypeService.memberFunc();
        demoSuperTypeService.prefixFunc();
        demoSuperTypeService.memberInfixFunc();
        demoSuperTypeService.funcSuffix();
    }

    /**
     * 通过类名精确匹配、字段设置、测试接口
     */
    private static void classNameEqualsAndBaseFuncTest() {
        LOGGER.info("classNameEqualsAndBaseFuncTest");
        final DemoNameService demoNameService = new DemoNameService();

        // 测试字段设置
        demoNameService.fieldFunc();
        demoNameService.fieldFunc();

        // 测试接口
        DemoNameService.interfaceFunc();
    }

    /**
     * 通过类名内缀匹配，通过注解匹配方法进行增强
     */
    private static void classInfixAndAnnotationTest() {
        LOGGER.info("classInfixAndAnnotationTest");
        DemoNameService.annotationFunc();
    }

    /**
     * 通过类名前缀匹配，通过方法返回值类型匹配方法进行增强
     */
    private static void classPrefixAndReturnTypeTest() {
        LOGGER.info("classPrefixAndReturnTypeTest");
        DemoNameService.returnTypeFunc();
    }

    /**
     * 通过类名后缀匹配，通过参数数量&&参数类型匹配方法进行增强
     */
    private static void classSuffixAndParamsTest() {
        LOGGER.info("classSuffixAndParamsTest");
        DemoNameService.paramsCountAndTypeFunc("A", 0);
    }

    /**
     * 启动类增强
     */
    private static void bootstrapClassEnhanceTest() {
        LOGGER.info("bootstrapClassEnhanceTest");

        // 测试启动类增强
        Thread.getAllStackTraces();
        new Thread().setName("demo-thread-test");
    }

    /**
     * 插件服务测试、插件配置测试
     */
    private static void pluginServiceAndConfigTest() {
        LOGGER.info("pluginServiceAndConfigTest");
        DemoNameService.serviceFunc();
    }

    /**
     * 链路追踪功能
     */
    private static void tracingFuncTest() {
        LOGGER.info("tracingFuncTest");
        DemoTraceService.trace();
    }
}
