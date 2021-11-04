package com.huawei.apm.core.lubanops.bootstrap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InterceptorManager {

    private static Map<String, Interceptor> nameMap = new ConcurrentHashMap<String, Interceptor>();

    public static Interceptor getInterceptor(String name) {
        return nameMap.get(name);
    }

    public static void setInterceptor(String name, Interceptor interceptor) {
        nameMap.put(name, interceptor);
    }

}
