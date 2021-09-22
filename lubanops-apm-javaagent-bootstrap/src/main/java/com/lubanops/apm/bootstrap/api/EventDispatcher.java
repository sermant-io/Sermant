package com.lubanops.apm.bootstrap.api;

import com.lubanops.apm.bootstrap.event.ApmEvent;

/**
 * @author
 * @date 2021/2/3 15:26
 */
public interface EventDispatcher {

    /**
     * dispatch event
     * @param event
     */
    void dispatch(ApmEvent event);

}
