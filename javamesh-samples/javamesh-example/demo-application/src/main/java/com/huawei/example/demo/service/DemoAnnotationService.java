/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.service;

/**
 * 被注解修饰的示例被拦截点
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
@DemoAnnotation
public class DemoAnnotationService {
    /**
     * 被拦截的构造函数
     */
    public DemoAnnotationService() {
        System.out.println("DemoAnnotationEntity: constructor");
    }

    /**
     * 被拦截的实例方法
     */
    public void instFunc() {
        System.out.println("DemoAnnotationEntity: instFunc");
    }

    /**
     * 被拦截的静态方法
     */
    public static void staticFunc() {
        System.out.println("DemoAnnotationEntity: staticFunc");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
