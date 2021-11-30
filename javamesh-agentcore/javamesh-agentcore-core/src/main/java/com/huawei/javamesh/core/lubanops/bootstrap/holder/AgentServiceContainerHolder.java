package com.huawei.javamesh.core.lubanops.bootstrap.holder;

import com.huawei.javamesh.core.lubanops.bootstrap.api.Container;

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
