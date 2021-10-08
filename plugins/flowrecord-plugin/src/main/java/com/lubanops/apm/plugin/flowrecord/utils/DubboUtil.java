package com.lubanops.apm.plugin.flowrecord.utils;

public class DubboUtil {

    public static String buildServiceKey(String path, String group, String version) {
        StringBuilder buf = new StringBuilder();
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(path);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }

    public static String buildSourceKey(String serviceUniqueName, String methodName) {
        StringBuilder buf = new StringBuilder();
        if (serviceUniqueName != null && serviceUniqueName.length() > 0 && methodName != null && methodName.length() > 0) {
            return buf.append(serviceUniqueName).append("/").append(methodName).toString();
        } else {
            throw new IllegalArgumentException(
                    String.format("build sourceKey failed, serviceUniqueName:[%s], methosName:[%s]", serviceUniqueName, methodName)
            );
        }
    }

}
