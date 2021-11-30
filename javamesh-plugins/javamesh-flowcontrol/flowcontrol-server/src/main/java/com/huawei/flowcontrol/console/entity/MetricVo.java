package com.huawei.flowcontrol.console.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 此处部分引用alibaba/Sentinel开源社区代码，诚挚感谢alibaba/Sentinel开源团队的慷慨贡献
 */
@Getter
@Setter
public class MetricVo implements Comparable<MetricVo> {
    private Long id;
    private String app;
    private Long timestamp;
    private Long gmtCreate = System.currentTimeMillis();
    private String resource;
    private Long passQps;
    private Long blockQps;
    private Long successQps;
    private Long exceptionQps;

    /**
     * average rt
     */
    private Double rt;
    private Integer count;

    /**
     * 保留资源名为identity的结果。
     *
     * @param entities 通过hashCode查找到的MetricEntities
     * @param identity 真正需要查找的资源名
     * @return List
     */
    public static List<MetricVo> fromMetricEntities(Collection<MetricEntity> entities, String identity) {
        List<MetricVo> list = new ArrayList<>();
        if (entities != null) {
            for (MetricEntity entity : entities) {
                if (entity.getResource().equals(identity)) {
                    list.add(fromMetricEntity(entity));
                }
            }
        }
        return list;
    }

    public static MetricVo fromMetricEntity(MetricEntity entity) {
        MetricVo vo = new MetricVo();
        vo.id = entity.getId();
        vo.app = entity.getApp();
        vo.timestamp = entity.getTimestamp().getTime();
        vo.gmtCreate = entity.getGmtCreate().getTime();
        vo.resource = entity.getResource();
        vo.passQps = entity.getPassQps();
        vo.blockQps = entity.getBlockQps();
        vo.successQps = entity.getSuccessQps();
        vo.exceptionQps = entity.getExceptionQps();
        if (entity.getSuccessQps() != 0) {
            vo.rt = entity.getRt() / entity.getSuccessQps();
        } else {
            vo.rt = 0D;
        }
        vo.count = entity.getCount();
        return vo;
    }

    @Override
    public int compareTo(MetricVo o) {
        return Long.compare(this.timestamp, o.timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetricVo metricVo = (MetricVo) o;
        return Objects.equals(id, metricVo.id)
            && Objects.equals(app, metricVo.app)
            && Objects.equals(resource, metricVo.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, app, resource);
    }
}
