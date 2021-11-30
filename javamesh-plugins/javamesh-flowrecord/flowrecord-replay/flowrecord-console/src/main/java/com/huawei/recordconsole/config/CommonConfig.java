/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.recordconsole.config;

/**
 * 常量类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-04-13
 */
public class CommonConfig {
    public static final int THREAD_POOL_SIZE = 5;

    public static final int THREAD_POOL_MAX_SIZE = 10;

    public static final int THREAD_POOL_CAPACITY = 100;

    public static final int RECORD_COMMIT_RETRIES_TIME = 5;

    public static final String RECORDJOB_TYPE = "type";

    public static final String RECORDJOB_KEYWORD = "keyword";

    public static final String ES_INDEX_SEPARATOR = "@";

    public static final int ES_FORMAT_NUM = 6;

    public static final int ES_SAVING_DOC_NUM = 1000;

    public static final String ES_STORAGE_BEGINNING = "@000001";

    public static final String ES_FORMAT_ZERO = "%0";

    /**
     * attachments字段
     */
    public static final String ATTACHMENTS_FIELD = "attachments";

    /**
     * interface字段
     */
    public static final String INTERFACE_FIELD = "interface";

    /**
     * arguments字段
     */
    public static final String ARGUMENTS_FIELD = "arguments";

    /**
     * zookeeper项目根节点
     */
    public static final String PROJECT_NODE = "/flow_record_replay";

    /**
     * zookeeper脱敏模块节点
     */
    public static final String DESENSITIZE_NODE = "/desensitization";

    /**
     * dubbo应用层级脱敏规则节点
     */
    public static final String GENERAL_NODE = "/general_rule";

    /**
     * 遮盖脱敏方式
     */
    public static final String COVER_TYPE = "cover";

    /**
     * 字符偏移脱敏方式
     */
    public static final String OFFSET_TYPE = "offset";

    /**
     * groovy脚本脱敏方式
     */
    public static final String GROOVY_TYPE = "groovy";

    /**
     * 斜杠符号"/"
     */
    public static final String SLASH = "/";

    /**
     * Dubbo应用类型
     */
    public static final String DUBBO = "Dubbo";
}
