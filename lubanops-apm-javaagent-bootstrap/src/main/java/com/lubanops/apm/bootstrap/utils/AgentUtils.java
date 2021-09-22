package com.lubanops.apm.bootstrap.utils;

import java.lang.instrument.Instrumentation;

/**
 * @author
 * @date 2021/1/4 10:48
 */
public class AgentUtils {

    public static final int ONE_MB = 1024 * 1024;

    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
    }

    public static long getObjectSize(Object o) {
        if (o == null) {
            return 0;
        }
        //计算指定对象本身在堆空间的大小，单位字节
        long byteCount = instrumentation.getObjectSize(o);
        if (byteCount == 0) {
            return 0;
        }
        double oneMb = ONE_MB;

        if (byteCount < oneMb) {
            return 1;
        }

        Double v = Double.valueOf(byteCount) / oneMb;
        return v.intValue();
    }

    public static void setInstrumentation(Instrumentation instrumentation) {
        AgentUtils.instrumentation = instrumentation;
    }
}
