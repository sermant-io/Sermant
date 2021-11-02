/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.common;


import com.huawei.javamesh.sample.servermonitor.entity.IbmPoolType;

/**
 * IBM MemoryPoolMXBean 枚举类
 *
 * @author zhengbin zhao
 * @since 2021-03-16
 */
public enum CheckIBMParameter {
    /**
     * 年老代婴儿区收集(小对象分配区域)
     */
    TENURED_SOA("tenured-SOA"),

    /**
     * 年老代长存区(大对象分配区域)
     */
    TENURED_LOA("tenured-LOA"),

    /**
     * 年轻代分配区
     */
    NURSERY_ALLOCATE("nursery-allocate"),

    /**
     * 年轻代幸存区
     */
    NURSERY_SURVIVOR("nursery-survivor"),

    /**
     * Class存储区
     */
    CLASS_STORAGE("class storage"),

    /**
     * 混合存储区
     */
    MISCELLANEOUS("miscellaneous non-heap storage"),

    /**
     * 代码缓存区
     */
    CODE_CACHE("JIT code cache"),

    /**
     * 数据缓存区
     */
    DATA_CACHE("JIT data cache");

    private String parameter;

    CheckIBMParameter(String param) {
        this.parameter = param;
    }

    public String getParameter() {
        return parameter;
    }

    /**
     * 根据名称确认是否存在
     *
     * @param param jvm参数名称
     * @return 返回匹配结果
     */
    public static boolean returnCheckResult(String param) {
        boolean isExist = false;
        for (CheckIBMParameter checkParameter : CheckIBMParameter.values()) {
            if (checkParameter.parameter.equals(param)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    /**
     * 根据pooltype名称返回对应的pooltype
     *
     * @param param jvm参数名称
     * @return 返回池类型
     */
    public static IbmPoolType returnPoolType(String param) {
        IbmPoolType type;
        if (param.equals(CheckIBMParameter.CLASS_STORAGE.getParameter())) {
            type = IbmPoolType.IBM_CLASS_STORAGE_USAGE;
        } else if (param.equals(CheckIBMParameter.MISCELLANEOUS.getParameter())) {
            type = IbmPoolType.IBM_MISCELLANEOUS_USAGE;
        } else if (param.equals(CheckIBMParameter.NURSERY_ALLOCATE.getParameter())) {
            type = IbmPoolType.IBM_NURSERY_ALLOCATE_USAGE;
        } else if (param.equals(CheckIBMParameter.NURSERY_SURVIVOR.getParameter())) {
            type = IbmPoolType.IBM_NURSERY_SURVIVOR_USAGE;
        } else if (param.equals(CheckIBMParameter.TENURED_LOA.getParameter())) {
            type = IbmPoolType.IBM_TENURED_LOA_USAGE;
        } else if (param.equals(CheckIBMParameter.TENURED_SOA.getParameter())) {
            type = IbmPoolType.IBM_TENURED_SOA_USAGE;
        } else if (param.equals(CheckIBMParameter.CODE_CACHE.getParameter())) {
            type = IbmPoolType.IBM_CODE_CACHE_USAGE;
        } else {
            type = IbmPoolType.IBM_DATA_CACHE_USAGE;
        }
        return type;
    }
}
