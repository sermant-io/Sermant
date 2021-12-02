package com.huawei.javamesh.core.lubanops.core.api;

import com.huawei.javamesh.core.lubanops.bootstrap.exception.ApmRuntimeException;
import com.huawei.javamesh.core.lubanops.core.container.Priority;

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
