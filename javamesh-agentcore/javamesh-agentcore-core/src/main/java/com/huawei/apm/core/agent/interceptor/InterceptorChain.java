package com.huawei.apm.core.agent.interceptor;

import com.huawei.apm.core.common.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 拦截器链
 */
public class InterceptorChain {

    private final static Logger LOGGER = LoggerFactory.getLogger();

    private static final int NOT_IN_CHAIN = Integer.MAX_VALUE;

    private final Map<String, Integer> priorities = new HashMap<String, Integer>();

    public InterceptorChain(String[] interceptors) {
        int index = 1;
        for (String interceptor : interceptors) {
            final Integer integer = priorities.get(interceptor);
            if (integer == null) {
                priorities.put(interceptor, index++);
            }
        }
        LOGGER.info(String.format("Build interceptor chain {%s} successfully.", Arrays.toString(interceptors)));
    }

    public int getPriority(String interceptor) {
        final Integer priority = priorities.get(interceptor);
        return priority == null ? NOT_IN_CHAIN : priority;
    }
}
