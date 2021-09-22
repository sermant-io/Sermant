package com.lubanops.apm.bootstrap.holder;

import com.lubanops.apm.bootstrap.api.Container;

/**
 * Agent Service Container Holder.
 *
 * @author
 * @date 2020/10/20 18:27
 */
public class AgentServiceContainerHolder {

    private static volatile Container agentServiceContainer;

    public static Container get() {
        return agentServiceContainer;
    }

    public static void set(Container agentServiceContainer) {
        AgentServiceContainerHolder.agentServiceContainer = agentServiceContainer;
    }
}
