package com.lubanops.apm.integration.access.inbound;

import com.lubanops.apm.integration.access.MessageType;

/**
 * 监控数据的返回数据
 *
 * @author
 * @since 2020/5/7
 **/
public class MonitorDataResponse extends CommonResponse {

    public MonitorDataResponse(long msgId) {
        super(MessageType.MONITOR_DATA_RESPONSE, msgId);
    }
}
