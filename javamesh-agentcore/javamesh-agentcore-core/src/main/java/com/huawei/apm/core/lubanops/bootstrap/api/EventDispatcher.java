package com.huawei.apm.core.lubanops.bootstrap.api;

import com.huawei.apm.core.lubanops.bootstrap.event.ApmEvent;

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
