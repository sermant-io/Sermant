package com.huawei.apm.bootstrap.lubanops.event;

import com.huawei.apm.bootstrap.lubanops.enums.EventType;

/**
 * @author
 * @date 2021/2/3 10:29
 */
public interface ApmEvent {

    /**
     * get event type.
     *
     * @return
     */
    EventType getEventType();

}
