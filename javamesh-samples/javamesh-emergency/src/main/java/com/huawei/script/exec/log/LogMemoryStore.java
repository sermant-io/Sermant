/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.log;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于存放脚本运行中产生的实时日志
 *
 * @author y30010171
 * @since 2021-10-25
 **/
public class LogMemoryStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogMemoryStore.class);

    private static Map<Integer, String[]> allTaskLogs = new HashMap<>();
    public static final String[] EMPTY_ARRAY = new String[0];

    private LogMemoryStore() {
    }

    /**
     * 获取任务详情的日志
     *
     * @param taskId 任务详情ID
     * @param lines  日志行号
     * @return 日志数组
     */
    public static LogResponse getLog(int taskId, int lines) {
        String[] allLogs = allTaskLogs.get(taskId);
        if (allLogs == null) {
            return new LogResponse(null, EMPTY_ARRAY);
        }
        if (allLogs.length < lines) {
            return new LogResponse(lines, EMPTY_ARRAY);
        }
        String[] logs = Arrays.copyOfRange(allLogs, lines - 1, allLogs.length);
        return new LogResponse(allLogs.length + 1, logs);
    }

    /**
     * 添加新的日志
     *
     * @param taskId    任务详情ID
     * @param extraLogs 额外的日志
     */
    public static void addLog(int taskId, String[] extraLogs) {
        String[] oldLogs = allTaskLogs.getOrDefault(taskId, EMPTY_ARRAY);
        allTaskLogs.put(taskId, (String[]) ArrayUtils.addAll(oldLogs, extraLogs));
    }

    /**
     * 删除日志
     *
     * @param taskId 任务详情ID
     * @return 日志
     */
    public static String[] removeLog(int taskId) {
        LOGGER.info("Task's log  cleaning, id is {}", taskId);
        String[] remove = allTaskLogs.remove(taskId);
        if (remove == null) {
            return EMPTY_ARRAY;
        }
        return remove;
    }
}
