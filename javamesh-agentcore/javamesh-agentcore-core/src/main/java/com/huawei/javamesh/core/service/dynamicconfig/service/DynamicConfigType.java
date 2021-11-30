package com.huawei.javamesh.core.service.dynamicconfig.service;


/**
 *
 * Enum for DynamicConfigType,
 * Currently support ZooKeeper, Kie, Nop.
 * Probably will support Nacos, etcd in the future.
 *
 */
public enum DynamicConfigType {

    /**
     * zookeeper 配置中心
     */
    ZOO_KEEPER,

    /**
     * servicecomb-kie 配置中心
     */
    KIE,

    /**
     * 配置中心无实现
     */
    NOP;

}
