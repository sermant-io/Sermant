/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.mockserver.config;

/**
 * Mock Server 字符串常量
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-05-05
 */
public class MSConst {
    /**
     * 配置跳过的method列表的zk节点
     */
    public static final String SKIP_METHOD = "/mock_method/";

    /**
     * 子调用index前缀
     */
    public static final String SUB_CALL_RECORD_PREFIX = "subcall_";

    /**
     * 子调用查找的keyword
     */
    public static final String SUB_CALL_KEY = "subCallKey";

    /**
     * 子调用计数
     */
    public static final String SUB_CALL_COUNT = "subCallCount";

    /**
     * 录制应用类型
     */
    public static final String APP_TYPE = "appType";

    /**
     * 录制请求体
     */
    public static final String REQUEST_BODY = "requestBody";

    /**
     * 录制返回值类型
     */
    public static final String RESPONSE_CLASS = "responseClass";

    /**
     * 录制返回值
     */
    public static final String RESPONSE_BODY = "responseBody";

    /**
     * Dubbo
     */
    public static final String DUBBO = "Dubbo";

    /**
     * Redisson
     */
    public static final String REDISSON = "Redisson";

    /**
     * 逗号
     */
    public static final String COMMA = ",";

    /**
     * 冒号
     */
    public static final String COLON = ":";


}
