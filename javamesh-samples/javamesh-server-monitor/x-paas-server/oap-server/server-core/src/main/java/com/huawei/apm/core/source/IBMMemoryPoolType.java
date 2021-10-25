/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.source;

/**
 * ibm jdk jvm内存年老代等的名称的枚举类
 *
 * @author zhengbin zhao
 * @since 2021-03-09
 */
public enum IBMMemoryPoolType {
    /**
     * 代码缓存区
     */
    IBM_CODE_CACHE_USAGE,
    /**
     * 数据缓存区
     */
    IBM_DATA_CACHE_USAGE,
    /**
     * 年老代婴儿区收集(小对象分配区域)
     */
    IBM_TENURED_SOA_USAGE,
    /**
     * 年老代长存区(大对象分配区域)
     */
    IBM_TENURED_LOA_USAGE,
    /**
     * 年轻代分配区
     */
    IBM_NURSERY_ALLOCATE_USAGE,
    /**
     * 年轻代幸存区
     */
    IBM_NURSERY_SURVIVOR_USAGE,
    /**
     * Class存储区
     */
    IBM_CLASS_STORAGE_USAGE,
    /**
     * 混合存储区
     */
    IBM_MISCELLANEOUS_USAGE
}
