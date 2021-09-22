package com.lubanops.apm.bootstrap.event;

import com.lubanops.apm.bootstrap.enums.EventType;

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
