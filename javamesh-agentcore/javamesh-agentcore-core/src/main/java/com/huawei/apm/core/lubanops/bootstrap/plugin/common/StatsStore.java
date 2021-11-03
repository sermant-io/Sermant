package com.huawei.apm.core.lubanops.bootstrap.plugin.common;

import com.huawei.apm.core.lubanops.bootstrap.collector.api.PrimaryKey;
import com.huawei.apm.core.lubanops.bootstrap.collector.api.StatsBase;

/**
 * @Author: shenxueqi
 * @Date: 2020/3/26 15:01
 */
public interface StatsStore<T extends StatsBase> {

    /**
     * get statistics by primary keys
     *
     * @param pks
     * @return
     */
    T get(PrimaryKey pks);

    /**
     * clear all statistics
     */
    void clear();
}
