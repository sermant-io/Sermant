package com.huawei.javamesh.core.lubanops.bootstrap.event;

import com.huawei.javamesh.core.lubanops.bootstrap.enums.EventType;

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
