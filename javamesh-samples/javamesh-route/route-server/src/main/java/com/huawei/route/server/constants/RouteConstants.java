/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.constants;

/**
 * 路由公共参数
 *
 * @author zhouss
 * @since 2021-10-18
 */
public class RouteConstants {
    /**
     * 匹配值分割符
     */
    public static final String VALUES_SEPARATOR = ",";

    /**
     * 服务映射初始化容量大小
     */
    public static final int INIT_SERVICE_MAPPER_CAPACITY = 2;

    /**
     * 标记实例版本的标签键
     */
    public static final String TAG_NAME_KEY = "version";

    /**
     * LDC标签键
     */
    public static final String LDC_KEY = "ldc";

    /**
     * LDC缺省值
     */
    public static final String DEFAULT_LDC = "DEFAULT_LDC";

    /**
     * 标签缺省值
     */
    public static final String DEFAULT_TAG_NAME = "DEFAULT_VERSION";

    /**
     * 标签通知更新路径
     */
    public static final String TAG_NOTIFIER_PATH = "/notifier/gray/tag";

    /**
     * 标签通知更新路径
     */
    public static final String SHARE_NOTIFIER_PATH = "/notifier/gray/share";

    /**
     * 通用分隔符
     */
    public static final String COMMON_SEPARATOR = "@";

    /**
     * 数据分隔数
     */
    public static final int TAG_NOTIFIER_CONTENT_LEN = 3;

    /**
     * 最大权重百分比
     */
    public static final int MAX_WEIGHT_PERCENT = 100;

    /**
     * dubbo协议前缀
     */
    public static final String DUBBO_PROTOCOL_PREFIX = "dubbo://";

    /**
     * 延时初始化时间
     */
    public static final long INIT_WAIT_MS = 5 * 1000;
}
