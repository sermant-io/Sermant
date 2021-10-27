/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.utils;

/**
 * 监控节点状态
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-19
 */
public class WorkerStatusUtil {
    private static WorkerStatusUtil workerStatusUtil = new WorkerStatusUtil();

    /**
     * 回放线程是否还在运行
     */
    private boolean isReplaying = false;

    /**
     * 结果比对线程是否还在运行
     */
    private boolean isComparing = false;

    private WorkerStatusUtil() {
    }

    /**
     * 获取单例
     *
     * @return 返回一个WorkerStatusUtil单例
     */
    public static WorkerStatusUtil getInstance() {
        return workerStatusUtil;
    }

    /**
     * 判断回放和结果比对是否已经全部结束了
     *
     * @return boolean
     */
    public boolean isRunning() {
        return isComparing || isReplaying;
    }

    public boolean isReplaying() {
        return isReplaying;
    }

    public void setReplaying(boolean replaying) {
        isReplaying = replaying;
    }

    public boolean isComparing() {
        return isComparing;
    }

    public void setComparing(boolean comparing) {
        isComparing = comparing;
    }
}
