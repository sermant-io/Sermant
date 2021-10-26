/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.service;

/**
 * 名称匹配的示例被拦截点
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoNameService {
    /**
     * 被拦截的构造函数
     */
    public DemoNameService() {
        System.out.println("DemoNameEntity: constructor");
    }

    /**
     * 被拦截的实例方法
     */
    public void instFunc() {
        System.out.println("DemoNameEntity: instFunc");
    }

    /**
     * 被拦截的静态方法
     */
    public static void staticFunc() {
        System.out.println("DemoNameEntity: staticFunc");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
