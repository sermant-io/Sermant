package com.lubanops.apm.bootstrap.agent;

import java.util.concurrent.atomic.AtomicLong;

import com.lubanops.apm.bootstrap.config.IdentityConfigManager;

/**
 * 保存agent信息 <br>
 * @author
 * @since 2020年3月19日
 */
public class AgentInfo {

    private static long agentStartTime;

    private static String javaagentVersion;

    public final static AtomicLong ATOMIC_INTEGER = new AtomicLong(1);

    public final static AtomicLong VIRTUAL_ATOMIC_INTEGER = new AtomicLong(1);

    /**
     * 获取新的traceId
     * @return
     */
    public static String generateTraceId() {
        long newId = ATOMIC_INTEGER.getAndIncrement();
        return IdentityConfigManager.getInstanceId() + "-" + System.currentTimeMillis() + "-" + newId;
    }

    /**
     * 获取新的虚拟traceId
     * @return
     */
    public static String generateVirtualTraceId() {
        long newId = VIRTUAL_ATOMIC_INTEGER.getAndIncrement();
        return "v-" + IdentityConfigManager.getInstanceId() + "-" + System.currentTimeMillis() + "-" + newId;
    }

    public static String getJavaagentVersion() {
        return javaagentVersion;
    }

    public static void setJavaagentVersion(String javaagentVersion) {
        AgentInfo.javaagentVersion = javaagentVersion;
    }

    public static long getAgentStartTime() {
        return agentStartTime;
    }

    public static void setAgentStartTime(long agentStartTime) {
        AgentInfo.agentStartTime = agentStartTime;
    }
}
