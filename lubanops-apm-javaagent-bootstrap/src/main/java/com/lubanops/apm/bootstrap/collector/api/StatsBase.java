package com.lubanops.apm.bootstrap.collector.api;

/**
 * 所有统计数据的基类 <br>
 *
 * @author
 * @since 2020年3月13日
 */
public interface StatsBase {

    /**
     * 获取当前的内存的值 <br>
     *
     * @return 不可以返回null
     * @author
     * @since 2020年3月13日
     */
    public MonitorDataRow getStatus();

    /**
     * 实现数据收割 <br>
     *
     * @return 可以返回null
     * @author
     * @since 2020年3月18日
     */
    public MonitorDataRow harvest();

}
