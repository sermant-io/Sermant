package com.huawei.apm.premain.lubanops.classloader;

public class ExcludeClass {

    private static final String[] CLASS = new String[] { //
        "com.lubanops.apm.premain", 
        "com.lubanops.apm.integration",
        "javassist", //
        "org.apache", //
        "ch.qos.logback", //
        "org.slf4j", //
        "org.dom4j", //
        "org.wcc", //
        "com.fasterxml.jackson",
        "org.java_websocket",
        "com.alibaba.fastjson",
        "com.google",
        "org.aopalliance",
        "org.jctools"
    };

    public static boolean onLoadClass(String clazzName) {
        final int length = CLASS.length;
        for (int i = 0; i < length; i++) {
            if (clazzName.startsWith(CLASS[i])) {
                return true;
            }
        }
        return false;
    }
}
