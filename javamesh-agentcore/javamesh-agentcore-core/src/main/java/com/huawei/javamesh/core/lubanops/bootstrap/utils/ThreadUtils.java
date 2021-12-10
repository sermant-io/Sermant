/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.Date;

public class ThreadUtils {

    public static String getThreadInfoString(ThreadInfo threadInfo, int maxFrames) {
        StringBuilder sb = new StringBuilder();
        sb.append(new Date());
        sb.append('\n');
        sb.append("\"" + threadInfo.getThreadName() + "\""
                + " Id=" + threadInfo.getThreadId() + " "
                + threadInfo.getThreadState());
        if (threadInfo.getLockName() != null) {
            sb.append(" on " + threadInfo.getLockName());
        }
        if (threadInfo.getLockOwnerName() != null) {
            sb.append(" owned by \"" + threadInfo.getLockOwnerName()
                    + "\" Id=" + threadInfo.getLockOwnerId());
        }
        if (threadInfo.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (threadInfo.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        getStackTrace(stackTrace, maxFrames, threadInfo, sb);

        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n\tNumber of locked synchronizers = " + locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append("\t- " + li);
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString();
    }

    private static void getStackTrace(StackTraceElement[] stackTrace, int maxFrames, ThreadInfo threadInfo,
            StringBuilder sb) {
        int i = 0;
        for (; i < stackTrace.length && i < maxFrames; i++) {
            StackTraceElement ste = stackTrace[i];
            sb.append("\tat " + ste.toString());
            sb.append('\n');
            if (i == 0 && threadInfo.getLockInfo() != null) {
                Thread.State ts = threadInfo.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked " + mi);
                    sb.append('\n');
                }
            }
        }
        if (i < stackTrace.length) {
            sb.append("\t...");
            sb.append('\n');
        }
    }

}
