package com.huawei.apm.backend.entity;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeartBeatResult extends Result {
    /**
     * 下一次心跳的周期
     */
    private Integer heartBeatInterval;

    /**
     * 附属信息，收到这个信息后，原封不动得通过数据上报
     */
    private Map<String, String> attachment;

    private List<MonitorItem> monitorItemList;

    /**
     * 系统属性
     */
    private Map<String, String> systemProperties;

    /**
     * access的地址描述
     */
    private List<Address> accessAddressList;

    /**
     * 实例状态0 代表ok， 1代表disabled
     */
    private Integer instanceStatus;

    /**
     * 对结果进行md5计算，如果内容变化了就下发新的配置
     */
    private String md5;
}
