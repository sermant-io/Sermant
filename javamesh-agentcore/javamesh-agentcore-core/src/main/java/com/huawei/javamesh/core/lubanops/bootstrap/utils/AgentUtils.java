/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.core.lubanops.bootstrap.utils;

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
