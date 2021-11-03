package com.huawei.apm.core.lubanops.core.container;

/**
 *
 * @author
 */
public interface Priority {

    int HIGHEST = Integer.MIN_VALUE;

    int LOWEST = Integer.MAX_VALUE;

    int DEFAULT = 100;

    int AGENT_INTERNAL_SERVICE = 10;

    int AGENT_INTERNAL_INFRASTRUCTURE_SERVICE = 0;

    int EARLY_EXPOSE = -1;

    /**
     * get priority.
     *
     * @return
     */
    int getPriority();
}
