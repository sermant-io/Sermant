/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.drill;

/**
 * graph查询oap时保存新增字段的ThreadLocal辅助类
 *
 * @author qinfurong
 * @since 2021-07-29
 */
public class DrillThreadLocal {
    private static final ThreadLocal<Object> threadLocal = new ThreadLocal<>();

    /** 无损演练查询新增的flag值 **/
    public static final String DRILL_FLAG = "DRILL_FLAG";

    /**
     * 保存值
     * @param value 前段传入flag值
     */
    public static void set(Object value) {
        threadLocal.set(value);
    }

    /**
     * 获取保存的值
     * @return 保存的flag值
     */
    public static Object get() {
        return threadLocal.get();
    }

    /**
     * 移除
     */
    public static void remove() {
        threadLocal.remove();
    }

    /**
     * 判断是否为“无损演练”的标签值,并删除ThreadLocal值
     * @return true：无损演练查询；false：基础查询
     */
    public static boolean isDrillFlag() {
        String flagValue = String.valueOf(get());
        return DRILL_FLAG.equals(flagValue);
    }
}
