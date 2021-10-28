/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
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
 * @since 2021/10/25
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
        new DemoAnnotationService().instFunc();
        // 测试命名的方式增强的功能点
        DemoNameService.staticFunc(); // 此处还会一同测试日志功能和统一配置功能
        new DemoNameService().instFunc();
        // 测试超类的方式增强的功能点
        DemoSuperTypeService.staticFunc();
        new DemoSuperTypeService().instFunc();
        // 测试链路监控功能
        DemoTraceService.trace();
        DemoTraceService.trace();
        DemoTraceService.trace();
        // 测试启动类增强
        Thread.getAllStackTraces();
        new Thread().setName("demo-thread-test");
    }
}
