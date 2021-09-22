package com.lubanops.apm.core.api;

import com.lubanops.apm.bootstrap.exception.ApmRuntimeException;
import com.lubanops.apm.core.container.Priority;

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
