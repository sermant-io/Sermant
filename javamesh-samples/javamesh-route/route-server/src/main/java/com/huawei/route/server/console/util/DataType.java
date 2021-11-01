/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.route.server.console.util;

/**
 * 枚举类定义常量
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
public enum DataType {
    /**
     * 流控规则
     */
    FLOW("/flow"),

    /**
     * redis存储流控规则前缀
     */
    FLOW_RULE_KEY("sentinel_flow_rule_"),

    /**
     * 热点规则
     */
    PARAMFLOW("/paramFlow"),

    /**
     * redis存储热点规则前缀
     */
    PARAMFLOW_RULE_KEY("sentinel_paramFlow_rule_"),

    /**
     * 降级规则
     */
    DEGRADE("/degrade"),

    /**
     * redis存储降级规则前缀
     */
    DEGRADE_RULE_KEY("sentinel_degrade_rule_"),

    /**
     * 授权规则
     */
    AUTHORITY("/authority"),

    /**
     * redis存储授权规则前缀
     */
    AUTHORITY_RULE_KEY("sentinel_authority_rule_"),

    /**
     * 系统规则
     */
    SYSTEM("/system"),

    /**
     * redis存储系统规则前缀
     */
    SYSTEM_RULE_KEY("sentinel_system_rule_"),

    /**
     * zk保存前缀
     */
    RULE_ROOT_PATH("/sentinel_rule_config"),

    /**
     * redis存储应用名
     */
    APPNAMES("appnames"),

    /**
     * 心跳数据
     */
    HEARTBEAT_DATA("heartbeat"),

    /**
     * 监控数据存储redis后缀
     */
    REDISMETRICE("metric"),

    /**
     * CAS加载顺序
     */
    ORDER_ONE("1"),

    /**
     * CAS加载顺序
     */
    ORDER_TWO("2"),

    /**
     * swagger 在线接口文档扫描路径
     */
    SWAGGER_SCAN_BASE_PACKAGE("com.huawei.flowcontrol.controller"),

    /**
     * 流控规则controllerWrapper后缀
     */
    FLOW_RULE_SUFFIX("-flowRule"),

    /**
     * 降级规则controllerWrapper后缀
     */
    DEGRADE_RULE_SUFFIX("-degradeRule"),

    /**
     * 系统规则controllerWrapper后缀
     */
    SYSTEM_RULE_SUFFIX("-systemRule"),

    /**
     * 热点参数规则controllerWrapper后缀
     */
    PARAMFLOW_RULE_SUFFIX("-paramFlowRule"),

    /**
     * 操作成功
     */
    OPERATION_SUCCESS("SUCCESS"),

    /**
     * 操作失败
     */
    OPERATION_FAIL("FAIL"),

    /**
     * cas/mo存储session name
     */
    CONST_CAS_ASSERTION("_const_cas_assertion_"),

    /**
     * 连接符
     */
    SEPARATOR_HYPHEN("-"),

    /**
     * 下划线
     */
    SEPARATOR_UNDERLINE("_"),

    /**
     * 分隔符
     */
    SEPARATOR_COLON(":"),

    /**
     * 存储告警规则yaml命名空间
     */
    YAML_ROOT_PATH("/default"),

    /**
     * yaml
     */
    YAML("/alarm.default.alarm-settings");

    private String value;

    /**
     * 构造函数
     *
     * @param value 设置值
     */
    DataType(String value) {
        this.value = value;
    }

    /**
     * 获取数据
     *
     * @return 枚举值
     */
    public String getDataType() {
        return value;
    }
}
