package com.huawei.apm.core.service.dynamicconfig.service;


/**
 *
 * Enum for DynamicConfigType,
 * Currently support ZooKeeper, Nop.
 * Probably will support Nacos, etcd, Kie in the future.
 *
 */
public enum DynamicConfigType {

    ZOO_KEEPER,

    NOP;

}
