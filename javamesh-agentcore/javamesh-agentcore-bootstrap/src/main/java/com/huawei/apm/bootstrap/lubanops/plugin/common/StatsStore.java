package com.huawei.apm.bootstrap.lubanops.plugin.common;

import com.huawei.apm.bootstrap.lubanops.collector.api.PrimaryKey;
import com.huawei.apm.bootstrap.lubanops.collector.api.StatsBase;

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
