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

/**
 * 示例应用
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class DemoApplication {
    /**
     * 主要方法
     *
     * @param args 入参
     */
    public static void main(String[] args) {
        // 测试通过注解修饰的方式增强的功能点
        DemoAnnotationService.staticFunc();
        new DemoAnnotationService().memberFunc();

        // 测试命名的方式增强的功能点
        DemoNameService.staticFunc();
        new DemoNameService().memberFunc();

        // 测试超类的方式增强的功能点
        DemoSuperTypeService.staticFunc();
        new DemoSuperTypeService().memberFunc();

        // 测试服务
        DemoNameService.serviceFunc();

        // 测试统一配置
        DemoNameService.configFunc();

        // 测试字段设置
        final DemoNameService nameService = new DemoNameService();
        nameService.fieldFunc();
        nameService.fieldFunc();
        new DemoNameService().fieldFunc();

        // 测试接口
        nameService.interfaceFunc();

        // 测试链路监控功能
        DemoTraceService.trace();
        DemoTraceService.trace();
        DemoTraceService.trace();

        // 测试启动类增强
        Thread.getAllStackTraces();
        new Thread().setName("demo-thread-test");
    }
}
