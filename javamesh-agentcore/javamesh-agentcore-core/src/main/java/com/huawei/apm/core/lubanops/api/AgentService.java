package com.huawei.apm.core.lubanops.api;

import com.huawei.apm.bootstrap.lubanops.exception.ApmRuntimeException;
import com.huawei.apm.core.lubanops.container.Priority;

/**
 * @author
 * @date 2020/10/20 17:17
 */
public interface AgentService extends Priority {

    /**
     * Agent Service init
     *
     * @throws ApmRuntimeException
     */
    void init() throws ApmRuntimeException;

    /**
     * Agent Service dispose
     *
     * @throws ApmRuntimeException
     */
    void dispose() throws ApmRuntimeException;

}
