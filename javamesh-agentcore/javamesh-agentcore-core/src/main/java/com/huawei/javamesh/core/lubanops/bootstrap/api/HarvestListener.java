package com.huawei.javamesh.core.lubanops.bootstrap.api;

/**
 * @author
 * @date 2020/12/17 11:26
 */
public interface HarvestListener<T> {
    /**
     * harvest task finish.
     */
    void onHarvest(T collector, long time);

}
