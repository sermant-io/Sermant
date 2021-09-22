package com.lubanops.apm.bootstrap.collector.api;

import java.util.List;
import java.util.Map;

/**
 * 对应一个指标集的聚合器 <br>
 *
 * @author
 * @since 2020年3月9日
 */
public interface MetricSetAggregator {

    /**
     * 默认采集的最大行数
     */
    public final static int DEFAUL_ROW_MAX = 200;

    /**
     * 最大的行数的最大值
     */
    public final static int ROW_MAX_THRESHOLD = 10000;

    /**
     * 指标集名称
     *
     * @return
     */
    public String getName();

    /**
     * 对参数进行解析，需要的参数转成自己可以表达的类型，比如字符串参数true，转成bolean的true等
     */
    public void setParameters(Map<String, String> parameters);

    /**
     * 监控框架定时器定期调用的方法，收集用户的监控数据，一次收集一行或者多行
     */
    public List<MonitorDataRow> harvest();

    /**
     * 被收割之后调用的方法，用于一些其他的聚合运算
     *
     * @param collected
     * @return
     */
    public Map<String, List<MonitorDataRow>> afterHarvest(List<MonitorDataRow> collected);

    /**
     * 获取主键对应的状态值 <br>
     *
     * @param primaryKeyMap
     * @return
     * @author
     * @since 2020年3月13日
     */
    public MonitorDataRow getStatus(Map<String, String> primaryKeyMap);

    /**
     * 获取所有的主键状态 <br>
     *
     * @return
     * @author
     * @since 2020年3月13日
     */
    public List<MonitorDataRow> getAllStatus();

    /**
     * 当采集满时调用的方法，用于dump文件或数据上报
     */
    public List<MonitorDataRow> dump();

    /**
     * 获取采集器状态
     *
     * @return
     */
    public boolean isEnable();

    /**
     * 设置采集状态
     *
     * @param isEnable
     */
    public void setEnable(boolean isEnable);

    /**
     * 这个模型的数据的最大的行数,用户可以定义一个最大值，但是总体而言，如果超过了监控系统规定的最大值，系统也不会采集，因为会给系统归档造成压力
     */
    public int getMaxRowCount();

    /**
     * 设置最大行数
     *
     * @param c
     */
    public void setMaxRowCount(int c);

    /**
     * 行数是否满了
     *
     * @return
     */
    public boolean isFull();

    /**
     * 设置行数是否满了
     *
     * @return
     */
    public void setFull(boolean f);

    /**
     * 当主键满了，是否已经打印到磁盘了，在采集的时候，如果发现主键满了就打印一次主键的所有的值，以后停止采集
     *
     * @return
     */
    public boolean isFullOutputted();

    /**
     * 是否已经打印到磁盘了
     *
     * @param f
     */
    public void setFullOutputted(boolean f);

    /**
     * 如果超过了行的最大值，是否还采集
     */
    public boolean isCollectAfterFull();

    /**
     * 设置如果超过了行的最大值，是否还采集
     *
     * @return
     */
    public void setCollectAfterFull(boolean f);

    /**
     * 清空数据
     *
     * @return
     */
    public void clear();

    /**
     * 是否需要上报数据
     *
     * @return
     */
    public boolean isNeedToUpload();

}
