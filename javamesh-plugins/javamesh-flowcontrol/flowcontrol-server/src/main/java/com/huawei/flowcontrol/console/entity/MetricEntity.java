package com.huawei.flowcontrol.console.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 此处部分引用alibaba/Sentinel开源社区代码，诚挚感谢alibaba/Sentinel开源团队的慷慨贡献
 */
@Setter
@Getter
public class MetricEntity {
    private Long id;
    private Date gmtCreate;
    private Date gmtModified;
    private String app;

    /**
     * 监控信息的时间戳
     */
    private Date timestamp;
    private String resource;
    private Long passQps;
    private Long successQps;
    private Long blockQps;
    private Long exceptionQps;

    /**
     * summary rt of all success exit qps.
     */
    private double rt;

    /**
     * 本次聚合的总条数
     */
    private int count;

    private int resourceCode;

    public static MetricEntity copyOf(MetricEntity oldEntity) {
        MetricEntity entity = new MetricEntity();
        entity.setId(oldEntity.getId());
        entity.setGmtCreate(oldEntity.getGmtCreate());
        entity.setGmtModified(oldEntity.getGmtModified());
        entity.setApp(oldEntity.getApp());
        entity.setTimestamp(oldEntity.getTimestamp());
        entity.setResource(oldEntity.getResource());
        entity.setPassQps(oldEntity.getPassQps());
        entity.setBlockQps(oldEntity.getBlockQps());
        entity.setSuccessQps(oldEntity.getSuccessQps());
        entity.setExceptionQps(oldEntity.getExceptionQps());
        entity.setRt(oldEntity.getRt());
        entity.setCount(oldEntity.getCount());
        return entity;
    }
}
